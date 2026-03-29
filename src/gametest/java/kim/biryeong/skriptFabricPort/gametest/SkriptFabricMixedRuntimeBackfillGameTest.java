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
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.zombie.Zombie;
import net.minecraft.world.entity.monster.zombie.ZombifiedPiglin;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.entity.monster.skeleton.Skeleton;
import net.minecraft.world.entity.animal.cow.Cow;
import net.minecraft.world.entity.decoration.ItemFrame;
import net.minecraft.world.entity.projectile.arrow.Arrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.fabric.runtime.FabricBlockEventHandle;
import org.skriptlang.skript.fabric.runtime.FabricEntityEventHandle;
import org.skriptlang.skript.fabric.runtime.SkriptRuntime;

public final class SkriptFabricMixedRuntimeBackfillGameTest extends AbstractSkriptFabricGameTestSupport {

    private static final AtomicBoolean CUSTOM_EVENTS_REGISTERED = new AtomicBoolean(false);

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
    public void playerUnleashProducerExecutesRealScript(GameTestHelper helper) {
        runWithRuntimeLock(helper, () -> {
            SkriptRuntime runtime = SkriptRuntime.instance();
            runtime.clearScripts();
            runtime.loadFromResource("skript/gametest/event/player_unleash_runtime_marks_block.sk");

            BlockPos unleashMarker = helper.absolutePos(new BlockPos(2, 1, 0));
            helper.getLevel().setBlockAndUpdate(unleashMarker, Blocks.AIR.defaultBlockState());

            ServerPlayer leashHolder = helper.makeMockServerPlayerInLevel();
            Cow cow = createCow(helper, false);
            cow.setLeashedTo(leashHolder, true);
            cow.dropLeash();

            helper.assertTrue(
                    helper.getLevel().getBlockState(unleashMarker).is(Blocks.IRON_BLOCK),
                    Component.literal("Expected real player unleash producer to execute loaded Skript file.")
            );
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

            Creeper creeper = (Creeper) helper.spawnWithNoFreeWill(EntityType.CREEPER, 1.5F, 1.0F, 0.5F);
            assertExecuted(helper, dispatch(runtime, new ExplosiveEntityContextHandle(creeper), helper, null), "explosive entity context");
            helper.assertTrue(
                    readIntField(creeper, "explosionRadius") == 5,
                    Component.literal("Expected explosive yield script to update the creeper radius.")
            );

            BlockPos fertilized = helper.absolutePos(new BlockPos(4, 1, 0));
            BlockState initialState = Blocks.WHEAT.defaultBlockState();
            helper.getLevel().setBlockAndUpdate(fertilized, initialState);
            ServerPlayer player = helper.makeMockServerPlayerInLevel();
            player.setGameMode(GameType.SURVIVAL);
            player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.BONE_MEAL));
            player.teleportTo(fertilized.getX() + 0.5D, fertilized.getY() + 1.0D, fertilized.getZ() + 0.5D);
            InteractionResult fertilizeResult = player.getItemInHand(InteractionHand.MAIN_HAND).useOn(new UseOnContext(
                    player,
                    InteractionHand.MAIN_HAND,
                    new BlockHitResult(Vec3.atCenterOf(fertilized), Direction.UP, fertilized, false)
            ));
            helper.assertTrue(
                    fertilizeResult.consumesAction(),
                    Component.literal("Expected the real bonemeal item path to fertilize the crop.")
            );
            helper.assertTrue(
                    !helper.getLevel().getBlockState(fertilized).equals(initialState),
                    Component.literal("Expected bonemeal to grow the crop before asserting block fertilize hooks.")
            );
            helper.assertBlockPresent(Blocks.GOLD_BLOCK, new BlockPos(4, 2, 0));

            runtime.clearScripts();
        });
    }

    @GameTest
    public void hangingBreakPayloadExecutesPublicSyntaxOnRealItemFrameBreak(GameTestHelper helper) {
        runWithRuntimeLock(helper, () -> {
            SkriptRuntime runtime = SkriptRuntime.instance();
            runtime.clearScripts();
            runtime.loadFromResource("skript/gametest/expression/mixed_runtime_event_payload_bundle.sk");

            BlockPos supportPos = helper.absolutePos(new BlockPos(2, 1, 0));
            helper.getLevel().setBlockAndUpdate(supportPos, Blocks.STONE.defaultBlockState());

            ItemFrame itemFrame = new ItemFrame(helper.getLevel(), supportPos, Direction.NORTH);
            helper.getLevel().addFreshEntity(itemFrame);

            ServerPlayer player = helper.makeMockServerPlayerInLevel();
            player.setGameMode(GameType.SURVIVAL);

            helper.assertTrue(
                    itemFrame.hurtServer(helper.getLevel(), helper.getLevel().damageSources().playerAttack(player), 1.0F),
                    Component.literal("Expected the real item frame damage path to report a successful hanging break.")
            );
            helper.assertTrue(
                    !itemFrame.isAlive(),
                    Component.literal("Expected the attacked item frame to break on the real damage path.")
            );
            helper.assertTrue(
                    "hanging entity".equals(itemFrame.getCustomName() == null ? null : itemFrame.getCustomName().getString())
                            && "hanging remover".equals(player.getCustomName() == null ? null : player.getCustomName().getString()),
                    Component.literal("Expected public hanging expressions to resolve both the broken item frame and its remover.")
            );

            runtime.clearScripts();
        });
    }

    @GameTest
    public void explosionPrimeMutableProducerExecutesRealScript(GameTestHelper helper) {
        runWithRuntimeLock(helper, () -> {
            SkriptRuntime runtime = SkriptRuntime.instance();
            BlockPos woolPos = new BlockPos(2, 1, 0);
            runtime.clearScripts();
            runtime.loadFromResource("skript/gametest/event/explosion_prime_mutates_radius.sk");

            helper.setBlock(woolPos, Blocks.WHITE_WOOL);
            Creeper creeper = (Creeper) helper.spawnWithNoFreeWill(EntityType.CREEPER, 1.5F, 1.0F, 0.5F);
            setIntField(creeper, "maxSwell", 1);

            ServerPlayer player = helper.makeMockServerPlayerInLevel();
            player.setGameMode(GameType.CREATIVE);
            player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.FLINT_AND_STEEL));

            helper.assertTrue(
                    player.interactOn(creeper, InteractionHand.MAIN_HAND, creeper.position()).consumesAction(),
                    Component.literal("Expected creeper ignition to succeed through the real interaction path.")
            );
            for (int i = 0; i < 4 && creeper.isAlive(); i++) {
                creeper.tick();
            }

            helper.assertBlockPresent(Blocks.WHITE_WOOL, woolPos);
            helper.assertTrue(
                    !creeper.isAlive(),
                    Component.literal("Expected the ignited creeper to reach its real explosion path.")
            );
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
        Skript.registerEvent(GameTestExplosiveEntityContextEvent.class, "gametest explosive entity context");
        Skript.registerEvent(GameTestHelperContextEvent.class, "gametest helper context");
        Skript.registerEvent(GameTestProjectileContextEvent.class, "gametest projectile context");
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

}
