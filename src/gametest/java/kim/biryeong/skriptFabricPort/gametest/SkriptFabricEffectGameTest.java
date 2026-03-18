package kim.biryeong.skriptFabricPort.gametest;

import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.lang.Condition;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ParseContext;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.lang.Trigger;
import ch.njol.skript.lang.TriggerItem;
import ch.njol.skript.lang.TriggerSection;
import ch.njol.skript.lang.parser.ParserInstance;
import ch.njol.skript.lang.util.SimpleLiteral;
import ch.njol.skript.registrations.Classes;
import ch.njol.skript.util.Timespan;
import ch.njol.skript.util.Timespan.TimePeriod;
import ch.njol.skript.variables.Variables;
import com.mojang.authlib.GameProfile;
import com.mojang.math.Transformation;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.event.player.AttackEntityCallback;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.fabricmc.fabric.api.event.player.UseEntityCallback;
import net.fabricmc.fabric.api.event.player.UseItemCallback;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ServerboundPlayerInputPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.core.Direction;
import net.minecraft.util.Brightness;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.AreaEffectCloud;
import net.minecraft.world.entity.Display;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.InsideBlockEffectApplier;
import net.minecraft.world.entity.Interaction;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.animal.Cow;
import net.minecraft.world.entity.animal.Dolphin;
import net.minecraft.world.entity.animal.Pufferfish;
import net.minecraft.world.entity.animal.axolotl.Axolotl;
import net.minecraft.world.entity.animal.horse.Horse;
import net.minecraft.world.entity.animal.sheep.Sheep;
import net.minecraft.world.entity.animal.wolf.Wolf;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.monster.Illusioner;
import net.minecraft.world.entity.monster.Spider;
import net.minecraft.world.entity.monster.ZombieVillager;
import net.minecraft.world.entity.monster.warden.Warden;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.player.Input;
import net.minecraft.world.entity.projectile.Arrow;
import net.minecraft.world.entity.projectile.FishingHook;
import net.minecraft.world.entity.projectile.ThrownSplashPotion;
import net.minecraft.world.entity.vehicle.MinecartChest;
import net.minecraft.world.inventory.FurnaceResultSlot;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.item.equipment.Equippable;
import net.minecraft.world.item.trading.ItemCost;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.WitherRoseBlock;
import net.minecraft.world.entity.animal.Bee;
import net.minecraft.world.level.block.entity.AbstractFurnaceBlockEntity;
import net.minecraft.world.level.block.entity.BeaconBlockEntity;
import net.minecraft.world.level.block.entity.BeehiveBlockEntity;
import net.minecraft.world.level.block.entity.BrewingStandBlockEntity;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.block.entity.ConduitBlockEntity;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import org.skriptlang.skript.bukkit.base.types.InventoryClassInfo;
import org.skriptlang.skript.bukkit.base.types.ItemStackClassInfo;
import org.skriptlang.skript.bukkit.base.types.ItemTypeClassInfo;
import org.skriptlang.skript.bukkit.base.types.LocationClassInfo;
import org.skriptlang.skript.bukkit.base.types.NameableClassInfo;
import org.skriptlang.skript.bukkit.base.types.OfflinePlayerClassInfo;
import org.skriptlang.skript.bukkit.base.types.SlotClassInfo;
import org.skriptlang.skript.bukkit.base.types.VectorClassInfo;
import org.skriptlang.skript.bukkit.damagesource.elements.CondScalesWithDifficulty;
import org.skriptlang.skript.bukkit.damagesource.elements.CondWasIndirect;
import org.skriptlang.skript.bukkit.displays.text.CondTextDisplayHasDropShadow;
import org.skriptlang.skript.bukkit.brewing.elements.CondBrewingConsume;
import org.skriptlang.skript.bukkit.fishing.elements.CondFishingLure;
import org.skriptlang.skript.bukkit.fishing.elements.CondIsInOpenWater;
import org.skriptlang.skript.bukkit.input.InputKey;
import org.skriptlang.skript.bukkit.input.elements.conditions.CondIsPressingKey;
import org.skriptlang.skript.bukkit.itemcomponents.equippable.elements.CondEquipCompDamage;
import org.skriptlang.skript.bukkit.itemcomponents.equippable.elements.CondEquipCompDispensable;
import org.skriptlang.skript.bukkit.itemcomponents.equippable.elements.CondEquipCompInteract;
import org.skriptlang.skript.bukkit.itemcomponents.equippable.elements.CondEquipCompShearable;
import org.skriptlang.skript.bukkit.itemcomponents.equippable.elements.CondEquipCompSwapEquipment;
import org.skriptlang.skript.bukkit.interactions.elements.conditions.CondIsResponsive;
import org.skriptlang.skript.bukkit.loottables.LootTable;
import org.skriptlang.skript.bukkit.loottables.LootTableUtils;
import org.skriptlang.skript.bukkit.loottables.elements.conditions.CondHasLootTable;
import org.skriptlang.skript.bukkit.breeding.elements.CondCanAge;
import org.skriptlang.skript.bukkit.breeding.elements.CondCanBreed;
import org.skriptlang.skript.bukkit.breeding.elements.CondIsAdult;
import org.skriptlang.skript.bukkit.breeding.elements.CondIsBaby;
import org.skriptlang.skript.bukkit.breeding.elements.CondIsInLove;
import org.skriptlang.skript.bukkit.potion.elements.conditions.CondHasPotion;
import org.skriptlang.skript.bukkit.potion.elements.conditions.CondIsPoisoned;
import org.skriptlang.skript.bukkit.potion.elements.conditions.CondIsPotionAmbient;
import org.skriptlang.skript.bukkit.potion.elements.conditions.CondIsPotionInstant;
import org.skriptlang.skript.bukkit.potion.elements.conditions.CondPotionHasIcon;
import org.skriptlang.skript.bukkit.potion.elements.conditions.CondPotionHasParticles;
import org.skriptlang.skript.bukkit.potion.util.PotionEffectSupport;
import org.skriptlang.skript.bukkit.potion.util.SkriptPotionEffect;
import org.skriptlang.skript.bukkit.tags.elements.CondIsTagged;
import org.skriptlang.skript.fabric.compat.FabricBlock;
import org.skriptlang.skript.fabric.compat.FabricBreedingState;
import org.skriptlang.skript.fabric.compat.FabricFishingState;
import org.skriptlang.skript.fabric.compat.FabricInventory;
import org.skriptlang.skript.fabric.compat.FabricItemType;
import org.skriptlang.skript.fabric.compat.FabricLocation;
import org.skriptlang.skript.fabric.compat.MinecraftResourceParser;
import org.skriptlang.skript.fabric.compat.PrivateBlockEntityAccess;
import org.skriptlang.skript.fabric.compat.PrivateEntityAccess;
import org.skriptlang.skript.fabric.compat.PrivateFishingHookAccess;
import org.skriptlang.skript.fabric.compat.PrivateFurnaceAccess;
import org.skriptlang.skript.fabric.runtime.FabricAttackEntityHandle;
import org.skriptlang.skript.fabric.runtime.FabricBlockBreakHandle;
import org.skriptlang.skript.fabric.runtime.FabricBrewingFuelHandle;
import org.skriptlang.skript.fabric.runtime.FabricDamageHandle;
import org.skriptlang.skript.fabric.runtime.FabricFishingEventHandle;
import org.skriptlang.skript.fabric.runtime.FabricPotionEffectCause;
import org.skriptlang.skript.fabric.runtime.FabricFishingHandle;
import org.skriptlang.skript.fabric.runtime.FabricPlayerInputHandle;
import org.skriptlang.skript.fabric.runtime.FabricUseEntityHandle;
import org.skriptlang.skript.fabric.runtime.FabricUseItemHandle;
import org.skriptlang.skript.fabric.runtime.GameTestRuntimeContext;
import org.skriptlang.skript.fabric.runtime.SkriptRuntime;
import org.skriptlang.skript.lang.properties.Property;
import org.skriptlang.skript.lang.properties.handlers.WXYZHandler;
import ch.njol.util.Kleenean;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;
import org.joml.Quaternionf;
import org.joml.Vector3f;

public final class SkriptFabricEffectGameTest extends AbstractSkriptFabricGameTestSupport {

    @GameTest
    public void preventAgingEffectExecutesRealScript(GameTestHelper helper) {
        Cow cow = createCow(helper, false);
        assertUseEntityScriptSetsMarker(
                helper,
                "skript/gametest/effect/allow_aging_marks_block.sk",
                cow,
                new BlockPos(9, 1, 0),
                Blocks.LIME_WOOL,
                () -> helper.assertTrue(
                        !FabricBreedingState.canAge(cow),
                        Component.literal("Expected prevent-aging effect to lock animal aging.")
                )
        );
    }

    @GameTest
    public void makeUnbreedableEffectExecutesRealScript(GameTestHelper helper) {
        Cow cow = createCow(helper, false);
        assertUseEntityScriptSetsMarker(
                helper,
                "skript/gametest/effect/unbreedable_marks_block.sk",
                cow,
                new BlockPos(9, 1, 0),
                Blocks.ORANGE_WOOL,
                () -> helper.assertTrue(
                        !FabricBreedingState.canBreed(cow),
                        Component.literal("Expected breedable effect to persist custom unbreedable state.")
                )
        );
    }

    @GameTest
    public void makeAdultEffectExecutesRealScript(GameTestHelper helper) {
        Cow cow = createCow(helper, true);
        assertUseEntityScriptSetsMarker(
                helper,
                "skript/gametest/effect/make_adult_marks_block.sk",
                cow,
                new BlockPos(9, 1, 0),
                Blocks.WHITE_WOOL,
                () -> helper.assertTrue(
                        !cow.isBaby(),
                        Component.literal("Expected make-adult effect to convert the cow into an adult.")
                )
        );
    }

    @GameTest
    public void makeBabyEffectExecutesRealScript(GameTestHelper helper) {
        Cow cow = createCow(helper, false);
        assertUseEntityScriptSetsMarker(
                helper,
                "skript/gametest/effect/make_baby_marks_block.sk",
                cow,
                new BlockPos(9, 1, 0),
                Blocks.BLACK_WOOL,
                () -> helper.assertTrue(
                        cow.isBaby(),
                        Component.literal("Expected make-baby effect to convert the cow into a baby.")
                )
        );
    }

    @GameTest
    public void doIfEffectExecutesRealScript(GameTestHelper helper) {
        Interaction interaction = createInteraction(helper, true);
        assertUseEntityScriptNamesEntity(
                helper,
                "skript/gametest/effect/do_if_names_entity.sk",
                interaction,
                "do_if_effect"
        );
    }

    @GameTest
    public void equipEffectExecutesRealScript(GameTestHelper helper) {
        Cow cow = createCow(helper, false);
        runWithRuntimeLock(helper, () -> {
            SkriptRuntime runtime = SkriptRuntime.instance();
            BlockPos markerPos = new BlockPos(9, 1, 0);
            runtime.clearScripts();
            runtime.loadFromResource("skript/gametest/effect/equip_entity_marks_block.sk");

            BlockPos markerAbsolute = helper.absolutePos(markerPos);
            helper.getLevel().setBlockAndUpdate(markerAbsolute, Blocks.AIR.defaultBlockState());

            ServerPlayer player = helper.makeMockServerPlayerInLevel();
            player.setGameMode(GameType.CREATIVE);
            player.teleportTo(markerAbsolute.getX() + 0.5D, markerAbsolute.getY() + 1.0D, markerAbsolute.getZ() + 0.5D);

            InteractionResult result = GameTestRuntimeContext.withHelper(helper, () -> UseEntityCallback.EVENT.invoker().interact(
                    player,
                    helper.getLevel(),
                    InteractionHand.MAIN_HAND,
                    cow,
                    new EntityHitResult(cow)
            ));
            helper.assertTrue(
                    result == InteractionResult.PASS,
                    Component.literal("Expected use entity equip test to keep Fabric callback flow in PASS state.")
            );
            helper.assertBlockPresent(Blocks.EMERALD_BLOCK, markerPos);
            helper.assertTrue(
                    cow.getItemBySlot(EquipmentSlot.HEAD).is(Items.DIAMOND_HELMET),
                    Component.literal("Expected equip effect to place a diamond helmet on the target entity.")
            );
            runtime.clearScripts();
            helper.succeed();
        });
    }

    @GameTest
    public void recoveredMobAndPlayerEffectsExecuteRealScript(GameTestHelper helper) {
        Wolf wolf = (Wolf) helper.spawnWithNoFreeWill(EntityType.WOLF, 0.5F, 1.0F, 0.5F);
        runWithRuntimeLock(helper, () -> {
            SkriptRuntime runtime = SkriptRuntime.instance();
            BlockPos markerPos = new BlockPos(9, 1, 0);
            runtime.clearScripts();
            runtime.loadFromResource("skript/gametest/effect/recovered_mob_effects_mark_block.sk");

            BlockPos markerAbsolute = helper.absolutePos(markerPos);
            helper.getLevel().setBlockAndUpdate(markerAbsolute, Blocks.AIR.defaultBlockState());

            wolf.setCustomNameVisible(false);
            wolf.setLeftHanded(false);
            wolf.setCanPickUpLoot(false);
            wolf.setTame(false, true);
            wolf.dropLeash();
            wolf.setRemainingFireTicks(0);

            ServerPlayer player = helper.makeMockServerPlayerInLevel();
            player.setGameMode(GameType.CREATIVE);
            player.getAbilities().mayfly = false;
            player.getAbilities().flying = false;
            player.onUpdateAbilities();
            player.teleportTo(markerAbsolute.getX() + 0.5D, markerAbsolute.getY() + 1.0D, markerAbsolute.getZ() + 0.5D);

            InteractionResult result = GameTestRuntimeContext.withHelper(helper, () -> UseEntityCallback.EVENT.invoker().interact(
                    player,
                    helper.getLevel(),
                    InteractionHand.MAIN_HAND,
                    wolf,
                    new EntityHitResult(wolf)
            ));
            helper.assertTrue(
                    result == InteractionResult.PASS,
                    Component.literal("Expected recovered effect batch to keep Fabric use-entity callback flow in PASS state.")
            );
            helper.assertBlockPresent(Blocks.EMERALD_BLOCK, markerPos);
            helper.assertTrue(wolf.isCustomNameVisible(), Component.literal("Expected custom-name effect to show the wolf's custom name."));
            helper.assertTrue(wolf.isLeftHanded(), Component.literal("Expected handedness effect to make the wolf left-handed."));
            helper.assertTrue(wolf.getRemainingFireTicks() > 0, Component.literal("Expected ignite effect to set fire ticks on the wolf."));
            helper.assertTrue(wolf.isTame(), Component.literal("Expected tame effect to tame the wolf."));
            helper.assertTrue(wolf.canPickUpLoot(), Component.literal("Expected pick-up-items effect to allow loot pickup."));
            helper.assertTrue(wolf.isLeashed() && wolf.getLeashHolder() == player, Component.literal("Expected leash effect to attach the wolf to the player."));
            helper.assertTrue(player.getAbilities().mayfly && player.getAbilities().flying, Component.literal("Expected make-fly effect to enable and start player flight."));
            runtime.clearScripts();
            helper.succeed();
        });
    }

    @GameTest
    public void recoveredShearEffectExecutesRealScript(GameTestHelper helper) {
        Sheep sheep = (Sheep) helper.spawnWithNoFreeWill(EntityType.SHEEP, 0.5F, 1.0F, 0.5F);
        runWithRuntimeLock(helper, () -> {
            SkriptRuntime runtime = SkriptRuntime.instance();
            BlockPos markerPos = new BlockPos(9, 1, 0);
            runtime.clearScripts();
            runtime.loadFromResource("skript/gametest/effect/recovered_shear_marks_block.sk");

            BlockPos markerAbsolute = helper.absolutePos(markerPos);
            helper.getLevel().setBlockAndUpdate(markerAbsolute, Blocks.AIR.defaultBlockState());
            sheep.setSheared(false);

            ServerPlayer player = helper.makeMockServerPlayerInLevel();
            player.setGameMode(GameType.CREATIVE);
            player.teleportTo(markerAbsolute.getX() + 0.5D, markerAbsolute.getY() + 1.0D, markerAbsolute.getZ() + 0.5D);

            InteractionResult result = GameTestRuntimeContext.withHelper(helper, () -> UseEntityCallback.EVENT.invoker().interact(
                    player,
                    helper.getLevel(),
                    InteractionHand.MAIN_HAND,
                    sheep,
                    new EntityHitResult(sheep)
            ));
            helper.assertTrue(result == InteractionResult.PASS, Component.literal("Expected shear effect callback flow to stay PASS."));
            helper.assertBlockPresent(Blocks.LIME_WOOL, markerPos);
            helper.assertTrue(sheep.isSheared(), Component.literal("Expected shear effect to shear the sheep."));
            runtime.clearScripts();
            helper.succeed();
        });
    }

    @GameTest
    public void recoveredAxolotlPlayDeadEffectExecutesRealScript(GameTestHelper helper) {
        Axolotl axolotl = (Axolotl) helper.spawnWithNoFreeWill(EntityType.AXOLOTL, 0.5F, 1.0F, 0.5F);
        runWithRuntimeLock(helper, () -> {
            SkriptRuntime runtime = SkriptRuntime.instance();
            BlockPos markerPos = new BlockPos(9, 1, 0);
            runtime.clearScripts();
            runtime.loadFromResource("skript/gametest/effect/recovered_axolotl_play_dead_marks_block.sk");

            BlockPos markerAbsolute = helper.absolutePos(markerPos);
            helper.getLevel().setBlockAndUpdate(markerAbsolute, Blocks.AIR.defaultBlockState());
            axolotl.setPlayingDead(false);

            ServerPlayer player = helper.makeMockServerPlayerInLevel();
            player.setGameMode(GameType.CREATIVE);
            player.teleportTo(markerAbsolute.getX() + 0.5D, markerAbsolute.getY() + 1.0D, markerAbsolute.getZ() + 0.5D);

            InteractionResult result = GameTestRuntimeContext.withHelper(helper, () -> UseEntityCallback.EVENT.invoker().interact(
                    player,
                    helper.getLevel(),
                    InteractionHand.MAIN_HAND,
                    axolotl,
                    new EntityHitResult(axolotl)
            ));
            helper.assertTrue(result == InteractionResult.PASS, Component.literal("Expected axolotl play-dead effect callback flow to stay PASS."));
            helper.assertBlockPresent(Blocks.BLUE_WOOL, markerPos);
            helper.assertTrue(axolotl.isPlayingDead(), Component.literal("Expected play-dead effect to toggle axolotl state."));
            runtime.clearScripts();
            helper.succeed();
        });
    }

    @GameTest
    public void recoveredHorseEatingEffectExecutesRealScript(GameTestHelper helper) {
        Horse horse = (Horse) helper.spawnWithNoFreeWill(EntityType.HORSE, 0.5F, 1.0F, 0.5F);
        runWithRuntimeLock(helper, () -> {
            SkriptRuntime runtime = SkriptRuntime.instance();
            BlockPos markerPos = new BlockPos(9, 1, 0);
            runtime.clearScripts();
            runtime.loadFromResource("skript/gametest/effect/recovered_horse_eating_marks_block.sk");

            BlockPos markerAbsolute = helper.absolutePos(markerPos);
            helper.getLevel().setBlockAndUpdate(markerAbsolute, Blocks.AIR.defaultBlockState());
            horse.setEating(false);

            ServerPlayer player = helper.makeMockServerPlayerInLevel();
            player.setGameMode(GameType.CREATIVE);
            player.teleportTo(markerAbsolute.getX() + 0.5D, markerAbsolute.getY() + 1.0D, markerAbsolute.getZ() + 0.5D);

            InteractionResult result = GameTestRuntimeContext.withHelper(helper, () -> UseEntityCallback.EVENT.invoker().interact(
                    player,
                    helper.getLevel(),
                    InteractionHand.MAIN_HAND,
                    horse,
                    new EntityHitResult(horse)
            ));
            helper.assertTrue(result == InteractionResult.PASS, Component.literal("Expected horse eating effect callback flow to stay PASS."));
            helper.assertBlockPresent(Blocks.GOLD_BLOCK, markerPos);
            helper.assertTrue(horse.isEating(), Component.literal("Expected eating effect to set horse eating state."));
            runtime.clearScripts();
            helper.succeed();
        });
    }

    @GameTest
    public void damageEffectExecutesRealScript(GameTestHelper helper) {
        Cow cow = createCow(helper, false);
        float startingHealth = cow.getHealth();
        runWithRuntimeLock(helper, () -> {
            SkriptRuntime runtime = SkriptRuntime.instance();
            BlockPos markerPos = new BlockPos(9, 1, 0);
            runtime.clearScripts();
            runtime.loadFromResource("skript/gametest/effect/damage_entity_marks_block.sk");

            BlockPos markerAbsolute = helper.absolutePos(markerPos);
            helper.getLevel().setBlockAndUpdate(markerAbsolute, Blocks.AIR.defaultBlockState());

            ServerPlayer player = helper.makeMockServerPlayerInLevel();
            player.setGameMode(GameType.CREATIVE);
            player.teleportTo(markerAbsolute.getX() + 0.5D, markerAbsolute.getY() + 1.0D, markerAbsolute.getZ() + 0.5D);

            InteractionResult result = GameTestRuntimeContext.withHelper(helper, () -> UseEntityCallback.EVENT.invoker().interact(
                    player,
                    helper.getLevel(),
                    InteractionHand.MAIN_HAND,
                    cow,
                    new EntityHitResult(cow)
            ));
            helper.assertTrue(
                    result == InteractionResult.PASS,
                    Component.literal("Expected use entity damage test to keep Fabric callback flow in PASS state.")
            );
            helper.assertBlockPresent(Blocks.REDSTONE_BLOCK, markerPos);
            helper.assertTrue(
                    cow.getHealth() < startingHealth,
                    Component.literal("Expected damage effect to reduce the target entity's health.")
            );
            runtime.clearScripts();
            helper.succeed();
        });
    }

    @GameTest
    public void delayEffectExecutesRealScript(GameTestHelper helper) {
        runWithRuntimeLock(helper, () -> {
            SkriptRuntime runtime = SkriptRuntime.instance();
            BlockPos markerPos = new BlockPos(0, 1, 0);
            helper.startSequence()
                    .thenExecute(() -> {
                        Variables.clearAll();
                        runtime.clearScripts();
                        helper.getLevel().setBlockAndUpdate(helper.absolutePos(markerPos), Blocks.AIR.defaultBlockState());
                        runtime.loadFromResource("skript/gametest/effect/wait_one_tick_sets_block.sk");

                        int executed = GameTestRuntimeContext.withHelper(helper, () -> runtime.dispatch(new org.skriptlang.skript.lang.event.SkriptEvent(
                                helper,
                                helper.getLevel().getServer(),
                                helper.getLevel(),
                                null
                        )));
                        helper.assertTrue(
                                executed == 1,
                                Component.literal("Expected exactly one delayed gametest trigger execution but got " + executed)
                        );
                    })
                    .thenWaitUntil(() -> helper.assertBlockPresent(Blocks.GOLD_BLOCK, markerPos))
                    .thenExecute(() -> {
                        runtime.clearScripts();
                        Variables.clearAll();
                    })
                    .thenSucceed();
        });
    }

    @GameTest
    public void brewingConsumeEffectExecutesRealScript(GameTestHelper helper) {
        runWithRuntimeLock(helper, () -> {
            SkriptRuntime runtime = SkriptRuntime.instance();
            runtime.clearScripts();
            runtime.loadFromResource("skript/gametest/effect/prevent_brewing_consume_marks_block.sk");

            BlockPos brewingRelative = new BlockPos(0, 1, 0);
            BlockPos brewingAbsolute = helper.absolutePos(brewingRelative);
            helper.getLevel().setBlockAndUpdate(brewingAbsolute, Blocks.BREWING_STAND.defaultBlockState());

            BrewingStandBlockEntity brewingStand = (BrewingStandBlockEntity) helper.getLevel().getBlockEntity(brewingAbsolute);
            helper.assertTrue(brewingStand != null, Component.literal("Expected brewing stand block entity to exist for effect test."));
            if (brewingStand == null) {
                throw new IllegalStateException("Brewing stand block entity was not created.");
            }

            FabricBrewingFuelHandle handle = new FabricBrewingFuelHandle(helper.getLevel(), brewingAbsolute, brewingStand, true);
            int executed = runtime.dispatch(new org.skriptlang.skript.lang.event.SkriptEvent(
                    handle,
                    helper.getLevel().getServer(),
                    helper.getLevel(),
                    null
            ));
            helper.assertTrue(executed == 1, Component.literal("Expected brewing consume effect script to execute exactly one trigger."));
            helper.assertTrue(!handle.willConsume(), Component.literal("Expected brewing consume effect to update the event handle."));
            helper.assertBlockPresent(Blocks.DIAMOND_BLOCK, new BlockPos(0, 2, 0));
            runtime.clearScripts();
        });
    }

    @GameTest
    public void textDisplayDropShadowEffectExecutesRealScript(GameTestHelper helper) {
        Display.TextDisplay textDisplay = createTextDisplay(helper, (byte) 0);
        assertUseEntityScriptNamesEntity(
                helper,
                "skript/gametest/effect/text_display_add_shadow_names_entity.sk",
                textDisplay,
                "shadow added",
                () -> helper.assertTrue(
                        (PrivateEntityAccess.textDisplayFlags(textDisplay) & Display.TextDisplay.FLAG_SHADOW) != 0,
                        Component.literal("Expected drop-shadow effect to set the Mojang text display flag.")
                )
        );
    }

    @GameTest
    public void textDisplaySeeThroughEffectExecutesRealScript(GameTestHelper helper) {
        Display.TextDisplay textDisplay = createTextDisplay(helper, (byte) 0);
        assertUseEntityScriptNamesEntity(
                helper,
                "skript/gametest/effect/text_display_make_see_through_names_entity.sk",
                textDisplay,
                "see through added",
                () -> helper.assertTrue(
                        (PrivateEntityAccess.textDisplayFlags(textDisplay) & Display.TextDisplay.FLAG_SEE_THROUGH) != 0,
                        Component.literal("Expected see-through effect to set the Mojang text display flag.")
                )
        );
    }

    @GameTest
    public void fishingLureEffectExecutesRealScript(GameTestHelper helper) {
        runWithRuntimeLock(helper, () -> {
            SkriptRuntime runtime = SkriptRuntime.instance();
            runtime.clearScripts();
            runtime.loadFromResource("skript/gametest/effect/fishing_remove_lure_names_hook.sk");

            ServerPlayer player = helper.makeMockServerPlayerInLevel();
            FishingHook hook = new FishingHook(player, helper.getLevel(), 0, 3);
            FabricFishingHandle handle = new FabricFishingHandle(helper.getLevel(), player, hook, true);
            int executed = runtime.dispatch(new org.skriptlang.skript.lang.event.SkriptEvent(
                    handle,
                    helper.getLevel().getServer(),
                    helper.getLevel(),
                    player
            ));
            helper.assertTrue(executed == 1, Component.literal("Expected fishing lure effect script to execute exactly one trigger."));
            helper.assertTrue(!handle.lureApplied(), Component.literal("Expected fishing lure effect to disable lure application."));
            helper.assertTrue(
                    hook.getCustomName() != null && "lure removed".equals(hook.getCustomName().getString()),
                    Component.literal("Expected fishing lure effect script to name the hook after the condition re-check.")
            );
            runtime.clearScripts();
        });
    }

    @GameTest
    public void pullHookedEntityEffectExecutesRealScript(GameTestHelper helper) {
        runWithRuntimeLock(helper, () -> {
            SkriptRuntime runtime = SkriptRuntime.instance();
            runtime.clearScripts();
            runtime.loadFromResource("skript/gametest/effect/pull_hooked_entity_marks_block.sk");

            BlockPos playerMarkerAbsolute = helper.absolutePos(new BlockPos(15, 1, 0));
            helper.getLevel().setBlockAndUpdate(playerMarkerAbsolute, Blocks.AIR.defaultBlockState());

            ServerPlayer player = helper.makeMockServerPlayerInLevel();
            player.setGameMode(GameType.CREATIVE);
            player.teleportTo(playerMarkerAbsolute.getX() + 0.5D, playerMarkerAbsolute.getY() + 1.0D, playerMarkerAbsolute.getZ() + 0.5D);

            FishingHook hook = new FishingHook(player, helper.getLevel(), 0, 0);
            Cow cow = createCow(helper, false);
            cow.setDeltaMovement(Vec3.ZERO);
            setFishingHookedEntity(hook, cow);

            int executed = runtime.dispatch(new org.skriptlang.skript.lang.event.SkriptEvent(
                    new FabricFishingHandle(helper.getLevel(), player, hook, false),
                    helper.getLevel().getServer(),
                    helper.getLevel(),
                    player
            ));
            helper.assertTrue(executed == 1, Component.literal("Expected pull-hooked-entity effect script to execute exactly one trigger."));
            helper.assertTrue(cow.getDeltaMovement().lengthSqr() > 0.0D, Component.literal("Expected pull-hooked-entity effect to change the hooked entity motion."));
            helper.assertBlockPresent(Blocks.REDSTONE_BLOCK, new BlockPos(15, 1, 0));
            runtime.clearScripts();
        });
    }

    @GameTest
    public void makeResponsiveEffectExecutesRealScript(GameTestHelper helper) {
        Interaction interaction = createInteraction(helper, false);
        assertUseEntityScriptNamesEntity(
                helper,
                "skript/gametest/effect/make_responsive_names_entity.sk",
                interaction,
                "made responsive",
                () -> helper.assertTrue(
                        PrivateEntityAccess.interactionResponse(interaction),
                        Component.literal("Expected responsive effect to toggle the Mojang interaction flag.")
                )
        );
    }

    @GameTest
    public void equippableDamageEffectExecutesRealScript(GameTestHelper helper) {
        assertUseItemScriptNamesItem(
                helper,
                "skript/gametest/effect/equippable_damage_effect_renames_item.sk",
                createEquippableTestItem(false, false, false, false, false),
                "damage effect",
                held -> {
                    Equippable equippable = held.get(DataComponents.EQUIPPABLE);
                    helper.assertTrue(
                            equippable != null && equippable.damageOnHurt(),
                            Component.literal("Expected damage effect to enable equippable durability loss on injury. Actual: "
                                    + describeEquippable(equippable))
                    );
                }
        );
    }

    @GameTest
    public void equippableDispensableEffectExecutesRealScript(GameTestHelper helper) {
        assertUseItemScriptNamesItem(
                helper,
                "skript/gametest/effect/equippable_dispensable_effect_renames_item.sk",
                createEquippableTestItem(false, true, false, false, false),
                "dispense effect",
                held -> {
                    Equippable equippable = held.get(DataComponents.EQUIPPABLE);
                    helper.assertTrue(
                            equippable != null && !equippable.dispensable(),
                            Component.literal("Expected dispense effect to disable equippable dispensing. Actual: "
                                    + describeEquippable(equippable))
                    );
                }
        );
    }

    @GameTest
    public void equippableInteractEffectExecutesRealScript(GameTestHelper helper) {
        assertUseItemScriptNamesItem(
                helper,
                "skript/gametest/effect/equippable_interact_effect_renames_item.sk",
                createEquippableTestItem(false, false, false, false, false),
                "interact effect",
                held -> {
                    Equippable equippable = held.get(DataComponents.EQUIPPABLE);
                    helper.assertTrue(
                            equippable != null && equippable.equipOnInteract(),
                            Component.literal("Expected interact effect to enable equipping onto entities. Actual: "
                                    + describeEquippable(equippable))
                    );
                }
        );
    }

    @GameTest
    public void equippableShearableEffectExecutesRealScript(GameTestHelper helper) {
        assertUseItemScriptNamesItem(
                helper,
                "skript/gametest/effect/equippable_shearable_effect_renames_item.sk",
                createEquippableTestItem(false, false, false, false, false),
                "shear effect",
                held -> {
                    Equippable equippable = held.get(DataComponents.EQUIPPABLE);
                    helper.assertTrue(
                            equippable != null && equippable.canBeSheared(),
                            Component.literal("Expected shear effect to enable equippable shearing. Actual: "
                                    + describeEquippable(equippable))
                    );
                }
        );
    }

    @GameTest
    public void equippableSwappableEffectExecutesRealScript(GameTestHelper helper) {
        assertUseItemScriptNamesItem(
                helper,
                "skript/gametest/effect/equippable_swappable_effect_renames_item.sk",
                createEquippableTestItem(false, false, false, false, true),
                "swap effect",
                held -> {
                    Equippable equippable = held.get(DataComponents.EQUIPPABLE);
                    helper.assertTrue(
                            equippable != null && !equippable.swappable(),
                            Component.literal("Expected swap effect to disable equippable equipment swapping. Actual: "
                                    + describeEquippable(equippable))
                    );
                }
        );
    }

    @GameTest
    public void generateLootEffectExecutesRealScript(GameTestHelper helper) {
        runWithRuntimeLock(helper, () -> {
            SkriptRuntime runtime = SkriptRuntime.instance();
            runtime.clearScripts();
            runtime.loadFromResource("skript/gametest/effect/generate_loot_marks_block.sk");

            BlockPos chestRelative = new BlockPos(14, 1, 0);
            BlockPos chestAbsolute = helper.absolutePos(chestRelative);
            helper.getLevel().setBlockAndUpdate(chestAbsolute, Blocks.CHEST.defaultBlockState());

            ChestBlockEntity chest = (ChestBlockEntity) helper.getLevel().getBlockEntity(chestAbsolute);
            helper.assertTrue(chest != null, Component.literal("Expected chest block entity to exist for generate-loot effect test."));
            if (chest == null) {
                throw new IllegalStateException("Chest block entity was not created.");
            }

            ServerPlayer player = helper.makeMockServerPlayerInLevel();
            player.setGameMode(GameType.CREATIVE);
            player.teleportTo(chestAbsolute.getX() + 0.5D, chestAbsolute.getY() + 1.0D, chestAbsolute.getZ() + 0.5D);
            BlockHitResult hitResult = new BlockHitResult(Vec3.atCenterOf(chestAbsolute), Direction.UP, chestAbsolute, false);

            InteractionResult result = UseBlockCallback.EVENT.invoker().interact(
                    player,
                    helper.getLevel(),
                    InteractionHand.MAIN_HAND,
                    hitResult
            );
            helper.assertTrue(result == InteractionResult.PASS, Component.literal("Expected generate-loot effect bridge to keep Fabric callback flow in PASS state."));
            helper.assertBlockPresent(Blocks.EMERALD_BLOCK, new BlockPos(14, 2, 0));
            boolean foundLoot = false;
            StringBuilder contents = new StringBuilder();
            for (int slot = 0; slot < chest.getContainerSize(); slot++) {
                if (!chest.getItem(slot).isEmpty()) {
                    foundLoot = true;
                    if (contents.length() > 0) {
                        contents.append(", ");
                    }
                    contents.append(chest.getItem(slot).getItem());
                }
            }
            helper.assertTrue(foundLoot, Component.literal("Expected generate-loot effect to insert loot into the chest. Contents: " + contents));
            runtime.clearScripts();
        });
    }

    @GameTest
    public void rotateEffectExecutesRealScript(GameTestHelper helper) {
        Display.TextDisplay textDisplay = createTextDisplay(helper, (byte) 0);
        assertUseEntityScriptSetsMarker(
                helper,
                "skript/gametest/effect/rotate_display_marks_block.sk",
                textDisplay,
                new BlockPos(10, 1, 0),
                Blocks.YELLOW_WOOL,
                () -> {
                    Transformation transformation = PrivateEntityAccess.displayTransformation(textDisplay);
                    helper.assertTrue(
                            Math.abs(transformation.getLeftRotation().y) > 0.5F,
                            Component.literal("Expected rotate effect to modify the display left rotation quaternion.")
                    );
                }
        );
    }

    @GameTest
    public void applyPotionEffectExecutesRealScript(GameTestHelper helper) {
        Cow cow = createCow(helper, false);
        assertUseEntityScriptNamesEntity(
                helper,
                "skript/gametest/effect/apply_potion_names_entity.sk",
                cow,
                "applied poison",
                () -> helper.assertTrue(
                        cow.hasEffect(MobEffects.POISON),
                        Component.literal("Expected apply-potion effect to add poison to the entity.")
                )
        );
    }

    @GameTest
    public void poisonEffectExecutesRealScript(GameTestHelper helper) {
        Cow cow = createCow(helper, false);
        assertUseEntityScriptNamesEntity(
                helper,
                "skript/gametest/effect/poison_effect_names_entity.sk",
                cow,
                "poisoned by effect",
                () -> helper.assertTrue(
                        cow.hasEffect(MobEffects.POISON),
                        Component.literal("Expected poison effect to add poison to the entity.")
                )
        );
    }

    @GameTest
    public void curePoisonEffectExecutesRealScript(GameTestHelper helper) {
        Cow cow = createCow(helper, false);
        cow.addEffect(new MobEffectInstance(MobEffects.POISON, 200, 0));
        assertUseEntityScriptSetsMarker(
                helper,
                "skript/gametest/effect/cure_poison_marks_block.sk",
                cow,
                new BlockPos(10, 1, 0),
                Blocks.CYAN_WOOL,
                () -> helper.assertTrue(
                        !cow.hasEffect(MobEffects.POISON),
                        Component.literal("Expected cure-poison effect to remove poison from the entity.")
                )
        );
    }

    @GameTest
    public void potionAmbientEffectExecutesRealScript(GameTestHelper helper) {
        Cow cow = createCow(helper, false);
        cow.addEffect(new MobEffectInstance(MobEffects.POISON, 200, 0, false, true, true));
        assertUseEntityScriptNamesEntity(
                helper,
                "skript/gametest/effect/potion_ambient_effect_names_entity.sk",
                cow,
                "ambient potion",
                () -> {
                    MobEffectInstance effect = cow.getEffect(MobEffects.POISON);
                    helper.assertTrue(effect != null && effect.isAmbient(), Component.literal("Expected ambient potion effect modifier to update the active entity effect."));
                }
        );
    }

    @GameTest
    public void potionIconEffectExecutesRealScript(GameTestHelper helper) {
        Cow cow = createCow(helper, false);
        cow.addEffect(new MobEffectInstance(MobEffects.POISON, 200, 0, false, true, true));
        assertUseEntityScriptNamesEntity(
                helper,
                "skript/gametest/effect/potion_icon_effect_names_entity.sk",
                cow,
                "hidden icon potion",
                () -> {
                    MobEffectInstance effect = cow.getEffect(MobEffects.POISON);
                    helper.assertTrue(effect != null && !effect.showIcon(), Component.literal("Expected icon potion effect modifier to hide the icon on the active entity effect."));
                }
        );
    }

    @GameTest
    public void potionParticlesEffectExecutesRealScript(GameTestHelper helper) {
        Cow cow = createCow(helper, false);
        cow.addEffect(new MobEffectInstance(MobEffects.POISON, 200, 0, false, true, true));
        assertUseEntityScriptNamesEntity(
                helper,
                "skript/gametest/effect/potion_particles_effect_names_entity.sk",
                cow,
                "hidden particles potion",
                () -> {
                    MobEffectInstance effect = cow.getEffect(MobEffects.POISON);
                    helper.assertTrue(effect != null && !effect.isVisible(), Component.literal("Expected particle potion effect modifier to hide particles on the active entity effect."));
                }
        );
    }

    @GameTest
    public void potionInfiniteEffectExecutesRealScript(GameTestHelper helper) {
        Cow cow = createCow(helper, false);
        cow.addEffect(new MobEffectInstance(MobEffects.POISON, 200, 0, false, true, true));
        assertUseEntityScriptSetsMarker(
                helper,
                "skript/gametest/effect/potion_infinite_effect_marks_block.sk",
                cow,
                new BlockPos(10, 1, 0),
                Blocks.MAGENTA_WOOL,
                () -> {
                    MobEffectInstance effect = cow.getEffect(MobEffects.POISON);
                    helper.assertTrue(effect != null && effect.isInfiniteDuration(), Component.literal("Expected infinite potion effect modifier to update the active entity effect duration."));
                }
        );
    }

    @GameTest
    public void registerTagEffectExecutesRealScript(GameTestHelper helper) {
        runWithRuntimeLock(helper, () -> {
            SkriptRuntime runtime = SkriptRuntime.instance();
            runtime.clearScripts();
            runtime.loadFromResource("skript/gametest/effect/register_custom_tag_renames_item.sk");

            ServerPlayer player = helper.makeMockServerPlayerInLevel();
            player.setGameMode(GameType.CREATIVE);
            player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.STICK));

            InteractionResult result = UseItemCallback.EVENT.invoker().interact(
                    player,
                    helper.getLevel(),
                    InteractionHand.MAIN_HAND
            );
            helper.assertTrue(result == InteractionResult.PASS, Component.literal("Expected register-tag effect script to keep Fabric callback flow in PASS state."));
            ItemStack held = player.getItemInHand(InteractionHand.MAIN_HAND);
            helper.assertTrue(
                    held.getCustomName() != null && "custom tagged item".equals(held.getCustomName().getString()),
                    Component.literal("Expected register-tag effect to create a custom runtime tag and satisfy the follow-up condition.")
            );
            helper.assertTrue(
                    org.skriptlang.skript.bukkit.tags.TagSupport.isTagged(held, "skript:effect_test_items"),
                    Component.literal("Expected the newly registered custom tag to be visible through TagSupport.")
            );
            runtime.clearScripts();
        });
    }

    @GameTest
    public void playEffectExecutesRealScript(GameTestHelper helper) {
        runWithRuntimeLock(helper, () -> {
            SkriptRuntime runtime = SkriptRuntime.instance();
            runtime.clearScripts();
            runtime.loadFromResource("skript/gametest/effect/play_bone_meal_effect_marks_block.sk");

            int executed = runtime.dispatch(new org.skriptlang.skript.lang.event.SkriptEvent(
                    helper,
                    helper.getLevel().getServer(),
                    helper.getLevel(),
                    null
            ));
            helper.assertTrue(executed == 1, Component.literal("Expected play-effect script to execute exactly one trigger."));
            helper.assertBlockPresent(Blocks.LIME_CONCRETE, new BlockPos(8, 1, 0));
            runtime.clearScripts();
        });
    }

    @GameTest
    public void explosionEffectExecutesRealScript(GameTestHelper helper) {
        runWithRuntimeLock(helper, () -> {
            SkriptRuntime runtime = SkriptRuntime.instance();
            runtime.clearScripts();

            BlockPos fakeNeighbor = helper.absolutePos(new BlockPos(2, 1, 1));
            BlockPos realNeighbor = helper.absolutePos(new BlockPos(8, 1, 1));
            helper.getLevel().setBlockAndUpdate(fakeNeighbor, Blocks.STONE.defaultBlockState());
            helper.getLevel().setBlockAndUpdate(realNeighbor, Blocks.STONE.defaultBlockState());

            runtime.loadFromResource("skript/gametest/effect/explosion_effect_marks_blocks.sk");

            helper.assertBlockPresent(Blocks.GOLD_BLOCK, new BlockPos(10, 1, 0));
            helper.assertBlockPresent(Blocks.LIME_CONCRETE, new BlockPos(11, 1, 0));
            helper.assertTrue(
                    helper.getLevel().getBlockState(fakeNeighbor).is(Blocks.STONE),
                    Component.literal("Expected fake explosion effect to leave nearby blocks untouched.")
            );
            helper.assertTrue(
                    helper.getLevel().getBlockState(realNeighbor).isAir(),
                    Component.literal("Expected explosion effect fixture to destroy the nearby block.")
            );

            runtime.clearScripts();
        });
    }

    // --- Phase A/B: New effect tests ---

    @GameTest
    public void enchantAtLevelEffectExecutesRealScript(GameTestHelper helper) {
        Cow cow = createCow(helper, false);
        runWithRuntimeLock(helper, () -> {
            SkriptRuntime runtime = SkriptRuntime.instance();
            BlockPos markerPos = new BlockPos(9, 1, 0);
            runtime.clearScripts();
            runtime.loadFromResource("skript/gametest/effect/enchant_at_level_marks_block.sk");

            BlockPos markerAbsolute = helper.absolutePos(markerPos);
            helper.getLevel().setBlockAndUpdate(markerAbsolute, Blocks.AIR.defaultBlockState());

            ServerPlayer player = helper.makeMockServerPlayerInLevel();
            player.setGameMode(GameType.CREATIVE);
            player.teleportTo(markerAbsolute.getX() + 0.5D, markerAbsolute.getY() + 1.0D, markerAbsolute.getZ() + 0.5D);
            player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.DIAMOND_SWORD));

            InteractionResult result = GameTestRuntimeContext.withHelper(helper, () -> UseEntityCallback.EVENT.invoker().interact(
                    player, helper.getLevel(), InteractionHand.MAIN_HAND, cow, new EntityHitResult(cow)
            ));
            helper.assertTrue(result == InteractionResult.PASS,
                    Component.literal("Expected enchant-at-level test to keep Fabric callback flow in PASS state."));
            helper.assertBlockPresent(Blocks.DIAMOND_BLOCK, markerPos);
            ItemStack tool = player.getItemInHand(InteractionHand.MAIN_HAND);
            helper.assertTrue(!EnchantmentHelper.getEnchantmentsForCrafting(tool).isEmpty(),
                    Component.literal("Expected enchant-at-level effect to add enchantments to the player's tool."));
            runtime.clearScripts();
            helper.succeed();
        });
    }

    @GameTest
    public void enchantAtLevelTreasureEffectExecutesRealScript(GameTestHelper helper) {
        Cow cow = createCow(helper, false);
        runWithRuntimeLock(helper, () -> {
            SkriptRuntime runtime = SkriptRuntime.instance();
            BlockPos markerPos = new BlockPos(9, 1, 0);
            runtime.clearScripts();
            runtime.loadFromResource("skript/gametest/effect/enchant_at_level_treasure_marks_block.sk");

            BlockPos markerAbsolute = helper.absolutePos(markerPos);
            helper.getLevel().setBlockAndUpdate(markerAbsolute, Blocks.AIR.defaultBlockState());

            ServerPlayer player = helper.makeMockServerPlayerInLevel();
            player.setGameMode(GameType.CREATIVE);
            player.teleportTo(markerAbsolute.getX() + 0.5D, markerAbsolute.getY() + 1.0D, markerAbsolute.getZ() + 0.5D);
            player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.DIAMOND_SWORD));

            InteractionResult result = GameTestRuntimeContext.withHelper(helper, () -> UseEntityCallback.EVENT.invoker().interact(
                    player, helper.getLevel(), InteractionHand.MAIN_HAND, cow, new EntityHitResult(cow)
            ));
            helper.assertTrue(result == InteractionResult.PASS,
                    Component.literal("Expected enchant-at-level-treasure test to keep Fabric callback flow in PASS state."));
            helper.assertBlockPresent(Blocks.GOLD_BLOCK, markerPos);
            ItemStack tool = player.getItemInHand(InteractionHand.MAIN_HAND);
            helper.assertTrue(!EnchantmentHelper.getEnchantmentsForCrafting(tool).isEmpty(),
                    Component.literal("Expected enchant-at-level-treasure effect to add enchantments to the player's tool."));
            runtime.clearScripts();
            helper.succeed();
        });
    }

    @GameTest
    public void makeSayCommandEffectExecutesRealScript(GameTestHelper helper) {
        Interaction interaction = createInteraction(helper, true);
        runWithRuntimeLock(helper, () -> {
            SkriptRuntime runtime = SkriptRuntime.instance();
            BlockPos markerPos = new BlockPos(9, 1, 0);
            runtime.clearScripts();
            runtime.loadFromResource("skript/gametest/effect/make_say_command_marks_block.sk");

            BlockPos markerAbsolute = helper.absolutePos(markerPos);
            helper.getLevel().setBlockAndUpdate(markerAbsolute, Blocks.AIR.defaultBlockState());

            ServerPlayer player = helper.makeMockServerPlayerInLevel();
            player.setGameMode(GameType.CREATIVE);
            player.teleportTo(markerAbsolute.getX() + 0.5D, markerAbsolute.getY() + 1.0D, markerAbsolute.getZ() + 0.5D);

            InteractionResult result = GameTestRuntimeContext.withHelper(helper, () -> UseEntityCallback.EVENT.invoker().interact(
                    player, helper.getLevel(), InteractionHand.MAIN_HAND, interaction, new EntityHitResult(interaction)
            ));
            helper.assertTrue(result == InteractionResult.PASS,
                    Component.literal("Expected make-say-command test to keep Fabric callback flow in PASS state."));
            helper.assertBlockPresent(Blocks.EMERALD_BLOCK, markerPos);
            runtime.clearScripts();
            helper.succeed();
        });
    }

    @GameTest
    public void makeSayMessageEffectExecutesRealScript(GameTestHelper helper) {
        Interaction interaction = createInteraction(helper, true);
        runWithRuntimeLock(helper, () -> {
            SkriptRuntime runtime = SkriptRuntime.instance();
            BlockPos markerPos = new BlockPos(9, 1, 0);
            runtime.clearScripts();
            runtime.loadFromResource("skript/gametest/effect/make_say_message_marks_block.sk");

            BlockPos markerAbsolute = helper.absolutePos(markerPos);
            helper.getLevel().setBlockAndUpdate(markerAbsolute, Blocks.AIR.defaultBlockState());

            ServerPlayer player = helper.makeMockServerPlayerInLevel();
            player.setGameMode(GameType.CREATIVE);
            player.teleportTo(markerAbsolute.getX() + 0.5D, markerAbsolute.getY() + 1.0D, markerAbsolute.getZ() + 0.5D);

            InteractionResult result = GameTestRuntimeContext.withHelper(helper, () -> UseEntityCallback.EVENT.invoker().interact(
                    player, helper.getLevel(), InteractionHand.MAIN_HAND, interaction, new EntityHitResult(interaction)
            ));
            helper.assertTrue(result == InteractionResult.PASS,
                    Component.literal("Expected make-say-message test to keep Fabric callback flow in PASS state."));
            helper.assertBlockPresent(Blocks.LAPIS_BLOCK, markerPos);
            runtime.clearScripts();
            helper.succeed();
        });
    }

    @GameTest
    public void clearEntityStorageEffectExecutesRealScript(GameTestHelper helper) {
        runWithRuntimeLock(helper, () -> {
            SkriptRuntime runtime = SkriptRuntime.instance();
            runtime.clearScripts();
            runtime.loadFromResource("skript/gametest/effect/clear_entity_storage_marks_block.sk");

            BlockPos hiveRelative = new BlockPos(14, 1, 0);
            BlockPos hiveAbsolute = helper.absolutePos(hiveRelative);
            BlockPos markerRelative = new BlockPos(14, 2, 0);
            helper.getLevel().setBlockAndUpdate(hiveAbsolute, Blocks.BEE_NEST.defaultBlockState());

            BeehiveBlockEntity beehive = (BeehiveBlockEntity) helper.getLevel().getBlockEntity(hiveAbsolute);
            helper.assertTrue(beehive != null, Component.literal("Expected beehive block entity to exist."));
            Bee bee = new Bee(EntityType.BEE, helper.getLevel());
            bee.setPos(hiveAbsolute.getX() + 0.5D, hiveAbsolute.getY() + 0.5D, hiveAbsolute.getZ() + 0.5D);
            helper.getLevel().addFreshEntity(bee);
            beehive.addOccupant(bee);
            helper.assertTrue(beehive.getOccupantCount() > 0,
                    Component.literal("Expected beehive to contain at least one bee before clearing."));

            ServerPlayer player = helper.makeMockServerPlayerInLevel();
            player.setGameMode(GameType.CREATIVE);
            player.teleportTo(hiveAbsolute.getX() + 0.5D, hiveAbsolute.getY() + 1.0D, hiveAbsolute.getZ() + 0.5D);
            BlockHitResult hitResult = new BlockHitResult(Vec3.atCenterOf(hiveAbsolute), Direction.UP, hiveAbsolute, false);

            InteractionResult result = GameTestRuntimeContext.withHelper(helper, () -> UseBlockCallback.EVENT.invoker().interact(
                    player, helper.getLevel(), InteractionHand.MAIN_HAND, hitResult
            ));
            helper.assertTrue(result == InteractionResult.PASS,
                    Component.literal("Expected clear-entity-storage test to keep Fabric callback flow in PASS state."));
            helper.assertBlockPresent(Blocks.LIME_WOOL, markerRelative);
            helper.assertTrue(beehive.getOccupantCount() == 0,
                    Component.literal("Expected clear-entity-storage effect to empty the beehive."));
            runtime.clearScripts();
            helper.succeed();
        });
    }

    @GameTest
    public void releaseEntityStorageEffectExecutesRealScript(GameTestHelper helper) {
        runWithRuntimeLock(helper, () -> {
            SkriptRuntime runtime = SkriptRuntime.instance();
            runtime.clearScripts();
            runtime.loadFromResource("skript/gametest/effect/release_entity_storage_marks_block.sk");

            BlockPos hiveRelative = new BlockPos(14, 1, 0);
            BlockPos hiveAbsolute = helper.absolutePos(hiveRelative);
            BlockPos markerRelative = new BlockPos(14, 2, 0);
            helper.getLevel().setBlockAndUpdate(hiveAbsolute, Blocks.BEE_NEST.defaultBlockState());

            BeehiveBlockEntity beehive = (BeehiveBlockEntity) helper.getLevel().getBlockEntity(hiveAbsolute);
            helper.assertTrue(beehive != null, Component.literal("Expected beehive block entity to exist."));
            Bee bee = new Bee(EntityType.BEE, helper.getLevel());
            bee.setPos(hiveAbsolute.getX() + 0.5D, hiveAbsolute.getY() + 0.5D, hiveAbsolute.getZ() + 0.5D);
            helper.getLevel().addFreshEntity(bee);
            beehive.addOccupant(bee);
            helper.assertTrue(beehive.getOccupantCount() > 0,
                    Component.literal("Expected beehive to contain at least one bee before releasing."));

            ServerPlayer player = helper.makeMockServerPlayerInLevel();
            player.setGameMode(GameType.CREATIVE);
            player.teleportTo(hiveAbsolute.getX() + 0.5D, hiveAbsolute.getY() + 1.0D, hiveAbsolute.getZ() + 0.5D);
            BlockHitResult hitResult = new BlockHitResult(Vec3.atCenterOf(hiveAbsolute), Direction.UP, hiveAbsolute, false);

            InteractionResult result = GameTestRuntimeContext.withHelper(helper, () -> UseBlockCallback.EVENT.invoker().interact(
                    player, helper.getLevel(), InteractionHand.MAIN_HAND, hitResult
            ));
            helper.assertTrue(result == InteractionResult.PASS,
                    Component.literal("Expected release-entity-storage test to keep Fabric callback flow in PASS state."));
            helper.assertBlockPresent(Blocks.ORANGE_WOOL, markerRelative);
            helper.assertTrue(beehive.getOccupantCount() == 0,
                    Component.literal("Expected release-entity-storage effect to empty the beehive after releasing."));
            runtime.clearScripts();
            helper.succeed();
        });
    }

    @GameTest
    public void insertEntityStorageEffectExecutesRealScript(GameTestHelper helper) {
        runWithRuntimeLock(helper, () -> {
            SkriptRuntime runtime = SkriptRuntime.instance();
            BlockPos markerPos = new BlockPos(9, 1, 0);
            runtime.clearScripts();
            runtime.loadFromResource("skript/gametest/effect/insert_entity_storage_marks_block.sk");

            BlockPos hiveRelative = new BlockPos(9, 1, 0);
            BlockPos hiveAbsolute = helper.absolutePos(hiveRelative);
            helper.getLevel().setBlockAndUpdate(hiveAbsolute, Blocks.BEE_NEST.defaultBlockState());

            BeehiveBlockEntity beehive = (BeehiveBlockEntity) helper.getLevel().getBlockEntity(hiveAbsolute);
            helper.assertTrue(beehive != null, Component.literal("Expected beehive block entity to exist."));
            helper.assertTrue(beehive.getOccupantCount() == 0,
                    Component.literal("Expected beehive to be empty before inserting."));

            Bee bee = new Bee(EntityType.BEE, helper.getLevel());
            bee.setPos(hiveAbsolute.getX() + 0.5D, hiveAbsolute.getY() + 1.5D, hiveAbsolute.getZ() + 0.5D);
            helper.getLevel().addFreshEntity(bee);

            ServerPlayer player = helper.makeMockServerPlayerInLevel();
            player.setGameMode(GameType.CREATIVE);
            player.teleportTo(hiveAbsolute.getX() + 0.5D, hiveAbsolute.getY() + 1.0D, hiveAbsolute.getZ() + 0.5D);

            InteractionResult result = GameTestRuntimeContext.withHelper(helper, () -> UseEntityCallback.EVENT.invoker().interact(
                    player, helper.getLevel(), InteractionHand.MAIN_HAND, bee, new EntityHitResult(bee)
            ));
            helper.assertTrue(result == InteractionResult.PASS,
                    Component.literal("Expected insert-entity-storage test to keep Fabric callback flow in PASS state."));
            helper.assertTrue(beehive.getOccupantCount() > 0,
                    Component.literal("Expected insert-entity-storage effect to add a bee to the beehive."));
            runtime.clearScripts();
            helper.succeed();
        });
    }
}
