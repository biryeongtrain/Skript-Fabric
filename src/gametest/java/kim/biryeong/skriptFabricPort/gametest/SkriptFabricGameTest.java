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
import com.mojang.authlib.GameProfile;
import com.mojang.math.Transformation;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.event.player.AttackEntityCallback;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.fabricmc.fabric.api.event.player.UseEntityCallback;
import net.fabricmc.fabric.api.event.player.UseItemCallback;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.core.BlockPos;
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
import net.minecraft.world.entity.Display;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Interaction;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.Cow;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.player.Input;
import net.minecraft.world.entity.projectile.FishingHook;
import net.minecraft.world.entity.vehicle.MinecartChest;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.equipment.Equippable;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BrewingStandBlockEntity;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;
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
import org.skriptlang.skript.fabric.runtime.FabricAttackEntityHandle;
import org.skriptlang.skript.fabric.runtime.FabricBlockBreakHandle;
import org.skriptlang.skript.fabric.runtime.FabricBrewingFuelHandle;
import org.skriptlang.skript.fabric.runtime.FabricDamageHandle;
import org.skriptlang.skript.fabric.runtime.FabricFishingEventHandle;
import org.skriptlang.skript.fabric.runtime.FabricFishingHandle;
import org.skriptlang.skript.fabric.runtime.FabricPlayerInputHandle;
import org.skriptlang.skript.fabric.runtime.FabricUseEntityHandle;
import org.skriptlang.skript.fabric.runtime.FabricUseItemHandle;
import org.skriptlang.skript.fabric.runtime.SkriptRuntime;
import org.skriptlang.skript.lang.properties.Property;
import org.skriptlang.skript.lang.properties.handlers.WXYZHandler;
import ch.njol.util.Kleenean;
import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;
import org.joml.Quaternionf;
import org.joml.Vector3f;

public final class SkriptFabricGameTest {

    private static final AtomicBoolean RUNTIME_LOCK = new AtomicBoolean(false);

    @GameTest
    public void executesRealSkriptFile(GameTestHelper helper) {
        runWithRuntimeLock(helper, () -> {
            SkriptRuntime runtime = SkriptRuntime.instance();
            runtime.clearScripts();
            runtime.loadFromResource("skript/gametest/set_test_block.sk");

            int executed = runtime.dispatch(new org.skriptlang.skript.lang.event.SkriptEvent(
                    helper,
                    helper.getLevel().getServer(),
                    helper.getLevel(),
                    null
            ));

            helper.assertTrue(
                    executed == 1,
                    Component.literal("Expected exactly one Skript trigger execution but got " + executed)
            );
            helper.assertBlockPresent(Blocks.DIAMOND_BLOCK, new BlockPos(0, 1, 0));
            runtime.clearScripts();
        });
    }

    @GameTest
    public void executesRealSkriptFileUsingMappedLocationType(GameTestHelper helper) {
        runWithRuntimeLock(helper, () -> {
            SkriptRuntime runtime = SkriptRuntime.instance();
            runtime.clearScripts();
            runtime.loadFromResource("skript/gametest/set_test_block_at_location.sk");

            int executed = runtime.dispatch(new org.skriptlang.skript.lang.event.SkriptEvent(
                    helper,
                    helper.getLevel().getServer(),
                    helper.getLevel(),
                    null
            ));

            helper.assertTrue(
                    executed == 1,
                    Component.literal("Expected exactly one Skript trigger execution but got " + executed)
            );
            helper.assertBlockPresent(Blocks.EMERALD_BLOCK, new BlockPos(0, 1, 0));
            runtime.clearScripts();
        });
    }

    @GameTest
    public void fabricServerTickBridgeExecutesLoadedScript(GameTestHelper helper) {
        SkriptRuntime runtime = SkriptRuntime.instance();
        BlockPos absoluteTarget = new BlockPos(1, 80, 1);
        AtomicBoolean loaded = new AtomicBoolean(false);
        helper.succeedWhen(() -> {
            if (!loaded.get()) {
                helper.assertTrue(
                        RUNTIME_LOCK.compareAndSet(false, true),
                        Component.literal("Waiting for exclusive Skript runtime access.")
                );
                runtime.clearScripts();
                helper.getLevel().setBlockAndUpdate(absoluteTarget, Blocks.AIR.defaultBlockState());
                runtime.loadFromResource("skript/gametest/server_tick_sets_block.sk");
                loaded.set(true);
            }
            helper.assertTrue(
                    helper.getLevel().getBlockState(absoluteTarget).is(Blocks.LAPIS_BLOCK),
                    Component.literal("Expected server tick event bridge to execute loaded Skript file.")
            );
            runtime.clearScripts();
            RUNTIME_LOCK.set(false);
        });
    }

    @GameTest
    public void fabricBlockBreakBridgeExecutesLoadedScript(GameTestHelper helper) {
        runWithRuntimeLock(helper, () -> {
            SkriptRuntime runtime = SkriptRuntime.instance();
            runtime.clearScripts();
            runtime.loadFromResource("skript/gametest/block_break_sets_block.sk");

            BlockPos brokenRelative = new BlockPos(0, 1, 0);
            BlockPos brokenAbsolute = helper.absolutePos(brokenRelative);

            helper.getLevel().setBlockAndUpdate(brokenAbsolute, Blocks.STONE.defaultBlockState());

            var player = helper.makeMockServerPlayerInLevel();
            player.setGameMode(GameType.CREATIVE);
            player.teleportTo(brokenAbsolute.getX() + 0.5D, brokenAbsolute.getY(), brokenAbsolute.getZ() + 0.5D);

            helper.assertTrue(
                player.gameMode.destroyBlock(brokenAbsolute),
                Component.literal("Expected mock server player to break the test block.")
            );
            helper.assertTrue(
                helper.getLevel().getBlockState(brokenAbsolute).is(Blocks.REDSTONE_BLOCK),
                Component.literal("Expected block break bridge to execute loaded Skript file.")
            );
            runtime.clearScripts();
        });
    }

    @GameTest
    public void attackEntityBridgeExecutesLoadedScript(GameTestHelper helper) {
        runWithRuntimeLock(helper, () -> {
            SkriptRuntime runtime = SkriptRuntime.instance();
            runtime.clearScripts();
            runtime.loadFromResource("skript/gametest/attack_entity_marks_target.sk");

            BlockPos playerMarkerAbsolute = helper.absolutePos(new BlockPos(5, 1, 0));
            helper.getLevel().setBlockAndUpdate(playerMarkerAbsolute, Blocks.AIR.defaultBlockState());

            ArmorStand armorStand = new ArmorStand(helper.getLevel(), 0.5D, 1.0D, 0.5D);
            helper.getLevel().addFreshEntity(armorStand);

            ServerPlayer player = helper.makeMockServerPlayerInLevel();
            player.setGameMode(GameType.CREATIVE);
            player.teleportTo(playerMarkerAbsolute.getX() + 0.5D, playerMarkerAbsolute.getY() + 1.0D, playerMarkerAbsolute.getZ() + 0.5D);

            InteractionResult result = AttackEntityCallback.EVENT.invoker().interact(
                    player,
                    helper.getLevel(),
                    InteractionHand.MAIN_HAND,
                    armorStand,
                    new EntityHitResult(armorStand)
            );
            helper.assertTrue(
                    result == InteractionResult.PASS,
                    Component.literal("Expected attack entity bridge to keep Fabric callback flow in PASS state.")
            );
            helper.assertTrue(
                    armorStand.getCustomName() != null && "attacked entity".equals(armorStand.getCustomName().getString()),
                    Component.literal("Expected attack entity bridge to resolve event-entity inside a real .sk file.")
            );
            helper.assertTrue(
                    helper.getLevel().getBlockState(playerMarkerAbsolute).is(Blocks.RED_WOOL),
                    Component.literal("Expected attack entity bridge to resolve event-player inside a real .sk file.")
            );
            runtime.clearScripts();
        });
    }

    @GameTest
    public void fabricUseBlockBridgeExecutesLoadedScript(GameTestHelper helper) {
        runWithRuntimeLock(helper, () -> {
            SkriptRuntime runtime = SkriptRuntime.instance();
            runtime.clearScripts();
            runtime.loadFromResource("skript/gametest/use_block_sets_blocks.sk");

            BlockPos clickedAbsolute = helper.absolutePos(new BlockPos(0, 1, 0));
            BlockPos playerMarkerAbsolute = helper.absolutePos(new BlockPos(2, 1, 0));

            helper.getLevel().setBlockAndUpdate(clickedAbsolute, Blocks.STONE.defaultBlockState());
            helper.getLevel().setBlockAndUpdate(playerMarkerAbsolute, Blocks.AIR.defaultBlockState());

            ServerPlayer player = helper.makeMockServerPlayerInLevel();
            player.setGameMode(GameType.CREATIVE);
            player.teleportTo(playerMarkerAbsolute.getX() + 0.5D, playerMarkerAbsolute.getY() + 1.0D, playerMarkerAbsolute.getZ() + 0.5D);

            InteractionResult result = UseBlockCallback.EVENT.invoker().interact(
                    player,
                    helper.getLevel(),
                    InteractionHand.MAIN_HAND,
                    new BlockHitResult(Vec3.atCenterOf(clickedAbsolute), Direction.UP, clickedAbsolute, false)
            );
            helper.assertTrue(
                    result == InteractionResult.PASS,
                    Component.literal("Expected use block bridge to keep Fabric callback flow in PASS state.")
            );
            helper.assertTrue(
                    helper.getLevel().getBlockState(clickedAbsolute).is(Blocks.EMERALD_BLOCK),
                    Component.literal("Expected use block bridge to resolve event-block inside a real .sk file.")
            );
            helper.assertTrue(
                    helper.getLevel().getBlockState(playerMarkerAbsolute).is(Blocks.GOLD_BLOCK),
                    Component.literal("Expected use block bridge to resolve event-player inside a real .sk file.")
            );
            runtime.clearScripts();
        });
    }

    @GameTest
    public void useEntityBridgeExecutesLoadedScript(GameTestHelper helper) {
        runWithRuntimeLock(helper, () -> {
            SkriptRuntime runtime = SkriptRuntime.instance();
            runtime.clearScripts();
            runtime.loadFromResource("skript/gametest/use_entity_names_entity.sk");

            BlockPos playerMarkerAbsolute = helper.absolutePos(new BlockPos(3, 1, 0));
            helper.getLevel().setBlockAndUpdate(playerMarkerAbsolute, Blocks.AIR.defaultBlockState());

            ArmorStand armorStand = new ArmorStand(helper.getLevel(), 0.5D, 1.0D, 0.5D);
            helper.getLevel().addFreshEntity(armorStand);

            ServerPlayer player = helper.makeMockServerPlayerInLevel();
            player.setGameMode(GameType.CREATIVE);
            player.teleportTo(playerMarkerAbsolute.getX() + 0.5D, playerMarkerAbsolute.getY() + 1.0D, playerMarkerAbsolute.getZ() + 0.5D);

            InteractionResult result = UseEntityCallback.EVENT.invoker().interact(
                    player,
                    helper.getLevel(),
                    InteractionHand.MAIN_HAND,
                    armorStand,
                    new EntityHitResult(armorStand)
            );
            helper.assertTrue(
                    result == InteractionResult.PASS,
                    Component.literal("Expected use entity bridge to keep Fabric callback flow in PASS state.")
            );
            helper.assertTrue(
                    armorStand.getCustomName() != null && "clicked entity".equals(armorStand.getCustomName().getString()),
                    Component.literal("Expected use entity bridge to resolve event-entity inside a real .sk file.")
            );
            helper.assertTrue(
                    helper.getLevel().getBlockState(playerMarkerAbsolute).is(Blocks.AMETHYST_BLOCK),
                    Component.literal("Expected use entity bridge to resolve event-player inside a real .sk file.")
            );
            runtime.clearScripts();
        });
    }

    @GameTest
    public void adultEntityConditionExecutesRealScript(GameTestHelper helper) {
        Cow cow = createCow(helper, false);
        assertUseEntityScriptSetsMarker(
                helper,
                "skript/gametest/adult_entity_marks_block.sk",
                cow,
                new BlockPos(7, 1, 0),
                Blocks.LIME_WOOL
        );
    }

    @GameTest
    public void babyEntityConditionExecutesRealScript(GameTestHelper helper) {
        Cow cow = createCow(helper, true);
        assertUseEntityScriptSetsMarker(
                helper,
                "skript/gametest/baby_entity_marks_block.sk",
                cow,
                new BlockPos(8, 1, 0),
                Blocks.PINK_WOOL
        );
    }

    @GameTest
    public void canBreedConditionExecutesRealScript(GameTestHelper helper) {
        Cow cow = createCow(helper, false);
        assertUseEntityScriptSetsMarker(
                helper,
                "skript/gametest/breedable_entity_marks_block.sk",
                cow,
                new BlockPos(9, 1, 0),
                Blocks.ORANGE_WOOL
        );
    }

    @GameTest
    public void canAgeConditionExecutesRealScript(GameTestHelper helper) {
        Cow cow = createCow(helper, false);
        assertUseEntityScriptSetsMarker(
                helper,
                "skript/gametest/can_age_entity_marks_block.sk",
                cow,
                new BlockPos(10, 1, 0),
                Blocks.WHITE_WOOL
        );
    }

    @GameTest
    public void inLoveConditionExecutesRealScript(GameTestHelper helper) {
        runWithRuntimeLock(helper, () -> {
            SkriptRuntime runtime = SkriptRuntime.instance();
            runtime.clearScripts();
            runtime.loadFromResource("skript/gametest/in_love_entity_marks_block.sk");

            BlockPos playerMarkerAbsolute = helper.absolutePos(new BlockPos(11, 1, 0));
            helper.getLevel().setBlockAndUpdate(playerMarkerAbsolute, Blocks.AIR.defaultBlockState());

            Cow cow = createCow(helper, false);

            ServerPlayer player = helper.makeMockServerPlayerInLevel();
            player.setGameMode(GameType.CREATIVE);
            player.teleportTo(playerMarkerAbsolute.getX() + 0.5D, playerMarkerAbsolute.getY() + 1.0D, playerMarkerAbsolute.getZ() + 0.5D);
            cow.setInLove(player);

            InteractionResult result = UseEntityCallback.EVENT.invoker().interact(
                    player,
                    helper.getLevel(),
                    InteractionHand.MAIN_HAND,
                    cow,
                    new EntityHitResult(cow)
            );
            helper.assertTrue(
                    result == InteractionResult.PASS,
                    Component.literal("Expected in-love condition bridge test to keep Fabric callback flow in PASS state.")
            );
            helper.assertTrue(
                    helper.getLevel().getBlockState(playerMarkerAbsolute).is(Blocks.RED_CONCRETE),
                    Component.literal("Expected in-love condition to execute a real .sk file for an in-love cow.")
            );
            runtime.clearScripts();
        });
    }

    @GameTest
    public void brewingConsumeConditionExecutesRealScript(GameTestHelper helper) {
        runWithRuntimeLock(helper, () -> {
            SkriptRuntime runtime = SkriptRuntime.instance();
            runtime.clearScripts();
            runtime.loadFromResource("skript/gametest/brewing_consume_marks_block.sk");

            BlockPos brewingRelative = new BlockPos(0, 1, 0);
            BlockPos brewingAbsolute = helper.absolutePos(brewingRelative);
            helper.getLevel().setBlockAndUpdate(brewingAbsolute, Blocks.BREWING_STAND.defaultBlockState());

            BrewingStandBlockEntity brewingStand = (BrewingStandBlockEntity) helper.getLevel().getBlockEntity(brewingAbsolute);
            helper.assertTrue(
                    brewingStand != null,
                    Component.literal("Expected brewing stand block entity to exist for brewing condition test.")
            );
            if (brewingStand == null) {
                throw new IllegalStateException("Brewing stand block entity was not created.");
            }

            brewingStand.setItem(4, new ItemStack(Items.BLAZE_POWDER));
            int executed = runtime.dispatch(new org.skriptlang.skript.lang.event.SkriptEvent(
                    new FabricBrewingFuelHandle(helper.getLevel(), brewingAbsolute, brewingStand, true),
                    helper.getLevel().getServer(),
                    helper.getLevel(),
                    null
            ));
            helper.assertTrue(
                    executed == 1,
                    Component.literal("Expected brewing consume script to execute exactly one trigger.")
            );

            helper.assertBlockPresent(Blocks.GOLD_BLOCK, new BlockPos(0, 2, 0));
            runtime.clearScripts();
        });
    }

    @GameTest
    public void brewingFuelSlotExpressionExecutesRealScript(GameTestHelper helper) {
        runWithRuntimeLock(helper, () -> {
            SkriptRuntime runtime = SkriptRuntime.instance();
            runtime.clearScripts();
            runtime.loadFromResource("skript/gametest/brewing_fuel_slot_marks_block.sk");

            BlockPos brewingRelative = new BlockPos(0, 1, 0);
            BlockPos brewingAbsolute = helper.absolutePos(brewingRelative);
            helper.getLevel().setBlockAndUpdate(brewingAbsolute, Blocks.BREWING_STAND.defaultBlockState());

            BrewingStandBlockEntity brewingStand = (BrewingStandBlockEntity) helper.getLevel().getBlockEntity(brewingAbsolute);
            helper.assertTrue(
                    brewingStand != null,
                    Component.literal("Expected brewing stand block entity to exist for brewing slot expression test.")
            );
            if (brewingStand == null) {
                throw new IllegalStateException("Brewing stand block entity was not created.");
            }

            brewingStand.setItem(4, new ItemStack(Items.BLAZE_POWDER));
            int executed = runtime.dispatch(new org.skriptlang.skript.lang.event.SkriptEvent(
                    new FabricBrewingFuelHandle(helper.getLevel(), brewingAbsolute, brewingStand, true),
                    helper.getLevel().getServer(),
                    helper.getLevel(),
                    null
            ));
            helper.assertTrue(
                    executed == 1,
                    Component.literal("Expected brewing fuel slot script to execute exactly one trigger.")
            );
            helper.assertBlockPresent(Blocks.EMERALD_BLOCK, new BlockPos(0, 2, 0));
            runtime.clearScripts();
        });
    }

    @GameTest
    public void fishingLureConditionExecutesRealScript(GameTestHelper helper) {
        runWithRuntimeLock(helper, () -> {
            SkriptRuntime runtime = SkriptRuntime.instance();
            runtime.clearScripts();
            runtime.loadFromResource("skript/gametest/fishing_lure_names_hook.sk");

            ServerPlayer player = helper.makeMockServerPlayerInLevel();
            FishingHook hook = new FishingHook(player, helper.getLevel(), 0, 3);
            int executed = runtime.dispatch(new org.skriptlang.skript.lang.event.SkriptEvent(
                    new FabricFishingHandle(helper.getLevel(), player, hook, true),
                    helper.getLevel().getServer(),
                    helper.getLevel(),
                    player
            ));
            helper.assertTrue(
                    executed == 1,
                    Component.literal("Expected fishing lure script to execute exactly one trigger.")
            );

            helper.assertTrue(
                    hook.getCustomName() != null && "lured hook".equals(hook.getCustomName().getString()),
                    Component.literal("Expected fishing lure condition script to name the created hook.")
            );
            runtime.clearScripts();
        });
    }

    @GameTest
    public void fishingHookExpressionExecutesRealScript(GameTestHelper helper) {
        runWithRuntimeLock(helper, () -> {
            SkriptRuntime runtime = SkriptRuntime.instance();
            runtime.clearScripts();
            runtime.loadFromResource("skript/gametest/fishing_hook_names_hook.sk");

            ServerPlayer player = helper.makeMockServerPlayerInLevel();
            FishingHook hook = new FishingHook(player, helper.getLevel(), 0, 0);
            int executed = runtime.dispatch(new org.skriptlang.skript.lang.event.SkriptEvent(
                    new FabricFishingHandle(helper.getLevel(), player, hook, false),
                    helper.getLevel().getServer(),
                    helper.getLevel(),
                    player
            ));
            helper.assertTrue(
                    executed == 1,
                    Component.literal("Expected fishing hook expression script to execute exactly one trigger.")
            );
            helper.assertTrue(
                    hook.getCustomName() != null && "fishing hook".equals(hook.getCustomName().getString()),
                    Component.literal("Expected fishing hook expression script to name the hook.")
            );
            runtime.clearScripts();
        });
    }

    @GameTest
    public void fishingOpenWaterConditionExecutesRealScript(GameTestHelper helper) {
        runWithRuntimeLock(helper, () -> {
            SkriptRuntime runtime = SkriptRuntime.instance();
            runtime.clearScripts();
            runtime.loadFromResource("skript/gametest/fishing_open_water_names_hook.sk");

            ServerPlayer player = helper.makeMockServerPlayerInLevel();
            FishingHook hook = new FishingHook(player, helper.getLevel(), 0, 0);
            setFishingHookOpenWater(hook, true);

            int executed = runtime.dispatch(new org.skriptlang.skript.lang.event.SkriptEvent(
                    new FabricFishingHandle(helper.getLevel(), player, hook, false),
                    helper.getLevel().getServer(),
                    helper.getLevel(),
                    player
            ));
            helper.assertTrue(
                    executed == 1,
                    Component.literal("Expected fishing open water script to execute exactly one trigger.")
            );
            helper.assertTrue(
                    hook.getCustomName() != null && "open water hook".equals(hook.getCustomName().getString()),
                    Component.literal("Expected open water condition script to name the hook.")
            );
            runtime.clearScripts();
        });
    }

    @GameTest
    public void hookedEntityExpressionExecutesRealScript(GameTestHelper helper) {
        runWithRuntimeLock(helper, () -> {
            SkriptRuntime runtime = SkriptRuntime.instance();
            runtime.clearScripts();
            runtime.loadFromResource("skript/gametest/hooked_entity_names_entity.sk");

            ServerPlayer player = helper.makeMockServerPlayerInLevel();
            FishingHook hook = new FishingHook(player, helper.getLevel(), 0, 0);
            ArmorStand armorStand = new ArmorStand(helper.getLevel(), 0.5D, 1.0D, 0.5D);
            helper.getLevel().addFreshEntity(armorStand);
            setFishingHookedEntity(hook, armorStand);

            int executed = runtime.dispatch(new org.skriptlang.skript.lang.event.SkriptEvent(
                    new FabricFishingHandle(helper.getLevel(), player, hook, false),
                    helper.getLevel().getServer(),
                    helper.getLevel(),
                    player
            ));
            helper.assertTrue(
                    executed == 1,
                    Component.literal("Expected hooked entity expression script to execute exactly one trigger.")
            );
            helper.assertTrue(
                    armorStand.getCustomName() != null && "hooked entity".equals(armorStand.getCustomName().getString()),
                    Component.literal("Expected hooked entity expression script to name the hooked entity.")
            );
            runtime.clearScripts();
        });
    }

    @GameTest
    public void fishingWaitTimeExpressionExecutesRealScript(GameTestHelper helper) {
        FishingHook hook = createFishingHook(helper);
        FabricFishingState.minWaitTime(hook, 40);
        FabricFishingState.maxWaitTime(hook, 80);
        assertFishingScriptSetsMarker(
                helper,
                "skript/gametest/fishing_wait_time_marks_block.sk",
                hook,
                new BlockPos(3, 1, 0),
                Blocks.GOLD_BLOCK,
                () -> {
                    helper.assertTrue(
                            FabricFishingState.minWaitTime(hook) == 5,
                            Component.literal("Expected fishing wait time script to set the minimum wait time to 5 ticks.")
                    );
                    helper.assertTrue(
                            FabricFishingState.maxWaitTime(hook) == 10,
                            Component.literal("Expected fishing wait time script to set the maximum wait time to 10 ticks.")
                    );
                }
        );
    }

    @GameTest
    public void fishingBiteTimeExpressionExecutesRealScript(GameTestHelper helper) {
        FishingHook hook = createFishingHook(helper);
        PrivateFishingHookAccess.setTimeUntilHooked(hook, 20);
        assertFishingScriptSetsMarker(
                helper,
                "skript/gametest/fishing_bite_time_marks_block.sk",
                hook,
                new BlockPos(4, 1, 0),
                Blocks.IRON_BLOCK,
                () -> helper.assertTrue(
                        PrivateFishingHookAccess.timeUntilHooked(hook) == 7,
                        Component.literal("Expected fishing bite time script to set the bite time to 7 ticks.")
                )
        );
    }

    @GameTest
    public void fishingApproachAngleExpressionExecutesRealScript(GameTestHelper helper) {
        FishingHook hook = createFishingHook(helper);
        FabricFishingState.minLureAngle(hook, 0.0F);
        FabricFishingState.maxLureAngle(hook, 360.0F);
        assertFishingScriptSetsMarker(
                helper,
                "skript/gametest/fishing_approach_angle_marks_block.sk",
                hook,
                new BlockPos(5, 1, 0),
                Blocks.DIAMOND_BLOCK,
                () -> {
                    float minimumAngle = FabricFishingState.minLureAngle(hook);
                    float maximumAngle = FabricFishingState.maxLureAngle(hook);
                    helper.assertTrue(
                            Float.compare(minimumAngle, 30.0F) == 0,
                            Component.literal("Expected fishing approach angle script to set the minimum angle to 30 degrees, got " + minimumAngle + ".")
                    );
                    helper.assertTrue(
                            Float.compare(maximumAngle, 300.0F) == 0,
                            Component.literal("Expected fishing approach angle script to set the maximum angle to 300 degrees, got " + maximumAngle + ".")
                    );
                }
        );
    }

    @GameTest
    public void playerInputConditionExecutesRealScript(GameTestHelper helper) {
        runWithRuntimeLock(helper, () -> {
            SkriptRuntime runtime = SkriptRuntime.instance();
            runtime.clearScripts();
            runtime.loadFromResource("skript/gametest/player_input_forward_marks_block.sk");

            BlockPos markerAbsolute = helper.absolutePos(new BlockPos(6, 1, 0));
            helper.getLevel().setBlockAndUpdate(markerAbsolute, Blocks.AIR.defaultBlockState());

            ServerPlayer player = helper.makeMockServerPlayerInLevel();
            player.setGameMode(GameType.CREATIVE);
            player.teleportTo(markerAbsolute.getX() + 0.5D, markerAbsolute.getY() + 1.0D, markerAbsolute.getZ() + 0.5D);
            int executed = runtime.dispatch(new org.skriptlang.skript.lang.event.SkriptEvent(
                    new FabricPlayerInputHandle(
                            helper.getLevel(),
                            player,
                            Input.EMPTY,
                            new Input(true, false, false, false, false, false, false)
                    ),
                    helper.getLevel().getServer(),
                    helper.getLevel(),
                    player
            ));
            helper.assertTrue(
                    executed == 1,
                    Component.literal("Expected player input script to execute exactly one trigger.")
            );

            helper.assertTrue(
                    helper.getLevel().getBlockState(markerAbsolute).is(Blocks.LIME_WOOL),
                    Component.literal("Expected player input condition script to mark the block under the player.")
            );
            runtime.clearScripts();
        });
    }

    @GameTest
    public void currentInputKeysExpressionExecutesRealScript(GameTestHelper helper) {
        runWithRuntimeLock(helper, () -> {
            SkriptRuntime runtime = SkriptRuntime.instance();
            runtime.clearScripts();
            runtime.loadFromResource("skript/gametest/current_input_keys_marks_block.sk");

            BlockPos markerAbsolute = helper.absolutePos(new BlockPos(8, 1, 0));
            helper.getLevel().setBlockAndUpdate(markerAbsolute, Blocks.AIR.defaultBlockState());

            ServerPlayer player = helper.makeMockServerPlayerInLevel();
            player.setGameMode(GameType.CREATIVE);
            player.teleportTo(markerAbsolute.getX() + 0.5D, markerAbsolute.getY() + 1.0D, markerAbsolute.getZ() + 0.5D);
            int executed = runtime.dispatch(new org.skriptlang.skript.lang.event.SkriptEvent(
                    new FabricPlayerInputHandle(
                            helper.getLevel(),
                            player,
                            Input.EMPTY,
                            new Input(true, false, false, false, false, false, false)
                    ),
                    helper.getLevel().getServer(),
                    helper.getLevel(),
                    player
            ));
            helper.assertTrue(
                    executed == 1,
                    Component.literal("Expected current input keys script to execute exactly one trigger.")
            );
            helper.assertTrue(
                    helper.getLevel().getBlockState(markerAbsolute).is(Blocks.GREEN_WOOL),
                    Component.literal("Expected current input keys expression script to mark the block under the player.")
            );
            runtime.clearScripts();
        });
    }

    @GameTest
    public void playerInputPastConditionExecutesRealScript(GameTestHelper helper) {
        runWithRuntimeLock(helper, () -> {
            SkriptRuntime runtime = SkriptRuntime.instance();
            runtime.clearScripts();
            runtime.loadFromResource("skript/gametest/player_input_past_forward_marks_block.sk");

            BlockPos markerAbsolute = helper.absolutePos(new BlockPos(7, 1, 0));
            helper.getLevel().setBlockAndUpdate(markerAbsolute, Blocks.AIR.defaultBlockState());

            ServerPlayer player = helper.makeMockServerPlayerInLevel();
            player.setGameMode(GameType.CREATIVE);
            player.teleportTo(markerAbsolute.getX() + 0.5D, markerAbsolute.getY() + 1.0D, markerAbsolute.getZ() + 0.5D);
            int executed = runtime.dispatch(new org.skriptlang.skript.lang.event.SkriptEvent(
                    new FabricPlayerInputHandle(
                            helper.getLevel(),
                            player,
                            new Input(true, false, false, false, false, false, false),
                            Input.EMPTY
                    ),
                    helper.getLevel().getServer(),
                    helper.getLevel(),
                    player
            ));
            helper.assertTrue(
                    executed == 1,
                    Component.literal("Expected past player input script to execute exactly one trigger.")
            );

            helper.assertTrue(
                    helper.getLevel().getBlockState(markerAbsolute).is(Blocks.ORANGE_WOOL),
                    Component.literal("Expected past player input condition script to observe the previous input state.")
            );
            runtime.clearScripts();
        });
    }

    @GameTest
    public void namedEntityConditionBlocksEffectsWhenFalse(GameTestHelper helper) {
        runWithRuntimeLock(helper, () -> {
            SkriptRuntime runtime = SkriptRuntime.instance();
            runtime.clearScripts();
            runtime.loadFromResource("skript/gametest/named_entity_blocks_effects.sk");

            BlockPos playerMarkerAbsolute = helper.absolutePos(new BlockPos(6, 1, 0));
            helper.getLevel().setBlockAndUpdate(playerMarkerAbsolute, Blocks.AIR.defaultBlockState());

            ArmorStand armorStand = new ArmorStand(helper.getLevel(), 0.5D, 1.0D, 0.5D);
            helper.getLevel().addFreshEntity(armorStand);

            ServerPlayer player = helper.makeMockServerPlayerInLevel();
            player.setGameMode(GameType.CREATIVE);
            player.teleportTo(playerMarkerAbsolute.getX() + 0.5D, playerMarkerAbsolute.getY() + 1.0D, playerMarkerAbsolute.getZ() + 0.5D);

            InteractionResult result = UseEntityCallback.EVENT.invoker().interact(
                    player,
                    helper.getLevel(),
                    InteractionHand.MAIN_HAND,
                    armorStand,
                    new EntityHitResult(armorStand)
            );
            helper.assertTrue(
                    result == InteractionResult.PASS,
                    Component.literal("Expected named condition test to keep Fabric callback flow in PASS state.")
            );
            helper.assertTrue(
                    helper.getLevel().getBlockState(playerMarkerAbsolute).is(Blocks.AIR),
                    Component.literal("Expected false named condition to stop later effects in the trigger.")
            );
            runtime.clearScripts();
        });
    }

    @GameTest
    public void scaledDamageSourceConditionExecutesRealScript(GameTestHelper helper) {
        runWithRuntimeLock(helper, () -> {
            SkriptRuntime runtime = SkriptRuntime.instance();
            runtime.clearScripts();
            runtime.loadFromResource("skript/gametest/scaled_damage_source_names_entity.sk");

            Cow victim = createCow(helper, false);
            Cow attacker = createCow(helper, false);

            boolean allowed = ServerLivingEntityEvents.ALLOW_DAMAGE.invoker().allowDamage(
                    victim,
                    helper.getLevel().damageSources().mobAttack(attacker),
                    2.0F
            );
            helper.assertTrue(
                    allowed,
                    Component.literal("Expected damage bridge to preserve Fabric allow-damage flow.")
            );
            helper.assertTrue(
                    victim.getCustomName() != null && "scaled target".equals(victim.getCustomName().getString()),
                    Component.literal("Expected scaled damage source condition to rename the damaged entity from a real .sk file.")
            );
            runtime.clearScripts();
        });
    }

    @GameTest
    public void indirectDamageSourceConditionExecutesRealScript(GameTestHelper helper) {
        runWithRuntimeLock(helper, () -> {
            SkriptRuntime runtime = SkriptRuntime.instance();
            runtime.clearScripts();
            runtime.loadFromResource("skript/gametest/indirect_damage_source_names_entity.sk");

            Cow victim = createCow(helper, false);
            ServerPlayer attacker = helper.makeMockServerPlayerInLevel();
            ArmorStand projectile = new ArmorStand(helper.getLevel(), 1.5D, 1.0D, 0.5D);
            helper.getLevel().addFreshEntity(projectile);

            boolean allowed = ServerLivingEntityEvents.ALLOW_DAMAGE.invoker().allowDamage(
                    victim,
                    helper.getLevel().damageSources().thrown(projectile, attacker),
                    1.0F
            );
            helper.assertTrue(
                    allowed,
                    Component.literal("Expected damage bridge to preserve Fabric allow-damage flow.")
            );
            helper.assertTrue(
                    victim.getCustomName() != null && "indirect target".equals(victim.getCustomName().getString()),
                    Component.literal("Expected indirect damage source condition to rename the damaged entity from a real .sk file.")
            );
            runtime.clearScripts();
        });
    }

    @GameTest
    public void directDamageSourceDoesNotExecuteIndirectScript(GameTestHelper helper) {
        runWithRuntimeLock(helper, () -> {
            SkriptRuntime runtime = SkriptRuntime.instance();
            runtime.clearScripts();
            runtime.loadFromResource("skript/gametest/indirect_damage_source_names_entity.sk");

            Cow victim = createCow(helper, false);
            ServerPlayer attacker = helper.makeMockServerPlayerInLevel();

            boolean allowed = ServerLivingEntityEvents.ALLOW_DAMAGE.invoker().allowDamage(
                    victim,
                    helper.getLevel().damageSources().playerAttack(attacker),
                    1.0F
            );
            helper.assertTrue(
                    allowed,
                    Component.literal("Expected damage bridge to preserve Fabric allow-damage flow.")
            );
            helper.assertTrue(
                    victim.getCustomName() == null,
                    Component.literal("Expected direct damage source to fail the indirect condition and stop later effects.")
            );
            runtime.clearScripts();
        });
    }

    @GameTest
    public void causingEntityExpressionExecutesRealScript(GameTestHelper helper) {
        runWithRuntimeLock(helper, () -> {
            SkriptRuntime runtime = SkriptRuntime.instance();
            runtime.clearScripts();
            runtime.loadFromResource("skript/gametest/causing_entity_names_target.sk");

            Cow victim = createCow(helper, false);
            Cow attacker = createCow(helper, false);
            attacker.setCustomName(Component.literal("named attacker"));

            boolean allowed = ServerLivingEntityEvents.ALLOW_DAMAGE.invoker().allowDamage(
                    victim,
                    helper.getLevel().damageSources().mobAttack(attacker),
                    1.0F
            );
            helper.assertTrue(
                    allowed,
                    Component.literal("Expected causing entity expression script to preserve Fabric allow-damage flow.")
            );
            helper.assertTrue(
                    victim.getCustomName() != null && "caused target".equals(victim.getCustomName().getString()),
                    Component.literal("Expected causing entity expression script to rename the damaged entity.")
            );
            runtime.clearScripts();
        });
    }

    @GameTest
    public void directEntityExpressionExecutesRealScript(GameTestHelper helper) {
        runWithRuntimeLock(helper, () -> {
            SkriptRuntime runtime = SkriptRuntime.instance();
            runtime.clearScripts();
            runtime.loadFromResource("skript/gametest/direct_entity_names_target.sk");

            Cow victim = createCow(helper, false);
            ServerPlayer attacker = helper.makeMockServerPlayerInLevel();
            ArmorStand projectile = new ArmorStand(helper.getLevel(), 1.5D, 1.0D, 0.5D);
            projectile.setCustomName(Component.literal("named projectile"));
            helper.getLevel().addFreshEntity(projectile);

            boolean allowed = ServerLivingEntityEvents.ALLOW_DAMAGE.invoker().allowDamage(
                    victim,
                    helper.getLevel().damageSources().thrown(projectile, attacker),
                    1.0F
            );
            helper.assertTrue(
                    allowed,
                    Component.literal("Expected direct entity expression script to preserve Fabric allow-damage flow.")
            );
            helper.assertTrue(
                    victim.getCustomName() != null && "direct target".equals(victim.getCustomName().getString()),
                    Component.literal("Expected direct entity expression script to rename the damaged entity.")
            );
            runtime.clearScripts();
        });
    }

    @GameTest
    public void sourceLocationExpressionExecutesRealScript(GameTestHelper helper) {
        runWithRuntimeLock(helper, () -> {
            SkriptRuntime runtime = SkriptRuntime.instance();
            runtime.clearScripts();
            runtime.loadFromResource("skript/gametest/source_location_sets_block.sk");

            Cow victim = createCow(helper, false);
            ServerPlayer attacker = helper.makeMockServerPlayerInLevel();
            BlockPos markerRelative = new BlockPos(4, 1, 0);
            BlockPos markerAbsolute = helper.absolutePos(markerRelative);
            ArmorStand projectile = new ArmorStand(
                    helper.getLevel(),
                    markerAbsolute.getX() + 0.5D,
                    markerAbsolute.getY() + 0.5D,
                    markerAbsolute.getZ() + 0.5D
            );
            helper.getLevel().addFreshEntity(projectile);
            helper.getLevel().setBlockAndUpdate(markerAbsolute, Blocks.AIR.defaultBlockState());

            boolean allowed = ServerLivingEntityEvents.ALLOW_DAMAGE.invoker().allowDamage(
                    victim,
                    helper.getLevel().damageSources().thrown(projectile, attacker),
                    1.0F
            );
            helper.assertTrue(
                    allowed,
                    Component.literal("Expected source location expression script to preserve Fabric allow-damage flow.")
            );
            helper.assertBlockPresent(Blocks.RED_WOOL, markerRelative);
            runtime.clearScripts();
        });
    }

    @GameTest
    public void damageLocationExpressionExecutesRealScript(GameTestHelper helper) {
        runWithRuntimeLock(helper, () -> {
            SkriptRuntime runtime = SkriptRuntime.instance();
            runtime.clearScripts();
            runtime.loadFromResource("skript/gametest/damage_location_sets_block.sk");

            Cow victim = createCow(helper, false);
            BlockPos markerRelative = new BlockPos(5, 1, 0);
            BlockPos markerAbsolute = helper.absolutePos(markerRelative);
            helper.getLevel().setBlockAndUpdate(markerAbsolute, Blocks.AIR.defaultBlockState());

            boolean allowed = ServerLivingEntityEvents.ALLOW_DAMAGE.invoker().allowDamage(
                    victim,
                    helper.getLevel().damageSources().badRespawnPointExplosion(Vec3.atCenterOf(markerAbsolute)),
                    1.0F
            );
            helper.assertTrue(
                    allowed,
                    Component.literal("Expected damage location expression script to preserve Fabric allow-damage flow.")
            );
            helper.assertBlockPresent(Blocks.BLUE_WOOL, markerRelative);
            runtime.clearScripts();
        });
    }

    @GameTest
    public void foodExhaustionExpressionExecutesRealScript(GameTestHelper helper) {
        runWithRuntimeLock(helper, () -> {
            SkriptRuntime runtime = SkriptRuntime.instance();
            runtime.clearScripts();

            Cow victim = createCow(helper, false);
            ServerPlayer attacker = helper.makeMockServerPlayerInLevel();
            DamageSource damageSource = helper.getLevel().damageSources().playerAttack(attacker);
            Path scriptPath = writeTempScript(
                    "food_exhaustion_expression",
                    """
                    on damage:
                        food exhaustion of event-damage source is "%s"
                        set test name of entity event-entity to "food exhaustion target"
                    """.formatted(Float.toString(damageSource.getFoodExhaustion()))
            );
            try {
                runtime.loadFromPath(scriptPath);
            } catch (IOException exception) {
                throw new IllegalStateException("Failed to load temporary food exhaustion script.", exception);
            }

            boolean allowed = ServerLivingEntityEvents.ALLOW_DAMAGE.invoker().allowDamage(
                    victim,
                    damageSource,
                    1.0F
            );
            helper.assertTrue(
                    allowed,
                    Component.literal("Expected food exhaustion expression script to preserve Fabric allow-damage flow.")
            );
            helper.assertTrue(
                    victim.getCustomName() != null && "food exhaustion target".equals(victim.getCustomName().getString()),
                    Component.literal("Expected food exhaustion expression script to rename the damaged entity.")
            );
            runtime.clearScripts();
        });
    }

    @GameTest
    public void brewingFuelLevelExpressionExecutesRealScript(GameTestHelper helper) {
        runWithRuntimeLock(helper, () -> {
            SkriptRuntime runtime = SkriptRuntime.instance();
            runtime.clearScripts();
            runtime.loadFromResource("skript/gametest/brewing_fuel_level_marks_block.sk");

            BlockPos brewingRelative = new BlockPos(9, 1, 0);
            BlockPos brewingAbsolute = helper.absolutePos(brewingRelative);
            helper.getLevel().setBlockAndUpdate(brewingAbsolute, Blocks.BREWING_STAND.defaultBlockState());

            BrewingStandBlockEntity brewingStand = (BrewingStandBlockEntity) helper.getLevel().getBlockEntity(brewingAbsolute);
            helper.assertTrue(
                    brewingStand != null,
                    Component.literal("Expected brewing stand block entity to exist for fuel level expression test.")
            );
            if (brewingStand == null) {
                throw new IllegalStateException("Brewing stand block entity was not created.");
            }
            PrivateBlockEntityAccess.setBrewingFuel(brewingStand, 20);

            ServerPlayer player = helper.makeMockServerPlayerInLevel();
            player.setGameMode(GameType.CREATIVE);
            player.teleportTo(brewingAbsolute.getX() + 0.5D, brewingAbsolute.getY() + 1.0D, brewingAbsolute.getZ() + 0.5D);

            InteractionResult result = UseBlockCallback.EVENT.invoker().interact(
                    player,
                    helper.getLevel(),
                    InteractionHand.MAIN_HAND,
                    new BlockHitResult(Vec3.atCenterOf(brewingAbsolute), Direction.UP, brewingAbsolute, false)
            );
            helper.assertTrue(
                    result == InteractionResult.PASS,
                    Component.literal("Expected brewing fuel level expression script to keep Fabric callback flow in PASS state.")
            );
            helper.assertBlockPresent(Blocks.GOLD_BLOCK, new BlockPos(9, 2, 0));
            runtime.clearScripts();
        });
    }

    @GameTest
    public void brewingTimeExpressionExecutesRealScript(GameTestHelper helper) {
        runWithRuntimeLock(helper, () -> {
            SkriptRuntime runtime = SkriptRuntime.instance();
            runtime.clearScripts();
            runtime.loadFromResource("skript/gametest/brewing_time_sets_block.sk");

            BlockPos brewingRelative = new BlockPos(10, 1, 0);
            BlockPos brewingAbsolute = helper.absolutePos(brewingRelative);
            helper.getLevel().setBlockAndUpdate(brewingAbsolute, Blocks.BREWING_STAND.defaultBlockState());

            BrewingStandBlockEntity brewingStand = (BrewingStandBlockEntity) helper.getLevel().getBlockEntity(brewingAbsolute);
            helper.assertTrue(
                    brewingStand != null,
                    Component.literal("Expected brewing stand block entity to exist for brewing time expression test.")
            );
            if (brewingStand == null) {
                throw new IllegalStateException("Brewing stand block entity was not created.");
            }
            PrivateBlockEntityAccess.setBrewingTime(brewingStand, 5);

            ServerPlayer player = helper.makeMockServerPlayerInLevel();
            player.setGameMode(GameType.CREATIVE);
            player.teleportTo(brewingAbsolute.getX() + 0.5D, brewingAbsolute.getY() + 1.0D, brewingAbsolute.getZ() + 0.5D);

            InteractionResult result = UseBlockCallback.EVENT.invoker().interact(
                    player,
                    helper.getLevel(),
                    InteractionHand.MAIN_HAND,
                    new BlockHitResult(Vec3.atCenterOf(brewingAbsolute), Direction.UP, brewingAbsolute, false)
            );
            helper.assertTrue(
                    result == InteractionResult.PASS,
                    Component.literal("Expected brewing time expression script to keep Fabric callback flow in PASS state.")
            );
            helper.assertBlockPresent(Blocks.DIAMOND_BLOCK, new BlockPos(10, 2, 0));
            helper.assertTrue(
                    PrivateBlockEntityAccess.brewingTime(brewingStand) == 6,
                    Component.literal("Expected brewing time expression script to change the brewing time to 6 ticks.")
            );
            runtime.clearScripts();
        });
    }

    @GameTest
    public void textDisplayDropShadowConditionExecutesRealScript(GameTestHelper helper) {
        Display.TextDisplay textDisplay = createTextDisplay(helper, Display.TextDisplay.FLAG_SHADOW);
        assertUseEntityScriptNamesEntity(
                helper,
                "skript/gametest/text_display_shadow_names_entity.sk",
                textDisplay,
                "shadowed text"
        );
    }

    @GameTest
    public void textDisplaySeeThroughConditionExecutesRealScript(GameTestHelper helper) {
        Display.TextDisplay textDisplay = createTextDisplay(helper, Display.TextDisplay.FLAG_SEE_THROUGH);
        assertUseEntityScriptNamesEntity(
                helper,
                "skript/gametest/text_display_see_through_names_entity.sk",
                textDisplay,
                "see through text"
        );
    }

    @GameTest
    public void textDisplayAlignmentExpressionExecutesRealScript(GameTestHelper helper) {
        Display.TextDisplay textDisplay = createTextDisplay(helper, Display.TextDisplay.FLAG_ALIGN_LEFT);
        assertUseEntityScriptNamesEntity(
                helper,
                "skript/gametest/text_display_alignment_names_entity.sk",
                textDisplay,
                "left aligned text"
        );
    }

    @GameTest
    public void textDisplayLineWidthExpressionExecutesRealScript(GameTestHelper helper) {
        Display.TextDisplay textDisplay = createTextDisplay(helper, (byte) 0);
        PrivateEntityAccess.setTextDisplayLineWidth(textDisplay, 300);
        assertUseEntityScriptNamesEntity(
                helper,
                "skript/gametest/text_display_line_width_names_entity.sk",
                textDisplay,
                "wide text"
        );
    }

    @GameTest
    public void textDisplayOpacityExpressionExecutesRealScript(GameTestHelper helper) {
        Display.TextDisplay textDisplay = createTextDisplay(helper, (byte) 0);
        PrivateEntityAccess.setTextDisplayOpacity(textDisplay, (byte) -128);
        assertUseEntityScriptNamesEntity(
                helper,
                "skript/gametest/text_display_opacity_names_entity.sk",
                textDisplay,
                "semi transparent text"
        );
    }

    @GameTest
    public void displayBillboardExpressionExecutesRealScript(GameTestHelper helper) {
        Display.TextDisplay textDisplay = createTextDisplay(helper, (byte) 0);
        PrivateEntityAccess.setDisplayBillboardConstraints(textDisplay, Display.BillboardConstraints.CENTER);
        assertUseEntityScriptNamesEntity(
                helper,
                "skript/gametest/display_billboard_names_entity.sk",
                textDisplay,
                "center billboard"
        );
    }

    @GameTest
    public void displayBrightnessExpressionExecutesRealScript(GameTestHelper helper) {
        Display.TextDisplay textDisplay = createTextDisplay(helper, (byte) 0);
        PrivateEntityAccess.setDisplayBrightnessOverride(textDisplay, new Brightness(4, 7));
        assertUseEntityScriptNamesEntity(
                helper,
                "skript/gametest/display_brightness_names_entity.sk",
                textDisplay,
                "bright text"
        );
    }

    @GameTest
    public void displayHeightExpressionExecutesRealScript(GameTestHelper helper) {
        Display.TextDisplay textDisplay = createTextDisplay(helper, (byte) 0);
        PrivateEntityAccess.setDisplayHeight(textDisplay, 2.5F);
        assertUseEntityScriptNamesEntity(
                helper,
                "skript/gametest/display_height_names_entity.sk",
                textDisplay,
                "tall text"
        );
    }

    @GameTest
    public void displayShadowExpressionExecutesRealScript(GameTestHelper helper) {
        Display.TextDisplay textDisplay = createTextDisplay(helper, (byte) 0);
        PrivateEntityAccess.setDisplayShadowRadius(textDisplay, 1.75F);
        assertUseEntityScriptNamesEntity(
                helper,
                "skript/gametest/display_shadow_names_entity.sk",
                textDisplay,
                "shadow radius text"
        );
    }

    @GameTest
    public void displayViewRangeExpressionExecutesRealScript(GameTestHelper helper) {
        Display.TextDisplay textDisplay = createTextDisplay(helper, (byte) 0);
        PrivateEntityAccess.setDisplayViewRange(textDisplay, 2.5F);
        assertUseEntityScriptNamesEntity(
                helper,
                "skript/gametest/display_view_range_names_entity.sk",
                textDisplay,
                "long range text"
        );
    }

    @GameTest
    public void displayInterpolationDelayExpressionExecutesRealScript(GameTestHelper helper) {
        Display.TextDisplay textDisplay = createTextDisplay(helper, (byte) 0);
        assertUseEntityScriptSetsMarker(
                helper,
                "skript/gametest/display_interpolation_delay_names_entity.sk",
                textDisplay,
                new BlockPos(9, 1, 0),
                Blocks.RED_WOOL,
                () -> helper.assertTrue(
                        PrivateEntityAccess.displayTransformationInterpolationDelay(textDisplay) == 3,
                        Component.literal("Expected interpolation delay script to update Mojang display interpolation delay.")
                )
        );
    }

    @GameTest
    public void displayInterpolationDurationExpressionExecutesRealScript(GameTestHelper helper) {
        Display.TextDisplay textDisplay = createTextDisplay(helper, (byte) 0);
        assertUseEntityScriptSetsMarker(
                helper,
                "skript/gametest/display_interpolation_duration_names_entity.sk",
                textDisplay,
                new BlockPos(10, 1, 0),
                Blocks.BLUE_WOOL,
                () -> helper.assertTrue(
                        PrivateEntityAccess.displayTransformationInterpolationDuration(textDisplay) == 4,
                        Component.literal("Expected interpolation duration script to update Mojang display interpolation duration.")
                )
        );
    }

    @GameTest
    public void displayTeleportDurationExpressionExecutesRealScript(GameTestHelper helper) {
        Display.TextDisplay textDisplay = createTextDisplay(helper, (byte) 0);
        assertUseEntityScriptSetsMarker(
                helper,
                "skript/gametest/display_teleport_duration_names_entity.sk",
                textDisplay,
                new BlockPos(11, 1, 0),
                Blocks.LIME_WOOL,
                () -> helper.assertTrue(
                        PrivateEntityAccess.displayPosRotInterpolationDuration(textDisplay) == 2,
                        Component.literal("Expected teleport duration script to update Mojang display position interpolation duration.")
                )
        );
    }

    @GameTest
    public void displayTransformationTranslationExpressionExecutesRealScript(GameTestHelper helper) {
        Display.TextDisplay textDisplay = createTextDisplay(helper, (byte) 0);
        PrivateEntityAccess.setDisplayTransformation(
                textDisplay,
                new Transformation(
                        new Vector3f(1.25F, 2.5F, 3.75F),
                        new Quaternionf(0.0F, 0.0F, 0.0F, 1.0F),
                        new Vector3f(1.0F, 1.0F, 1.0F),
                        new Quaternionf(0.0F, 0.0F, 0.0F, 1.0F)
                )
        );
        assertUseEntityScriptNamesEntity(
                helper,
                "skript/gametest/display_translation_names_entity.sk",
                textDisplay,
                "translated text"
        );
    }

    @GameTest
    public void displayTransformationScaleExpressionExecutesRealScript(GameTestHelper helper) {
        Display.TextDisplay textDisplay = createTextDisplay(helper, (byte) 0);
        PrivateEntityAccess.setDisplayTransformation(
                textDisplay,
                new Transformation(
                        new Vector3f(0.0F, 0.0F, 0.0F),
                        new Quaternionf(0.0F, 0.0F, 0.0F, 1.0F),
                        new Vector3f(2.0F, 3.0F, 4.0F),
                        new Quaternionf(0.0F, 0.0F, 0.0F, 1.0F)
                )
        );
        assertUseEntityScriptNamesEntity(
                helper,
                "skript/gametest/display_scale_names_entity.sk",
                textDisplay,
                "scaled text"
        );
    }

    @GameTest
    public void displayLeftRotationExpressionExecutesRealScript(GameTestHelper helper) {
        Display.TextDisplay textDisplay = createTextDisplay(helper, (byte) 0);
        PrivateEntityAccess.setDisplayTransformation(
                textDisplay,
                new Transformation(
                        new Vector3f(0.0F, 0.0F, 0.0F),
                        new Quaternionf(0.1F, 0.2F, 0.3F, 0.4F),
                        new Vector3f(1.0F, 1.0F, 1.0F),
                        new Quaternionf(0.0F, 0.0F, 0.0F, 1.0F)
                )
        );
        assertUseEntityScriptNamesEntity(
                helper,
                "skript/gametest/display_left_rotation_names_entity.sk",
                textDisplay,
                "left rotated text"
        );
    }

    @GameTest
    public void displayRightRotationExpressionExecutesRealScript(GameTestHelper helper) {
        Display.TextDisplay textDisplay = createTextDisplay(helper, (byte) 0);
        PrivateEntityAccess.setDisplayTransformation(
                textDisplay,
                new Transformation(
                        new Vector3f(0.0F, 0.0F, 0.0F),
                        new Quaternionf(0.0F, 0.0F, 0.0F, 1.0F),
                        new Vector3f(1.0F, 1.0F, 1.0F),
                        new Quaternionf(0.5F, 0.6F, 0.7F, 0.8F)
                )
        );
        assertUseEntityScriptNamesEntity(
                helper,
                "skript/gametest/display_right_rotation_names_entity.sk",
                textDisplay,
                "right rotated text"
        );
    }

    @GameTest
    public void itemDisplayTransformExpressionExecutesRealScript(GameTestHelper helper) {
        Display.ItemDisplay itemDisplay = createItemDisplay(helper, ItemDisplayContext.FIRST_PERSON_LEFT_HAND);
        assertUseEntityScriptNamesEntity(
                helper,
                "skript/gametest/item_display_transform_names_entity.sk",
                itemDisplay,
                "left hand transform"
        );
    }

    @GameTest
    public void responsiveInteractionConditionExecutesRealScript(GameTestHelper helper) {
        Interaction interaction = createInteraction(helper, true);
        assertUseEntityScriptNamesEntity(
                helper,
                "skript/gametest/responsive_interaction_names_entity.sk",
                interaction,
                "responsive interaction"
        );
    }

    @GameTest
    public void loveTimeExpressionExecutesRealScript(GameTestHelper helper) {
        Cow cow = createCow(helper, false);
        assertUseEntityScriptSetsMarker(
                helper,
                "skript/gametest/love_time_marks_block.sk",
                cow,
                new BlockPos(12, 1, 0),
                Blocks.BLUE_WOOL,
                () -> helper.assertTrue(
                        cow.getInLoveTime() == 30 * 20,
                        Component.literal("Expected love time expression script to set the animal love time to 30 seconds.")
                )
        );
    }

    @GameTest
    public void interactionDimensionsExpressionExecutesRealScript(GameTestHelper helper) {
        Interaction interaction = createInteraction(helper, true);
        assertUseEntityScriptSetsMarker(
                helper,
                "skript/gametest/interaction_dimensions_marks_block.sk",
                interaction,
                new BlockPos(13, 1, 0),
                Blocks.RED_WOOL,
                () -> {
                    helper.assertTrue(
                            Float.compare(PrivateEntityAccess.interactionWidth(interaction), 2.0F) == 0,
                            Component.literal("Expected interaction width expression script to set the interaction width to 2.")
                    );
                    helper.assertTrue(
                            Float.compare(PrivateEntityAccess.interactionHeight(interaction), 3.0F) == 0,
                            Component.literal("Expected interaction height expression script to set the interaction height to 3.")
                    );
                }
        );
    }

    @GameTest
    public void lastClickedInteractionPlayerExpressionExecutesRealScript(GameTestHelper helper) {
        Interaction interaction = createInteraction(helper, true);
        assertUseEntityScriptNamesEntity(
                helper,
                "skript/gametest/last_click_player_names_entity.sk",
                interaction,
                "clicked interaction"
        );
    }

    @GameTest
    public void lastAttackedInteractionPlayerExpressionExecutesRealScript(GameTestHelper helper) {
        Interaction interaction = createInteraction(helper, true);
        assertAttackEntityScriptNamesEntity(
                helper,
                "skript/gametest/last_attack_player_names_entity.sk",
                interaction,
                "attacked interaction"
        );
    }

    @GameTest
    public void lootableEntityConditionExecutesRealScript(GameTestHelper helper) {
        MinecartChest chestMinecart = createChestMinecart(helper, false);
        assertUseEntityScriptNamesEntity(
                helper,
                "skript/gametest/lootable_entity_names_entity.sk",
                chestMinecart,
                "lootable entity"
        );
    }

    @GameTest
    public void lootTableConditionExecutesRealScript(GameTestHelper helper) {
        MinecartChest chestMinecart = createChestMinecart(helper, true);
        assertUseEntityScriptNamesEntity(
                helper,
                "skript/gametest/loot_table_entity_names_entity.sk",
                chestMinecart,
                "loot table entity"
        );
    }

    @GameTest
    public void missingLootTableDoesNotExecuteLootTableScript(GameTestHelper helper) {
        MinecartChest chestMinecart = createChestMinecart(helper, false);
        assertUseEntityScriptDoesNotNameEntity(
                helper,
                "skript/gametest/loot_table_entity_names_entity.sk",
                chestMinecart
        );
    }

    @GameTest
    public void lootTableExpressionExecutesRealScript(GameTestHelper helper) {
        MinecartChest chestMinecart = createChestMinecart(helper, true);
        assertUseEntityScriptNamesEntity(
                helper,
                "skript/gametest/loot_table_expression_names_entity.sk",
                chestMinecart,
                "loot table expression"
        );
    }

    @GameTest
    public void lootTableSetterExecutesRealScript(GameTestHelper helper) {
        runWithRuntimeLock(helper, () -> {
            SkriptRuntime runtime = SkriptRuntime.instance();
            runtime.clearScripts();
            runtime.loadFromResource("skript/gametest/loot_table_setter_names_entity.sk");

            MinecartChest chestMinecart = createChestMinecart(helper, false);
            Trigger trigger = getOnlyLoadedTrigger(runtime);

            ServerPlayer player = helper.makeMockServerPlayerInLevel();
            player.setGameMode(GameType.CREATIVE);
            player.teleportTo(chestMinecart.getX(), chestMinecart.getY() + 1.0D, chestMinecart.getZ());
            EntityHitResult hitResult = new EntityHitResult(chestMinecart);
            var skriptEvent = new org.skriptlang.skript.lang.event.SkriptEvent(
                    new FabricUseEntityHandle(helper.getLevel(), player, InteractionHand.MAIN_HAND, chestMinecart, hitResult),
                    helper.getLevel().getServer(),
                    helper.getLevel(),
                    player
            );

            InteractionResult result = UseEntityCallback.EVENT.invoker().interact(
                    player,
                    helper.getLevel(),
                    InteractionHand.MAIN_HAND,
                    chestMinecart,
                    hitResult
            );
            helper.assertTrue(
                    result == InteractionResult.PASS,
                    Component.literal("Expected loot table setter script to keep Fabric callback flow in PASS state.")
            );
            helper.assertTrue(
                    chestMinecart.getCustomName() != null && "loot table set".equals(chestMinecart.getCustomName().getString()),
                    Component.literal("Expected loot table setter script to name the chest minecart. Items: "
                            + describeTriggerItems(trigger)
                            + ", fields0: " + describeExpressionFields(getTriggerItem(trigger, 0), skriptEvent)
                            + ", fields1: " + describeExpressionFields(getTriggerItem(trigger, 1), skriptEvent)
                            + ", fields2: " + describeExpressionFields(getTriggerItem(trigger, 2), skriptEvent))
            );
            helper.assertTrue(
                    BuiltInLootTables.SIMPLE_DUNGEON.equals(chestMinecart.getContainerLootTable()),
                    Component.literal("Expected loot table setter script to assign the simple dungeon loot table to the chest minecart. Items: "
                            + describeTriggerItems(trigger))
            );
            helper.assertTrue(
                    chestMinecart.getContainerLootTableSeed() == 23L,
                    Component.literal("Expected loot table setter script to assign the configured seed to the chest minecart. Items: "
                            + describeTriggerItems(trigger))
            );
            runtime.clearScripts();
        });
    }

    @GameTest
    public void lootTableBlockExpressionsExecuteRealScript(GameTestHelper helper) {
        runWithRuntimeLock(helper, () -> {
            SkriptRuntime runtime = SkriptRuntime.instance();
            runtime.clearScripts();
            runtime.loadFromResource("skript/gametest/loot_table_block_sets_marker.sk");
            Trigger trigger = getOnlyLoadedTrigger(runtime);

            BlockPos chestRelative = new BlockPos(14, 1, 0);
            BlockPos chestAbsolute = helper.absolutePos(chestRelative);
            helper.getLevel().setBlockAndUpdate(chestAbsolute, Blocks.CHEST.defaultBlockState());

            ChestBlockEntity chest = (ChestBlockEntity) helper.getLevel().getBlockEntity(chestAbsolute);
            helper.assertTrue(
                    chest != null,
                    Component.literal("Expected chest block entity to exist for loot table block expression test.")
            );
            if (chest == null) {
                throw new IllegalStateException("Chest block entity was not created.");
            }

            ServerPlayer player = helper.makeMockServerPlayerInLevel();
            player.setGameMode(GameType.CREATIVE);
            player.teleportTo(chestAbsolute.getX() + 0.5D, chestAbsolute.getY() + 1.0D, chestAbsolute.getZ() + 0.5D);
            BlockHitResult hitResult = new BlockHitResult(Vec3.atCenterOf(chestAbsolute), Direction.UP, chestAbsolute, false);
            var skriptEvent = new org.skriptlang.skript.lang.event.SkriptEvent(
                    new org.skriptlang.skript.fabric.runtime.FabricUseBlockHandle(helper.getLevel(), player, InteractionHand.MAIN_HAND, hitResult),
                    helper.getLevel().getServer(),
                    helper.getLevel(),
                    player
            );

            InteractionResult result = UseBlockCallback.EVENT.invoker().interact(
                    player,
                    helper.getLevel(),
                    InteractionHand.MAIN_HAND,
                    hitResult
            );
            helper.assertTrue(
                    result == InteractionResult.PASS,
                    Component.literal("Expected loot table block expression script to keep Fabric callback flow in PASS state.")
            );
            helper.assertTrue(
                    helper.getLevel().getBlockState(helper.absolutePos(new BlockPos(14, 2, 0))).is(Blocks.EMERALD_BLOCK),
                    Component.literal("Expected loot table block expression script to mark the block above the chest. Items: "
                            + describeTriggerItems(trigger)
                            + ", fields0: " + describeExpressionFields(getTriggerItem(trigger, 0), skriptEvent)
                            + ", fields1: " + describeExpressionFields(getTriggerItem(trigger, 1), skriptEvent)
                            + ", fields2: " + describeExpressionFields(getTriggerItem(trigger, 2), skriptEvent))
            );
            helper.assertTrue(
                    BuiltInLootTables.SIMPLE_DUNGEON.equals(chest.getLootTable()),
                    Component.literal("Expected loot table block expression script to assign the simple dungeon loot table to the chest block entity. Items: "
                            + describeTriggerItems(trigger))
            );
            helper.assertTrue(
                    chest.getLootTableSeed() == 29L,
                    Component.literal("Expected loot table block expression script to assign the configured seed to the chest block entity. Items: "
                            + describeTriggerItems(trigger))
            );
            runtime.clearScripts();
        });
    }

    @GameTest
    public void poisonedEntityConditionExecutesRealScript(GameTestHelper helper) {
        Cow cow = createCow(helper, false);
        cow.addEffect(new MobEffectInstance(MobEffects.POISON, 200, 0));
        assertUseEntityScriptNamesEntity(
                helper,
                "skript/gametest/poisoned_entity_names_entity.sk",
                cow,
                "poisoned entity"
        );
    }

    @GameTest
    public void hasPotionConditionExecutesRealScript(GameTestHelper helper) {
        Cow cow = createCow(helper, false);
        cow.addEffect(new MobEffectInstance(MobEffects.POISON, 200, 0));
        runWithRuntimeLock(helper, () -> {
            SkriptRuntime runtime = SkriptRuntime.instance();
            runtime.clearScripts();
            runtime.loadFromResource("skript/gametest/entity_with_poison_names_entity.sk");

            ServerPlayer player = helper.makeMockServerPlayerInLevel();
            player.setGameMode(GameType.CREATIVE);
            player.teleportTo(cow.getX(), cow.getY() + 1.0D, cow.getZ());
            helper.assertTrue(
                    skriptPatternMatches("event-entity has \"poison\" active", "%livingentities% (has|have) %objects% [active]"),
                    Component.literal("Expected has-potion syntax pattern to regex-match the script line.")
            );

            var event = new org.skriptlang.skript.lang.event.SkriptEvent(
                    new FabricUseEntityHandle(
                            helper.getLevel(),
                            player,
                            InteractionHand.MAIN_HAND,
                            cow,
                            new EntityHitResult(cow)
                    ),
                    helper.getLevel().getServer(),
                    helper.getLevel(),
                    player
            );
            Trigger trigger = getOnlyLoadedTrigger(runtime);
            helper.assertTrue(
                    getFirstTriggerItem(trigger) instanceof CondHasPotion,
                    Component.literal("Expected has-potion script to load CondHasPotion as the first trigger item. Items: "
                            + describeTriggerItems(trigger))
            );
            helper.assertTrue(
                    trigger.execute(event),
                    Component.literal("Expected loaded has-potion trigger to execute. Items: " + describeTriggerItems(trigger))
            );
            helper.assertTrue(
                    cow.getCustomName() != null && "poison effect entity".equals(cow.getCustomName().getString()),
                    Component.literal("Expected direct trigger execution for has-potion script to rename the entity. Items: "
                            + describeTriggerItems(trigger) + ", fields: " + describeExpressionFields(getFirstTriggerItem(trigger), event))
            );

            cow.setCustomName(null);
            InteractionResult result = UseEntityCallback.EVENT.invoker().interact(
                    player,
                    helper.getLevel(),
                    InteractionHand.MAIN_HAND,
                    cow,
                    new EntityHitResult(cow)
            );
            helper.assertTrue(
                    result == InteractionResult.PASS,
                    Component.literal("Expected use entity condition test to keep Fabric callback flow in PASS state.")
            );
            helper.assertTrue(
                    cow.getCustomName() != null && "poison effect entity".equals(cow.getCustomName().getString()),
                    Component.literal("Expected condition script to name the interacted entity. Items: " + describeTriggerItems(trigger))
            );
            runtime.clearScripts();
        });
    }

    @GameTest
    public void potionEffectsExpressionExecutesRealScript(GameTestHelper helper) {
        Cow cow = createCow(helper, false);
        cow.addEffect(new MobEffectInstance(MobEffects.POISON, 200, 0));
        assertUseEntityScriptNamesEntity(
                helper,
                "skript/gametest/potion_effects_names_entity.sk",
                cow,
                "active potion effects"
        );
    }

    @GameTest
    public void specificPotionEffectExpressionExecutesRealScript(GameTestHelper helper) {
        Cow cow = createCow(helper, false);
        cow.addEffect(new MobEffectInstance(MobEffects.POISON, 200, 0));
        assertUseEntityScriptNamesEntity(
                helper,
                "skript/gametest/potion_effect_names_entity.sk",
                cow,
                "specific potion effect"
        );
    }

    @GameTest
    public void potionDurationExpressionExecutesRealScript(GameTestHelper helper) {
        Cow cow = createCow(helper, false);
        cow.addEffect(new MobEffectInstance(MobEffects.POISON, 200, 0));
        assertUseEntityScriptNamesEntity(
                helper,
                "skript/gametest/potion_duration_names_entity.sk",
                cow,
                "potion duration"
        );
    }

    @GameTest
    public void potionAmplifierExpressionExecutesRealScript(GameTestHelper helper) {
        Cow cow = createCow(helper, false);
        cow.addEffect(new MobEffectInstance(MobEffects.POISON, 200, 1));
        assertUseEntityScriptNamesEntity(
                helper,
                "skript/gametest/potion_amplifier_names_entity.sk",
                cow,
                "potion amplifier"
        );
    }

    @GameTest
    public void potionCategoryExpressionExecutesRealScript(GameTestHelper helper) {
        Cow cow = createCow(helper, false);
        assertUseEntityScriptNamesEntity(
                helper,
                "skript/gametest/potion_category_names_entity.sk",
                cow,
                "potion category"
        );
    }

    @GameTest
    public void taggedItemConditionExecutesRealScript(GameTestHelper helper) {
        runWithRuntimeLock(helper, () -> {
            SkriptRuntime runtime = SkriptRuntime.instance();
            runtime.clearScripts();
            runtime.loadFromResource("skript/gametest/tagged_item_renames_item.sk");

            ServerPlayer player = helper.makeMockServerPlayerInLevel();
            player.setGameMode(GameType.CREATIVE);
            player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.GOLD_INGOT));
            helper.assertTrue(
                    skriptPatternMatches("event-item is tagged with \"piglin_loved\"", "%objects% (is|are) tagged (as|with) %objects%"),
                    Component.literal("Expected tagged-item syntax pattern to regex-match the script line.")
            );

            var event = new org.skriptlang.skript.lang.event.SkriptEvent(
                    new FabricUseItemHandle(helper.getLevel(), player, InteractionHand.MAIN_HAND),
                    helper.getLevel().getServer(),
                    helper.getLevel(),
                    player
            );
            Trigger trigger = getOnlyLoadedTrigger(runtime);
            helper.assertTrue(
                    getFirstTriggerItem(trigger) instanceof CondIsTagged,
                    Component.literal("Expected tagged-item script to load CondIsTagged as the first trigger item. Items: "
                            + describeTriggerItems(trigger))
            );
            helper.assertTrue(
                    trigger.execute(event),
                    Component.literal("Expected loaded tagged-item trigger to execute. Items: " + describeTriggerItems(trigger))
            );
            ItemStack directHeld = player.getItemInHand(InteractionHand.MAIN_HAND);
            helper.assertTrue(
                    directHeld.getCustomName() != null && "tagged item".equals(directHeld.getCustomName().getString()),
                    Component.literal("Expected direct trigger execution for tagged-item script to rename the held item. Items: "
                            + describeTriggerItems(trigger) + ", fields: " + describeExpressionFields(getFirstTriggerItem(trigger), event))
            );

            player.getItemInHand(InteractionHand.MAIN_HAND).remove(DataComponents.CUSTOM_NAME);
            InteractionResult result = UseItemCallback.EVENT.invoker().interact(
                    player,
                    helper.getLevel(),
                    InteractionHand.MAIN_HAND
            );
            helper.assertTrue(
                    result == InteractionResult.PASS,
                    Component.literal("Expected use item condition test to keep Fabric callback flow in PASS state.")
            );
            ItemStack held = player.getItemInHand(InteractionHand.MAIN_HAND);
            helper.assertTrue(
                    held.getCustomName() != null && "tagged item".equals(held.getCustomName().getString()),
                    Component.literal("Expected condition script to rename the held item. Items: " + describeTriggerItems(trigger))
            );
            runtime.clearScripts();
        });
    }

    @GameTest
    public void equippableDamageConditionExecutesRealScript(GameTestHelper helper) {
        assertUseItemScriptNamesItem(
                helper,
                "skript/gametest/equippable_damage_renames_item.sk",
                createEquippableTestItem(true, false, false, false, false),
                "damage equippable"
        );
    }

    @GameTest
    public void equippableDispensableConditionExecutesRealScript(GameTestHelper helper) {
        assertUseItemScriptNamesItem(
                helper,
                "skript/gametest/equippable_dispensable_renames_item.sk",
                createEquippableTestItem(false, true, false, false, false),
                "dispensable equippable"
        );
    }

    @GameTest
    public void equippableInteractConditionExecutesRealScript(GameTestHelper helper) {
        assertUseItemScriptNamesItem(
                helper,
                "skript/gametest/equippable_interact_renames_item.sk",
                createEquippableTestItem(false, false, true, false, false),
                "interactable equippable"
        );
    }

    @GameTest
    public void equippableShearableConditionExecutesRealScript(GameTestHelper helper) {
        assertUseItemScriptNamesItem(
                helper,
                "skript/gametest/equippable_shearable_renames_item.sk",
                createEquippableTestItem(false, false, false, true, false),
                "shearable equippable"
        );
    }

    @GameTest
    public void equippableSwappableConditionExecutesRealScript(GameTestHelper helper) {
        assertUseItemScriptNamesItem(
                helper,
                "skript/gametest/equippable_swappable_renames_item.sk",
                createEquippableTestItem(false, false, false, false, true),
                "swappable equippable"
        );
    }

    @GameTest
    public void preventAgingEffectExecutesRealScript(GameTestHelper helper) {
        Cow cow = createCow(helper, false);
        assertUseEntityScriptSetsMarker(
                helper,
                "skript/gametest/allow_aging_marks_block.sk",
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
                "skript/gametest/unbreedable_marks_block.sk",
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
                "skript/gametest/make_adult_marks_block.sk",
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
                "skript/gametest/make_baby_marks_block.sk",
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
    public void brewingConsumeEffectExecutesRealScript(GameTestHelper helper) {
        runWithRuntimeLock(helper, () -> {
            SkriptRuntime runtime = SkriptRuntime.instance();
            runtime.clearScripts();
            runtime.loadFromResource("skript/gametest/prevent_brewing_consume_marks_block.sk");

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
                "skript/gametest/text_display_add_shadow_names_entity.sk",
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
                "skript/gametest/text_display_make_see_through_names_entity.sk",
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
            runtime.loadFromResource("skript/gametest/fishing_remove_lure_names_hook.sk");

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
            runtime.loadFromResource("skript/gametest/pull_hooked_entity_marks_block.sk");

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
                "skript/gametest/make_responsive_names_entity.sk",
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
                "skript/gametest/equippable_damage_effect_renames_item.sk",
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
                "skript/gametest/equippable_dispensable_effect_renames_item.sk",
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
                "skript/gametest/equippable_interact_effect_renames_item.sk",
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
                "skript/gametest/equippable_shearable_effect_renames_item.sk",
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
                "skript/gametest/equippable_swappable_effect_renames_item.sk",
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
            runtime.loadFromResource("skript/gametest/generate_loot_marks_block.sk");

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
            boolean foundSaddle = false;
            StringBuilder contents = new StringBuilder();
            for (int slot = 0; slot < chest.getContainerSize(); slot++) {
                if (!chest.getItem(slot).isEmpty()) {
                    if (contents.length() > 0) {
                        contents.append(", ");
                    }
                    contents.append(chest.getItem(slot).getItem());
                }
                if (chest.getItem(slot).is(Items.SADDLE)) {
                    foundSaddle = true;
                    break;
                }
            }
            helper.assertTrue(foundSaddle, Component.literal("Expected generate-loot effect to insert simple dungeon loot into the chest. Contents: " + contents));
            runtime.clearScripts();
        });
    }

    @GameTest
    public void rotateEffectExecutesRealScript(GameTestHelper helper) {
        Display.TextDisplay textDisplay = createTextDisplay(helper, (byte) 0);
        assertUseEntityScriptSetsMarker(
                helper,
                "skript/gametest/rotate_display_marks_block.sk",
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
                "skript/gametest/apply_potion_names_entity.sk",
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
                "skript/gametest/poison_effect_names_entity.sk",
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
                "skript/gametest/cure_poison_marks_block.sk",
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
                "skript/gametest/potion_ambient_effect_names_entity.sk",
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
                "skript/gametest/potion_icon_effect_names_entity.sk",
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
                "skript/gametest/potion_particles_effect_names_entity.sk",
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
                "skript/gametest/potion_infinite_effect_marks_block.sk",
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
            runtime.loadFromResource("skript/gametest/register_custom_tag_renames_item.sk");

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
            runtime.loadFromResource("skript/gametest/play_bone_meal_effect_marks_block.sk");

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
    public void useItemBridgeExecutesLoadedScript(GameTestHelper helper) {
        runWithRuntimeLock(helper, () -> {
            SkriptRuntime runtime = SkriptRuntime.instance();
            runtime.clearScripts();
            runtime.loadFromResource("skript/gametest/use_item_renames_item.sk");

            BlockPos playerMarkerAbsolute = helper.absolutePos(new BlockPos(4, 1, 0));
            helper.getLevel().setBlockAndUpdate(playerMarkerAbsolute, Blocks.AIR.defaultBlockState());

            ServerPlayer player = helper.makeMockServerPlayerInLevel();
            player.setGameMode(GameType.CREATIVE);
            player.teleportTo(playerMarkerAbsolute.getX() + 0.5D, playerMarkerAbsolute.getY() + 1.0D, playerMarkerAbsolute.getZ() + 0.5D);
            player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.STICK));

            InteractionResult result = UseItemCallback.EVENT.invoker().interact(
                    player,
                    helper.getLevel(),
                    InteractionHand.MAIN_HAND
            );
            helper.assertTrue(
                    result == InteractionResult.PASS,
                    Component.literal("Expected use item bridge to keep Fabric callback flow in PASS state.")
            );
            ItemStack held = player.getItemInHand(InteractionHand.MAIN_HAND);
            helper.assertTrue(
                    held.getCustomName() != null && "used item".equals(held.getCustomName().getString()),
                    Component.literal("Expected use item bridge to resolve event-item inside a real .sk file.")
            );
            helper.assertTrue(
                    helper.getLevel().getBlockState(playerMarkerAbsolute).is(Blocks.BLUE_WOOL),
                    Component.literal("Expected use item bridge to resolve event-player inside a real .sk file.")
            );
            runtime.clearScripts();
        });
    }

    @GameTest
    public void eventPayloadExpressionsParseAndResolve(GameTestHelper helper) {
        BlockPos pos = helper.absolutePos(new BlockPos(3, 1, 0));
        helper.getLevel().setBlockAndUpdate(pos, Blocks.STONE.defaultBlockState());

        ServerPlayer player = helper.makeMockServerPlayerInLevel();
        player.setGameMode(GameType.CREATIVE);

        @SuppressWarnings("unchecked")
        Expression<? extends ServerPlayer> eventPlayerExpression = new SkriptParser(
                "event-player",
                SkriptParser.ALL_FLAGS,
                ParseContext.DEFAULT
        ).parseExpression(new Class[]{ServerPlayer.class});
        helper.assertTrue(
                eventPlayerExpression != null,
                Component.literal("Expected event-player expression to parse from registry.")
        );
        if (eventPlayerExpression == null) {
            throw new IllegalStateException("event-player expression did not parse");
        }

        ServerPlayer resolvedPlayer = eventPlayerExpression.getSingle(new org.skriptlang.skript.lang.event.SkriptEvent(
                null,
                helper.getLevel().getServer(),
                helper.getLevel(),
                player
        ));
        helper.assertTrue(
                resolvedPlayer == player,
                Component.literal("event-player expression should resolve the event player.")
        );

        @SuppressWarnings("unchecked")
        Expression<? extends FabricBlock> eventBlockExpression = new SkriptParser(
                "event-block",
                SkriptParser.ALL_FLAGS,
                ParseContext.DEFAULT
        ).parseExpression(new Class[]{FabricBlock.class});
        helper.assertTrue(
                eventBlockExpression != null,
                Component.literal("Expected event-block expression to parse from registry.")
        );
        if (eventBlockExpression == null) {
            throw new IllegalStateException("event-block expression did not parse");
        }

        FabricBlockBreakHandle handle = new FabricBlockBreakHandle(
                helper.getLevel(),
                player,
                pos,
                helper.getLevel().getBlockState(pos),
                helper.getLevel().getBlockEntity(pos)
        );
        FabricBlock resolvedBlock = eventBlockExpression.getSingle(new org.skriptlang.skript.lang.event.SkriptEvent(
                handle,
                helper.getLevel().getServer(),
                helper.getLevel(),
                player
        ));
        helper.assertTrue(
                resolvedBlock != null && resolvedBlock.position().equals(pos),
                Component.literal("event-block expression should resolve the broken block position.")
        );

        ArmorStand armorStand = new ArmorStand(helper.getLevel(), pos.getX() + 0.5D, pos.getY(), pos.getZ() + 0.5D);
        helper.getLevel().addFreshEntity(armorStand);

        @SuppressWarnings("unchecked")
        Expression<? extends net.minecraft.world.entity.Entity> eventEntityExpression = new SkriptParser(
                "event-entity",
                SkriptParser.ALL_FLAGS,
                ParseContext.DEFAULT
        ).parseExpression(new Class[]{net.minecraft.world.entity.Entity.class});
        helper.assertTrue(
                eventEntityExpression != null,
                Component.literal("Expected event-entity expression to parse from registry.")
        );
        if (eventEntityExpression == null) {
            throw new IllegalStateException("event-entity expression did not parse");
        }

        net.minecraft.world.entity.Entity resolvedEntity = eventEntityExpression.getSingle(new org.skriptlang.skript.lang.event.SkriptEvent(
                new FabricUseEntityHandle(
                        helper.getLevel(),
                        player,
                        InteractionHand.MAIN_HAND,
                        armorStand,
                        new EntityHitResult(armorStand)
                ),
                helper.getLevel().getServer(),
                helper.getLevel(),
                player
        ));
        helper.assertTrue(
                resolvedEntity == armorStand,
                Component.literal("event-entity expression should resolve the interacted entity.")
        );

        @SuppressWarnings("unchecked")
        Expression<? extends ItemStack> eventItemExpression = new SkriptParser(
                "event-item",
                SkriptParser.ALL_FLAGS,
                ParseContext.DEFAULT
        ).parseExpression(new Class[]{ItemStack.class});
        helper.assertTrue(
                eventItemExpression != null,
                Component.literal("Expected event-item expression to parse from registry.")
        );
        if (eventItemExpression == null) {
            throw new IllegalStateException("event-item expression did not parse");
        }

        player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.APPLE));
        ItemStack resolvedItem = eventItemExpression.getSingle(new org.skriptlang.skript.lang.event.SkriptEvent(
                new FabricUseItemHandle(
                        helper.getLevel(),
                        player,
                        InteractionHand.MAIN_HAND
                ),
                helper.getLevel().getServer(),
                helper.getLevel(),
                player
        ));
        helper.assertTrue(
                resolvedItem != null && resolvedItem.is(Items.APPLE),
                Component.literal("event-item expression should resolve the used item stack.")
        );

        net.minecraft.world.entity.Entity attackedEntity = eventEntityExpression.getSingle(new org.skriptlang.skript.lang.event.SkriptEvent(
                new FabricAttackEntityHandle(
                        helper.getLevel(),
                        player,
                        InteractionHand.MAIN_HAND,
                        armorStand,
                        new EntityHitResult(armorStand)
                ),
                helper.getLevel().getServer(),
                helper.getLevel(),
                player
        ));
        helper.assertTrue(
                attackedEntity == armorStand,
                Component.literal("event-entity expression should also resolve attacked entities.")
        );

        @SuppressWarnings("unchecked")
        Expression<? extends DamageSource> eventDamageSourceExpression = new SkriptParser(
                "event-damage source",
                SkriptParser.ALL_FLAGS,
                ParseContext.DEFAULT
        ).parseExpression(new Class[]{DamageSource.class});
        helper.assertTrue(
                eventDamageSourceExpression != null,
                Component.literal("Expected event-damage source expression to parse from registry.")
        );
        if (eventDamageSourceExpression == null) {
            throw new IllegalStateException("event-damage source expression did not parse");
        }

        DamageSource expectedDamageSource = helper.getLevel().damageSources().playerAttack(player);
        DamageSource resolvedDamageSource = eventDamageSourceExpression.getSingle(new org.skriptlang.skript.lang.event.SkriptEvent(
                new FabricDamageHandle(
                        helper.getLevel(),
                        armorStand,
                        expectedDamageSource,
                        2.0F
                ),
                helper.getLevel().getServer(),
                helper.getLevel(),
                player
        ));
        helper.assertTrue(
                resolvedDamageSource == expectedDamageSource,
                Component.literal("event-damage source expression should resolve the current Mojang damage source.")
        );

        @SuppressWarnings("unchecked")
        Expression<? extends net.minecraft.world.entity.Entity> causingEntityExpression = new SkriptParser(
                "causing entity of event-damage source",
                SkriptParser.ALL_FLAGS,
                ParseContext.DEFAULT
        ).parseExpression(new Class[]{net.minecraft.world.entity.Entity.class});
        helper.assertTrue(
                causingEntityExpression != null,
                Component.literal("Expected causing entity expression to parse from registry.")
        );
        if (causingEntityExpression == null) {
            throw new IllegalStateException("causing entity expression did not parse");
        }
        Cow causingAttacker = createCow(helper, false);
        net.minecraft.world.entity.Entity resolvedCausingEntity = causingEntityExpression.getSingle(new org.skriptlang.skript.lang.event.SkriptEvent(
                new FabricDamageHandle(
                        helper.getLevel(),
                        armorStand,
                        helper.getLevel().damageSources().mobAttack(causingAttacker),
                        2.0F
                ),
                helper.getLevel().getServer(),
                helper.getLevel(),
                player
        ));
        helper.assertTrue(
                resolvedCausingEntity == causingAttacker,
                Component.literal("Expected causing entity expression to resolve the current damage source attacker.")
        );

        @SuppressWarnings("unchecked")
        Expression<? extends net.minecraft.world.entity.Entity> directEntityExpression = new SkriptParser(
                "direct entity of event-damage source",
                SkriptParser.ALL_FLAGS,
                ParseContext.DEFAULT
        ).parseExpression(new Class[]{net.minecraft.world.entity.Entity.class});
        helper.assertTrue(
                directEntityExpression != null,
                Component.literal("Expected direct entity expression to parse from registry.")
        );
        if (directEntityExpression == null) {
            throw new IllegalStateException("direct entity expression did not parse");
        }
        ArmorStand damageProjectile = new ArmorStand(helper.getLevel(), 2.5D, 1.0D, 0.5D);
        helper.getLevel().addFreshEntity(damageProjectile);
        net.minecraft.world.entity.Entity resolvedDirectEntity = directEntityExpression.getSingle(new org.skriptlang.skript.lang.event.SkriptEvent(
                new FabricDamageHandle(
                        helper.getLevel(),
                        armorStand,
                        helper.getLevel().damageSources().thrown(damageProjectile, player),
                        2.0F
                ),
                helper.getLevel().getServer(),
                helper.getLevel(),
                player
        ));
        helper.assertTrue(
                resolvedDirectEntity == damageProjectile,
                Component.literal("Expected direct entity expression to resolve the current damage projectile.")
        );

        @SuppressWarnings("unchecked")
        Expression<? extends FabricLocation> sourceLocationExpression = new SkriptParser(
                "source location of event-damage source",
                SkriptParser.ALL_FLAGS,
                ParseContext.DEFAULT
        ).parseExpression(new Class[]{FabricLocation.class});
        helper.assertTrue(
                sourceLocationExpression != null,
                Component.literal("Expected source location expression to parse from registry.")
        );
        if (sourceLocationExpression == null) {
            throw new IllegalStateException("source location expression did not parse");
        }
        FabricLocation resolvedSourceLocation = sourceLocationExpression.getSingle(new org.skriptlang.skript.lang.event.SkriptEvent(
                new FabricDamageHandle(
                        helper.getLevel(),
                        armorStand,
                        helper.getLevel().damageSources().thrown(damageProjectile, player),
                        2.0F
                ),
                helper.getLevel().getServer(),
                helper.getLevel(),
                player
        ));
        helper.assertTrue(
                resolvedSourceLocation != null
                        && BlockPos.containing(resolvedSourceLocation.position()).equals(BlockPos.containing(damageProjectile.position())),
                Component.literal("Expected source location expression to resolve the direct entity position.")
        );

        @SuppressWarnings("unchecked")
        Expression<? extends FabricLocation> damageLocationExpression = new SkriptParser(
                "damage location of event-damage source",
                SkriptParser.ALL_FLAGS,
                ParseContext.DEFAULT
        ).parseExpression(new Class[]{FabricLocation.class});
        helper.assertTrue(
                damageLocationExpression != null,
                Component.literal("Expected damage location expression to parse from registry.")
        );
        if (damageLocationExpression == null) {
            throw new IllegalStateException("damage location expression did not parse");
        }
        Vec3 explicitDamagePosition = Vec3.atCenterOf(helper.absolutePos(new BlockPos(14, 1, 0)));
        FabricLocation resolvedDamageLocation = damageLocationExpression.getSingle(new org.skriptlang.skript.lang.event.SkriptEvent(
                new FabricDamageHandle(
                        helper.getLevel(),
                        armorStand,
                        helper.getLevel().damageSources().badRespawnPointExplosion(explicitDamagePosition),
                        2.0F
                ),
                helper.getLevel().getServer(),
                helper.getLevel(),
                player
        ));
        helper.assertTrue(
                resolvedDamageLocation != null
                        && BlockPos.containing(resolvedDamageLocation.position()).equals(BlockPos.containing(explicitDamagePosition)),
                Component.literal("Expected damage location expression to resolve the explicit damage source position.")
        );

        @SuppressWarnings("unchecked")
        Expression<? extends Float> foodExhaustionExpression = new SkriptParser(
                "food exhaustion of event-damage source",
                SkriptParser.ALL_FLAGS,
                ParseContext.DEFAULT
        ).parseExpression(new Class[]{Float.class});
        helper.assertTrue(
                foodExhaustionExpression != null,
                Component.literal("Expected food exhaustion expression to parse from registry.")
        );
        if (foodExhaustionExpression == null) {
            throw new IllegalStateException("food exhaustion expression did not parse");
        }
        DamageSource foodExhaustionSource = helper.getLevel().damageSources().playerAttack(player);
        Float resolvedFoodExhaustion = foodExhaustionExpression.getSingle(new org.skriptlang.skript.lang.event.SkriptEvent(
                new FabricDamageHandle(
                        helper.getLevel(),
                        armorStand,
                        foodExhaustionSource,
                        2.0F
                ),
                helper.getLevel().getServer(),
                helper.getLevel(),
                player
        ));
        helper.assertTrue(
                resolvedFoodExhaustion != null && Float.compare(resolvedFoodExhaustion, foodExhaustionSource.getFoodExhaustion()) == 0,
                Component.literal("Expected food exhaustion expression to resolve the current damage source food exhaustion.")
        );
        helper.succeed();
    }

    @GameTest
    public void textDisplayExpressionsParseResolveAndChange(GameTestHelper helper) {
        Display.TextDisplay textDisplay = createTextDisplay(helper, Display.TextDisplay.FLAG_ALIGN_LEFT);
        PrivateEntityAccess.setTextDisplayLineWidth(textDisplay, 300);
        PrivateEntityAccess.setTextDisplayOpacity(textDisplay, (byte) -128);

        ServerPlayer player = helper.makeMockServerPlayerInLevel();
        player.setGameMode(GameType.CREATIVE);

        var event = new org.skriptlang.skript.lang.event.SkriptEvent(
                new FabricUseEntityHandle(
                        helper.getLevel(),
                        player,
                        InteractionHand.MAIN_HAND,
                        textDisplay,
                        new EntityHitResult(textDisplay)
                ),
                helper.getLevel().getServer(),
                helper.getLevel(),
                player
        );

        @SuppressWarnings("unchecked")
        Expression<? extends Display.TextDisplay.Align> alignmentExpression = new SkriptParser(
                "text alignment of event-entity",
                SkriptParser.ALL_FLAGS,
                ParseContext.DEFAULT
        ).parseExpression(new Class[]{Display.TextDisplay.Align.class});
        helper.assertTrue(
                alignmentExpression != null,
                Component.literal("Expected text alignment expression to parse from registry.")
        );
        if (alignmentExpression == null) {
            throw new IllegalStateException("text alignment expression did not parse");
        }
        helper.assertTrue(
                alignmentExpression.getSingle(event) == Display.TextDisplay.Align.LEFT,
                Component.literal("Expected text alignment expression to resolve the current text display alignment.")
        );
        alignmentExpression.change(event, new Object[]{Display.TextDisplay.Align.RIGHT}, ChangeMode.SET);
        helper.assertTrue(
                PrivateEntityAccess.textDisplayAlignment(textDisplay) == Display.TextDisplay.Align.RIGHT,
                Component.literal("Expected text alignment expression to apply SET changes.")
        );
        alignmentExpression.change(event, null, ChangeMode.RESET);
        helper.assertTrue(
                PrivateEntityAccess.textDisplayAlignment(textDisplay) == Display.TextDisplay.Align.CENTER,
                Component.literal("Expected text alignment expression to reset to center.")
        );

        @SuppressWarnings("unchecked")
        Expression<? extends Integer> lineWidthExpression = new SkriptParser(
                "line width of event-entity",
                SkriptParser.ALL_FLAGS,
                ParseContext.DEFAULT
        ).parseExpression(new Class[]{Integer.class});
        helper.assertTrue(
                lineWidthExpression != null,
                Component.literal("Expected line width expression to parse from registry.")
        );
        if (lineWidthExpression == null) {
            throw new IllegalStateException("line width expression did not parse");
        }
        PrivateEntityAccess.setTextDisplayLineWidth(textDisplay, 300);
        helper.assertTrue(
                Integer.valueOf(300).equals(lineWidthExpression.getSingle(event)),
                Component.literal("Expected line width expression to resolve the current line width.")
        );
        lineWidthExpression.change(event, new Object[]{20}, ChangeMode.ADD);
        helper.assertTrue(
                PrivateEntityAccess.textDisplayLineWidth(textDisplay) == 320,
                Component.literal("Expected line width expression to apply ADD changes.")
        );
        lineWidthExpression.change(event, new Object[]{50}, ChangeMode.REMOVE);
        helper.assertTrue(
                PrivateEntityAccess.textDisplayLineWidth(textDisplay) == 270,
                Component.literal("Expected line width expression to apply REMOVE changes.")
        );
        lineWidthExpression.change(event, null, ChangeMode.RESET);
        helper.assertTrue(
                PrivateEntityAccess.textDisplayLineWidth(textDisplay) == 200,
                Component.literal("Expected line width expression to reset to the vanilla default.")
        );

        @SuppressWarnings("unchecked")
        Expression<? extends Integer> opacityExpression = new SkriptParser(
                "text opacity of event-entity",
                SkriptParser.ALL_FLAGS,
                ParseContext.DEFAULT
        ).parseExpression(new Class[]{Integer.class});
        helper.assertTrue(
                opacityExpression != null,
                Component.literal("Expected text opacity expression to parse from registry.")
        );
        if (opacityExpression == null) {
            throw new IllegalStateException("text opacity expression did not parse");
        }
        PrivateEntityAccess.setTextDisplayOpacity(textDisplay, (byte) -128);
        helper.assertTrue(
                Integer.valueOf(128).equals(opacityExpression.getSingle(event)),
                Component.literal("Expected text opacity expression to resolve the current unsigned opacity.")
        );
        opacityExpression.change(event, new Object[]{4}, ChangeMode.SET);
        helper.assertTrue(
                Byte.toUnsignedInt(PrivateEntityAccess.textDisplayOpacity(textDisplay)) == 4,
                Component.literal("Expected text opacity expression to apply SET changes.")
        );
        opacityExpression.change(event, new Object[]{20}, ChangeMode.ADD);
        helper.assertTrue(
                Byte.toUnsignedInt(PrivateEntityAccess.textDisplayOpacity(textDisplay)) == 24,
                Component.literal("Expected text opacity expression to apply ADD changes.")
        );
        opacityExpression.change(event, new Object[]{30}, ChangeMode.REMOVE);
        helper.assertTrue(
                Byte.toUnsignedInt(PrivateEntityAccess.textDisplayOpacity(textDisplay)) == 0,
                Component.literal("Expected text opacity expression to clamp REMOVE changes at zero.")
        );
        opacityExpression.change(event, null, ChangeMode.RESET);
        helper.assertTrue(
                Byte.toUnsignedInt(PrivateEntityAccess.textDisplayOpacity(textDisplay)) == 255,
                Component.literal("Expected text opacity expression to reset to fully opaque.")
        );

        Condition alignmentCondition = Condition.parse("text alignment of event-entity is \"left aligned\"", null);
        helper.assertTrue(
                alignmentCondition != null,
                Component.literal("Expected text alignment comparison condition to parse.")
        );
        if (alignmentCondition == null) {
            throw new IllegalStateException("text alignment comparison condition did not parse");
        }
        PrivateEntityAccess.setTextDisplayAlignment(textDisplay, Display.TextDisplay.Align.LEFT);
        helper.assertTrue(
                alignmentCondition.check(event),
                Component.literal("Expected text alignment comparison condition to evaluate true.")
        );

        Condition lineWidthCondition = Condition.parse("line width of event-entity is \"300\"", null);
        helper.assertTrue(
                lineWidthCondition != null,
                Component.literal("Expected line width comparison condition to parse.")
        );
        if (lineWidthCondition == null) {
            throw new IllegalStateException("line width comparison condition did not parse");
        }
        PrivateEntityAccess.setTextDisplayLineWidth(textDisplay, 300);
        helper.assertTrue(
                lineWidthCondition.check(event),
                Component.literal("Expected line width comparison condition to evaluate true.")
        );

        Condition opacityCondition = Condition.parse("text opacity of event-entity is \"128\"", null);
        helper.assertTrue(
                opacityCondition != null,
                Component.literal("Expected text opacity comparison condition to parse.")
        );
        if (opacityCondition == null) {
            throw new IllegalStateException("text opacity comparison condition did not parse");
        }
        PrivateEntityAccess.setTextDisplayOpacity(textDisplay, (byte) -128);
        helper.assertTrue(
                opacityCondition.check(event),
                Component.literal("Expected text opacity comparison condition to evaluate true.")
        );
        helper.succeed();
    }

    @GameTest
    public void genericDisplayExpressionsParseResolveAndChange(GameTestHelper helper) {
        Display.TextDisplay textDisplay = createTextDisplay(helper, (byte) 0);
        PrivateEntityAccess.setDisplayBillboardConstraints(textDisplay, Display.BillboardConstraints.CENTER);
        PrivateEntityAccess.setDisplayBrightnessOverride(textDisplay, new Brightness(4, 7));
        PrivateEntityAccess.setDisplayHeight(textDisplay, 2.5F);
        PrivateEntityAccess.setDisplayWidth(textDisplay, 1.5F);
        PrivateEntityAccess.setDisplayShadowRadius(textDisplay, 1.75F);
        PrivateEntityAccess.setDisplayShadowStrength(textDisplay, 0.5F);
        PrivateEntityAccess.setDisplayViewRange(textDisplay, 2.5F);

        ServerPlayer player = helper.makeMockServerPlayerInLevel();
        player.setGameMode(GameType.CREATIVE);

        var event = new org.skriptlang.skript.lang.event.SkriptEvent(
                new FabricUseEntityHandle(
                        helper.getLevel(),
                        player,
                        InteractionHand.MAIN_HAND,
                        textDisplay,
                        new EntityHitResult(textDisplay)
                ),
                helper.getLevel().getServer(),
                helper.getLevel(),
                player
        );

        @SuppressWarnings("unchecked")
        Expression<? extends Display.BillboardConstraints> billboardExpression = new SkriptParser(
                "billboard of event-entity",
                SkriptParser.ALL_FLAGS,
                ParseContext.DEFAULT
        ).parseExpression(new Class[]{Display.BillboardConstraints.class});
        helper.assertTrue(billboardExpression != null, Component.literal("Expected billboard expression to parse from registry."));
        if (billboardExpression == null) {
            throw new IllegalStateException("billboard expression did not parse");
        }
        helper.assertTrue(
                billboardExpression.getSingle(event) == Display.BillboardConstraints.CENTER,
                Component.literal("Expected billboard expression to resolve the current billboard setting.")
        );
        billboardExpression.change(event, new Object[]{Display.BillboardConstraints.VERTICAL}, ChangeMode.SET);
        helper.assertTrue(
                PrivateEntityAccess.displayBillboardConstraints(textDisplay) == Display.BillboardConstraints.VERTICAL,
                Component.literal("Expected billboard expression to apply SET changes.")
        );
        billboardExpression.change(event, null, ChangeMode.RESET);
        helper.assertTrue(
                PrivateEntityAccess.displayBillboardConstraints(textDisplay) == Display.BillboardConstraints.FIXED,
                Component.literal("Expected billboard expression to reset to fixed.")
        );

        @SuppressWarnings("unchecked")
        Expression<? extends Float> viewRangeExpression = new SkriptParser(
                "view range of event-entity",
                SkriptParser.ALL_FLAGS,
                ParseContext.DEFAULT
        ).parseExpression(new Class[]{Float.class});
        helper.assertTrue(viewRangeExpression != null, Component.literal("Expected view range expression to parse from registry."));
        if (viewRangeExpression == null) {
            throw new IllegalStateException("view range expression did not parse");
        }
        PrivateEntityAccess.setDisplayViewRange(textDisplay, 2.5F);
        helper.assertTrue(
                Math.abs(viewRangeExpression.getSingle(event) - 2.5F) < 0.0001F,
                Component.literal("Expected view range expression to resolve the current view range.")
        );
        viewRangeExpression.change(event, new Object[]{0.5F}, ChangeMode.ADD);
        helper.assertTrue(
                Math.abs(PrivateEntityAccess.displayViewRange(textDisplay) - 3.0F) < 0.0001F,
                Component.literal("Expected view range expression to apply ADD changes.")
        );
        viewRangeExpression.change(event, new Object[]{1.0F}, ChangeMode.REMOVE);
        helper.assertTrue(
                Math.abs(PrivateEntityAccess.displayViewRange(textDisplay) - 2.0F) < 0.0001F,
                Component.literal("Expected view range expression to apply REMOVE changes.")
        );
        viewRangeExpression.change(event, null, ChangeMode.RESET);
        helper.assertTrue(
                Math.abs(PrivateEntityAccess.displayViewRange(textDisplay) - 1.0F) < 0.0001F,
                Component.literal("Expected view range expression to reset to the vanilla default.")
        );

        @SuppressWarnings("unchecked")
        Expression<? extends Float> heightExpression = new SkriptParser(
                "display height of event-entity",
                SkriptParser.ALL_FLAGS,
                ParseContext.DEFAULT
        ).parseExpression(new Class[]{Float.class});
        helper.assertTrue(heightExpression != null, Component.literal("Expected display height expression to parse from registry."));
        if (heightExpression == null) {
            throw new IllegalStateException("display height expression did not parse");
        }
        PrivateEntityAccess.setDisplayHeight(textDisplay, 2.5F);
        helper.assertTrue(
                Math.abs(heightExpression.getSingle(event) - 2.5F) < 0.0001F,
                Component.literal("Expected display height expression to resolve the current display height.")
        );
        heightExpression.change(event, new Object[]{1.0F}, ChangeMode.ADD);
        helper.assertTrue(
                Math.abs(PrivateEntityAccess.displayHeight(textDisplay) - 3.5F) < 0.0001F,
                Component.literal("Expected display height expression to apply ADD changes.")
        );
        heightExpression.change(event, null, ChangeMode.RESET);
        helper.assertTrue(
                Math.abs(PrivateEntityAccess.displayHeight(textDisplay)) < 0.0001F,
                Component.literal("Expected display height expression to reset to zero.")
        );

        @SuppressWarnings("unchecked")
        Expression<? extends Float> widthExpression = new SkriptParser(
                "display width of event-entity",
                SkriptParser.ALL_FLAGS,
                ParseContext.DEFAULT
        ).parseExpression(new Class[]{Float.class});
        helper.assertTrue(widthExpression != null, Component.literal("Expected display width expression to parse from registry."));
        if (widthExpression == null) {
            throw new IllegalStateException("display width expression did not parse");
        }
        PrivateEntityAccess.setDisplayWidth(textDisplay, 1.5F);
        helper.assertTrue(
                Math.abs(widthExpression.getSingle(event) - 1.5F) < 0.0001F,
                Component.literal("Expected display width expression to resolve the current display width.")
        );
        widthExpression.change(event, new Object[]{0.5F}, ChangeMode.ADD);
        helper.assertTrue(
                Math.abs(PrivateEntityAccess.displayWidth(textDisplay) - 2.0F) < 0.0001F,
                Component.literal("Expected display width expression to apply ADD changes.")
        );

        @SuppressWarnings("unchecked")
        Expression<? extends Float> shadowRadiusExpression = new SkriptParser(
                "shadow radius of event-entity",
                SkriptParser.ALL_FLAGS,
                ParseContext.DEFAULT
        ).parseExpression(new Class[]{Float.class});
        helper.assertTrue(shadowRadiusExpression != null, Component.literal("Expected shadow radius expression to parse from registry."));
        if (shadowRadiusExpression == null) {
            throw new IllegalStateException("shadow radius expression did not parse");
        }
        PrivateEntityAccess.setDisplayShadowRadius(textDisplay, 1.75F);
        helper.assertTrue(
                Math.abs(shadowRadiusExpression.getSingle(event) - 1.75F) < 0.0001F,
                Component.literal("Expected shadow radius expression to resolve the current shadow radius.")
        );
        shadowRadiusExpression.change(event, new Object[]{0.25F}, ChangeMode.ADD);
        helper.assertTrue(
                Math.abs(PrivateEntityAccess.displayShadowRadius(textDisplay) - 2.0F) < 0.0001F,
                Component.literal("Expected shadow radius expression to apply ADD changes.")
        );
        shadowRadiusExpression.change(event, null, ChangeMode.RESET);
        helper.assertTrue(
                Math.abs(PrivateEntityAccess.displayShadowRadius(textDisplay)) < 0.0001F,
                Component.literal("Expected shadow radius expression to reset to zero.")
        );

        @SuppressWarnings("unchecked")
        Expression<? extends Float> shadowStrengthExpression = new SkriptParser(
                "shadow strength of event-entity",
                SkriptParser.ALL_FLAGS,
                ParseContext.DEFAULT
        ).parseExpression(new Class[]{Float.class});
        helper.assertTrue(shadowStrengthExpression != null, Component.literal("Expected shadow strength expression to parse from registry."));
        if (shadowStrengthExpression == null) {
            throw new IllegalStateException("shadow strength expression did not parse");
        }
        PrivateEntityAccess.setDisplayShadowStrength(textDisplay, 0.5F);
        helper.assertTrue(
                Math.abs(shadowStrengthExpression.getSingle(event) - 0.5F) < 0.0001F,
                Component.literal("Expected shadow strength expression to resolve the current shadow strength.")
        );
        shadowStrengthExpression.change(event, new Object[]{0.25F}, ChangeMode.ADD);
        helper.assertTrue(
                Math.abs(PrivateEntityAccess.displayShadowStrength(textDisplay) - 0.75F) < 0.0001F,
                Component.literal("Expected shadow strength expression to apply ADD changes.")
        );
        shadowStrengthExpression.change(event, null, ChangeMode.RESET);
        helper.assertTrue(
                Math.abs(PrivateEntityAccess.displayShadowStrength(textDisplay) - 1.0F) < 0.0001F,
                Component.literal("Expected shadow strength expression to reset to one.")
        );

        @SuppressWarnings("unchecked")
        Expression<? extends Integer> skyLightExpression = new SkriptParser(
                "sky light override of event-entity",
                SkriptParser.ALL_FLAGS,
                ParseContext.DEFAULT
        ).parseExpression(new Class[]{Integer.class});
        helper.assertTrue(skyLightExpression != null, Component.literal("Expected sky light override expression to parse from registry."));
        if (skyLightExpression == null) {
            throw new IllegalStateException("sky light override expression did not parse");
        }
        PrivateEntityAccess.setDisplayBrightnessOverride(textDisplay, new Brightness(4, 7));
        helper.assertTrue(
                Integer.valueOf(7).equals(skyLightExpression.getSingle(event)),
                Component.literal("Expected sky light override expression to resolve the current sky light value.")
        );
        skyLightExpression.change(event, new Object[]{2}, ChangeMode.ADD);
        helper.assertTrue(
                PrivateEntityAccess.displayBrightnessOverride(textDisplay).sky() == 9,
                Component.literal("Expected sky light override expression to apply ADD changes.")
        );
        skyLightExpression.change(event, new Object[]{20}, ChangeMode.REMOVE);
        helper.assertTrue(
                PrivateEntityAccess.displayBrightnessOverride(textDisplay).sky() == 0,
                Component.literal("Expected sky light override expression to clamp REMOVE changes at zero.")
        );

        @SuppressWarnings("unchecked")
        Expression<? extends Integer> brightnessExpression = new SkriptParser(
                "brightness override of event-entity",
                SkriptParser.ALL_FLAGS,
                ParseContext.DEFAULT
        ).parseExpression(new Class[]{Integer.class});
        helper.assertTrue(brightnessExpression != null, Component.literal("Expected brightness override expression to parse from registry."));
        if (brightnessExpression == null) {
            throw new IllegalStateException("brightness override expression did not parse");
        }
        PrivateEntityAccess.setDisplayBrightnessOverride(textDisplay, new Brightness(3, 6));
        Integer[] brightnessValues = brightnessExpression.getAll(event);
        helper.assertTrue(
                brightnessValues.length == 2 && brightnessValues[0] == 3 && brightnessValues[1] == 6,
                Component.literal("Expected brightness override expression to resolve both block and sky light values.")
        );
        brightnessExpression.change(event, new Object[]{9}, ChangeMode.SET);
        helper.assertTrue(
                PrivateEntityAccess.displayBrightnessOverride(textDisplay).block() == 9
                        && PrivateEntityAccess.displayBrightnessOverride(textDisplay).sky() == 9,
                Component.literal("Expected brightness override expression to apply SET changes to both light channels.")
        );
        brightnessExpression.change(event, null, ChangeMode.RESET);
        helper.assertTrue(
                PrivateEntityAccess.displayBrightnessOverride(textDisplay) == null,
                Component.literal("Expected brightness override expression to clear the override on RESET.")
        );

        Condition billboardCondition = Condition.parse("billboard of event-entity is \"center\"", null);
        helper.assertTrue(billboardCondition != null, Component.literal("Expected billboard comparison condition to parse."));
        if (billboardCondition == null) {
            throw new IllegalStateException("billboard comparison condition did not parse");
        }
        PrivateEntityAccess.setDisplayBillboardConstraints(textDisplay, Display.BillboardConstraints.CENTER);
        helper.assertTrue(
                billboardCondition.check(event),
                Component.literal("Expected billboard comparison condition to evaluate true.")
        );

        Condition viewRangeCondition = Condition.parse("view range of event-entity is \"2.5\"", null);
        helper.assertTrue(viewRangeCondition != null, Component.literal("Expected view range comparison condition to parse."));
        if (viewRangeCondition == null) {
            throw new IllegalStateException("view range comparison condition did not parse");
        }
        PrivateEntityAccess.setDisplayViewRange(textDisplay, 2.5F);
        helper.assertTrue(
                viewRangeCondition.check(event),
                Component.literal("Expected view range comparison condition to evaluate true.")
        );
        helper.succeed();
    }

    @GameTest
    public void displayTimeExpressionsParseResolveAndChange(GameTestHelper helper) {
        Display.TextDisplay textDisplay = createTextDisplay(helper, (byte) 0);
        PrivateEntityAccess.setDisplayTransformationInterpolationDelay(textDisplay, 3);
        PrivateEntityAccess.setDisplayTransformationInterpolationDuration(textDisplay, 4);
        PrivateEntityAccess.setDisplayPosRotInterpolationDuration(textDisplay, 2);

        ServerPlayer player = helper.makeMockServerPlayerInLevel();
        player.setGameMode(GameType.CREATIVE);

        var event = new org.skriptlang.skript.lang.event.SkriptEvent(
                new FabricUseEntityHandle(
                        helper.getLevel(),
                        player,
                        InteractionHand.MAIN_HAND,
                        textDisplay,
                        new EntityHitResult(textDisplay)
                ),
                helper.getLevel().getServer(),
                helper.getLevel(),
                player
        );

        @SuppressWarnings("unchecked")
        Expression<? extends Timespan> interpolationDelayExpression = new SkriptParser(
                "interpolation delay of event-entity",
                SkriptParser.ALL_FLAGS,
                ParseContext.DEFAULT
        ).parseExpression(new Class[]{Timespan.class});
        helper.assertTrue(interpolationDelayExpression != null, Component.literal("Expected interpolation delay expression to parse from registry."));
        if (interpolationDelayExpression == null) {
            throw new IllegalStateException("interpolation delay expression did not parse");
        }
        helper.assertTrue(
                interpolationDelayExpression.getSingle(event).getAs(TimePeriod.TICK) == 3,
                Component.literal("Expected interpolation delay expression to resolve the current delay.")
        );
        interpolationDelayExpression.change(event, new Object[]{new Timespan(TimePeriod.TICK, 2)}, ChangeMode.ADD);
        helper.assertTrue(
                PrivateEntityAccess.displayTransformationInterpolationDelay(textDisplay) == 5,
                Component.literal("Expected interpolation delay expression to apply ADD changes.")
        );
        interpolationDelayExpression.change(event, null, ChangeMode.RESET);
        helper.assertTrue(
                PrivateEntityAccess.displayTransformationInterpolationDelay(textDisplay) == 0,
                Component.literal("Expected interpolation delay expression to reset to zero.")
        );

        @SuppressWarnings("unchecked")
        Expression<? extends Timespan> interpolationDurationExpression = new SkriptParser(
                "interpolation duration of event-entity",
                SkriptParser.ALL_FLAGS,
                ParseContext.DEFAULT
        ).parseExpression(new Class[]{Timespan.class});
        helper.assertTrue(interpolationDurationExpression != null, Component.literal("Expected interpolation duration expression to parse from registry."));
        if (interpolationDurationExpression == null) {
            throw new IllegalStateException("interpolation duration expression did not parse");
        }
        PrivateEntityAccess.setDisplayTransformationInterpolationDuration(textDisplay, 4);
        helper.assertTrue(
                interpolationDurationExpression.getSingle(event).getAs(TimePeriod.TICK) == 4,
                Component.literal("Expected interpolation duration expression to resolve the current duration.")
        );
        interpolationDurationExpression.change(event, new Object[]{new Timespan(TimePeriod.TICK, 3)}, ChangeMode.SET);
        helper.assertTrue(
                PrivateEntityAccess.displayTransformationInterpolationDuration(textDisplay) == 3,
                Component.literal("Expected interpolation duration expression to apply SET changes.")
        );

        @SuppressWarnings("unchecked")
        Expression<? extends Timespan> teleportDurationExpression = new SkriptParser(
                "teleport duration of event-entity",
                SkriptParser.ALL_FLAGS,
                ParseContext.DEFAULT
        ).parseExpression(new Class[]{Timespan.class});
        helper.assertTrue(teleportDurationExpression != null, Component.literal("Expected teleport duration expression to parse from registry."));
        if (teleportDurationExpression == null) {
            throw new IllegalStateException("teleport duration expression did not parse");
        }
        PrivateEntityAccess.setDisplayPosRotInterpolationDuration(textDisplay, 2);
        helper.assertTrue(
                teleportDurationExpression.getSingle(event).getAs(TimePeriod.TICK) == 2,
                Component.literal("Expected teleport duration expression to resolve the current teleport duration.")
        );
        teleportDurationExpression.change(event, new Object[]{new Timespan(TimePeriod.TICK, 10)}, ChangeMode.ADD);
        helper.assertTrue(
                PrivateEntityAccess.displayPosRotInterpolationDuration(textDisplay) == 12,
                Component.literal("Expected teleport duration expression to apply ADD changes.")
        );
        teleportDurationExpression.change(event, new Object[]{new Timespan(TimePeriod.TICK, 99)}, ChangeMode.SET);
        helper.assertTrue(
                PrivateEntityAccess.displayPosRotInterpolationDuration(textDisplay) == 59,
                Component.literal("Expected teleport duration expression to clamp SET changes to the vanilla maximum.")
        );
        teleportDurationExpression.change(event, null, ChangeMode.RESET);
        helper.assertTrue(
                PrivateEntityAccess.displayPosRotInterpolationDuration(textDisplay) == 0,
                Component.literal("Expected teleport duration expression to reset to zero.")
        );
        helper.succeed();
    }

    @GameTest
    public void displayTransformationExpressionsParseResolveAndChange(GameTestHelper helper) {
        Display.TextDisplay textDisplay = createTextDisplay(helper, (byte) 0);
        PrivateEntityAccess.setDisplayTransformation(
                textDisplay,
                new Transformation(
                        new Vector3f(1.25F, 2.5F, 3.75F),
                        new Quaternionf(0.1F, 0.2F, 0.3F, 0.4F),
                        new Vector3f(2.0F, 3.0F, 4.0F),
                        new Quaternionf(0.5F, 0.6F, 0.7F, 0.8F)
                )
        );

        ServerPlayer player = helper.makeMockServerPlayerInLevel();
        player.setGameMode(GameType.CREATIVE);

        var event = new org.skriptlang.skript.lang.event.SkriptEvent(
                new FabricUseEntityHandle(
                        helper.getLevel(),
                        player,
                        InteractionHand.MAIN_HAND,
                        textDisplay,
                        new EntityHitResult(textDisplay)
                ),
                helper.getLevel().getServer(),
                helper.getLevel(),
                player
        );

        @SuppressWarnings("unchecked")
        Expression<? extends Vec3> translationExpression = new SkriptParser(
                "transformation translation of event-entity",
                SkriptParser.ALL_FLAGS,
                ParseContext.DEFAULT
        ).parseExpression(new Class[]{Vec3.class});
        helper.assertTrue(translationExpression != null, Component.literal("Expected transformation translation expression to parse from registry."));
        if (translationExpression == null) {
            throw new IllegalStateException("transformation translation expression did not parse");
        }
        Vec3 translation = translationExpression.getSingle(event);
        helper.assertTrue(
                translation != null && Math.abs(translation.x - 1.25D) < 0.0001D && Math.abs(translation.y - 2.5D) < 0.0001D
                        && Math.abs(translation.z - 3.75D) < 0.0001D,
                Component.literal("Expected transformation translation expression to resolve the current translation.")
        );
        translationExpression.change(event, new Object[]{new Vec3(5.0D, 6.0D, 7.0D)}, ChangeMode.SET);
        Transformation translationChange = PrivateEntityAccess.displayTransformation(textDisplay);
        helper.assertTrue(
                Math.abs(translationChange.getTranslation().x - 5.0F) < 0.0001F
                        && Math.abs(translationChange.getTranslation().y - 6.0F) < 0.0001F
                        && Math.abs(translationChange.getTranslation().z - 7.0F) < 0.0001F,
                Component.literal("Expected transformation translation expression to apply SET changes.")
        );
        translationExpression.change(event, null, ChangeMode.RESET);
        Transformation translationReset = PrivateEntityAccess.displayTransformation(textDisplay);
        helper.assertTrue(
                Math.abs(translationReset.getTranslation().x) < 0.0001F
                        && Math.abs(translationReset.getTranslation().y) < 0.0001F
                        && Math.abs(translationReset.getTranslation().z) < 0.0001F,
                Component.literal("Expected transformation translation expression to reset to zero.")
        );

        @SuppressWarnings("unchecked")
        Expression<? extends Vec3> scaleExpression = new SkriptParser(
                "transformation scale of event-entity",
                SkriptParser.ALL_FLAGS,
                ParseContext.DEFAULT
        ).parseExpression(new Class[]{Vec3.class});
        helper.assertTrue(scaleExpression != null, Component.literal("Expected transformation scale expression to parse from registry."));
        if (scaleExpression == null) {
            throw new IllegalStateException("transformation scale expression did not parse");
        }
        PrivateEntityAccess.setDisplayTransformation(
                textDisplay,
                new Transformation(
                        new Vector3f(0.0F, 0.0F, 0.0F),
                        new Quaternionf(0.0F, 0.0F, 0.0F, 1.0F),
                        new Vector3f(2.0F, 3.0F, 4.0F),
                        new Quaternionf(0.0F, 0.0F, 0.0F, 1.0F)
                )
        );
        Vec3 scale = scaleExpression.getSingle(event);
        helper.assertTrue(
                scale != null && Math.abs(scale.x - 2.0D) < 0.0001D && Math.abs(scale.y - 3.0D) < 0.0001D
                        && Math.abs(scale.z - 4.0D) < 0.0001D,
                Component.literal("Expected transformation scale expression to resolve the current scale.")
        );
        scaleExpression.change(event, new Object[]{new Vec3(8.0D, 9.0D, 10.0D)}, ChangeMode.SET);
        Transformation scaleChange = PrivateEntityAccess.displayTransformation(textDisplay);
        helper.assertTrue(
                Math.abs(scaleChange.getScale().x - 8.0F) < 0.0001F
                        && Math.abs(scaleChange.getScale().y - 9.0F) < 0.0001F
                        && Math.abs(scaleChange.getScale().z - 10.0F) < 0.0001F,
                Component.literal("Expected transformation scale expression to apply SET changes.")
        );
        scaleExpression.change(event, null, ChangeMode.RESET);
        Transformation scaleReset = PrivateEntityAccess.displayTransformation(textDisplay);
        helper.assertTrue(
                Math.abs(scaleReset.getScale().x - 1.0F) < 0.0001F
                        && Math.abs(scaleReset.getScale().y - 1.0F) < 0.0001F
                        && Math.abs(scaleReset.getScale().z - 1.0F) < 0.0001F,
                Component.literal("Expected transformation scale expression to reset to one.")
        );

        @SuppressWarnings("unchecked")
        Expression<? extends Quaternionf> leftRotationExpression = new SkriptParser(
                "left rotation of event-entity",
                SkriptParser.ALL_FLAGS,
                ParseContext.DEFAULT
        ).parseExpression(new Class[]{Quaternionf.class});
        helper.assertTrue(leftRotationExpression != null, Component.literal("Expected left rotation expression to parse from registry."));
        if (leftRotationExpression == null) {
            throw new IllegalStateException("left rotation expression did not parse");
        }
        PrivateEntityAccess.setDisplayTransformation(
                textDisplay,
                new Transformation(
                        new Vector3f(0.0F, 0.0F, 0.0F),
                        new Quaternionf(0.1F, 0.2F, 0.3F, 0.4F),
                        new Vector3f(1.0F, 1.0F, 1.0F),
                        new Quaternionf(0.0F, 0.0F, 0.0F, 1.0F)
                )
        );
        Quaternionf leftRotation = leftRotationExpression.getSingle(event);
        helper.assertTrue(
                leftRotation != null
                        && Math.abs(leftRotation.x - 0.1F) < 0.0001F
                        && Math.abs(leftRotation.y - 0.2F) < 0.0001F
                        && Math.abs(leftRotation.z - 0.3F) < 0.0001F
                        && Math.abs(leftRotation.w - 0.4F) < 0.0001F,
                Component.literal("Expected left rotation expression to resolve the current quaternion.")
        );
        leftRotationExpression.change(event, new Object[]{new Quaternionf(0.11F, 0.22F, 0.33F, 0.44F)}, ChangeMode.SET);
        Transformation leftRotationChange = PrivateEntityAccess.displayTransformation(textDisplay);
        helper.assertTrue(
                Math.abs(leftRotationChange.getLeftRotation().x - 0.11F) < 0.0001F
                        && Math.abs(leftRotationChange.getLeftRotation().w - 0.44F) < 0.0001F,
                Component.literal("Expected left rotation expression to apply SET changes.")
        );
        leftRotationExpression.change(event, null, ChangeMode.RESET);
        Transformation leftRotationReset = PrivateEntityAccess.displayTransformation(textDisplay);
        helper.assertTrue(
                Math.abs(leftRotationReset.getLeftRotation().x) < 0.0001F
                        && Math.abs(leftRotationReset.getLeftRotation().y) < 0.0001F
                        && Math.abs(leftRotationReset.getLeftRotation().z) < 0.0001F
                        && Math.abs(leftRotationReset.getLeftRotation().w - 1.0F) < 0.0001F,
                Component.literal("Expected left rotation expression to reset to identity.")
        );

        @SuppressWarnings("unchecked")
        Expression<? extends Quaternionf> rightRotationExpression = new SkriptParser(
                "right rotation of event-entity",
                SkriptParser.ALL_FLAGS,
                ParseContext.DEFAULT
        ).parseExpression(new Class[]{Quaternionf.class});
        helper.assertTrue(rightRotationExpression != null, Component.literal("Expected right rotation expression to parse from registry."));
        if (rightRotationExpression == null) {
            throw new IllegalStateException("right rotation expression did not parse");
        }
        PrivateEntityAccess.setDisplayTransformation(
                textDisplay,
                new Transformation(
                        new Vector3f(0.0F, 0.0F, 0.0F),
                        new Quaternionf(0.0F, 0.0F, 0.0F, 1.0F),
                        new Vector3f(1.0F, 1.0F, 1.0F),
                        new Quaternionf(0.5F, 0.6F, 0.7F, 0.8F)
                )
        );
        Quaternionf rightRotation = rightRotationExpression.getSingle(event);
        helper.assertTrue(
                rightRotation != null
                        && Math.abs(rightRotation.x - 0.5F) < 0.0001F
                        && Math.abs(rightRotation.y - 0.6F) < 0.0001F
                        && Math.abs(rightRotation.z - 0.7F) < 0.0001F
                        && Math.abs(rightRotation.w - 0.8F) < 0.0001F,
                Component.literal("Expected right rotation expression to resolve the current quaternion.")
        );
        rightRotationExpression.change(event, new Object[]{new Quaternionf(0.9F, 1.0F, 1.1F, 1.2F)}, ChangeMode.SET);
        Transformation rightRotationChange = PrivateEntityAccess.displayTransformation(textDisplay);
        helper.assertTrue(
                Math.abs(rightRotationChange.getRightRotation().x - 0.9F) < 0.0001F
                        && Math.abs(rightRotationChange.getRightRotation().w - 1.2F) < 0.0001F,
                Component.literal("Expected right rotation expression to apply SET changes.")
        );
        rightRotationExpression.change(event, null, ChangeMode.RESET);
        Transformation rightRotationReset = PrivateEntityAccess.displayTransformation(textDisplay);
        helper.assertTrue(
                Math.abs(rightRotationReset.getRightRotation().x) < 0.0001F
                        && Math.abs(rightRotationReset.getRightRotation().y) < 0.0001F
                        && Math.abs(rightRotationReset.getRightRotation().z) < 0.0001F
                        && Math.abs(rightRotationReset.getRightRotation().w - 1.0F) < 0.0001F,
                Component.literal("Expected right rotation expression to reset to identity.")
        );
        helper.succeed();
    }

    @GameTest
    public void itemDisplayTransformExpressionParsesResolvesAndChanges(GameTestHelper helper) {
        Display.ItemDisplay itemDisplay = createItemDisplay(helper, ItemDisplayContext.FIRST_PERSON_LEFT_HAND);

        ServerPlayer player = helper.makeMockServerPlayerInLevel();
        player.setGameMode(GameType.CREATIVE);

        var event = new org.skriptlang.skript.lang.event.SkriptEvent(
                new FabricUseEntityHandle(
                        helper.getLevel(),
                        player,
                        InteractionHand.MAIN_HAND,
                        itemDisplay,
                        new EntityHitResult(itemDisplay)
                ),
                helper.getLevel().getServer(),
                helper.getLevel(),
                player
        );

        @SuppressWarnings("unchecked")
        Expression<? extends ItemDisplayContext> transformExpression = new SkriptParser(
                "item transform of event-entity",
                SkriptParser.ALL_FLAGS,
                ParseContext.DEFAULT
        ).parseExpression(new Class[]{ItemDisplayContext.class});
        helper.assertTrue(transformExpression != null, Component.literal("Expected item transform expression to parse from registry."));
        if (transformExpression == null) {
            throw new IllegalStateException("item transform expression did not parse");
        }
        helper.assertTrue(
                transformExpression.getSingle(event) == ItemDisplayContext.FIRST_PERSON_LEFT_HAND,
                Component.literal("Expected item transform expression to resolve the current item display transform.")
        );
        transformExpression.change(event, new Object[]{ItemDisplayContext.GUI}, ChangeMode.SET);
        helper.assertTrue(
                PrivateEntityAccess.itemDisplayTransform(itemDisplay) == ItemDisplayContext.GUI,
                Component.literal("Expected item transform expression to apply SET changes.")
        );
        transformExpression.change(event, null, ChangeMode.RESET);
        helper.assertTrue(
                PrivateEntityAccess.itemDisplayTransform(itemDisplay) == ItemDisplayContext.NONE,
                Component.literal("Expected item transform expression to reset to no transform.")
        );

        Condition transformCondition = Condition.parse("item transform of event-entity is \"first person left hand\"", null);
        helper.assertTrue(transformCondition != null, Component.literal("Expected item transform comparison condition to parse."));
        if (transformCondition == null) {
            throw new IllegalStateException("item transform comparison condition did not parse");
        }
        PrivateEntityAccess.setItemDisplayTransform(itemDisplay, ItemDisplayContext.FIRST_PERSON_LEFT_HAND);
        helper.assertTrue(
                transformCondition.check(event),
                Component.literal("Expected item transform comparison condition to evaluate true.")
        );
        helper.succeed();
    }

    @GameTest
    public void baseConditionsWorkOnMappedTypes(GameTestHelper helper) {
        ServerPlayer player = helper.makeMockServerPlayerInLevel();
        player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.APPLE));

        Condition eventItemNotEmpty = Condition.parse("event-item is not empty", null);
        helper.assertTrue(
                eventItemNotEmpty != null,
                Component.literal("Expected event-item emptiness condition to parse.")
        );
        if (eventItemNotEmpty == null) {
            throw new IllegalStateException("event-item is not empty did not parse");
        }
        helper.assertTrue(
                eventItemNotEmpty.check(new org.skriptlang.skript.lang.event.SkriptEvent(
                        new FabricUseItemHandle(helper.getLevel(), player, InteractionHand.MAIN_HAND),
                        helper.getLevel().getServer(),
                        helper.getLevel(),
                        player
                )),
                Component.literal("Expected event-item emptiness condition to evaluate true for a held apple.")
        );

        Condition unnamedEntityCondition = Condition.parse("event-entity is not named", null);
        helper.assertTrue(
                unnamedEntityCondition != null,
                Component.literal("Expected event-entity named condition to parse.")
        );
        if (unnamedEntityCondition == null) {
            throw new IllegalStateException("event-entity is not named did not parse");
        }

        ArmorStand unnamedStand = new ArmorStand(helper.getLevel(), 1.0D, 1.0D, 1.0D);
        helper.assertTrue(
                unnamedEntityCondition.check(new org.skriptlang.skript.lang.event.SkriptEvent(
                        new FabricUseEntityHandle(
                                helper.getLevel(),
                                player,
                                InteractionHand.MAIN_HAND,
                                unnamedStand,
                                new EntityHitResult(unnamedStand)
                        ),
                        helper.getLevel().getServer(),
                        helper.getLevel(),
                        player
                )),
                Component.literal("Expected unnamed entity condition to evaluate true for an unnamed armor stand.")
        );

        var emptySlotCondition = new org.skriptlang.skript.bukkit.base.conditions.CondIsEmpty();
        helper.assertTrue(
                emptySlotCondition.init(
                        new Expression[]{new SimpleLiteral<>(new Slot(new SimpleContainer(1), 0, 0, 0), false)},
                        2,
                        Kleenean.FALSE,
                        new SkriptParser.ParseResult()
                ),
                Component.literal("Expected slot emptiness condition to initialize with a literal slot.")
        );
        helper.assertTrue(
                emptySlotCondition.check(new org.skriptlang.skript.lang.event.SkriptEvent(null, helper.getLevel().getServer(), helper.getLevel(), null)),
                Component.literal("Expected empty slot condition to evaluate true for an empty slot.")
        );

        var emptyInventoryCondition = new org.skriptlang.skript.bukkit.base.conditions.CondIsEmpty();
        helper.assertTrue(
                emptyInventoryCondition.init(
                        new Expression[]{new SimpleLiteral<>(new FabricInventory(new SimpleContainer(2)), false)},
                        4,
                        Kleenean.FALSE,
                        new SkriptParser.ParseResult()
                ),
                Component.literal("Expected inventory emptiness condition to initialize with a literal inventory.")
        );
        helper.assertTrue(
                emptyInventoryCondition.check(new org.skriptlang.skript.lang.event.SkriptEvent(null, helper.getLevel().getServer(), helper.getLevel(), null)),
                Component.literal("Expected empty inventory condition to evaluate true for an empty container.")
        );

        var namedItemCondition = new org.skriptlang.skript.bukkit.base.conditions.CondIsNamed();
        ItemStack namedStack = new ItemStack(Items.STICK);
        namedStack.set(net.minecraft.core.component.DataComponents.CUSTOM_NAME, Component.literal("named stick"));
        helper.assertTrue(
                namedItemCondition.init(
                        new Expression[]{new SimpleLiteral<>(namedStack, false)},
                        2,
                        Kleenean.FALSE,
                        new SkriptParser.ParseResult()
                ),
                Component.literal("Expected item named condition to initialize with a literal item stack.")
        );
        helper.assertTrue(
                namedItemCondition.check(new org.skriptlang.skript.lang.event.SkriptEvent(null, helper.getLevel().getServer(), helper.getLevel(), null)),
                Component.literal("Expected named item condition to evaluate true for a custom-named item.")
        );

        Cow adultCow = createCow(helper, false);
        Cow babyCow = createCow(helper, true);
        Cow inLoveCow = createCow(helper, false);
        inLoveCow.setInLove(player);
        FabricBreedingState.setAgeLocked(adultCow, true);

        var adultCondition = new CondIsAdult();
        helper.assertTrue(
                adultCondition.init(
                        new Expression[]{new SimpleLiteral<>(adultCow, false)},
                        0,
                        Kleenean.FALSE,
                        new SkriptParser.ParseResult()
                ),
                Component.literal("Expected adult condition to initialize with a cow literal.")
        );
        helper.assertTrue(
                adultCondition.check(new org.skriptlang.skript.lang.event.SkriptEvent(null, helper.getLevel().getServer(), helper.getLevel(), player)),
                Component.literal("Expected adult condition to evaluate true for an adult cow.")
        );

        var babyCondition = new CondIsBaby();
        helper.assertTrue(
                babyCondition.init(
                        new Expression[]{new SimpleLiteral<>(babyCow, false)},
                        0,
                        Kleenean.FALSE,
                        new SkriptParser.ParseResult()
                ),
                Component.literal("Expected baby condition to initialize with a baby cow literal.")
        );
        helper.assertTrue(
                babyCondition.check(new org.skriptlang.skript.lang.event.SkriptEvent(null, helper.getLevel().getServer(), helper.getLevel(), player)),
                Component.literal("Expected baby condition to evaluate true for a baby cow.")
        );

        var canBreedCondition = new CondCanBreed();
        helper.assertTrue(
                canBreedCondition.init(
                        new Expression[]{new SimpleLiteral<>(adultCow, false)},
                        0,
                        Kleenean.FALSE,
                        new SkriptParser.ParseResult()
                ),
                Component.literal("Expected can-breed condition to initialize with an adult cow literal.")
        );
        helper.assertTrue(
                canBreedCondition.check(new org.skriptlang.skript.lang.event.SkriptEvent(null, helper.getLevel().getServer(), helper.getLevel(), player)),
                Component.literal("Expected can-breed condition to evaluate true for a breed-ready cow.")
        );

        var canAgeCondition = new CondCanAge();
        helper.assertTrue(
                canAgeCondition.init(
                        new Expression[]{new SimpleLiteral<>(adultCow, false)},
                        1,
                        Kleenean.FALSE,
                        new SkriptParser.ParseResult()
                ),
                Component.literal("Expected can-age condition to initialize with an adult cow literal.")
        );
        helper.assertTrue(
                canAgeCondition.check(new org.skriptlang.skript.lang.event.SkriptEvent(null, helper.getLevel().getServer(), helper.getLevel(), player)),
                Component.literal("Expected negated can-age condition to evaluate true after age lock emulation.")
        );

        var inLoveCondition = new CondIsInLove();
        helper.assertTrue(
                inLoveCondition.init(
                        new Expression[]{new SimpleLiteral<>(inLoveCow, false)},
                        0,
                        Kleenean.FALSE,
                        new SkriptParser.ParseResult()
                ),
                Component.literal("Expected in-love condition to initialize with a cow literal.")
        );
        helper.assertTrue(
                inLoveCondition.check(new org.skriptlang.skript.lang.event.SkriptEvent(null, helper.getLevel().getServer(), helper.getLevel(), player)),
                Component.literal("Expected in-love condition to evaluate true for a cow in love mode.")
        );

        DamageSource scaledDamageSource = helper.getLevel().damageSources().mobAttack(adultCow);
        var scalesWithDifficultyCondition = new CondScalesWithDifficulty();
        helper.assertTrue(
                scalesWithDifficultyCondition.init(
                        new Expression[]{new SimpleLiteral<>(scaledDamageSource, false)},
                        0,
                        Kleenean.FALSE,
                        new SkriptParser.ParseResult()
                ),
                Component.literal("Expected scales-with-difficulty condition to initialize with a damage source literal.")
        );
        helper.assertTrue(
                scalesWithDifficultyCondition.check(new org.skriptlang.skript.lang.event.SkriptEvent(null, helper.getLevel().getServer(), helper.getLevel(), player)),
                Component.literal("Expected mob damage source to scale with difficulty.")
        );

        ArmorStand projectile = new ArmorStand(helper.getLevel(), 4.0D, 1.0D, 4.0D);
        helper.getLevel().addFreshEntity(projectile);
        DamageSource indirectDamageSource = helper.getLevel().damageSources().thrown(projectile, player);
        var indirectCondition = new CondWasIndirect();
        helper.assertTrue(
                indirectCondition.init(
                        new Expression[]{new SimpleLiteral<>(indirectDamageSource, false)},
                        0,
                        Kleenean.FALSE,
                        new SkriptParser.ParseResult()
                ),
                Component.literal("Expected indirect-damage condition to initialize with a damage source literal.")
        );
        helper.assertTrue(
                indirectCondition.check(new org.skriptlang.skript.lang.event.SkriptEvent(null, helper.getLevel().getServer(), helper.getLevel(), player)),
                Component.literal("Expected thrown damage source to be treated as indirectly caused.")
        );

        Display.TextDisplay shadowedTextDisplay = createTextDisplay(helper, Display.TextDisplay.FLAG_SHADOW);
        var shadowCondition = new CondTextDisplayHasDropShadow();
        helper.assertTrue(
                shadowCondition.init(
                        new Expression[]{new SimpleLiteral<>(shadowedTextDisplay, false)},
                        0,
                        Kleenean.FALSE,
                        new SkriptParser.ParseResult()
                ),
                Component.literal("Expected text display shadow condition to initialize with a TextDisplay literal.")
        );
        helper.assertTrue(
                shadowCondition.check(new org.skriptlang.skript.lang.event.SkriptEvent(null, helper.getLevel().getServer(), helper.getLevel(), player)),
                Component.literal("Expected text display shadow condition to evaluate true for a flagged TextDisplay.")
        );

        Interaction responsiveInteraction = createInteraction(helper, true);
        var responsiveCondition = new CondIsResponsive();
        helper.assertTrue(
                responsiveCondition.init(
                        new Expression[]{new SimpleLiteral<>(responsiveInteraction, false)},
                        0,
                        Kleenean.FALSE,
                        new SkriptParser.ParseResult()
                ),
                Component.literal("Expected responsive interaction condition to initialize with an interaction literal.")
        );
        helper.assertTrue(
                responsiveCondition.check(new org.skriptlang.skript.lang.event.SkriptEvent(null, helper.getLevel().getServer(), helper.getLevel(), player)),
                Component.literal("Expected responsive interaction condition to evaluate true for a responsive interaction entity.")
        );

        MinecartChest chestMinecart = createChestMinecart(helper, true);
        var lootTableCondition = new CondHasLootTable();
        helper.assertTrue(
                lootTableCondition.init(
                        new Expression[]{new SimpleLiteral<>(chestMinecart, false)},
                        0,
                        Kleenean.FALSE,
                        new SkriptParser.ParseResult()
                ),
                Component.literal("Expected loot table condition to initialize with a lootable entity literal.")
        );
        helper.assertTrue(
                lootTableCondition.check(new org.skriptlang.skript.lang.event.SkriptEvent(null, helper.getLevel().getServer(), helper.getLevel(), player)),
                Component.literal("Expected loot table condition to evaluate true for a chest minecart with a loot table.")
        );

        Cow poisonedCow = createCow(helper, false);
        poisonedCow.addEffect(new MobEffectInstance(MobEffects.POISON, 200, 0));
        var poisonedCondition = new CondIsPoisoned();
        helper.assertTrue(
                poisonedCondition.init(
                        new Expression[]{new SimpleLiteral<>(poisonedCow, false)},
                        0,
                        Kleenean.FALSE,
                        new SkriptParser.ParseResult()
                ),
                Component.literal("Expected poisoned condition to initialize with a living entity literal.")
        );
        helper.assertTrue(
                poisonedCondition.check(new org.skriptlang.skript.lang.event.SkriptEvent(null, helper.getLevel().getServer(), helper.getLevel(), player)),
                Component.literal("Expected poisoned condition to evaluate true for a poisoned cow.")
        );

        var hasPotionCondition = new CondHasPotion();
        helper.assertTrue(
                hasPotionCondition.init(
                        new Expression[]{
                                new SimpleLiteral<>(poisonedCow, false),
                                new SimpleLiteral<>("poison", false)
                        },
                        2,
                        Kleenean.FALSE,
                        new SkriptParser.ParseResult()
                ),
                Component.literal("Expected has-potion condition to initialize with string effect literals.")
        );
        helper.assertTrue(
                hasPotionCondition.check(new org.skriptlang.skript.lang.event.SkriptEvent(null, helper.getLevel().getServer(), helper.getLevel(), player)),
                Component.literal("Expected has-potion condition to match a poisoned cow.")
        );

        SkriptPotionEffect ambientPotion = SkriptPotionEffect.fromInstance(new MobEffectInstance(MobEffects.SPEED, 200, 0, true, false, false));
        var ambientPotionCondition = new CondIsPotionAmbient();
        helper.assertTrue(
                ambientPotionCondition.init(
                        new Expression[]{new SimpleLiteral<>(ambientPotion, false)},
                        0,
                        Kleenean.FALSE,
                        new SkriptParser.ParseResult()
                ),
                Component.literal("Expected potion ambient condition to initialize with a potion wrapper.")
        );
        helper.assertTrue(
                ambientPotionCondition.check(new org.skriptlang.skript.lang.event.SkriptEvent(null, helper.getLevel().getServer(), helper.getLevel(), player)),
                Component.literal("Expected potion ambient condition to evaluate true for an ambient effect.")
        );

        var instantPotionCondition = new CondIsPotionInstant();
        helper.assertTrue(
                instantPotionCondition.init(
                        new Expression[]{new SimpleLiteral<>("instant_health", false)},
                        0,
                        Kleenean.FALSE,
                        new SkriptParser.ParseResult()
                ),
                Component.literal("Expected instant potion condition to initialize with a string literal.")
        );
        helper.assertTrue(
                instantPotionCondition.check(new org.skriptlang.skript.lang.event.SkriptEvent(null, helper.getLevel().getServer(), helper.getLevel(), player)),
                Component.literal("Expected instant potion condition to evaluate true for instant health.")
        );

        var iconPotionCondition = new CondPotionHasIcon();
        helper.assertTrue(
                iconPotionCondition.init(
                        new Expression[]{new SimpleLiteral<>(SkriptPotionEffect.fromInstance(new MobEffectInstance(MobEffects.SPEED, 200, 0, false, true, true)), false)},
                        0,
                        Kleenean.FALSE,
                        new SkriptParser.ParseResult()
                ),
                Component.literal("Expected potion icon condition to initialize with a potion wrapper.")
        );
        helper.assertTrue(
                iconPotionCondition.check(new org.skriptlang.skript.lang.event.SkriptEvent(null, helper.getLevel().getServer(), helper.getLevel(), player)),
                Component.literal("Expected potion icon condition to evaluate true for an icon-visible effect.")
        );

        var particlesPotionCondition = new CondPotionHasParticles();
        helper.assertTrue(
                particlesPotionCondition.init(
                        new Expression[]{new SimpleLiteral<>(SkriptPotionEffect.fromInstance(new MobEffectInstance(MobEffects.SPEED, 200, 0, false, true, false)), false)},
                        0,
                        Kleenean.FALSE,
                        new SkriptParser.ParseResult()
                ),
                Component.literal("Expected potion particles condition to initialize with a potion wrapper.")
        );
        helper.assertTrue(
                particlesPotionCondition.check(new org.skriptlang.skript.lang.event.SkriptEvent(null, helper.getLevel().getServer(), helper.getLevel(), player)),
                Component.literal("Expected potion particles condition to evaluate true for a visible effect.")
        );

        var taggedCondition = new CondIsTagged();
        helper.assertTrue(
                taggedCondition.init(
                        new Expression[]{
                                new SimpleLiteral<>(new ItemStack(Items.GOLD_INGOT), false),
                                new SimpleLiteral<>("piglin_loved", false)
                        },
                        0,
                        Kleenean.FALSE,
                        new SkriptParser.ParseResult()
                ),
                Component.literal("Expected tagged condition to initialize with item and tag literals.")
        );
        helper.assertTrue(
                taggedCondition.check(new org.skriptlang.skript.lang.event.SkriptEvent(null, helper.getLevel().getServer(), helper.getLevel(), player)),
                Component.literal("Expected tagged condition to evaluate true for gold ingots in piglin_loved.")
        );

        ItemStack equippableStack = createEquippableTestItem(true, true, true, true, true);

        var damageEquippableCondition = new CondEquipCompDamage();
        helper.assertTrue(
                damageEquippableCondition.init(
                        new Expression[]{new SimpleLiteral<>(equippableStack, false)},
                        0,
                        Kleenean.FALSE,
                        new SkriptParser.ParseResult()
                ),
                Component.literal("Expected equippable damage condition to initialize with an item stack literal.")
        );
        helper.assertTrue(
                damageEquippableCondition.check(new org.skriptlang.skript.lang.event.SkriptEvent(null, helper.getLevel().getServer(), helper.getLevel(), player)),
                Component.literal("Expected equippable damage condition to evaluate true for a damage-on-hurt component.")
        );

        var dispensableEquippableCondition = new CondEquipCompDispensable();
        helper.assertTrue(
                dispensableEquippableCondition.init(
                        new Expression[]{new SimpleLiteral<>(equippableStack, false)},
                        0,
                        Kleenean.FALSE,
                        new SkriptParser.ParseResult()
                ),
                Component.literal("Expected equippable dispensable condition to initialize with an item stack literal.")
        );
        helper.assertTrue(
                dispensableEquippableCondition.check(new org.skriptlang.skript.lang.event.SkriptEvent(null, helper.getLevel().getServer(), helper.getLevel(), player)),
                Component.literal("Expected equippable dispensable condition to evaluate true for a dispensable component.")
        );

        var interactEquippableCondition = new CondEquipCompInteract();
        helper.assertTrue(
                interactEquippableCondition.init(
                        new Expression[]{new SimpleLiteral<>(equippableStack, false)},
                        0,
                        Kleenean.FALSE,
                        new SkriptParser.ParseResult()
                ),
                Component.literal("Expected equippable interact condition to initialize with an item stack literal.")
        );
        helper.assertTrue(
                interactEquippableCondition.check(new org.skriptlang.skript.lang.event.SkriptEvent(null, helper.getLevel().getServer(), helper.getLevel(), player)),
                Component.literal("Expected equippable interact condition to evaluate true for an interactable component.")
        );

        var shearableEquippableCondition = new CondEquipCompShearable();
        helper.assertTrue(
                shearableEquippableCondition.init(
                        new Expression[]{new SimpleLiteral<>(equippableStack, false)},
                        0,
                        Kleenean.FALSE,
                        new SkriptParser.ParseResult()
                ),
                Component.literal("Expected equippable shearable condition to initialize with an item stack literal.")
        );
        helper.assertTrue(
                shearableEquippableCondition.check(new org.skriptlang.skript.lang.event.SkriptEvent(null, helper.getLevel().getServer(), helper.getLevel(), player)),
                Component.literal("Expected equippable shearable condition to evaluate true for a shearable component.")
        );

        var swappableEquippableCondition = new CondEquipCompSwapEquipment();
        helper.assertTrue(
                swappableEquippableCondition.init(
                        new Expression[]{new SimpleLiteral<>(equippableStack, false)},
                        0,
                        Kleenean.FALSE,
                        new SkriptParser.ParseResult()
                ),
                Component.literal("Expected equippable swappable condition to initialize with an item stack literal.")
        );
        helper.assertTrue(
                swappableEquippableCondition.check(new org.skriptlang.skript.lang.event.SkriptEvent(null, helper.getLevel().getServer(), helper.getLevel(), player)),
                Component.literal("Expected equippable swappable condition to evaluate true for a swappable component.")
        );

        FishingHook hook = new FishingHook(player, helper.getLevel(), 0, 0);
        setFishingHookOpenWater(hook, true);

        var openWaterCondition = new CondIsInOpenWater();
        helper.assertTrue(
                openWaterCondition.init(
                        new Expression[]{new SimpleLiteral<>(hook, false)},
                        0,
                        Kleenean.FALSE,
                        new SkriptParser.ParseResult()
                ),
                Component.literal("Expected open water condition to initialize with a fishing hook literal.")
        );
        helper.assertTrue(
                openWaterCondition.check(new org.skriptlang.skript.lang.event.SkriptEvent(null, helper.getLevel().getServer(), helper.getLevel(), player)),
                Component.literal("Expected open water condition to evaluate true for a fishing hook in open water.")
        );

        @SuppressWarnings("unchecked")
        Expression<? extends Slot> brewingSlotExpression = new SkriptParser(
                "brewing stand fuel slot of event-block",
                SkriptParser.ALL_FLAGS,
                ParseContext.DEFAULT
        ).parseExpression(new Class[]{Slot.class});
        helper.assertTrue(
                brewingSlotExpression != null,
                Component.literal("Expected brewing stand fuel slot expression to parse from registry.")
        );
        if (brewingSlotExpression == null) {
            throw new IllegalStateException("brewing stand fuel slot expression did not parse");
        }

        BlockPos brewingAbsolute = helper.absolutePos(new BlockPos(12, 1, 0));
        helper.getLevel().setBlockAndUpdate(brewingAbsolute, Blocks.BREWING_STAND.defaultBlockState());
        BrewingStandBlockEntity brewingStand = (BrewingStandBlockEntity) helper.getLevel().getBlockEntity(brewingAbsolute);
        helper.assertTrue(
                brewingStand != null,
                Component.literal("Expected brewing stand block entity for expression resolution test.")
        );
        if (brewingStand == null) {
            throw new IllegalStateException("Brewing stand block entity missing for expression resolution test.");
        }
        brewingStand.setItem(4, new ItemStack(Items.BLAZE_POWDER));
        Slot brewingSlot = brewingSlotExpression.getSingle(new org.skriptlang.skript.lang.event.SkriptEvent(
                new FabricBrewingFuelHandle(helper.getLevel(), brewingAbsolute, brewingStand, true),
                helper.getLevel().getServer(),
                helper.getLevel(),
                null
        ));
        helper.assertTrue(
                brewingSlot != null && brewingSlot.getItem().is(Items.BLAZE_POWDER),
                Component.literal("Expected brewing stand fuel slot expression to resolve the fuel slot item.")
        );

        @SuppressWarnings("unchecked")
        Expression<? extends Integer> brewingFuelLevelExpression = new SkriptParser(
                "brewing fuel amount of event-block",
                SkriptParser.ALL_FLAGS,
                ParseContext.DEFAULT
        ).parseExpression(new Class[]{Integer.class});
        helper.assertTrue(
                brewingFuelLevelExpression != null,
                Component.literal("Expected brewing fuel amount expression to parse from registry.")
        );
        if (brewingFuelLevelExpression == null) {
            throw new IllegalStateException("brewing fuel amount expression did not parse");
        }
        PrivateBlockEntityAccess.setBrewingFuel(brewingStand, 20);
        Integer resolvedFuelLevel = brewingFuelLevelExpression.getSingle(new org.skriptlang.skript.lang.event.SkriptEvent(
                new org.skriptlang.skript.fabric.runtime.FabricUseBlockHandle(
                        helper.getLevel(),
                        player,
                        InteractionHand.MAIN_HAND,
                        new BlockHitResult(Vec3.atCenterOf(brewingAbsolute), Direction.UP, brewingAbsolute, false)
                ),
                helper.getLevel().getServer(),
                helper.getLevel(),
                player
        ));
        helper.assertTrue(
                resolvedFuelLevel != null && resolvedFuelLevel == 20,
                Component.literal("Expected brewing fuel amount expression to resolve the brewing stand fuel level.")
        );

        @SuppressWarnings("unchecked")
        Expression<? extends Timespan> brewingTimeExpression = new SkriptParser(
                "brewing time of event-block",
                SkriptParser.ALL_FLAGS,
                ParseContext.DEFAULT
        ).parseExpression(new Class[]{Timespan.class});
        helper.assertTrue(
                brewingTimeExpression != null,
                Component.literal("Expected brewing time expression to parse from registry.")
        );
        if (brewingTimeExpression == null) {
            throw new IllegalStateException("brewing time expression did not parse");
        }
        PrivateBlockEntityAccess.setBrewingTime(brewingStand, 6);
        Timespan resolvedBrewingTime = brewingTimeExpression.getSingle(new org.skriptlang.skript.lang.event.SkriptEvent(
                new org.skriptlang.skript.fabric.runtime.FabricUseBlockHandle(
                        helper.getLevel(),
                        player,
                        InteractionHand.MAIN_HAND,
                        new BlockHitResult(Vec3.atCenterOf(brewingAbsolute), Direction.UP, brewingAbsolute, false)
                ),
                helper.getLevel().getServer(),
                helper.getLevel(),
                player
        ));
        helper.assertTrue(
                resolvedBrewingTime != null && resolvedBrewingTime.getAs(TimePeriod.TICK) == 6,
                Component.literal("Expected brewing time expression to resolve the brewing time in ticks.")
        );

        @SuppressWarnings("unchecked")
        Expression<? extends Timespan> minimumFishingWaitTimeExpression = parseExpressionInEvent(
                "minimum fishing wait time",
                new Class[]{Timespan.class},
                FabricFishingEventHandle.class
        );
        helper.assertTrue(
                minimumFishingWaitTimeExpression != null,
                Component.literal("Expected minimum fishing wait time expression to parse from registry.")
        );
        if (minimumFishingWaitTimeExpression == null) {
            throw new IllegalStateException("minimum fishing wait time expression did not parse");
        }

        FishingHook fishingExpressionHook = new FishingHook(player, helper.getLevel(), 0, 0);
        FabricFishingState.minWaitTime(fishingExpressionHook, 12);
        FabricFishingState.maxWaitTime(fishingExpressionHook, 30);
        PrivateFishingHookAccess.setTimeUntilHooked(fishingExpressionHook, 9);
        FabricFishingState.minLureAngle(fishingExpressionHook, 15.0F);
        FabricFishingState.maxLureAngle(fishingExpressionHook, 270.0F);

        Timespan resolvedMinimumFishingWaitTime = minimumFishingWaitTimeExpression.getSingle(new org.skriptlang.skript.lang.event.SkriptEvent(
                new FabricFishingHandle(helper.getLevel(), player, fishingExpressionHook, false),
                helper.getLevel().getServer(),
                helper.getLevel(),
                player
        ));
        helper.assertTrue(
                resolvedMinimumFishingWaitTime != null && resolvedMinimumFishingWaitTime.getAs(TimePeriod.TICK) == 12,
                Component.literal("Expected minimum fishing wait time expression to resolve the minimum wait time.")
        );

        @SuppressWarnings("unchecked")
        Expression<? extends Timespan> fishingBiteTimeExpression = parseExpressionInEvent(
                "fishing bite time",
                new Class[]{Timespan.class},
                FabricFishingEventHandle.class
        );
        helper.assertTrue(
                fishingBiteTimeExpression != null,
                Component.literal("Expected fishing bite time expression to parse from registry.")
        );
        if (fishingBiteTimeExpression == null) {
            throw new IllegalStateException("fishing bite time expression did not parse");
        }
        Timespan resolvedFishingBiteTime = fishingBiteTimeExpression.getSingle(new org.skriptlang.skript.lang.event.SkriptEvent(
                new FabricFishingHandle(helper.getLevel(), player, fishingExpressionHook, false),
                helper.getLevel().getServer(),
                helper.getLevel(),
                player
        ));
        helper.assertTrue(
                resolvedFishingBiteTime != null && resolvedFishingBiteTime.getAs(TimePeriod.TICK) == 9,
                Component.literal("Expected fishing bite time expression to resolve the bite time.")
        );

        @SuppressWarnings("unchecked")
        Expression<? extends Float> maximumFishingApproachAngleExpression = parseExpressionInEvent(
                "maximum fishing approach angle",
                new Class[]{Float.class},
                FabricFishingEventHandle.class
        );
        helper.assertTrue(
                maximumFishingApproachAngleExpression != null,
                Component.literal("Expected maximum fishing approach angle expression to parse from registry.")
        );
        if (maximumFishingApproachAngleExpression == null) {
            throw new IllegalStateException("maximum fishing approach angle expression did not parse");
        }
        Float resolvedMaximumFishingApproachAngle = maximumFishingApproachAngleExpression.getSingle(new org.skriptlang.skript.lang.event.SkriptEvent(
                new FabricFishingHandle(helper.getLevel(), player, fishingExpressionHook, false),
                helper.getLevel().getServer(),
                helper.getLevel(),
                player
        ));
        helper.assertTrue(
                resolvedMaximumFishingApproachAngle != null && Float.compare(resolvedMaximumFishingApproachAngle, 270.0F) == 0,
                Component.literal("Expected maximum fishing approach angle expression to resolve the lure angle.")
        );

        @SuppressWarnings("unchecked")
        Expression<? extends InputKey> currentInputKeysExpression = new SkriptParser(
                "current input keys of event-player",
                SkriptParser.ALL_FLAGS,
                ParseContext.DEFAULT
        ).parseExpression(new Class[]{InputKey.class});
        helper.assertTrue(
                currentInputKeysExpression != null,
                Component.literal("Expected current input keys expression to parse from registry.")
        );
        if (currentInputKeysExpression == null) {
            throw new IllegalStateException("current input keys expression did not parse");
        }
        InputKey[] resolvedInputKeys = currentInputKeysExpression.getAll(new org.skriptlang.skript.lang.event.SkriptEvent(
                new FabricPlayerInputHandle(
                        helper.getLevel(),
                        player,
                        Input.EMPTY,
                        new Input(true, false, false, false, false, false, false)
                ),
                helper.getLevel().getServer(),
                helper.getLevel(),
                player
        ));
        helper.assertTrue(
                List.of(resolvedInputKeys).contains(InputKey.FORWARD),
                Component.literal("Expected current input keys expression to resolve the forward input key.")
        );

        var pressingKeyCondition = new CondIsPressingKey();
        helper.assertTrue(
                pressingKeyCondition.init(
                        new Expression[]{
                                new SimpleLiteral<>(player, false),
                                new SimpleLiteral<>(InputKey.FORWARD, false)
                        },
                        0,
                        Kleenean.FALSE,
                        new SkriptParser.ParseResult()
                ),
                Component.literal("Expected pressing key condition to initialize with player and input key literals.")
        );
        helper.assertTrue(
                pressingKeyCondition.check(new org.skriptlang.skript.lang.event.SkriptEvent(
                        new FabricPlayerInputHandle(
                                helper.getLevel(),
                                player,
                                Input.EMPTY,
                                new Input(true, false, false, false, false, false, false)
                        ),
                        helper.getLevel().getServer(),
                        helper.getLevel(),
                        player
                )),
                Component.literal("Expected pressing key condition to evaluate true for a forward input event.")
        );

        helper.succeed();
    }

    @GameTest
    public void coreMappingsExposeMojangBackedTypes(GameTestHelper helper) {
        helper.setBlock(new BlockPos(1, 1, 1), Blocks.GOLD_BLOCK.defaultBlockState());

        helper.assertTrue(
                Classes.getSuperClassInfo(FabricLocation.class).getPropertyInfo(Property.WXYZ) != null,
                Component.literal("FabricLocation should register the WXYZ property.")
        );
        helper.assertTrue(
                Classes.getSuperClassInfo(ItemStack.class).getPropertyInfo(Property.AMOUNT) != null,
                Component.literal("ItemStack should register the amount property.")
        );
        helper.assertTrue(
                Classes.getSuperClassInfo(FabricInventory.class).getPropertyInfo(Property.CONTAINS) != null,
                Component.literal("FabricInventory should register the contains property.")
        );
        helper.assertTrue(
                Classes.getSuperClassInfo(FabricItemType.class).getPropertyInfo(Property.AMOUNT) != null,
                Component.literal("FabricItemType should register the amount property.")
        );
        helper.assertTrue(
                Classes.getSuperClassInfo(Slot.class).getPropertyInfo(Property.IS_EMPTY) != null,
                Component.literal("Slot should register the empty property.")
        );
        helper.assertTrue(
                Classes.getSuperClassInfo(GameProfile.class).getPropertyInfo(Property.NAME) != null,
                Component.literal("Offline player mapping should register the name property.")
        );
        helper.assertTrue(
                Classes.getSuperClassInfo(DamageSource.class).getPropertyInfo(Property.NAME) != null,
                Component.literal("DamageSource should register the name property.")
        );

        FabricLocation location = new FabricLocation(helper.getLevel(), new Vec3(1.5, 2.0, 3.5));
        LocationClassInfo.LocationWXYZHandler locationX = new LocationClassInfo.LocationWXYZHandler();
        locationX.axis(WXYZHandler.Axis.X);
        helper.assertTrue(
                Double.compare(locationX.convert(location), 1.5D) == 0,
                Component.literal("Location X handler should expose Mojang Vec3 coordinates.")
        );

        VectorClassInfo.VectorWXYZHandler vectorZ = new VectorClassInfo.VectorWXYZHandler();
        vectorZ.axis(WXYZHandler.Axis.Z);
        helper.assertTrue(
                Double.compare(vectorZ.convert(new Vec3(4.0, 5.0, 6.25)), 6.25D) == 0,
                Component.literal("Vector Z handler should expose Mojang Vec3 coordinates.")
        );

        ItemStack stack = new ItemStack(Items.DIAMOND, 5);
        ItemStackClassInfo.ItemStackAmountHandler amountHandler = new ItemStackClassInfo.ItemStackAmountHandler();
        helper.assertTrue(
                Integer.valueOf(5).equals(amountHandler.convert(stack)),
                Component.literal("ItemStack amount handler should expose Mojang ItemStack counts.")
        );
        amountHandler.change(stack, new Object[]{2}, ChangeMode.SET);
        helper.assertTrue(
                stack.getCount() == 2,
                Component.literal("ItemStack amount handler should mutate Mojang ItemStack counts.")
        );

        FabricInventory inventory = new FabricInventory(new SimpleContainer(stack.copy()));
        InventoryClassInfo.InventoryContainsHandler containsHandler = new InventoryClassInfo.InventoryContainsHandler();
        helper.assertTrue(
                containsHandler.contains(inventory, new ItemStack(Items.DIAMOND, 1)),
                Component.literal("FabricInventory should match Mojang container contents.")
        );

        FabricBlock block = new FabricBlock(helper.getLevel(), helper.absolutePos(new BlockPos(1, 1, 1)));
        helper.assertTrue(
                block.block() == Blocks.GOLD_BLOCK,
                Component.literal("FabricBlock should expose Mojang block state at a world position.")
        );

        helper.succeed();
    }

    @GameTest
    public void baseTypeParsersAndWrappersWork(GameTestHelper helper) {
        FabricLocation parsedLocation = Classes.parse("0, 1, 2.5", FabricLocation.class, ParseContext.CONFIG);
        helper.assertTrue(
                parsedLocation != null && parsedLocation.level() == null && parsedLocation.position().z == 2.5D,
                Component.literal("Location parser should resolve coordinate literals into FabricLocation wrappers.")
        );

        FabricItemType parsedItemType = Classes.parse("3 diamond", FabricItemType.class, ParseContext.CONFIG);
        helper.assertTrue(
                parsedItemType != null && parsedItemType.amount() == 3 && parsedItemType.item() == Items.DIAMOND,
                Component.literal("Item type parser should default bare item ids to the minecraft namespace.")
        );
        if (parsedItemType == null) {
            throw new IllegalStateException("Parsed item type was null");
        }
        helper.assertTrue(
                "diamond".equals(parsedItemType.itemId()),
                Component.literal("Minecraft item ids should stringify without the default namespace.")
        );

        FabricItemType explicitNamespacedItemType = Classes.parse("2 minecraft:apple", FabricItemType.class, ParseContext.CONFIG);
        helper.assertTrue(
                explicitNamespacedItemType != null
                        && explicitNamespacedItemType.amount() == 2
                        && explicitNamespacedItemType.item() == Items.APPLE,
                Component.literal("Explicit namespaces should still be preserved during parsing.")
        );

        LootTable parsedLootTable = Classes.parse("chests/simple_dungeon", LootTable.class, ParseContext.CONFIG);
        helper.assertTrue(
                parsedLootTable != null && BuiltInLootTables.SIMPLE_DUNGEON.equals(parsedLootTable.key()),
                Component.literal("Loot table parser should default bare loot table ids to the minecraft namespace.")
        );
        if (parsedLootTable == null) {
            throw new IllegalStateException("Parsed loot table was null");
        }
        helper.assertTrue(
                "chests/simple_dungeon".equals(parsedLootTable.toString()),
                Component.literal("Minecraft loot table ids should stringify without the default namespace.")
        );

        ItemTypeClassInfo.ItemTypeNameHandler itemTypeNameHandler = new ItemTypeClassInfo.ItemTypeNameHandler();
        itemTypeNameHandler.change(parsedItemType, new Object[]{"cut gem"}, ChangeMode.SET);
        helper.assertTrue(
                "cut gem".equals(itemTypeNameHandler.convert(parsedItemType)),
                Component.literal("Item type display name handler should mutate adapter state.")
        );

        GameProfile offline = Classes.parse("Notch", GameProfile.class, ParseContext.CONFIG);
        helper.assertTrue(
                offline != null && "Notch".equals(offline.getName()),
                Component.literal("Offline player parser should resolve named GameProfiles.")
        );
        if (offline == null) {
            throw new IllegalStateException("Parsed offline player was null");
        }

        ArmorStand armorStand = new ArmorStand(helper.getLevel(), 2.0, 1.0, 2.0);
        helper.getLevel().addFreshEntity(armorStand);
        NameableClassInfo.NameableDisplayNameHandler nameableHandler = new NameableClassInfo.NameableDisplayNameHandler();
        nameableHandler.change(armorStand, new Object[]{"Target Dummy"}, ChangeMode.SET);
        helper.assertTrue(
                armorStand.getCustomName() != null && "Target Dummy".equals(armorStand.getCustomName().getString()),
                Component.literal("Nameable display name handler should mutate Mojang entities.")
        );

        SimpleContainer container = new SimpleContainer(1);
        container.setItem(0, new ItemStack(Items.APPLE, 4));
        Slot slot = new Slot(container, 0, 0, 0);

        SlotClassInfo.SlotAmountHandler slotAmountHandler = new SlotClassInfo.SlotAmountHandler();
        helper.assertTrue(
                Integer.valueOf(4).equals(slotAmountHandler.convert(slot)),
                Component.literal("Slot amount handler should read Mojang slot counts.")
        );
        slotAmountHandler.change(slot, new Object[]{2}, ChangeMode.SET);
        helper.assertTrue(
                slot.getItem().getCount() == 2,
                Component.literal("Slot amount handler should mutate Mojang slot counts.")
        );

        SlotClassInfo.SlotNameHandler slotNameHandler = new SlotClassInfo.SlotNameHandler();
        slotNameHandler.change(slot, new Object[]{"fresh apple"}, ChangeMode.SET);
        helper.assertTrue(
                "fresh apple".equals(slotNameHandler.convert(slot)),
                Component.literal("Slot name handler should mutate Mojang item custom names.")
        );

        OfflinePlayerClassInfo.OfflinePlayerNameHandler offlinePlayerNameHandler = new OfflinePlayerClassInfo.OfflinePlayerNameHandler();
        helper.assertTrue(
                "Notch".equals(offlinePlayerNameHandler.convert(offline)),
                Component.literal("Offline player name handler should expose GameProfile names.")
        );

        helper.succeed();
    }

    @GameTest
    public void potionExpressionsResolveFromRegistry(GameTestHelper helper) {
        ServerPlayer player = helper.makeMockServerPlayerInLevel();
        Cow cow = createCow(helper, false);
        cow.addEffect(new MobEffectInstance(MobEffects.POISON, 200, 1));

        var useEntityEvent = new org.skriptlang.skript.lang.event.SkriptEvent(
                new FabricUseEntityHandle(
                        helper.getLevel(),
                        player,
                        InteractionHand.MAIN_HAND,
                        cow,
                        new EntityHitResult(cow)
                ),
                helper.getLevel().getServer(),
                helper.getLevel(),
                player
        );

        @SuppressWarnings("unchecked")
        Expression<? extends SkriptPotionEffect> potionEffectsExpression = parseExpressionInEvent(
                "active potion effects of event-entity",
                new Class[]{SkriptPotionEffect.class},
                FabricUseEntityHandle.class
        );
        helper.assertTrue(
                potionEffectsExpression != null,
                Component.literal("Expected active potion effects expression to parse from registry.")
        );
        if (potionEffectsExpression == null) {
            throw new IllegalStateException("active potion effects expression did not parse");
        }
        SkriptPotionEffect resolvedPotionEffects = potionEffectsExpression.getSingle(useEntityEvent);
        helper.assertTrue(
                resolvedPotionEffects != null && resolvedPotionEffects.type().is(MobEffects.POISON),
                Component.literal("Expected active potion effects expression to resolve the current poison effect.")
        );

        @SuppressWarnings("unchecked")
        Expression<? extends SkriptPotionEffect> potionEffectExpression = parseExpressionInEvent(
                "poison effect of event-entity",
                new Class[]{SkriptPotionEffect.class},
                FabricUseEntityHandle.class
        );
        helper.assertTrue(
                potionEffectExpression != null,
                Component.literal("Expected specific potion effect expression to parse from registry.")
        );
        if (potionEffectExpression == null) {
            throw new IllegalStateException("specific potion effect expression did not parse");
        }
        SkriptPotionEffect resolvedPotionEffect = potionEffectExpression.getSingle(useEntityEvent);
        helper.assertTrue(
                resolvedPotionEffect != null && resolvedPotionEffect.type().is(MobEffects.POISON),
                Component.literal("Expected specific potion effect expression to resolve the requested poison effect. "
                        + "Fields: " + describeExpressionFields(potionEffectExpression, useEntityEvent)
                        + ", value: " + resolvedPotionEffect)
        );

        @SuppressWarnings("unchecked")
        Expression<? extends Timespan> potionDurationExpression = parseExpressionInEvent(
                "duration of poison effect of event-entity",
                new Class[]{Timespan.class},
                FabricUseEntityHandle.class
        );
        helper.assertTrue(
                potionDurationExpression != null,
                Component.literal("Expected potion duration expression to parse from registry.")
        );
        if (potionDurationExpression == null) {
            throw new IllegalStateException("potion duration expression did not parse");
        }
        Timespan resolvedDuration = potionDurationExpression.getSingle(useEntityEvent);
        helper.assertTrue(
                resolvedDuration != null && resolvedDuration.getAs(TimePeriod.TICK) == 200,
                Component.literal("Expected potion duration expression to resolve the poison duration in ticks.")
        );

        @SuppressWarnings("unchecked")
        Expression<? extends Integer> potionAmplifierExpression = parseExpressionInEvent(
                "potion amplifier of poison effect of event-entity",
                new Class[]{Integer.class},
                FabricUseEntityHandle.class
        );
        helper.assertTrue(
                potionAmplifierExpression != null,
                Component.literal("Expected potion amplifier expression to parse from registry.")
        );
        if (potionAmplifierExpression == null) {
            throw new IllegalStateException("potion amplifier expression did not parse");
        }
        Integer resolvedAmplifier = potionAmplifierExpression.getSingle(useEntityEvent);
        helper.assertTrue(
                resolvedAmplifier != null && resolvedAmplifier == 2,
                Component.literal("Expected potion amplifier expression to resolve the effect level plus one.")
        );

        @SuppressWarnings("unchecked")
        Expression<? extends MobEffectCategory> potionCategoryExpression = new SkriptParser(
                "potion effect type category of poison",
                SkriptParser.ALL_FLAGS,
                ParseContext.DEFAULT
        ).parseExpression(new Class[]{MobEffectCategory.class});
        helper.assertTrue(
                potionCategoryExpression != null,
                Component.literal("Expected potion effect type category expression to parse from registry.")
        );
        if (potionCategoryExpression == null) {
            throw new IllegalStateException("potion effect type category expression did not parse");
        }
        MobEffectCategory resolvedCategory = potionCategoryExpression.getSingle(useEntityEvent);
        helper.assertTrue(
                resolvedCategory == MobEffectCategory.HARMFUL,
                Component.literal("Expected potion effect type category expression to resolve poison as harmful.")
        );

        helper.succeed();
    }

    @GameTest
    public void lootTableExpressionsResolveFromRegistry(GameTestHelper helper) {
        ServerPlayer player = helper.makeMockServerPlayerInLevel();

        @SuppressWarnings("unchecked")
        Expression<? extends LootTable> lootTableFromStringExpression = new SkriptParser(
                "loot table \"chests/simple_dungeon\"",
                SkriptParser.ALL_FLAGS,
                ParseContext.DEFAULT
        ).parseExpression(new Class[]{LootTable.class});
        helper.assertTrue(
                lootTableFromStringExpression != null,
                Component.literal("Expected loot table from string expression to parse from registry.")
        );
        if (lootTableFromStringExpression == null) {
            throw new IllegalStateException("loot table from string expression did not parse");
        }
        LootTable parsed = lootTableFromStringExpression.getSingle(
                new org.skriptlang.skript.lang.event.SkriptEvent(null, helper.getLevel().getServer(), helper.getLevel(), player)
        );
        helper.assertTrue(
                parsed != null && BuiltInLootTables.SIMPLE_DUNGEON.equals(parsed.key()),
                Component.literal("Expected loot table from string expression to resolve the simple dungeon loot table.")
        );

        MinecartChest chestMinecart = createChestMinecart(helper, true);
        @SuppressWarnings("unchecked")
        Expression<? extends LootTable> eventLootTableExpression = new SkriptParser(
                "loot table of event-entity",
                SkriptParser.ALL_FLAGS,
                ParseContext.DEFAULT
        ).parseExpression(new Class[]{LootTable.class});
        helper.assertTrue(
                eventLootTableExpression != null,
                Component.literal("Expected loot table property expression to parse from registry.")
        );
        if (eventLootTableExpression == null) {
            throw new IllegalStateException("loot table property expression did not parse");
        }
        LootTable resolvedLootTable = eventLootTableExpression.getSingle(new org.skriptlang.skript.lang.event.SkriptEvent(
                new FabricUseEntityHandle(
                        helper.getLevel(),
                        player,
                        InteractionHand.MAIN_HAND,
                        chestMinecart,
                        new EntityHitResult(chestMinecart)
                ),
                helper.getLevel().getServer(),
                helper.getLevel(),
                player
        ));
        helper.assertTrue(
                resolvedLootTable != null && BuiltInLootTables.SIMPLE_DUNGEON.equals(resolvedLootTable.key()),
                Component.literal("Expected loot table property expression to resolve the chest minecart loot table.")
        );

        @SuppressWarnings("unchecked")
        Expression<? extends Long> lootTableSeedExpression = new SkriptParser(
                "loot table seed of event-entity",
                SkriptParser.ALL_FLAGS,
                ParseContext.DEFAULT
        ).parseExpression(new Class[]{Long.class});
        helper.assertTrue(
                lootTableSeedExpression != null,
                Component.literal("Expected loot table seed expression to parse from registry.")
        );
        if (lootTableSeedExpression == null) {
            throw new IllegalStateException("loot table seed expression did not parse");
        }
        Long resolvedSeed = lootTableSeedExpression.getSingle(new org.skriptlang.skript.lang.event.SkriptEvent(
                new FabricUseEntityHandle(
                        helper.getLevel(),
                        player,
                        InteractionHand.MAIN_HAND,
                        chestMinecart,
                        new EntityHitResult(chestMinecart)
                ),
                helper.getLevel().getServer(),
                helper.getLevel(),
                player
        ));
        helper.assertTrue(
                resolvedSeed != null && resolvedSeed == 1L,
                Component.literal("Expected loot table seed expression to resolve the chest minecart loot table seed.")
        );

        helper.succeed();
    }

    private void runWithRuntimeLock(GameTestHelper helper, LockedRuntimeBody body) {
        helper.succeedWhen(() -> {
            helper.assertTrue(
                    RUNTIME_LOCK.compareAndSet(false, true),
                    Component.literal("Waiting for exclusive Skript runtime access.")
            );
            try {
                body.run();
            } finally {
                RUNTIME_LOCK.set(false);
            }
        });
    }

    private void assertUseEntityScriptSetsMarker(
            GameTestHelper helper,
            String scriptPath,
            net.minecraft.world.entity.Entity entity,
            BlockPos playerMarkerRelative,
            net.minecraft.world.level.block.Block expectedBlock
    ) {
        assertUseEntityScriptSetsMarker(helper, scriptPath, entity, playerMarkerRelative, expectedBlock, null);
    }

    private void assertUseEntityScriptSetsMarker(
            GameTestHelper helper,
            String scriptPath,
            net.minecraft.world.entity.Entity entity,
            BlockPos playerMarkerRelative,
            net.minecraft.world.level.block.Block expectedBlock,
            @org.jetbrains.annotations.Nullable Runnable postEventAssertion
    ) {
        runWithRuntimeLock(helper, () -> {
            SkriptRuntime runtime = SkriptRuntime.instance();
            runtime.clearScripts();
            runtime.loadFromResource(scriptPath);
            Trigger trigger = getOnlyLoadedTrigger(runtime);

            BlockPos playerMarkerAbsolute = helper.absolutePos(playerMarkerRelative);
            helper.getLevel().setBlockAndUpdate(playerMarkerAbsolute, Blocks.AIR.defaultBlockState());

            ServerPlayer player = helper.makeMockServerPlayerInLevel();
            player.setGameMode(GameType.CREATIVE);
            player.teleportTo(playerMarkerAbsolute.getX() + 0.5D, playerMarkerAbsolute.getY() + 1.0D, playerMarkerAbsolute.getZ() + 0.5D);

            InteractionResult result = UseEntityCallback.EVENT.invoker().interact(
                    player,
                    helper.getLevel(),
                    InteractionHand.MAIN_HAND,
                    entity,
                    new EntityHitResult(entity)
            );
            helper.assertTrue(
                    result == InteractionResult.PASS,
                    Component.literal("Expected use entity breeding condition test to keep Fabric callback flow in PASS state.")
            );
            helper.assertTrue(
                    helper.getLevel().getBlockState(playerMarkerAbsolute).is(expectedBlock),
                    Component.literal("Expected breeding condition script to update the marker block.")
            );
            if (postEventAssertion != null) {
                postEventAssertion.run();
            }
            runtime.clearScripts();
        });
    }

    private void assertUseEntityScriptNamesEntity(
            GameTestHelper helper,
            String scriptPath,
            net.minecraft.world.entity.Entity entity,
            String expectedName
    ) {
        assertUseEntityScriptNamesEntity(helper, scriptPath, entity, expectedName, null);
    }

    private void assertUseEntityScriptNamesEntity(
            GameTestHelper helper,
            String scriptPath,
            net.minecraft.world.entity.Entity entity,
            String expectedName,
            @org.jetbrains.annotations.Nullable Runnable postEventAssertion
    ) {
        runWithRuntimeLock(helper, () -> {
            SkriptRuntime runtime = SkriptRuntime.instance();
            runtime.clearScripts();
            runtime.loadFromResource(scriptPath);
            Trigger trigger = getOnlyLoadedTrigger(runtime);

            ServerPlayer player = helper.makeMockServerPlayerInLevel();
            player.setGameMode(GameType.CREATIVE);
            player.teleportTo(entity.getX(), entity.getY() + 1.0D, entity.getZ());

            InteractionResult result = UseEntityCallback.EVENT.invoker().interact(
                    player,
                    helper.getLevel(),
                    InteractionHand.MAIN_HAND,
                    entity,
                    new EntityHitResult(entity)
            );
            helper.assertTrue(
                    result == InteractionResult.PASS,
                    Component.literal("Expected use entity condition test to keep Fabric callback flow in PASS state.")
            );
            helper.assertTrue(
                    entity.getCustomName() != null && expectedName.equals(entity.getCustomName().getString()),
                    Component.literal("Expected condition script to name the interacted entity. Items: "
                            + describeTriggerItems(trigger))
            );
            if (postEventAssertion != null) {
                postEventAssertion.run();
            }
            runtime.clearScripts();
        });
    }

    private void assertUseEntityScriptDoesNotNameEntity(
            GameTestHelper helper,
            String scriptPath,
            net.minecraft.world.entity.Entity entity
    ) {
        runWithRuntimeLock(helper, () -> {
            SkriptRuntime runtime = SkriptRuntime.instance();
            runtime.clearScripts();
            runtime.loadFromResource(scriptPath);

            ServerPlayer player = helper.makeMockServerPlayerInLevel();
            player.setGameMode(GameType.CREATIVE);
            player.teleportTo(entity.getX(), entity.getY() + 1.0D, entity.getZ());

            InteractionResult result = UseEntityCallback.EVENT.invoker().interact(
                    player,
                    helper.getLevel(),
                    InteractionHand.MAIN_HAND,
                    entity,
                    new EntityHitResult(entity)
            );
            helper.assertTrue(
                    result == InteractionResult.PASS,
                    Component.literal("Expected use entity condition test to keep Fabric callback flow in PASS state.")
            );
            helper.assertTrue(
                    entity.getCustomName() == null,
                    Component.literal("Expected false condition to stop later effects in the trigger.")
            );
            runtime.clearScripts();
        });
    }

    private void assertAttackEntityScriptNamesEntity(
            GameTestHelper helper,
            String scriptPath,
            net.minecraft.world.entity.Entity entity,
            String expectedName
    ) {
        runWithRuntimeLock(helper, () -> {
            SkriptRuntime runtime = SkriptRuntime.instance();
            runtime.clearScripts();
            runtime.loadFromResource(scriptPath);

            ServerPlayer player = helper.makeMockServerPlayerInLevel();
            player.setGameMode(GameType.CREATIVE);
            player.teleportTo(entity.getX(), entity.getY() + 1.0D, entity.getZ());

            InteractionResult result = AttackEntityCallback.EVENT.invoker().interact(
                    player,
                    helper.getLevel(),
                    InteractionHand.MAIN_HAND,
                    entity,
                    new EntityHitResult(entity)
            );
            helper.assertTrue(
                    result == InteractionResult.PASS,
                    Component.literal("Expected attack entity expression test to keep Fabric callback flow in PASS state.")
            );
            helper.assertTrue(
                    entity.getCustomName() != null && expectedName.equals(entity.getCustomName().getString()),
                    Component.literal("Expected attack entity expression script to name the attacked entity.")
            );
            runtime.clearScripts();
        });
    }

    private void assertUseItemScriptNamesItem(
            GameTestHelper helper,
            String scriptPath,
            ItemStack itemStack,
            String expectedName
    ) {
        assertUseItemScriptNamesItem(helper, scriptPath, itemStack, expectedName, null);
    }

    private void assertUseItemScriptNamesItem(
            GameTestHelper helper,
            String scriptPath,
            ItemStack itemStack,
            String expectedName,
            Consumer<ItemStack> postEventAssertion
    ) {
        runWithRuntimeLock(helper, () -> {
            SkriptRuntime runtime = SkriptRuntime.instance();
            runtime.clearScripts();
            runtime.loadFromResource(scriptPath);

            ServerPlayer player = helper.makeMockServerPlayerInLevel();
            player.setGameMode(GameType.CREATIVE);
            player.setItemInHand(InteractionHand.MAIN_HAND, itemStack.copy());

            InteractionResult result = UseItemCallback.EVENT.invoker().interact(
                    player,
                    helper.getLevel(),
                    InteractionHand.MAIN_HAND
            );
            helper.assertTrue(
                    result == InteractionResult.PASS,
                    Component.literal("Expected use item condition test to keep Fabric callback flow in PASS state.")
            );
            ItemStack held = player.getItemInHand(InteractionHand.MAIN_HAND);
            helper.assertTrue(
                    held.getCustomName() != null && expectedName.equals(held.getCustomName().getString()),
                    Component.literal("Expected condition script to rename the held item.")
            );
            if (postEventAssertion != null) {
                postEventAssertion.accept(held);
            }
            runtime.clearScripts();
        });
    }

    private FishingHook createFishingHook(GameTestHelper helper) {
        ServerPlayer player = helper.makeMockServerPlayerInLevel();
        player.setGameMode(GameType.CREATIVE);
        return new FishingHook(player, helper.getLevel(), 0, 0);
    }

    private void assertFishingScriptSetsMarker(
            GameTestHelper helper,
            String scriptPath,
            FishingHook hook,
            BlockPos playerMarkerRelative,
            net.minecraft.world.level.block.Block expectedBlock,
            @org.jetbrains.annotations.Nullable Runnable postEventAssertion
    ) {
        runWithRuntimeLock(helper, () -> {
            SkriptRuntime runtime = SkriptRuntime.instance();
            runtime.clearScripts();
            runtime.loadFromResource(scriptPath);

            BlockPos playerMarkerAbsolute = helper.absolutePos(playerMarkerRelative);
            helper.getLevel().setBlockAndUpdate(playerMarkerAbsolute, Blocks.AIR.defaultBlockState());

            ServerPlayer player = helper.makeMockServerPlayerInLevel();
            player.setGameMode(GameType.CREATIVE);
            player.teleportTo(playerMarkerAbsolute.getX() + 0.5D, playerMarkerAbsolute.getY() + 1.0D, playerMarkerAbsolute.getZ() + 0.5D);

            int executed = runtime.dispatch(new org.skriptlang.skript.lang.event.SkriptEvent(
                    new FabricFishingHandle(helper.getLevel(), player, hook, false),
                    helper.getLevel().getServer(),
                    helper.getLevel(),
                    player
            ));
            helper.assertTrue(
                    executed == 1,
                    Component.literal("Expected fishing expression script to execute exactly one trigger.")
            );
            helper.assertTrue(
                    helper.getLevel().getBlockState(playerMarkerAbsolute).is(expectedBlock),
                    Component.literal("Expected fishing expression script to update the marker block.")
            );
            if (postEventAssertion != null) {
                postEventAssertion.run();
            }
            runtime.clearScripts();
        });
    }

    private Display.TextDisplay createTextDisplay(GameTestHelper helper, byte flags) {
        Display.TextDisplay textDisplay = new Display.TextDisplay(EntityType.TEXT_DISPLAY, helper.getLevel());
        textDisplay.setPos(0.5D, 1.0D, 0.5D);
        PrivateEntityAccess.setTextDisplayFlags(textDisplay, flags);
        helper.getLevel().addFreshEntity(textDisplay);
        return textDisplay;
    }

    private Display.ItemDisplay createItemDisplay(GameTestHelper helper, ItemDisplayContext transform) {
        Display.ItemDisplay itemDisplay = new Display.ItemDisplay(EntityType.ITEM_DISPLAY, helper.getLevel());
        itemDisplay.setPos(0.5D, 1.0D, 0.5D);
        itemDisplay.getSlot(0).set(new ItemStack(Items.STICK));
        PrivateEntityAccess.setItemDisplayTransform(itemDisplay, transform);
        helper.getLevel().addFreshEntity(itemDisplay);
        return itemDisplay;
    }

    private Interaction createInteraction(GameTestHelper helper, boolean responsive) {
        Interaction interaction = new Interaction(EntityType.INTERACTION, helper.getLevel());
        interaction.setPos(0.5D, 1.0D, 0.5D);
        PrivateEntityAccess.setInteractionResponse(interaction, responsive);
        helper.getLevel().addFreshEntity(interaction);
        return interaction;
    }

    private MinecartChest createChestMinecart(GameTestHelper helper, boolean withLootTable) {
        MinecartChest chestMinecart = new MinecartChest(EntityType.CHEST_MINECART, helper.getLevel());
        chestMinecart.setPos(0.5D, 1.0D, 0.5D);
        if (withLootTable) {
            chestMinecart.setLootTable(BuiltInLootTables.SIMPLE_DUNGEON, 1L);
        }
        helper.getLevel().addFreshEntity(chestMinecart);
        return chestMinecart;
    }

    private Cow createCow(GameTestHelper helper, boolean baby) {
        Cow cow = (Cow) helper.spawnWithNoFreeWill(EntityType.COW, 0.5F, 1.0F, 0.5F);
        cow.setBaby(baby);
        return cow;
    }

    private Path writeTempScript(String prefix, String source) {
        try {
            Path scriptPath = Files.createTempFile(prefix, ".sk");
            Files.writeString(scriptPath, source);
            scriptPath.toFile().deleteOnExit();
            return scriptPath;
        } catch (IOException exception) {
            throw new IllegalStateException("Failed to create temporary Skript file for GameTest.", exception);
        }
    }

    @SuppressWarnings("unchecked")
    private <T> Expression<? extends T> parseExpressionInEvent(
            String expression,
            Class<? extends T>[] returnTypes,
            Class<?>... eventClasses
    ) {
        ParserInstance parser = ParserInstance.get();
        String previousEventName = parser.getCurrentEventName();
        Class<?>[] previousEventClasses = parser.getCurrentEventClasses();
        try {
            parser.setCurrentEvent("gametest", eventClasses);
            return new SkriptParser(
                    expression,
                    SkriptParser.ALL_FLAGS,
                    ParseContext.DEFAULT
            ).parseExpression(returnTypes);
        } finally {
            if (previousEventName == null) {
                parser.deleteCurrentEvent();
            } else {
                parser.setCurrentEvent(previousEventName, previousEventClasses);
            }
        }
    }

    private void setFishingHookOpenWater(FishingHook hook, boolean openWater) {
        try {
            Field openWaterField = FishingHook.class.getDeclaredField("openWater");
            openWaterField.setAccessible(true);
            openWaterField.setBoolean(hook, openWater);
        } catch (ReflectiveOperationException exception) {
            throw new IllegalStateException("Failed to set fishing hook open water state for test setup.", exception);
        }
    }

    private void setFishingHookedEntity(FishingHook hook, net.minecraft.world.entity.Entity entity) {
        try {
            Field hookedInField = FishingHook.class.getDeclaredField("hookedIn");
            hookedInField.setAccessible(true);
            hookedInField.set(hook, entity);
        } catch (ReflectiveOperationException exception) {
            throw new IllegalStateException("Failed to set fishing hook hooked entity for test setup.", exception);
        }
    }

    private ItemStack createEquippableTestItem(
            boolean damageOnHurt,
            boolean dispensable,
            boolean equipOnInteract,
            boolean canBeSheared,
            boolean swappable
    ) {
        ItemStack itemStack = new ItemStack(Items.LEATHER_HELMET);
        Equippable equippable = Equippable.builder(EquipmentSlot.HEAD)
                .setDamageOnHurt(damageOnHurt)
                .setDispensable(dispensable)
                .setEquipOnInteract(equipOnInteract)
                .setCanBeSheared(canBeSheared)
                .setSwappable(swappable)
                .build();
        itemStack.set(DataComponents.EQUIPPABLE, equippable);
        return itemStack;
    }

    private String describeEquippable(@org.jetbrains.annotations.Nullable Equippable equippable) {
        if (equippable == null) {
            return "null";
        }
        return "damageOnHurt=" + equippable.damageOnHurt()
                + ", dispensable=" + equippable.dispensable()
                + ", equipOnInteract=" + equippable.equipOnInteract()
                + ", canBeSheared=" + equippable.canBeSheared()
                + ", swappable=" + equippable.swappable();
    }

    private Trigger getOnlyLoadedTrigger(SkriptRuntime runtime) {
        try {
            Field scriptsField = SkriptRuntime.class.getDeclaredField("scripts");
            scriptsField.setAccessible(true);
            @SuppressWarnings("unchecked")
            List<org.skriptlang.skript.lang.script.Script> scripts =
                    (List<org.skriptlang.skript.lang.script.Script>) scriptsField.get(runtime);
            if (scripts.size() != 1) {
                throw new IllegalStateException("Expected exactly one loaded script but found " + scripts.size());
            }
            List<org.skriptlang.skript.lang.structure.Structure> structures = scripts.getFirst().getStructures();
            if (structures.size() != 1 || !(structures.getFirst() instanceof ch.njol.skript.lang.SkriptEvent skriptEvent)) {
                throw new IllegalStateException("Expected exactly one loaded event structure but found " + structures);
            }
            Trigger trigger = skriptEvent.getTrigger();
            if (trigger == null) {
                throw new IllegalStateException("Loaded event did not create a trigger.");
            }
            return trigger;
        } catch (ReflectiveOperationException exception) {
            throw new IllegalStateException("Failed to inspect loaded Skript runtime state.", exception);
        }
    }

    private String describeTriggerItems(Trigger trigger) {
        try {
            Field firstField = TriggerSection.class.getDeclaredField("first");
            Field nextField = TriggerItem.class.getDeclaredField("next");
            firstField.setAccessible(true);
            nextField.setAccessible(true);

            List<String> items = new ArrayList<>();
            TriggerItem current = (TriggerItem) firstField.get(trigger);
            while (current != null) {
                items.add(current.getClass().getName());
                current = (TriggerItem) nextField.get(current);
            }
            return items.toString();
        } catch (ReflectiveOperationException exception) {
            throw new IllegalStateException("Failed to describe trigger items.", exception);
        }
    }

    private Object getFirstTriggerItem(Trigger trigger) {
        try {
            Field firstField = TriggerSection.class.getDeclaredField("first");
            firstField.setAccessible(true);
            return firstField.get(trigger);
        } catch (ReflectiveOperationException exception) {
            throw new IllegalStateException("Failed to read first trigger item.", exception);
        }
    }

    private Object getTriggerItem(Trigger trigger, int index) {
        try {
            Field firstField = TriggerSection.class.getDeclaredField("first");
            Field nextField = TriggerItem.class.getDeclaredField("next");
            firstField.setAccessible(true);
            nextField.setAccessible(true);

            TriggerItem current = (TriggerItem) firstField.get(trigger);
            for (int i = 0; i < index && current != null; i++) {
                current = (TriggerItem) nextField.get(current);
            }
            if (current == null) {
                throw new IllegalStateException("No trigger item at index " + index);
            }
            return current;
        } catch (ReflectiveOperationException exception) {
            throw new IllegalStateException("Failed to read trigger item at index " + index, exception);
        }
    }

    private String describeExpressionFields(Object syntax, org.skriptlang.skript.lang.event.SkriptEvent event) {
        List<String> descriptions = new ArrayList<>();
        for (Field field : syntax.getClass().getDeclaredFields()) {
            if (!Expression.class.isAssignableFrom(field.getType())) {
                continue;
            }
            field.setAccessible(true);
            try {
                Expression<?> expression = (Expression<?>) field.get(syntax);
                if (expression == null) {
                    descriptions.add(field.getName() + "=null");
                    continue;
                }
                Object[] values = expression.getAll(event);
                List<String> rendered = new ArrayList<>(values.length);
                for (Object value : values) {
                    rendered.add(value == null ? "null" : value.getClass().getName() + ":" + value);
                }
                descriptions.add(field.getName() + "=" + expression.getClass().getName() + rendered);
            } catch (ReflectiveOperationException exception) {
                descriptions.add(field.getName() + "=<reflection failed>");
            }
        }
        return descriptions.toString();
    }

    private boolean skriptPatternMatches(String input, String pattern) {
        try {
            var method = SkriptParser.class.getDeclaredMethod(
                    "match",
                    String.class,
                    String.class,
                    ParseContext.class,
                    int.class
            );
            method.setAccessible(true);
            return method.invoke(null, input, pattern, ParseContext.DEFAULT, SkriptParser.ALL_FLAGS) != null;
        } catch (ReflectiveOperationException exception) {
            throw new IllegalStateException("Failed to invoke Skript pattern matcher.", exception);
        }
    }

    @FunctionalInterface
    private interface LockedRuntimeBody {
        void run();
    }
}
