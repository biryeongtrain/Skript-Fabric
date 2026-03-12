package kim.biryeong.skriptFabricPort.gametest;

import ch.njol.skript.Skript;
import ch.njol.skript.events.FabricEventCompatHandles;
import ch.njol.skript.expressions.ExprItemOwner;
import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import java.lang.reflect.Field;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.entity.monster.ZombifiedPiglin;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.entity.monster.Skeleton;
import net.minecraft.world.entity.animal.Cow;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.projectile.Arrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.fabric.compat.FabricBlock;
import org.skriptlang.skript.fabric.runtime.FabricBlockEventHandle;
import org.skriptlang.skript.fabric.runtime.FabricEntityEventHandle;
import org.skriptlang.skript.fabric.runtime.SkriptRuntime;

public final class SkriptFabricMixedRuntimeBackfillGameTest extends AbstractSkriptFabricGameTestSupport {

    private static final AtomicBoolean CUSTOM_EVENTS_REGISTERED = new AtomicBoolean(false);
    private static final Class<?> EXPLOSION_PRIME_EVENT = effectEventClass("ExplosionPrime");
    private static final Class<?> ENTITY_DEATH_EVENT = effectEventClass("EntityDeath");
    private static final Class<?> HANGING_BREAK_EVENT = effectEventClass("HangingBreak");

    @GameTest
    public void storageUtilityAndPropertySyntaxExecutesRealScript(GameTestHelper helper) {
        runWithRuntimeLock(helper, () -> {
            SkriptRuntime runtime = SkriptRuntime.instance();
            runtime.clearScripts();
            runtime.loadFromResource("skript/gametest/base/mixed_runtime_storage_and_properties.sk");

            int executed = runtime.dispatch(new org.skriptlang.skript.lang.event.SkriptEvent(
                    helper,
                    helper.getLevel().getServer(),
                    helper.getLevel(),
                    null
            ));
            assertExecuted(helper, executed, "storage and property backfill script");
            helper.assertBlockPresent(Blocks.EMERALD_BLOCK, new BlockPos(0, 1, 0));
            helper.assertBlockPresent(Blocks.LAPIS_BLOCK, new BlockPos(1, 1, 0));
            helper.assertBlockPresent(Blocks.DIAMOND_BLOCK, new BlockPos(2, 1, 0));
            helper.assertBlockPresent(Blocks.GOLD_BLOCK, new BlockPos(3, 1, 0));
            helper.assertBlockPresent(Blocks.REDSTONE_BLOCK, new BlockPos(4, 1, 0));
            helper.assertBlockPresent(Blocks.IRON_BLOCK, new BlockPos(5, 1, 0));
            helper.assertBlockPresent(Blocks.WHITE_WOOL, new BlockPos(6, 1, 0));
            helper.assertBlockPresent(Blocks.AMETHYST_BLOCK, new BlockPos(7, 1, 0));
            runtime.clearScripts();
        });
    }

    @GameTest
    public void exceptionDebugEffectThrowsFromRealScript(GameTestHelper helper) {
        runWithRuntimeLock(helper, () -> {
            SkriptRuntime runtime = SkriptRuntime.instance();
            runtime.clearScripts();
            runtime.loadFromResource("skript/gametest/effect/exception_debug_throws.sk");

            IllegalStateException thrown = null;
            try {
                runtime.dispatch(new org.skriptlang.skript.lang.event.SkriptEvent(
                        helper,
                        helper.getLevel().getServer(),
                        helper.getLevel(),
                        null
                ));
            } catch (IllegalStateException exception) {
                thrown = exception;
            }

            helper.assertTrue(
                    thrown != null && "Created by a script (debugging)...".equals(thrown.getMessage()),
                    Component.literal("Expected the real .sk exception debug fixture to propagate the debug exception.")
            );
            runtime.clearScripts();
        });
    }

    @GameTest
    public void mixedDamageAndHealingSyntaxExecutesRealScript(GameTestHelper helper) {
        runWithRuntimeLock(helper, () -> {
            SkriptRuntime runtime = SkriptRuntime.instance();
            runtime.clearScripts();
            runtime.loadFromResource("skript/gametest/condition/mixed_damage_context_runtime.sk");

            ServerPlayer player = helper.makeMockServerPlayerInLevel();
            player.setGameMode(GameType.SURVIVAL);
            player.setHealth(player.getMaxHealth());

            player.hurtServer(helper.getLevel(), helper.getLevel().damageSources().inFire(), 2.0F);
            helper.assertTrue(
                    player.experienceLevel == 7,
                    Component.literal("Expected damage context script to change the event-player xp level.")
            );
            helper.assertTrue(
                    Math.abs(player.getAbilities().getWalkingSpeed() - 0.4F) < 0.0001F,
                    Component.literal("Expected damage context script to change the event-player walk speed.")
            );
            helper.assertTrue(
                    Math.abs((player.getMaxHealth() / 2.0F) - 15.0F) < 0.0001F,
                    Component.literal("Expected damage context script to change maximum health.")
            );

            player.heal(2.5F);
            helper.assertTrue(
                    player.experienceLevel == 2,
                    Component.literal("Expected healing amount expression to update the event-player inside a real .sk fixture.")
            );
            runtime.clearScripts();
        });
    }

    @GameTest
    public void activeMixedRuntimeEventsExecuteRealScript(GameTestHelper helper) {
        runWithRuntimeLock(helper, () -> {
            SkriptRuntime runtime = SkriptRuntime.instance();
            runtime.clearScripts();
            runtime.loadFromResource("skript/gametest/event/active_mixed_runtime_events.sk");

            ServerPlayer player = helper.makeMockServerPlayerInLevel();
            player.setGameMode(GameType.CREATIVE);

            BlockPos activationPos = helper.absolutePos(new BlockPos(0, 1, 0));
            BlockPos effectPos = helper.absolutePos(new BlockPos(1, 1, 0));
            BlockPos blockDropPos = helper.absolutePos(new BlockPos(2, 1, 0));
            helper.getLevel().setBlockAndUpdate(activationPos, Blocks.BEACON.defaultBlockState());
            helper.getLevel().setBlockAndUpdate(effectPos, Blocks.BEACON.defaultBlockState());
            helper.getLevel().setBlockAndUpdate(blockDropPos, Blocks.STONE.defaultBlockState());

            assertExecuted(helper, dispatch(
                    runtime,
                    new FabricEventCompatHandles.BeaconToggle(helper.getLevel(), activationPos, true),
                    helper,
                    null
            ), "beacon activation event");
            helper.assertTrue(
                    helper.getLevel().getBlockState(activationPos.above()).is(Blocks.EMERALD_BLOCK),
                    Component.literal("Expected beacon activation event to mark the block above the beacon.")
            );

            assertExecuted(helper, dispatch(
                    runtime,
                    new FabricEventCompatHandles.BeaconEffect(helper.getLevel(), effectPos, true, null),
                    helper,
                    null
            ), "beacon effect event");
            helper.assertTrue(
                    helper.getLevel().getBlockState(effectPos.above()).is(Blocks.GOLD_BLOCK),
                    Component.literal("Expected beacon effect event to mark the block above the beacon.")
            );

            assertExecuted(helper, dispatch(
                    runtime,
                    new FabricEventCompatHandles.Block(
                            helper.getLevel(),
                            blockDropPos,
                            FabricEventCompatHandles.BlockAction.DROP,
                            helper.getLevel().getBlockState(blockDropPos),
                            new ItemStack(Items.STONE),
                            true
                    ),
                    helper,
                    null
            ), "block dropping event");
            helper.assertTrue(
                    helper.getLevel().getBlockState(blockDropPos.above()).is(Blocks.DIAMOND_BLOCK),
                    Component.literal("Expected block dropping event to mark the block above the dropped block.")
            );

            ItemStack editedBook = new ItemStack(Items.WRITABLE_BOOK);
            assertExecuted(helper, dispatch(
                    runtime,
                    new FabricEventCompatHandles.BookEdit(new ItemStack(Items.WRITABLE_BOOK), editedBook, false),
                    helper,
                    player
            ), "book edit event");
            helper.assertTrue(
                    "edited book".equals(editedBook.getHoverName().getString()),
                    Component.literal("Expected book edit event to rename the edited book item.")
            );

            ItemStack signedBook = new ItemStack(Items.WRITTEN_BOOK);
            assertExecuted(helper, dispatch(
                    runtime,
                    new FabricEventCompatHandles.BookEdit(new ItemStack(Items.WRITABLE_BOOK), signedBook, true),
                    helper,
                    player
            ), "book signing event");
            helper.assertTrue(
                    "signed book".equals(signedBook.getHoverName().getString()),
                    Component.literal("Expected book signing event to rename the signed book item.")
            );

            Skeleton skeleton = (Skeleton) helper.spawnWithNoFreeWill(EntityType.SKELETON, 3.5F, 1.0F, 0.5F);
            ItemStack shotArrow = new ItemStack(Items.ARROW);
            assertExecuted(helper, dispatch(
                    runtime,
                    new FabricEventCompatHandles.EntityShootBow(skeleton, shotArrow),
                    helper,
                    null
            ), "entity shoot bow event");
            helper.assertTrue(
                    "shot arrow".equals(shotArrow.getHoverName().getString()),
                    Component.literal("Expected entity shoot bow event to expose the consumed projectile item.")
            );

            ArmorStand armorStand = new ArmorStand(helper.getLevel(), 4.5D, 1.0D, 0.5D);
            helper.getLevel().addFreshEntity(armorStand);
            assertExecuted(helper, dispatch(
                    runtime,
                    new FabricEventCompatHandles.Click(
                            helper.getLevel(),
                            armorStand.blockPosition(),
                            FabricEventCompatHandles.ClickType.RIGHT,
                            armorStand,
                            null,
                            new ItemStack(Items.STICK)
                    ),
                    helper,
                    player
            ), "click event");
            helper.assertTrue(
                    armorStand.getCustomName() != null && "clicked entity".equals(armorStand.getCustomName().getString()),
                    Component.literal("Expected click event to resolve event-entity inside a real .sk fixture.")
            );

            Zombie zombie = (Zombie) helper.spawnWithNoFreeWill(EntityType.ZOMBIE, 5.5F, 1.0F, 0.5F);
            assertExecuted(helper, dispatch(
                    runtime,
                    new FabricEventCompatHandles.EntityLifecycle(zombie, true),
                    helper,
                    null
            ), "entity spawn event");
            helper.assertTrue(
                    zombie.getCustomName() != null && "spawned zombie".equals(zombie.getCustomName().getString()),
                    Component.literal("Expected entity spawn event to resolve event-entity inside a real .sk fixture.")
            );

            Zombie transformed = (Zombie) helper.spawnWithNoFreeWill(EntityType.ZOMBIE, 6.5F, 1.0F, 0.5F);
            assertExecuted(helper, dispatch(
                    runtime,
                    new FabricEventCompatHandles.EntityTransform(transformed, "curing"),
                    helper,
                    null
            ), "entity transform event");
            helper.assertTrue(
                    transformed.getCustomName() != null && "transformed zombie".equals(transformed.getCustomName().getString()),
                    Component.literal("Expected entity transform event to resolve the filtered transform reason.")
            );

            assertExecuted(helper, dispatch(
                    runtime,
                    new FabricEventCompatHandles.ExperienceSpawn(5),
                    helper,
                    null
            ), "experience spawn event");
            helper.assertBlockPresent(Blocks.LAPIS_BLOCK, new BlockPos(6, 1, 0));

            ServerPlayer healingPlayer = helper.makeMockServerPlayerInLevel();
            assertExecuted(helper, dispatch(
                    runtime,
                    new FabricEventCompatHandles.Healing(healingPlayer, "magic", 2.5F),
                    helper,
                    healingPlayer
            ), "healing event");
            helper.assertTrue(
                    healingPlayer.experienceLevel == 2,
                    Component.literal("Expected healing event to expose heal amount through the real .sk fixture.")
            );

            ItemStack spawnedApple = new ItemStack(Items.APPLE);
            assertExecuted(helper, dispatch(
                    runtime,
                    new FabricEventCompatHandles.Item(
                            helper.getLevel(),
                            helper.absolutePos(new BlockPos(7, 1, 0)),
                            FabricEventCompatHandles.ItemAction.SPAWN,
                            spawnedApple,
                            false
                    ),
                    helper,
                    null
            ), "item spawn event");
            helper.assertTrue(
                    "spawned apple".equals(spawnedApple.getHoverName().getString()),
                    Component.literal("Expected item spawn event to rename the spawned item.")
            );

            ItemStack consumedApple = new ItemStack(Items.APPLE);
            assertExecuted(helper, dispatch(
                    runtime,
                    new FabricEventCompatHandles.Item(
                            helper.getLevel(),
                            helper.absolutePos(new BlockPos(8, 1, 0)),
                            FabricEventCompatHandles.ItemAction.CONSUME,
                            consumedApple,
                            false
                    ),
                    helper,
                    player
            ), "consume item event");
            helper.assertTrue(
                    "consumed apple".equals(consumedApple.getHoverName().getString()),
                    Component.literal("Expected consumed item expression to rename the consumed item.")
            );

            runtime.clearScripts();
        });
    }

    @GameTest
    public void customContextBackfillExecutesRealScript(GameTestHelper helper) {
        ensureCustomEventsRegistered();
        runWithRuntimeLock(helper, () -> {
            SkriptRuntime runtime = SkriptRuntime.instance();
            runtime.clearScripts();
            runtime.loadFromResource("skript/gametest/event/custom_context_backfill.sk");

            BlockPos stonePos = helper.absolutePos(new BlockPos(0, 1, 0));
            helper.getLevel().setBlockAndUpdate(stonePos, Blocks.STONE.defaultBlockState());
            assertExecuted(helper, dispatch(
                    runtime,
                    new BlockContextHandle(helper.getLevel(), stonePos),
                    helper,
                    null
            ), "block context event for preferred tool");
            helper.assertTrue(
                    helper.getLevel().getBlockState(stonePos.above()).is(Blocks.EMERALD_BLOCK),
                    Component.literal("Expected preferred tool condition to mark the block above the target block.")
            );

            BlockPos campfirePos = helper.absolutePos(new BlockPos(8, 0, 0));
            BlockPos beehivePos = helper.absolutePos(new BlockPos(8, 1, 0));
            helper.getLevel().setBlockAndUpdate(campfirePos, Blocks.CAMPFIRE.defaultBlockState());
            helper.getLevel().setBlockAndUpdate(beehivePos, Blocks.BEEHIVE.defaultBlockState());
            assertExecuted(helper, dispatch(
                    runtime,
                    new BlockContextHandle(helper.getLevel(), beehivePos),
                    helper,
                    null
            ), "block context event for sedated beehive");
            helper.assertBlockPresent(Blocks.GOLD_BLOCK, new BlockPos(8, 1, 0));

            ServerPlayer leashHolder = helper.makeMockServerPlayerInLevel();
            leashHolder.setCustomName(Component.literal("pre-leash-holder"));
            Cow cow = createCow(helper, false);
            cow.setLeashedTo(leashHolder, true);
            setBooleanField(cow, "persistenceRequired", false);
            assertExecuted(helper, dispatch(
                    runtime,
                    new EntityContextHandle(cow),
                    helper,
                    leashHolder
            ), "entity context event");
            helper.assertBlockPresent(Blocks.LAPIS_BLOCK, new BlockPos(0, 1, 0));
            helper.assertTrue(
                    leashHolder.getCustomName() != null && "leash holder marker".equals(leashHolder.getCustomName().getString()),
                    Component.literal("Expected leash holder expression to resolve the mob leash holder.")
            );

            ServerPlayer itemPlayer = helper.makeMockServerPlayerInLevel();
            ItemEntity droppedItem = new ItemEntity(helper.getLevel(), 2.5D, 1.0D, 0.5D, new ItemStack(Items.APPLE));
            helper.getLevel().addFreshEntity(droppedItem);
            assertExecuted(helper, dispatch(
                    runtime,
                    new ItemEntityContextHandle(droppedItem),
                    helper,
                    itemPlayer
            ), "item entity context event");
            helper.assertBlockPresent(Blocks.DIAMOND_BLOCK, new BlockPos(1, 1, 0));
            helper.assertTrue(
                    itemPlayer.equals(droppedItem.getOwner()),
                    Component.literal("Expected dropped item thrower expression to assign the event-player entity.")
            );
            helper.assertTrue(
                    itemPlayer.getUUID().equals(readItemOwner(droppedItem)),
                    Component.literal("Expected dropped item owner setter to assign the event-player UUID.")
            );
            helper.assertTrue(
                    itemPlayer.getUUID().equals(new ExprItemOwner().convert(droppedItem)),
                    Component.literal("Expected dropped item owner expression to resolve the event-player UUID.")
            );

            Cow firstAffected = createCow(helper, false);
            Cow secondAffected = createCow(helper, false);
            assertExecuted(helper, dispatch(
                    runtime,
                    new FabricEventCompatHandles.AreaEffectCloudApply(List.of(firstAffected, secondAffected)),
                    helper,
                    null
            ), "area cloud effect event");
            helper.assertTrue(
                    "affected entity".equals(firstAffected.getCustomName() == null ? null : firstAffected.getCustomName().getString())
                            && "affected entity".equals(secondAffected.getCustomName() == null ? null : secondAffected.getCustomName().getString()),
                    Component.literal("Expected affected entities expression to loop through all affected entities.")
            );

            ServerPlayer cooldownPlayer = helper.makeMockServerPlayerInLevel();
            assertExecuted(helper, dispatch(
                    runtime,
                    new FabricEventCompatHandles.ExperienceCooldownChange("pickup"),
                    helper,
                    cooldownPlayer
            ), "experience cooldown change event");
            helper.assertTrue(
                    cooldownPlayer.getCustomName() != null && "pickup".equals(cooldownPlayer.getCustomName().getString()),
                    Component.literal("Expected experience cooldown change reason expression to expose the reason.")
            );

            BlockPos explodedFirst = helper.absolutePos(new BlockPos(4, 1, 0));
            BlockPos explodedSecond = helper.absolutePos(new BlockPos(5, 1, 0));
            helper.getLevel().setBlockAndUpdate(explodedFirst, Blocks.STONE.defaultBlockState());
            helper.getLevel().setBlockAndUpdate(explodedSecond, Blocks.DIRT.defaultBlockState());
            assertExecuted(helper, dispatch(
                    runtime,
                    new FabricEventCompatHandles.Explosion(List.of(
                            new org.skriptlang.skript.fabric.compat.FabricBlock(helper.getLevel(), explodedFirst),
                            new org.skriptlang.skript.fabric.compat.FabricBlock(helper.getLevel(), explodedSecond)
                    )),
                    helper,
                    null
            ), "explosion event");
            helper.assertTrue(
                    helper.getLevel().getBlockState(explodedFirst.above()).is(Blocks.REDSTONE_BLOCK)
                            && helper.getLevel().getBlockState(explodedSecond.above()).is(Blocks.REDSTONE_BLOCK),
                    Component.literal("Expected exploded blocks expression to loop through every exploded block.")
            );

            assertExecuted(helper, dispatch(
                    runtime,
                    new ExplosionPrimeHandle(true),
                    helper,
                    null
            ), "explosion prime event");
            helper.assertBlockPresent(Blocks.EMERALD_BLOCK, new BlockPos(4, 1, 0));

            runtime.clearScripts();
        });
    }

    @GameTest
    public void unleashProducerExecutesRealScript(GameTestHelper helper) {
        runWithRuntimeLock(helper, () -> {
            SkriptRuntime runtime = SkriptRuntime.instance();
            runtime.clearScripts();
            runtime.loadFromResource("skript/gametest/event/unleash_runtime_marks_block.sk");

            BlockPos unleashMarker = helper.absolutePos(new BlockPos(2, 1, 0));
            helper.getLevel().setBlockAndUpdate(unleashMarker, Blocks.AIR.defaultBlockState());

            ServerPlayer leashHolder = helper.makeMockServerPlayerInLevel();
            Cow cow = createCow(helper, false);
            cow.setLeashedTo(leashHolder, true);
            cow.dropLeash();

            helper.assertTrue(
                    helper.getLevel().getBlockState(unleashMarker).is(Blocks.IRON_BLOCK),
                    Component.literal("Expected real unleash producer to execute loaded Skript file.")
            );
            runtime.clearScripts();
        });
    }

    @GameTest
    public void respawnProducerExecutesRealScript(GameTestHelper helper) {
        runWithRuntimeLock(helper, () -> {
            SkriptRuntime runtime = SkriptRuntime.instance();
            runtime.clearScripts();
            runtime.loadFromResource("skript/gametest/event/custom_context_backfill.sk");

            ServerPlayer player = helper.makeMockServerPlayerInLevel();
            helper.getLevel().getServer().getPlayerList().respawn(player, false, Entity.RemovalReason.KILLED);

            helper.assertBlockPresent(Blocks.GOLD_BLOCK, new BlockPos(3, 1, 0));
            runtime.clearScripts();
        });
    }

    @GameTest
    public void eventPayloadBundleExecutesRealScript(GameTestHelper helper) {
        ensureCustomEventsRegistered();
        runWithRuntimeLock(helper, () -> {
            SkriptRuntime runtime = SkriptRuntime.instance();
            runtime.clearScripts();
            runtime.loadFromResource("skript/gametest/expression/mixed_runtime_event_payload_bundle.sk");

            Arrow arrow = new Arrow(helper.getLevel(), 0.5D, 1.0D, 0.5D, ItemStack.EMPTY, null);
            assertExecuted(helper, dispatch(runtime, new ProjectileContextHandle(arrow), helper, null), "projectile context");
            helper.assertTrue(
                    readIntMethod(arrow, "getKnockback") == 2 && readIntMethod(arrow, "getPierceLevel") == 3,
                    Component.literal("Expected projectile context script to mutate arrow knockback and pierce values.")
            );

            MutableEntityDeathHandle deathHandle = new MutableEntityDeathHandle(List.of(new ItemStack(Items.APPLE)), 1);
            assertExecuted(helper, dispatch(runtime, deathHandle, helper, null), "entity death context");
            helper.assertTrue(
                    deathHandle.drops().size() == 1 && deathHandle.drops().get(0).is(Items.DIAMOND),
                    Component.literal("Expected drops script to replace the death drops with diamonds.")
            );

            FabricEventCompatHandles.Explosion explosion = new FabricEventCompatHandles.Explosion(List.of(new FabricBlock(helper.getLevel(), helper.absolutePos(new BlockPos(0, 1, 0)))), 0.75F);
            assertExecuted(helper, dispatch(runtime, explosion, helper, null), "explode mutable context");
            helper.assertTrue(
                    explosion.yield() == 0.0F,
                    Component.literal("Expected explosion block yield script to clear the mutable explosion yield.")
            );

            MutableExplosionPrimeHandle explosionPrime = new MutableExplosionPrimeHandle(1.0F, false);
            assertExecuted(helper, dispatch(runtime, explosionPrime, helper, null), "explosion prime mutable context");
            helper.assertTrue(
                    explosionPrime.radius() == 3.0F,
                    Component.literal("Expected explosion yield script to update the explosion radius.")
            );

            Creeper creeper = (Creeper) helper.spawnWithNoFreeWill(EntityType.CREEPER, 1.5F, 1.0F, 0.5F);
            assertExecuted(helper, dispatch(runtime, new ExplosiveEntityContextHandle(creeper), helper, null), "explosive entity context");
            helper.assertTrue(
                    readIntField(creeper, "explosionRadius") == 5,
                    Component.literal("Expected explosive yield script to update the creeper radius.")
            );

            ArmorStand hangingEntity = new ArmorStand(helper.getLevel(), 2.5D, 1.0D, 0.5D);
            ArmorStand hangingRemover = new ArmorStand(helper.getLevel(), 3.5D, 1.0D, 0.5D);
            helper.getLevel().addFreshEntity(hangingEntity);
            helper.getLevel().addFreshEntity(hangingRemover);
            assertExecuted(helper, dispatch(runtime, new HangingBreakHandle(hangingEntity, hangingRemover), helper, null), "hanging break context");
            helper.assertTrue(
                    "hanging entity".equals(hangingEntity.getCustomName() == null ? null : hangingEntity.getCustomName().getString())
                            && "hanging remover".equals(hangingRemover.getCustomName() == null ? null : hangingRemover.getCustomName().getString()),
                    Component.literal("Expected hanging expressions to resolve both the hanging entity and remover.")
            );

            BlockPos fertilized = helper.absolutePos(new BlockPos(4, 1, 0));
            helper.getLevel().setBlockAndUpdate(fertilized, Blocks.WHEAT.defaultBlockState());
            assertExecuted(helper, dispatch(
                    runtime,
                    new FabricEventCompatHandles.BlockFertilize(List.of(new FabricBlock(helper.getLevel(), fertilized))),
                    helper,
                    null
            ), "block fertilize context");
            helper.assertBlockPresent(Blocks.GOLD_BLOCK, new BlockPos(4, 2, 0));

            runtime.clearScripts();
        });
    }

    private static void ensureCustomEventsRegistered() {
        if (!CUSTOM_EVENTS_REGISTERED.compareAndSet(false, true)) {
            return;
        }
        Skript.registerEvent(GameTestBlockContextEvent.class, "gametest block context");
        Skript.registerEvent(GameTestEntityContextEvent.class, "gametest entity context");
        Skript.registerEvent(GameTestItemEntityContextEvent.class, "gametest item entity context");
        Skript.registerEvent(GameTestAreaCloudEffectEvent.class, "gametest area cloud effect");
        Skript.registerEvent(GameTestExperienceCooldownChangeEvent.class, "gametest player experience cooldown change");
        Skript.registerEvent(GameTestExplosionEvent.class, "gametest explode");
        Skript.registerEvent(GameTestExplodeMutableEvent.class, "gametest explode mutable");
        Skript.registerEvent(GameTestEntityDeathEvent.class, "gametest entity death");
        Skript.registerEvent(GameTestExplosiveEntityContextEvent.class, "gametest explosive entity context");
        Skript.registerEvent(GameTestHangingBreakEvent.class, "gametest hanging break");
        Skript.registerEvent(GameTestHelperContextEvent.class, "gametest helper context");
        Skript.registerEvent(GameTestProjectileContextEvent.class, "gametest projectile context");
        Skript.registerEvent(GameTestExplosionPrimeMutableEvent.class, "gametest explosion prime mutable");
        Skript.registerEvent(GameTestBlockFertilizeEvent.class, "gametest block fertilize");
        Skript.registerEvent(GameTestExplosionPrimeEvent.class, "gametest explosion prime");
    }

    private int dispatch(SkriptRuntime runtime, Object handle, GameTestHelper helper, @Nullable ServerPlayer player) {
        return runtime.dispatch(new org.skriptlang.skript.lang.event.SkriptEvent(
                handle,
                helper.getLevel().getServer(),
                helper.getLevel(),
                player
        ));
    }

    private void assertExecuted(GameTestHelper helper, int executed, String description) {
        helper.assertTrue(
                executed == 1,
                Component.literal("Expected exactly one trigger for " + description + " but got " + executed + ".")
        );
    }

    private @Nullable java.util.UUID readItemOwner(ItemEntity itemEntity) {
        try {
            Field field;
            try {
                field = ItemEntity.class.getDeclaredField("target");
            } catch (NoSuchFieldException ignored) {
                field = ItemEntity.class.getDeclaredField("owner");
            }
            field.setAccessible(true);
            return (java.util.UUID) field.get(itemEntity);
        } catch (ReflectiveOperationException exception) {
            throw new IllegalStateException("Failed to read dropped item owner for GameTest.", exception);
        }
    }

    private static Class<?> effectEventClass(String simpleName) {
        try {
            return Class.forName("ch.njol.skript.effects.FabricEffectEventHandles$" + simpleName);
        } catch (ClassNotFoundException exception) {
            throw new IllegalStateException(exception);
        }
    }

    private static int readIntMethod(Object target, String methodName) {
        try {
            java.lang.reflect.Method method = target.getClass().getMethod(methodName);
            method.setAccessible(true);
            return ((Number) method.invoke(target)).intValue();
        } catch (ReflectiveOperationException exception) {
            throw new IllegalStateException("Failed to invoke " + methodName, exception);
        }
    }

    private static int readIntField(Object target, String fieldName) {
        try {
            for (Class<?> type = target.getClass(); type != null; type = type.getSuperclass()) {
                try {
                    Field field = type.getDeclaredField(fieldName);
                    field.setAccessible(true);
                    return ((Number) field.get(target)).intValue();
                } catch (NoSuchFieldException ignored) {
                }
            }
            throw new NoSuchFieldException(fieldName);
        } catch (ReflectiveOperationException exception) {
            throw new IllegalStateException("Failed to read " + fieldName, exception);
        }
    }

    private record BlockContextHandle(net.minecraft.server.level.ServerLevel level, BlockPos position)
            implements FabricBlockEventHandle {
    }

    private record EntityContextHandle(Entity entity) implements FabricEntityEventHandle {
    }

    private record ItemEntityContextHandle(ItemEntity entity) implements FabricEntityEventHandle {
    }

    private record ProjectileContextHandle(Entity entity) implements FabricEntityEventHandle {
    }

    private record ExplosiveEntityContextHandle(Entity entity) implements FabricEntityEventHandle {
    }

    private record HelperContextHandle() {
    }

    private static final class MutableEntityDeathHandle {

        private final List<ItemStack> drops;
        private int droppedExp;

        private MutableEntityDeathHandle(List<ItemStack> drops, int droppedExp) {
            this.drops = new java.util.ArrayList<>(drops);
            this.droppedExp = droppedExp;
        }

        public List<ItemStack> drops() {
            return drops;
        }

        public int droppedExp() {
            return droppedExp;
        }

        public void setDroppedExp(int droppedExp) {
            this.droppedExp = droppedExp;
        }
    }

    private static final class MutableExplosionPrimeHandle {

        private float radius;
        private boolean causesFire;

        private MutableExplosionPrimeHandle(float radius, boolean causesFire) {
            this.radius = radius;
            this.causesFire = causesFire;
        }

        public float radius() {
            return radius;
        }

        public void setRadius(float radius) {
            this.radius = radius;
        }

        public boolean causesFire() {
            return causesFire;
        }

        public void setCausesFire(boolean causesFire) {
            this.causesFire = causesFire;
        }
    }

    private record HangingBreakHandle(Entity entity, @Nullable Entity remover) {
    }

    private record ExplosionPrimeHandle(boolean causesFire) {
    }

    public static final class GameTestBlockContextEvent extends ch.njol.skript.lang.SkriptEvent {

        @Override
        public boolean init(Literal<?>[] args, int matchedPattern, ParseResult parseResult) {
            return args.length == 0;
        }

        @Override
        public boolean check(org.skriptlang.skript.lang.event.SkriptEvent event) {
            return event.handle() instanceof BlockContextHandle;
        }

        @Override
        public Class<?>[] getEventClasses() {
            return new Class<?>[]{BlockContextHandle.class};
        }

        @Override
        public String toString(@Nullable org.skriptlang.skript.lang.event.SkriptEvent event, boolean debug) {
            return "gametest block context";
        }
    }

    public static final class GameTestEntityContextEvent extends ch.njol.skript.lang.SkriptEvent {

        @Override
        public boolean init(Literal<?>[] args, int matchedPattern, ParseResult parseResult) {
            return args.length == 0;
        }

        @Override
        public boolean check(org.skriptlang.skript.lang.event.SkriptEvent event) {
            return event.handle() instanceof EntityContextHandle;
        }

        @Override
        public Class<?>[] getEventClasses() {
            return new Class<?>[]{EntityContextHandle.class};
        }

        @Override
        public String toString(@Nullable org.skriptlang.skript.lang.event.SkriptEvent event, boolean debug) {
            return "gametest entity context";
        }
    }

    public static final class GameTestItemEntityContextEvent extends ch.njol.skript.lang.SkriptEvent {

        @Override
        public boolean init(Literal<?>[] args, int matchedPattern, ParseResult parseResult) {
            return args.length == 0;
        }

        @Override
        public boolean check(org.skriptlang.skript.lang.event.SkriptEvent event) {
            return event.handle() instanceof ItemEntityContextHandle;
        }

        @Override
        public Class<?>[] getEventClasses() {
            return new Class<?>[]{ItemEntityContextHandle.class};
        }

        @Override
        public String toString(@Nullable org.skriptlang.skript.lang.event.SkriptEvent event, boolean debug) {
            return "gametest item entity context";
        }
    }

    public static final class GameTestAreaCloudEffectEvent extends ch.njol.skript.lang.SkriptEvent {

        @Override
        public boolean init(Literal<?>[] args, int matchedPattern, ParseResult parseResult) {
            return args.length == 0;
        }

        @Override
        public boolean check(org.skriptlang.skript.lang.event.SkriptEvent event) {
            return event.handle() instanceof FabricEventCompatHandles.AreaEffectCloudApply;
        }

        @Override
        public Class<?>[] getEventClasses() {
            return new Class<?>[]{FabricEventCompatHandles.AreaEffectCloudApply.class};
        }

        @Override
        public String toString(@Nullable org.skriptlang.skript.lang.event.SkriptEvent event, boolean debug) {
            return "gametest area cloud effect";
        }
    }

    public static final class GameTestExperienceCooldownChangeEvent extends ch.njol.skript.lang.SkriptEvent {

        @Override
        public boolean init(Literal<?>[] args, int matchedPattern, ParseResult parseResult) {
            return args.length == 0;
        }

        @Override
        public boolean check(org.skriptlang.skript.lang.event.SkriptEvent event) {
            return event.handle() instanceof FabricEventCompatHandles.ExperienceCooldownChange;
        }

        @Override
        public Class<?>[] getEventClasses() {
            return new Class<?>[]{FabricEventCompatHandles.ExperienceCooldownChange.class};
        }

        @Override
        public String toString(@Nullable org.skriptlang.skript.lang.event.SkriptEvent event, boolean debug) {
            return "gametest player experience cooldown change";
        }
    }

    public static final class GameTestExplosionEvent extends ch.njol.skript.lang.SkriptEvent {

        @Override
        public boolean init(Literal<?>[] args, int matchedPattern, ParseResult parseResult) {
            return args.length == 0;
        }

        @Override
        public boolean check(org.skriptlang.skript.lang.event.SkriptEvent event) {
            return event.handle() instanceof FabricEventCompatHandles.Explosion;
        }

        @Override
        public Class<?>[] getEventClasses() {
            return new Class<?>[]{FabricEventCompatHandles.Explosion.class};
        }

        @Override
        public String toString(@Nullable org.skriptlang.skript.lang.event.SkriptEvent event, boolean debug) {
            return "gametest explode";
        }
    }

    public static final class GameTestExplodeMutableEvent extends ch.njol.skript.lang.SkriptEvent {

        @Override
        public boolean init(Literal<?>[] args, int matchedPattern, ParseResult parseResult) {
            return args.length == 0;
        }

        @Override
        public boolean check(org.skriptlang.skript.lang.event.SkriptEvent event) {
            return event.handle() instanceof FabricEventCompatHandles.Explosion;
        }

        @Override
        public Class<?>[] getEventClasses() {
            return new Class<?>[]{FabricEventCompatHandles.Explosion.class};
        }

        @Override
        public String toString(@Nullable org.skriptlang.skript.lang.event.SkriptEvent event, boolean debug) {
            return "gametest explode mutable";
        }
    }

    public static final class GameTestExplosionPrimeEvent extends ch.njol.skript.lang.SkriptEvent {

        @Override
        public boolean init(Literal<?>[] args, int matchedPattern, ParseResult parseResult) {
            return args.length == 0;
        }

        @Override
        public boolean check(org.skriptlang.skript.lang.event.SkriptEvent event) {
            return event.handle() instanceof ExplosionPrimeHandle;
        }

        @Override
        public Class<?>[] getEventClasses() {
            return new Class<?>[]{EXPLOSION_PRIME_EVENT};
        }

        @Override
        public String toString(@Nullable org.skriptlang.skript.lang.event.SkriptEvent event, boolean debug) {
            return "gametest explosion prime";
        }
    }

    public static final class GameTestExplosionPrimeMutableEvent extends ch.njol.skript.lang.SkriptEvent {

        @Override
        public boolean init(Literal<?>[] args, int matchedPattern, ParseResult parseResult) {
            return args.length == 0;
        }

        @Override
        public boolean check(org.skriptlang.skript.lang.event.SkriptEvent event) {
            return event.handle() instanceof MutableExplosionPrimeHandle;
        }

        @Override
        public Class<?>[] getEventClasses() {
            return new Class<?>[]{EXPLOSION_PRIME_EVENT};
        }

        @Override
        public String toString(@Nullable org.skriptlang.skript.lang.event.SkriptEvent event, boolean debug) {
            return "gametest explosion prime mutable";
        }
    }

    public static final class GameTestEntityDeathEvent extends ch.njol.skript.lang.SkriptEvent {

        @Override
        public boolean init(Literal<?>[] args, int matchedPattern, ParseResult parseResult) {
            return args.length == 0;
        }

        @Override
        public boolean check(org.skriptlang.skript.lang.event.SkriptEvent event) {
            return event.handle() instanceof MutableEntityDeathHandle;
        }

        @Override
        public Class<?>[] getEventClasses() {
            return new Class<?>[]{ENTITY_DEATH_EVENT};
        }

        @Override
        public String toString(@Nullable org.skriptlang.skript.lang.event.SkriptEvent event, boolean debug) {
            return "gametest entity death";
        }
    }

    public static final class GameTestExplosiveEntityContextEvent extends ch.njol.skript.lang.SkriptEvent {

        @Override
        public boolean init(Literal<?>[] args, int matchedPattern, ParseResult parseResult) {
            return args.length == 0;
        }

        @Override
        public boolean check(org.skriptlang.skript.lang.event.SkriptEvent event) {
            return event.handle() instanceof ExplosiveEntityContextHandle;
        }

        @Override
        public Class<?>[] getEventClasses() {
            return new Class<?>[]{ExplosiveEntityContextHandle.class};
        }

        @Override
        public String toString(@Nullable org.skriptlang.skript.lang.event.SkriptEvent event, boolean debug) {
            return "gametest explosive entity context";
        }
    }

    public static final class GameTestHangingBreakEvent extends ch.njol.skript.lang.SkriptEvent {

        @Override
        public boolean init(Literal<?>[] args, int matchedPattern, ParseResult parseResult) {
            return args.length == 0;
        }

        @Override
        public boolean check(org.skriptlang.skript.lang.event.SkriptEvent event) {
            return event.handle() instanceof HangingBreakHandle;
        }

        @Override
        public Class<?>[] getEventClasses() {
            return new Class<?>[]{HANGING_BREAK_EVENT};
        }

        @Override
        public String toString(@Nullable org.skriptlang.skript.lang.event.SkriptEvent event, boolean debug) {
            return "gametest hanging break";
        }
    }

    public static final class GameTestHelperContextEvent extends ch.njol.skript.lang.SkriptEvent {

        @Override
        public boolean init(Literal<?>[] args, int matchedPattern, ParseResult parseResult) {
            return args.length == 0;
        }

        @Override
        public boolean check(org.skriptlang.skript.lang.event.SkriptEvent event) {
            return event.handle() instanceof HelperContextHandle;
        }

        @Override
        public Class<?>[] getEventClasses() {
            return new Class<?>[]{HelperContextHandle.class};
        }

        @Override
        public String toString(@Nullable org.skriptlang.skript.lang.event.SkriptEvent event, boolean debug) {
            return "gametest helper context";
        }
    }

    public static final class GameTestProjectileContextEvent extends ch.njol.skript.lang.SkriptEvent {

        @Override
        public boolean init(Literal<?>[] args, int matchedPattern, ParseResult parseResult) {
            return args.length == 0;
        }

        @Override
        public boolean check(org.skriptlang.skript.lang.event.SkriptEvent event) {
            return event.handle() instanceof ProjectileContextHandle;
        }

        @Override
        public Class<?>[] getEventClasses() {
            return new Class<?>[]{ProjectileContextHandle.class};
        }

        @Override
        public String toString(@Nullable org.skriptlang.skript.lang.event.SkriptEvent event, boolean debug) {
            return "gametest projectile context";
        }
    }

    public static final class GameTestBlockFertilizeEvent extends ch.njol.skript.lang.SkriptEvent {

        @Override
        public boolean init(Literal<?>[] args, int matchedPattern, ParseResult parseResult) {
            return args.length == 0;
        }

        @Override
        public boolean check(org.skriptlang.skript.lang.event.SkriptEvent event) {
            return event.handle() instanceof FabricEventCompatHandles.BlockFertilize;
        }

        @Override
        public Class<?>[] getEventClasses() {
            return new Class<?>[]{FabricEventCompatHandles.BlockFertilize.class};
        }

        @Override
        public String toString(@Nullable org.skriptlang.skript.lang.event.SkriptEvent event, boolean debug) {
            return "gametest block fertilize";
        }
    }
}
