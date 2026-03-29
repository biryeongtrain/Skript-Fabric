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
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.event.player.AttackEntityCallback;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.fabricmc.fabric.api.event.player.UseEntityCallback;
import net.fabricmc.fabric.api.event.player.UseItemCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLevelEvents;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.network.HashedStack;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ServerboundContainerClickPacket;
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
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.InsideBlockEffectApplier;
import net.minecraft.world.entity.Interaction;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.animal.cow.Cow;
import net.minecraft.world.entity.animal.dolphin.Dolphin;
import net.minecraft.world.entity.animal.pig.Pig;
import net.minecraft.world.entity.animal.fish.Pufferfish;
import net.minecraft.world.entity.animal.sheep.Sheep;
import net.minecraft.world.entity.animal.axolotl.Axolotl;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.item.PrimedTnt;
import net.minecraft.world.entity.monster.illager.Illusioner;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.entity.monster.skeleton.Skeleton;
import net.minecraft.world.entity.monster.piglin.Piglin;
import net.minecraft.world.entity.monster.spider.Spider;
import net.minecraft.world.entity.monster.zombie.Zombie;
import net.minecraft.world.entity.monster.zombie.ZombieVillager;
import net.minecraft.world.entity.monster.warden.Warden;
import net.minecraft.world.entity.npc.villager.Villager;
import net.minecraft.world.entity.player.Input;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.projectile.arrow.Arrow;
import net.minecraft.world.entity.projectile.FireworkRocketEntity;
import net.minecraft.world.entity.projectile.FishingHook;
import net.minecraft.world.entity.projectile.throwableitemprojectile.ThrownEgg;
import net.minecraft.world.entity.projectile.throwableitemprojectile.ThrownSplashPotion;
import net.minecraft.world.entity.vehicle.minecart.MinecartChest;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.CraftingMenu;
import net.minecraft.world.inventory.FurnaceResultSlot;
import net.minecraft.world.inventory.ResultSlot;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.inventory.StonecutterMenu;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.item.component.FireworkExplosion;
import net.minecraft.world.item.component.Fireworks;
import net.minecraft.world.item.equipment.Equippable;
import net.minecraft.world.item.trading.ItemCost;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraft.server.permissions.LevelBasedPermissionSet;
import net.minecraft.world.level.gamerules.GameRules;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.DispenserBlock;
import net.minecraft.world.level.block.SweetBerryBushBlock;
import net.minecraft.world.level.block.WitherRoseBlock;
import net.minecraft.world.level.block.entity.AbstractFurnaceBlockEntity;
import net.minecraft.world.level.block.entity.BeaconBlockEntity;
import net.minecraft.world.level.block.entity.BrewingStandBlockEntity;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.block.entity.ConduitBlockEntity;
import net.minecraft.world.level.block.entity.DispenserBlockEntity;
import net.minecraft.world.level.block.entity.HopperBlockEntity;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.state.BlockState;
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
import org.skriptlang.skript.fabric.compat.FabricBreedingItemSource;
import org.skriptlang.skript.fabric.compat.FabricBreedingState;
import org.skriptlang.skript.fabric.compat.FabricFishingState;
import org.skriptlang.skript.fabric.compat.FabricInventory;
import org.skriptlang.skript.fabric.compat.FabricItemType;
import org.skriptlang.skript.fabric.compat.FabricLocation;
import org.skriptlang.skript.fabric.compat.MinecraftResourceParser;
import org.skriptlang.skript.fabric.compat.PrivateBeaconAccess;
import org.skriptlang.skript.fabric.compat.PrivateBlockEntityAccess;
import org.skriptlang.skript.fabric.compat.PrivateEntityAccess;
import org.skriptlang.skript.fabric.compat.PrivateFishingHookAccess;
import org.skriptlang.skript.fabric.compat.PrivateFurnaceAccess;
import org.skriptlang.skript.fabric.runtime.FabricAttackEntityHandle;
import org.skriptlang.skript.fabric.runtime.FabricBlockBreakHandle;
import org.skriptlang.skript.fabric.runtime.FabricBreedingEventHandle;
import org.skriptlang.skript.fabric.runtime.FabricBreedingHandle;
import org.skriptlang.skript.fabric.runtime.FabricBrewingFuelHandle;
import org.skriptlang.skript.fabric.runtime.FabricDamageHandle;
import org.skriptlang.skript.fabric.runtime.FabricFishingEventHandle;
import org.skriptlang.skript.fabric.runtime.FabricPotionEffectCause;
import org.skriptlang.skript.fabric.runtime.FabricFishingHandle;
import org.skriptlang.skript.fabric.runtime.FabricPlayerInputHandle;
import org.skriptlang.skript.fabric.runtime.FabricUseEntityHandle;
import org.skriptlang.skript.fabric.runtime.FabricUseItemHandle;
import org.skriptlang.skript.fabric.runtime.SkriptFabricEventBridge;
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
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import ch.njol.skript.variables.Variables;

public final class SkriptFabricEventGameTest extends AbstractSkriptFabricGameTestSupport {

    @GameTest
    public void weatherChangeToRainExecutesRealScript(GameTestHelper helper) {
        runWithRuntimeLock(helper, () -> {
            SkriptRuntime runtime = SkriptRuntime.instance();
            runtime.clearScripts();
            Variables.clearAll();
            {
                var wd = helper.getLevel().getWeatherData();
                wd.setClearWeatherTime(0); wd.setRainTime(0); wd.setRaining(false); wd.setThundering(false);
            }
            runtime.loadFromResource("skript/gametest/event/weather_change_to_rain_sets_variable.sk");

            {
                var wd = helper.getLevel().getWeatherData();
                wd.setClearWeatherTime(0); wd.setRainTime(6000); wd.setRaining(true); wd.setThundering(false);
            }

            helper.assertTrue(
                    Boolean.TRUE.equals(Variables.getVariable("gametest::weather_change_to_rain", null, false)),
                    Component.literal("Expected real weather transition to execute the loaded weather change script.")
            );
            runtime.clearScripts();
        });
    }

    @GameTest
    public void worldSaveEventExecutesRealScript(GameTestHelper helper) {
        runWithRuntimeLock(helper, () -> {
            SkriptRuntime runtime = SkriptRuntime.instance();
            runtime.clearScripts();
            Variables.clearAll();
            runtime.loadFromResource("skript/gametest/event/world_save_marks_block.sk");

            BlockPos markerAbsolute = helper.absolutePos(new BlockPos(0, 1, 0));
            helper.getLevel().setBlockAndUpdate(markerAbsolute, Blocks.AIR.defaultBlockState());

            helper.getLevel().save(null, false, helper.getLevel().noSave());

            helper.assertTrue(
                    helper.getLevel().getBlockState(markerAbsolute).is(Blocks.EMERALD_BLOCK),
                    Component.literal("Expected the real world save path to execute the loaded world save script.")
            );
            runtime.clearScripts();
        });
    }

    @GameTest
    public void worldLoadAndUnloadEventsExecuteLifecycleScripts(GameTestHelper helper) {
        runWithRuntimeLock(helper, () -> {
            SkriptRuntime runtime = SkriptRuntime.instance();
            runtime.clearScripts();
            Variables.clearAll();
            runtime.loadFromResource("skript/gametest/event/world_load_unload_marks_blocks.sk");

            BlockPos loadMarker = helper.absolutePos(new BlockPos(0, 1, 0));
            BlockPos unloadMarker = helper.absolutePos(new BlockPos(1, 1, 0));
            helper.getLevel().setBlockAndUpdate(loadMarker, Blocks.AIR.defaultBlockState());
            helper.getLevel().setBlockAndUpdate(unloadMarker, Blocks.AIR.defaultBlockState());

            ServerLevelEvents.LOAD.invoker().onLevelLoad(helper.getLevel().getServer(), helper.getLevel());
            ServerLevelEvents.UNLOAD.invoker().onLevelUnload(helper.getLevel().getServer(), helper.getLevel());

            helper.assertTrue(
                    helper.getLevel().getBlockState(loadMarker).is(Blocks.EMERALD_BLOCK),
                    Component.literal("Expected the world load callback path to execute the loaded world lifecycle script.")
            );
            helper.assertTrue(
                    helper.getLevel().getBlockState(unloadMarker).is(Blocks.GOLD_BLOCK),
                    Component.literal("Expected the world unload callback path to execute the loaded world lifecycle script.")
            );
            runtime.clearScripts();
        });
    }

    @GameTest
    public void worldInitializationEventExecutesStartupScript(GameTestHelper helper) {
        runWithRuntimeLock(helper, () -> {
            helper.assertTrue(
                    WorldInitializationGameTestBootstrap.initializationCount(helper.getLevel().dimension()) == 1,
                    Component.literal("Expected the preloaded world initialization script to execute exactly once during the real createLevels path.")
            );
        });
    }

    @GameTest
    public void itemDispenseEventExecutesRealScript(GameTestHelper helper) {
        AtomicBoolean loaded = new AtomicBoolean(false);
        BlockPos dispenserPos = new BlockPos(0, 1, 0);
        BlockPos markerPos = dispenserPos.above();
        BlockPos powerPos = dispenserPos.below();
        AABB dropBox = AABB.encapsulatingFullBlocks(
                helper.absolutePos(new BlockPos(0, 1, 0)),
                helper.absolutePos(new BlockPos(2, 3, 1))
        );
        helper.succeedWhen(() -> {
            SkriptRuntime runtime = SkriptRuntime.instance();
            if (!loaded.get()) {
                if (!RUNTIME_LOCK.compareAndSet(false, true)) {
                    return;
                }
                runtime.clearScripts();
                Variables.clearAll();
                runtime.loadFromResource("skript/gametest/event/item_dispense_marks_block.sk");

                helper.getLevel().setBlockAndUpdate(helper.absolutePos(markerPos), Blocks.AIR.defaultBlockState());
                helper.getLevel().setBlockAndUpdate(helper.absolutePos(powerPos), Blocks.AIR.defaultBlockState());

                BlockState dispenserState = Blocks.DISPENSER.defaultBlockState()
                        .setValue(DispenserBlock.FACING, Direction.EAST);
                helper.getLevel().setBlockAndUpdate(helper.absolutePos(dispenserPos), dispenserState);

                DispenserBlockEntity dispenser = (DispenserBlockEntity) helper.getLevel().getBlockEntity(helper.absolutePos(dispenserPos));
                helper.assertTrue(
                        dispenser != null,
                        Component.literal("Expected dispenser block entity to exist for dispense event test.")
                );
                if (dispenser == null) {
                    runtime.clearScripts();
                    Variables.clearAll();
                    RUNTIME_LOCK.set(false);
                    throw new IllegalStateException("Dispenser block entity was not created.");
                }
                dispenser.setItem(0, new ItemStack(Items.APPLE));
                helper.getLevel().setBlockAndUpdate(helper.absolutePos(powerPos), Blocks.REDSTONE_BLOCK.defaultBlockState());
                loaded.set(true);
                return;
            }

            if (!helper.getLevel().getBlockState(helper.absolutePos(markerPos)).is(Blocks.EMERALD_BLOCK)) {
                return;
            }

            List<ItemEntity> drops = helper.getLevel().getEntitiesOfClass(ItemEntity.class, dropBox);
            helper.assertTrue(
                    drops.stream().anyMatch(item -> item.getItem().is(Items.APPLE)),
                    Component.literal("Expected the real dispenser path to spawn an apple item entity.")
            );
            runtime.clearScripts();
            Variables.clearAll();
            RUNTIME_LOCK.set(false);
        });
    }

    @GameTest
    public void fabricServerTickBridgeExecutesLoadedScript(GameTestHelper helper) {
        SkriptRuntime runtime = SkriptRuntime.instance();
        BlockPos absoluteTarget = new BlockPos(1, 80, 1);
        AtomicBoolean loaded = new AtomicBoolean(false);
        helper.succeedWhen(() -> {
            if (!loaded.get()) {
                if (!RUNTIME_LOCK.compareAndSet(false, true)) {
                    return;
                }
                runtime.clearScripts();
                helper.getLevel().setBlockAndUpdate(absoluteTarget, Blocks.AIR.defaultBlockState());
                runtime.loadFromResource("skript/gametest/event/server_tick_sets_block.sk");
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
            runtime.loadFromResource("skript/gametest/event/block_break_sets_block.sk");

            BlockPos brokenRelative = new BlockPos(0, 1, 0);
            BlockPos brokenAbsolute = helper.absolutePos(brokenRelative);

            helper.getLevel().setBlockAndUpdate(brokenAbsolute, Blocks.STONE.defaultBlockState());

            var player = helper.makeMockServerPlayerInLevel();
            player.setGameMode(GameType.SURVIVAL);
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
    public void blockDropProducerExecutesLoadedScript(GameTestHelper helper) {
        runWithRuntimeLock(helper, () -> {
            SkriptRuntime runtime = SkriptRuntime.instance();
            runtime.clearScripts();
            runtime.loadFromResource("skript/gametest/event/block_drop_marks_block.sk");

            BlockPos brokenAbsolute = helper.absolutePos(new BlockPos(2, 1, 0));
            BlockPos markerAbsolute = brokenAbsolute.above();

            helper.getLevel().setBlockAndUpdate(brokenAbsolute, Blocks.STONE.defaultBlockState());
            helper.getLevel().setBlockAndUpdate(markerAbsolute, Blocks.AIR.defaultBlockState());

            ServerPlayer player = helper.makeMockServerPlayerInLevel();
            player.setGameMode(GameType.SURVIVAL);
            player.teleportTo(brokenAbsolute.getX() + 0.5D, brokenAbsolute.getY(), brokenAbsolute.getZ() + 0.5D);

            helper.assertTrue(
                    player.gameMode.destroyBlock(brokenAbsolute),
                    Component.literal("Expected mock server player to break the dropped test block.")
            );
            helper.assertTrue(
                    helper.getLevel().getBlockState(markerAbsolute).is(Blocks.DIAMOND_BLOCK),
                    Component.literal("Expected real block drop producer to execute the loaded Skript file.")
            );
            runtime.clearScripts();
        });
    }

    @GameTest
    public void blockMineEventExecutesRealScript(GameTestHelper helper) {
        runWithRuntimeLock(helper, () -> {
            SkriptRuntime runtime = SkriptRuntime.instance();
            runtime.clearScripts();
            runtime.loadFromResource("skript/gametest/event/block_mine_sets_block.sk");

            BlockPos brokenAbsolute = helper.absolutePos(new BlockPos(3, 1, 0));
            BlockPos markerAbsolute = brokenAbsolute.above();

            helper.getLevel().setBlockAndUpdate(brokenAbsolute, Blocks.STONE.defaultBlockState());
            helper.getLevel().setBlockAndUpdate(markerAbsolute, Blocks.AIR.defaultBlockState());

            ServerPlayer player = helper.makeMockServerPlayerInLevel();
            player.setGameMode(GameType.SURVIVAL);
            player.teleportTo(brokenAbsolute.getX() + 0.5D, brokenAbsolute.getY(), brokenAbsolute.getZ() + 0.5D);

            helper.assertTrue(
                    player.gameMode.destroyBlock(brokenAbsolute),
                    Component.literal("Expected mock server player to mine the dropped test block.")
            );
            helper.assertTrue(
                    helper.getLevel().getBlockState(markerAbsolute).is(Blocks.EMERALD_BLOCK),
                    Component.literal("Expected the public mine syntax to execute through the real block break path.")
            );
            runtime.clearScripts();
        });
    }

    @GameTest
    public void inventoryClickEventExecutesRealScript(GameTestHelper helper) {
        runWithRuntimeLock(helper, () -> {
            SkriptRuntime runtime = SkriptRuntime.instance();
            runtime.clearScripts();
            Variables.clearAll();
            runtime.loadFromResource("skript/gametest/event/inventory_click_marks_block.sk");

            BlockPos chestAbsolute = helper.absolutePos(new BlockPos(19, 1, 3));
            helper.getLevel().setBlockAndUpdate(chestAbsolute, Blocks.CHEST.defaultBlockState());

            ChestBlockEntity chest = (ChestBlockEntity) helper.getLevel().getBlockEntity(chestAbsolute);
            helper.assertTrue(
                    chest != null,
                    Component.literal("Expected chest block entity to exist for the real inventory click path.")
            );
            if (chest == null) {
                throw new IllegalStateException("Inventory click test chest was not created.");
            }

            chest.setItem(0, new ItemStack(Items.STICK));
            chest.setChanged();

            ServerPlayer player = helper.makeMockServerPlayerInLevel();
            player.setGameMode(GameType.SURVIVAL);
            player.teleportTo(chestAbsolute.getX() + 0.5D, chestAbsolute.getY() + 1.0D, chestAbsolute.getZ() + 1.5D);
            player.openMenu(chest);

            player.connection.handleContainerClick(new ServerboundContainerClickPacket(
                    player.containerMenu.containerId,
                    player.containerMenu.getStateId(),
                    (short) 0,
                    (byte) 0,
                    ContainerInput.PICKUP,
                    new Int2ObjectOpenHashMap<>(),
                    HashedStack.EMPTY
            ));

            helper.assertTrue(
                    player.containerMenu.getSlot(0).getItem().isEmpty(),
                    Component.literal("Expected the real chest menu click path to take the stick from slot 0.")
            );
            helper.assertTrue(
                    helper.getLevel().getBlockState(chestAbsolute.below()).is(Blocks.EMERALD_BLOCK),
                    Component.literal("Expected the real inventory click packet path to execute the inventory click script.")
            );
            runtime.clearScripts();
            Variables.clearAll();
        });
    }

    @GameTest
    public void harvestSweetBerryBushExecutesRealScript(GameTestHelper helper) {
        runWithRuntimeLock(helper, () -> {
            SkriptRuntime runtime = SkriptRuntime.instance();
            runtime.clearScripts();
            runtime.loadFromResource("skript/gametest/event/harvest_sweet_berry_bush_marks_block.sk");

            BlockPos bushAbsolute = helper.absolutePos(new BlockPos(0, 1, 0));
            BlockPos markerAbsolute = helper.absolutePos(new BlockPos(2, 1, 0));
            helper.getLevel().setBlockAndUpdate(
                    bushAbsolute,
                    Blocks.SWEET_BERRY_BUSH.defaultBlockState().setValue(SweetBerryBushBlock.AGE, 3)
            );
            helper.getLevel().setBlockAndUpdate(markerAbsolute, Blocks.AIR.defaultBlockState());

            ServerPlayer player = helper.makeMockServerPlayerInLevel();
            player.setGameMode(GameType.SURVIVAL);
            player.teleportTo(bushAbsolute.getX() + 0.5D, bushAbsolute.getY(), bushAbsolute.getZ() + 1.5D);

            InteractionResult result = player.gameMode.useItemOn(
                    player,
                    helper.getLevel(),
                    player.getItemInHand(InteractionHand.MAIN_HAND),
                    InteractionHand.MAIN_HAND,
                    new BlockHitResult(Vec3.atCenterOf(bushAbsolute), Direction.NORTH, bushAbsolute, false)
            );

            helper.assertTrue(
                    result.consumesAction(),
                    Component.literal("Expected mock server player to harvest the ripe sweet berry bush.")
            );
            helper.assertTrue(
                    helper.getLevel().getBlockState(markerAbsolute).is(Blocks.DIAMOND_BLOCK),
                    Component.literal("Expected sweet berry bush harvest producer to execute the loaded Skript file.")
            );
            helper.assertTrue(
                    helper.getLevel().getBlockState(bushAbsolute).getValue(SweetBerryBushBlock.AGE) == 1,
                    Component.literal("Expected ripe sweet berry bush harvesting to reset the bush age to 1.")
            );
            runtime.clearScripts();
        });
    }

    @GameTest
    public void entityShootBowEventExecutesRealScript(GameTestHelper helper) {
        runWithRuntimeLock(helper, () -> {
            SkriptRuntime runtime = SkriptRuntime.instance();
            runtime.clearScripts();
            runtime.loadFromResource("skript/gametest/event/entity_shoot_bow_marks_block.sk");

            BlockPos markerAbsolute = helper.absolutePos(new BlockPos(4, 1, 0));
            helper.getLevel().setBlockAndUpdate(markerAbsolute, Blocks.AIR.defaultBlockState());

            Skeleton skeleton = (Skeleton) helper.spawnWithNoFreeWill(EntityType.SKELETON, 0.5F, 1.0F, 0.5F);
            skeleton.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.BOW));

            Cow target = (Cow) helper.spawnWithNoFreeWill(EntityType.COW, 6.5F, 1.0F, 0.5F);
            target.setCustomName(null);

            skeleton.performRangedAttack(target, 1.0F);

            helper.assertTrue(
                    helper.getLevel().getBlockState(markerAbsolute).is(Blocks.EMERALD_BLOCK),
                    Component.literal("Expected real bow shot path to expose the consumed projectile item inside the loaded Skript file.")
            );
            runtime.clearScripts();
        });
    }

    @GameTest
    public void blockPlaceProducerExecutesLoadedScript(GameTestHelper helper) {
        runWithRuntimeLock(helper, () -> {
            SkriptRuntime runtime = SkriptRuntime.instance();
            runtime.clearScripts();
            runtime.loadFromResource("skript/gametest/event/block_place_sets_block.sk");

            BlockPos supportAbsolute = helper.absolutePos(new BlockPos(1, 1, 1));
            BlockPos placedAbsolute = supportAbsolute.above();
            helper.getLevel().setBlockAndUpdate(supportAbsolute, Blocks.STONE.defaultBlockState());
            helper.getLevel().setBlockAndUpdate(placedAbsolute, Blocks.AIR.defaultBlockState());

            ServerPlayer player = helper.makeMockServerPlayerInLevel();
            player.setGameMode(GameType.SURVIVAL);
            player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.STONE));
            player.teleportTo(placedAbsolute.getX() + 0.5D, placedAbsolute.getY() + 1.0D, placedAbsolute.getZ() + 0.5D);

            InteractionResult result = player.getItemInHand(InteractionHand.MAIN_HAND).useOn(new UseOnContext(
                    player,
                    InteractionHand.MAIN_HAND,
                    new BlockHitResult(Vec3.atCenterOf(supportAbsolute), Direction.UP, supportAbsolute, false)
            ));

            helper.assertTrue(
                    result.consumesAction(),
                    Component.literal("Expected the real block item placement path to consume the player action.")
            );

            helper.assertTrue(
                    helper.getLevel().getBlockState(placedAbsolute.above()).is(Blocks.REDSTONE_BLOCK),
                    Component.literal("Expected the real block place producer to execute the loaded Skript file.")
            );
            runtime.clearScripts();
        });
    }

    @GameTest
    public void blockBurnProducerExecutesLoadedScript(GameTestHelper helper) {
        runWithRuntimeLock(helper, () -> {
            SkriptRuntime runtime = SkriptRuntime.instance();
            runtime.clearScripts();
            runtime.loadFromResource("skript/gametest/event/block_burn_marks_block.sk");

            BlockPos fireRelative = new BlockPos(0, 2, 0);
            BlockPos targetRelative = new BlockPos(1, 2, 0);
            BlockPos markerAbsolute = helper.absolutePos(targetRelative.above());
            BlockPos targetAbsolute = helper.absolutePos(targetRelative);

            helper.getLevel().getGameRules().set(GameRules.FIRE_SPREAD_RADIUS_AROUND_PLAYER, 128, helper.getLevel().getServer());
            helper.getLevel().setBlockAndUpdate(helper.absolutePos(new BlockPos(0, 1, 0)), Blocks.NETHERRACK.defaultBlockState());
            helper.getLevel().setBlockAndUpdate(helper.absolutePos(new BlockPos(1, 1, 0)), Blocks.STONE.defaultBlockState());
            helper.getLevel().setBlockAndUpdate(targetAbsolute, Blocks.OAK_PLANKS.defaultBlockState());
            helper.getLevel().setBlockAndUpdate(markerAbsolute, Blocks.AIR.defaultBlockState());
            helper.getLevel().setBlockAndUpdate(helper.absolutePos(fireRelative), Blocks.FIRE.defaultBlockState());

            for (int tick = 0; tick < 64 && helper.getLevel().getBlockState(markerAbsolute).is(Blocks.AIR); tick++) {
                BlockPos fireAbsolute = helper.absolutePos(fireRelative);
                helper.getLevel().getBlockState(fireAbsolute).tick(helper.getLevel(), fireAbsolute, helper.getLevel().getRandom());
            }

            helper.assertTrue(
                    !helper.getLevel().getBlockState(targetAbsolute).is(Blocks.OAK_PLANKS),
                    Component.literal("Expected fire spread to consume or replace the oak planks target block.")
            );
            helper.assertTrue(
                    helper.getLevel().getBlockState(markerAbsolute).is(Blocks.REDSTONE_BLOCK),
                    Component.literal("Expected real fire spread to execute the block burn Skript file.")
            );
            runtime.clearScripts();
        });
    }

    @GameTest
    public void blockFadeProducerExecutesLoadedScript(GameTestHelper helper) {
        runWithRuntimeLock(helper, () -> {
            SkriptRuntime runtime = SkriptRuntime.instance();
            runtime.clearScripts();
            runtime.loadFromResource("skript/gametest/event/block_fade_marks_block.sk");

            BlockPos iceRelative = new BlockPos(1, 2, 0);
            BlockPos iceAbsolute = helper.absolutePos(iceRelative);
            BlockPos markerAbsolute = iceAbsolute.above();

            helper.getLevel().setBlockAndUpdate(helper.absolutePos(new BlockPos(1, 1, 0)), Blocks.STONE.defaultBlockState());
            helper.getLevel().setBlockAndUpdate(iceAbsolute, Blocks.ICE.defaultBlockState());
            helper.getLevel().setBlockAndUpdate(markerAbsolute, Blocks.AIR.defaultBlockState());
            helper.getLevel().setBlockAndUpdate(helper.absolutePos(new BlockPos(2, 2, 0)), Blocks.GLOWSTONE.defaultBlockState());

            helper.getLevel().getBlockState(iceAbsolute).randomTick(helper.getLevel(), iceAbsolute, helper.getLevel().getRandom());

            helper.assertTrue(
                    !helper.getLevel().getBlockState(iceAbsolute).is(Blocks.ICE),
                    Component.literal("Expected real ice melt to replace the ice target block.")
            );
            helper.assertTrue(
                    helper.getLevel().getBlockState(markerAbsolute).is(Blocks.REDSTONE_BLOCK),
                    Component.literal("Expected real ice melt to execute the block fade Skript file.")
            );
            runtime.clearScripts();
        });
    }

    @GameTest
    public void blockFormProducerExecutesLoadedScript(GameTestHelper helper) {
        runWithRuntimeLock(helper, () -> {
            SkriptRuntime runtime = SkriptRuntime.instance();
            runtime.clearScripts();
            runtime.loadFromResource("skript/gametest/event/block_form_marks_block.sk");

            BlockPos anchor = helper.absolutePos(new BlockPos(1, 0, 0));
            BlockPos freezeTarget = null;
            for (int y = 240; y >= 120; y--) {
                BlockPos candidate = new BlockPos(anchor.getX(), y, anchor.getZ());
                helper.getLevel().setBlockAndUpdate(candidate.below(), Blocks.STONE.defaultBlockState());
                helper.getLevel().setBlockAndUpdate(candidate, Blocks.WATER.defaultBlockState());
                helper.getLevel().setBlockAndUpdate(candidate.above(), Blocks.AIR.defaultBlockState());
                if (helper.getLevel().getBiome(candidate).value().shouldFreeze(helper.getLevel(), candidate)) {
                    freezeTarget = candidate;
                    break;
                }
            }

            helper.assertTrue(
                    freezeTarget != null,
                    Component.literal("Expected to find a precipitation-freeze position in the GameTest column.")
            );

            helper.getLevel().tickPrecipitation(freezeTarget);

            helper.assertTrue(
                    helper.getLevel().getBlockState(freezeTarget).is(Blocks.ICE),
                    Component.literal("Expected precipitation freeze to form ice at the target position.")
            );
            helper.assertTrue(
                    helper.getLevel().getBlockState(freezeTarget.above()).is(Blocks.REDSTONE_BLOCK),
                    Component.literal("Expected real precipitation freeze to execute the block form Skript file.")
            );
            runtime.clearScripts();
        });
    }

    @GameTest
    public void plantGrowthCompatBridgeExecutesLoadedScript(GameTestHelper helper) {
        runWithRuntimeLock(helper, () -> {
            SkriptRuntime runtime = SkriptRuntime.instance();
            runtime.clearScripts();
            runtime.loadFromResource("skript/gametest/event/plant_growth_sets_blocks.sk");

            BlockPos cropAbsolute = helper.absolutePos(new BlockPos(0, 1, 0));
            helper.getLevel().setBlockAndUpdate(cropAbsolute, Blocks.WHEAT.defaultBlockState());

            SkriptFabricEventBridge.dispatchPlantGrowth(
                    helper.getLevel(),
                    cropAbsolute,
                    Blocks.WHEAT.defaultBlockState(),
                    Blocks.WHEAT.defaultBlockState()
            );
            SkriptFabricEventBridge.dispatchGrow(
                    helper.getLevel(),
                    cropAbsolute,
                    Blocks.WHEAT.defaultBlockState(),
                    Blocks.WHEAT.defaultBlockState(),
                    null
            );

            helper.assertTrue(
                    helper.getLevel().getBlockState(cropAbsolute.above()).is(Blocks.EMERALD_BLOCK),
                    Component.literal("Expected plant growth compat bridge to expose event-block in loaded Skript.")
            );
            helper.assertTrue(
                    helper.getLevel().getBlockState(helper.absolutePos(new BlockPos(3, 1, 0))).is(Blocks.GOLD_BLOCK),
                    Component.literal("Expected grow compat bridge to execute loaded Skript file.")
            );
            runtime.clearScripts();
        });
    }

    @GameTest
    public void cropGrowthProducerExecutesLoadedScript(GameTestHelper helper) {
        runWithRuntimeLock(helper, () -> {
            SkriptRuntime runtime = SkriptRuntime.instance();
            runtime.clearScripts();
            runtime.loadFromResource("skript/gametest/event/plant_growth_sets_blocks.sk");

            BlockPos cropAbsolute = helper.absolutePos(new BlockPos(0, 1, 0));
            BlockPos plantGrowthMarker = cropAbsolute.above();
            BlockPos growMarker = helper.absolutePos(new BlockPos(3, 1, 0));
            CropBlock crop = (CropBlock) Blocks.WHEAT;
            var initialState = crop.defaultBlockState();

            helper.getLevel().setBlockAndUpdate(plantGrowthMarker, Blocks.AIR.defaultBlockState());
            helper.getLevel().setBlockAndUpdate(growMarker, Blocks.AIR.defaultBlockState());
            helper.getLevel().setBlockAndUpdate(cropAbsolute, initialState);

            crop.performBonemeal(helper.getLevel(), helper.getLevel().getRandom(), cropAbsolute, initialState);

            helper.assertTrue(
                    !helper.getLevel().getBlockState(cropAbsolute).equals(initialState),
                    Component.literal("Expected bonemeal to grow the crop before asserting Skript event hooks.")
            );
            helper.assertTrue(
                    helper.getLevel().getBlockState(plantGrowthMarker).is(Blocks.EMERALD_BLOCK),
                    Component.literal("Expected the crop growth producer to fire the plant growth Skript event.")
            );
            helper.assertTrue(
                    helper.getLevel().getBlockState(growMarker).is(Blocks.GOLD_BLOCK),
                    Component.literal("Expected the crop growth producer to fire the grow Skript event.")
            );
            runtime.clearScripts();
        });
    }

    @GameTest
    public void sheepEatProducerExecutesLoadedScript(GameTestHelper helper) {
        runWithRuntimeLock(helper, () -> {
            SkriptRuntime runtime = SkriptRuntime.instance();
            runtime.clearScripts();
            runtime.loadFromResource("skript/gametest/event/entity_block_change_marks_block.sk");

            BlockPos changedAbsolute = helper.absolutePos(new BlockPos(1, 1, 0));
            helper.getLevel().setBlockAndUpdate(changedAbsolute, Blocks.GRASS_BLOCK.defaultBlockState());

            Sheep sheep = (Sheep) helper.spawnWithNoFreeWill(EntityType.SHEEP, 0.5F, 1.0F, 0.5F);
            sheep.teleportTo(changedAbsolute.getX() + 0.5D, changedAbsolute.getY() + 1.0D, changedAbsolute.getZ() + 0.5D);
            sheep.ate();

            helper.assertTrue(
                    helper.getLevel().getBlockState(changedAbsolute.above()).is(Blocks.REDSTONE_BLOCK),
                    Component.literal("Expected sheep eat producer to execute the loaded Skript file.")
            );
            runtime.clearScripts();
        });
    }

    @GameTest
    public void pressurePlateCompatBridgeExecutesLoadedScript(GameTestHelper helper) {
        runWithRuntimeLock(helper, () -> {
            SkriptRuntime runtime = SkriptRuntime.instance();
            runtime.clearScripts();
            runtime.loadFromResource("skript/gametest/event/pressure_plate_marks_blocks.sk");

            BlockPos pressurePos = helper.absolutePos(new BlockPos(2, 1, 0));
            BlockPos tripwirePos = helper.absolutePos(new BlockPos(3, 1, 0));
            BlockPos pressureMarker = helper.absolutePos(new BlockPos(4, 1, 0));
            BlockPos tripwireMarker = helper.absolutePos(new BlockPos(5, 1, 0));
            helper.getLevel().setBlockAndUpdate(pressurePos, Blocks.STONE_PRESSURE_PLATE.defaultBlockState());
            helper.getLevel().setBlockAndUpdate(tripwirePos, Blocks.TRIPWIRE.defaultBlockState());
            helper.getLevel().setBlockAndUpdate(pressureMarker, Blocks.AIR.defaultBlockState());
            helper.getLevel().setBlockAndUpdate(tripwireMarker, Blocks.AIR.defaultBlockState());

            ServerPlayer player = helper.makeMockServerPlayerInLevel();
            player.setGameMode(GameType.CREATIVE);
            player.teleportTo(pressurePos.getX() + 0.5D, pressurePos.getY(), pressurePos.getZ() + 0.5D);
            invokePressurePlateEntityInside(helper, pressurePos, player);

            ArmorStand tripwireProbe = new ArmorStand(EntityType.ARMOR_STAND, helper.getLevel());
            tripwireProbe.setPos(tripwirePos.getX() + 0.5D, tripwirePos.getY(), tripwirePos.getZ() + 0.5D);
            helper.getLevel().addFreshEntity(tripwireProbe);
            invokeTripWireEntityInside(helper, tripwirePos, tripwireProbe);

            helper.assertTrue(
                    helper.getLevel().getBlockState(pressureMarker).is(Blocks.LAPIS_BLOCK),
                    Component.literal("Expected stepping on a pressure plate to execute the loaded Skript file.")
            );
            helper.assertTrue(
                    helper.getLevel().getBlockState(tripwireMarker).is(Blocks.RED_WOOL),
                    Component.literal("Expected tripping tripwire to execute the loaded Skript file.")
            );
            runtime.clearScripts();
        });
    }

    @GameTest
    public void vehicleEntityCollisionProducerExecutesLoadedScript(GameTestHelper helper) {
        SkriptRuntime runtime = SkriptRuntime.instance();
        AtomicBoolean loaded = new AtomicBoolean(false);
        helper.succeedWhen(() -> {
            if (!loaded.get()) {
                if (!RUNTIME_LOCK.compareAndSet(false, true)) {
                    return;
                }
                runtime.clearScripts();
                runtime.loadFromResource("skript/gametest/event/vehicle_entity_collision_marks_block.sk");

                for (int x = 0; x <= 4; x++) {
                    BlockPos railAbsolute = helper.absolutePos(new BlockPos(x, 1, 0));
                    BlockPos powerAbsolute = railAbsolute.below();
                    helper.getLevel().setBlockAndUpdate(powerAbsolute, Blocks.REDSTONE_BLOCK.defaultBlockState());
                    helper.getLevel().setBlockAndUpdate(railAbsolute, Blocks.POWERED_RAIL.defaultBlockState());
                }

                BlockPos markerAbsolute = helper.absolutePos(new BlockPos(6, 1, 0));
                helper.getLevel().setBlockAndUpdate(markerAbsolute, Blocks.AIR.defaultBlockState());

                MinecartChest minecart = new MinecartChest(EntityType.CHEST_MINECART, helper.getLevel());
                minecart.setPos(0.5D, 1.0625D, 0.5D);
                minecart.setDeltaMovement(0.45D, 0.0D, 0.0D);
                helper.getLevel().addFreshEntity(minecart);

                Pig pig = (Pig) helper.spawnWithNoFreeWill(EntityType.PIG, 2.5F, 1.0F, 0.5F);
                pig.setDeltaMovement(Vec3.ZERO);

                loaded.set(true);
                return;
            }

            BlockPos markerAbsolute = helper.absolutePos(new BlockPos(6, 1, 0));
            helper.assertTrue(
                    helper.getLevel().getBlockState(markerAbsolute).is(Blocks.EMERALD_BLOCK),
                    Component.literal("Expected real minecart collision to execute the loaded Skript file.")
            );
            runtime.clearScripts();
            RUNTIME_LOCK.set(false);
        });
    }

    @GameTest
    public void attackEntityBridgeExecutesLoadedScript(GameTestHelper helper) {
        runWithRuntimeLock(helper, () -> {
            SkriptRuntime runtime = SkriptRuntime.instance();
            runtime.clearScripts();
            runtime.loadFromResource("skript/gametest/event/attack_entity_marks_target.sk");

            BlockPos playerMarkerAbsolute = helper.absolutePos(new BlockPos(5, 1, 0));
            helper.getLevel().setBlockAndUpdate(playerMarkerAbsolute, Blocks.AIR.defaultBlockState());

            ArmorStand armorStand = new ArmorStand(helper.getLevel(), 0.5D, 1.0D, 0.5D);
            helper.getLevel().addFreshEntity(armorStand);

            ServerPlayer player = helper.makeMockServerPlayerInLevel();
            player.setGameMode(GameType.SURVIVAL);
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
            runtime.loadFromResource("skript/gametest/event/use_block_sets_blocks.sk");

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
            runtime.loadFromResource("skript/gametest/event/use_entity_names_entity.sk");

            BlockPos playerMarkerAbsolute = helper.absolutePos(new BlockPos(3, 1, 0));
            helper.getLevel().setBlockAndUpdate(playerMarkerAbsolute, Blocks.AIR.defaultBlockState());

            ArmorStand armorStand = new ArmorStand(helper.getLevel(), 0.5D, 1.0D, 0.5D);
            helper.getLevel().addFreshEntity(armorStand);

            ServerPlayer player = helper.makeMockServerPlayerInLevel();
            player.setGameMode(GameType.CREATIVE);
            player.teleportTo(playerMarkerAbsolute.getX() + 0.5D, playerMarkerAbsolute.getY() + 1.0D, playerMarkerAbsolute.getZ() + 0.5D);
            player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.STICK));

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
    public void breedingEventExecutesRealScript(GameTestHelper helper) {
        runWithRuntimeLock(helper, () -> {
            SkriptRuntime runtime = SkriptRuntime.instance();
            runtime.clearScripts();
            runtime.loadFromResource("skript/gametest/event/breeding_event_marks_entities.sk");

            BlockPos playerMarkerAbsolute = helper.absolutePos(new BlockPos(12, 1, 0));
            helper.getLevel().setBlockAndUpdate(playerMarkerAbsolute, Blocks.AIR.defaultBlockState());

            Cow mother = (Cow) helper.spawnWithNoFreeWill(EntityType.COW, 0.5F, 1.0F, 0.5F);
            Cow father = (Cow) helper.spawnWithNoFreeWill(EntityType.COW, 2.5F, 1.0F, 0.5F);

            ServerPlayer player = helper.makeMockServerPlayerInLevel();
            player.setGameMode(GameType.CREATIVE);
            player.teleportTo(playerMarkerAbsolute.getX() + 0.5D, playerMarkerAbsolute.getY() + 1.0D, playerMarkerAbsolute.getZ() + 0.5D);

            mother.setInLove(player);
            father.setInLove(player);
            mother.spawnChildFromBreeding(helper.getLevel(), father);

            List<Cow> offspring = helper.getLevel().getEntitiesOfClass(
                    Cow.class,
                    mother.getBoundingBox().inflate(6.0D),
                    candidate -> candidate != mother && candidate != father
            );
            helper.assertTrue(
                    offspring.size() == 1,
                    Component.literal("Expected breeding event test to create exactly one offspring but got " + offspring.size() + ".")
            );
            if (offspring.isEmpty()) {
                throw new IllegalStateException("Breeding event test did not create an offspring.");
            }
            Cow baby = offspring.get(0);

            helper.assertTrue(
                    mother.getCustomName() != null && "breeding mother".equals(mother.getCustomName().getString()),
                    Component.literal("Expected breeding event script to resolve breeding mother.")
            );
            helper.assertTrue(
                    father.getCustomName() != null && "breeding father".equals(father.getCustomName().getString()),
                    Component.literal("Expected breeding event script to resolve breeding father.")
            );
            helper.assertTrue(
                    baby.getCustomName() != null && "bred offspring".equals(baby.getCustomName().getString()),
                    Component.literal("Expected breeding event script to resolve bred offspring.")
            );
            helper.assertTrue(
                    player.getCustomName() != null && "breeder".equals(player.getCustomName().getString()),
                    Component.literal("Expected breeding event script to resolve breeder.")
            );
            helper.assertTrue(
                    helper.getLevel().getBlockState(playerMarkerAbsolute).is(Blocks.YELLOW_WOOL),
                    Component.literal("Expected breeding event script to expose the breeder as event-player.")
            );
            runtime.clearScripts();
        });
    }

    @GameTest
    public void breedingEventItemExecutesRealScript(GameTestHelper helper) {
        runWithRuntimeLock(helper, () -> {
            SkriptRuntime runtime = SkriptRuntime.instance();
            runtime.clearScripts();
            runtime.loadFromResource("skript/gametest/event/breeding_event_item_marks_block.sk");

            BlockPos markerAbsolute = helper.absolutePos(new BlockPos(13, 1, 0));
            helper.getLevel().setBlockAndUpdate(markerAbsolute, Blocks.AIR.defaultBlockState());

            Cow mother = (Cow) helper.spawnWithNoFreeWill(EntityType.COW, 0.5F, 1.0F, 0.5F);
            Cow father = (Cow) helper.spawnWithNoFreeWill(EntityType.COW, 2.5F, 1.0F, 0.5F);

            ServerPlayer player = helper.makeMockServerPlayerInLevel();
            player.setGameMode(GameType.CREATIVE);
            player.teleportTo(markerAbsolute.getX() + 0.5D, markerAbsolute.getY() + 1.0D, markerAbsolute.getZ() + 0.5D);
            player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.WHEAT));

            InteractionResult motherResult = mother.interact(player, InteractionHand.MAIN_HAND, mother.position());
            InteractionResult fatherResult = father.interact(player, InteractionHand.MAIN_HAND, father.position());
            helper.assertTrue(
                    motherResult.consumesAction() && fatherResult.consumesAction(),
                    Component.literal("Expected feeding wheat to both parents to consume the player action.")
            );
            helper.assertTrue(
                    mother instanceof FabricBreedingItemSource && father instanceof FabricBreedingItemSource,
                    Component.literal("Expected breeding test animals to expose tracked breeding-item state.")
            );
            ItemStack motherLoveItem = ((FabricBreedingItemSource) mother).skript$getLastLoveItem();
            ItemStack fatherLoveItem = ((FabricBreedingItemSource) father).skript$getLastLoveItem();
            helper.assertTrue(
                    motherLoveItem.is(Items.WHEAT) || fatherLoveItem.is(Items.WHEAT),
                    Component.literal("Expected at least one parent to retain the wheat used to enter love mode.")
            );

            @SuppressWarnings("unchecked")
            Expression<? extends ItemStack> breedingEventItemExpression = parseExpressionInEvent(
                    "event-item",
                    new Class[]{ItemStack.class},
                    FabricBreedingEventHandle.class
            );
            helper.assertTrue(
                    breedingEventItemExpression != null,
                    Component.literal("Expected event-item to parse inside breeding events.")
            );
            if (breedingEventItemExpression == null) {
                throw new IllegalStateException("breeding event-item did not parse");
            }
            ItemStack resolvedBreedingItem = breedingEventItemExpression.getSingle(new org.skriptlang.skript.lang.event.SkriptEvent(
                    new FabricBreedingHandle(
                            helper.getLevel(),
                            mother,
                            father,
                            mother,
                            player,
                            motherLoveItem.isEmpty() ? fatherLoveItem : motherLoveItem
                    ),
                    helper.getLevel().getServer(),
                    helper.getLevel(),
                    player
            ));
            helper.assertTrue(
                    resolvedBreedingItem != null && resolvedBreedingItem.is(Items.WHEAT),
                    Component.literal("Expected breeding event-item expression to resolve the captured wheat item.")
            );
            FabricItemType wheatType = Classes.parse("wheat", FabricItemType.class, ParseContext.DEFAULT);
            helper.assertTrue(
                    wheatType != null && wheatType.matches(resolvedBreedingItem),
                    Component.literal("Expected bare item id 'wheat' to parse as an item type matching breeding event-item.")
            );
            Condition breedingEventItemIsWheat = parseConditionInEvent("event-item is wheat", FabricBreedingEventHandle.class);
            helper.assertTrue(
                    breedingEventItemIsWheat != null,
                    Component.literal("Expected breeding event-item comparison condition to parse.")
            );
            if (breedingEventItemIsWheat == null) {
                throw new IllegalStateException("breeding event-item is wheat did not parse");
            }

            mother.spawnChildFromBreeding(helper.getLevel(), father);

            helper.assertTrue(
                    helper.getLevel().getBlockState(markerAbsolute).is(Blocks.GOLD_BLOCK),
                    Component.literal("Expected breeding event-item script to execute and mark the breeder position.")
            );
            helper.assertTrue(
                    player.getCustomName() != null && "bred with item".equals(player.getCustomName().getString()),
                    Component.literal("Expected breeding event-item to resolve as a non-empty item inside the real script.")
            );
            runtime.clearScripts();
        });
    }

    @GameTest
    public void breedingFilteredCowEventExecutesRealScript(GameTestHelper helper) {
        runWithRuntimeLock(helper, () -> {
            SkriptRuntime runtime = SkriptRuntime.instance();
            runtime.clearScripts();
            runtime.loadFromResource("skript/gametest/event/breeding_of_cow_marks_block.sk");

            BlockPos markerAbsolute = helper.absolutePos(new BlockPos(14, 1, 0));
            helper.getLevel().setBlockAndUpdate(markerAbsolute, Blocks.AIR.defaultBlockState());

            Cow mother = (Cow) helper.spawnWithNoFreeWill(EntityType.COW, 0.5F, 1.0F, 0.5F);
            Cow father = (Cow) helper.spawnWithNoFreeWill(EntityType.COW, 2.5F, 1.0F, 0.5F);

            ServerPlayer player = helper.makeMockServerPlayerInLevel();
            player.setGameMode(GameType.CREATIVE);
            player.teleportTo(markerAbsolute.getX() + 0.5D, markerAbsolute.getY() + 1.0D, markerAbsolute.getZ() + 0.5D);

            mother.setInLove(player);
            father.setInLove(player);
            mother.spawnChildFromBreeding(helper.getLevel(), father);

            helper.assertTrue(
                    helper.getLevel().getBlockState(markerAbsolute).is(Blocks.LIME_WOOL),
                    Component.literal("Expected cow breeding filter to execute for a cow offspring.")
            );
            runtime.clearScripts();
        });
    }

    @GameTest
    public void breedingFilteredPigEventDoesNotExecuteForCowBreeding(GameTestHelper helper) {
        runWithRuntimeLock(helper, () -> {
            SkriptRuntime runtime = SkriptRuntime.instance();
            runtime.clearScripts();
            runtime.loadFromResource("skript/gametest/event/breeding_of_pig_marks_block.sk");

            BlockPos markerAbsolute = helper.absolutePos(new BlockPos(15, 1, 0));
            helper.getLevel().setBlockAndUpdate(markerAbsolute, Blocks.AIR.defaultBlockState());

            Cow mother = (Cow) helper.spawnWithNoFreeWill(EntityType.COW, 0.5F, 1.0F, 0.5F);
            Cow father = (Cow) helper.spawnWithNoFreeWill(EntityType.COW, 2.5F, 1.0F, 0.5F);

            ServerPlayer player = helper.makeMockServerPlayerInLevel();
            player.setGameMode(GameType.CREATIVE);
            player.teleportTo(markerAbsolute.getX() + 0.5D, markerAbsolute.getY() + 1.0D, markerAbsolute.getZ() + 0.5D);

            mother.setInLove(player);
            father.setInLove(player);
            mother.spawnChildFromBreeding(helper.getLevel(), father);

            helper.assertTrue(
                    helper.getLevel().getBlockState(markerAbsolute).is(Blocks.AIR),
                    Component.literal("Expected pig breeding filter to ignore a cow offspring.")
            );
            runtime.clearScripts();
        });
    }

    @GameTest
    public void loveModeEnterEventExecutesRealScript(GameTestHelper helper) {
        runWithRuntimeLock(helper, () -> {
            SkriptRuntime runtime = SkriptRuntime.instance();
            runtime.clearScripts();
            runtime.loadFromResource("skript/gametest/event/love_mode_enter_marks_entities.sk");

            BlockPos markerAbsolute = helper.absolutePos(new BlockPos(14, 1, 0));
            helper.getLevel().setBlockAndUpdate(markerAbsolute, Blocks.AIR.defaultBlockState());

            Cow cow = createCow(helper, false);
            ServerPlayer player = helper.makeMockServerPlayerInLevel();
            player.setGameMode(GameType.SURVIVAL);
            player.teleportTo(markerAbsolute.getX() + 0.5D, markerAbsolute.getY() + 1.0D, markerAbsolute.getZ() + 0.5D);
            player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.WHEAT));

            InteractionResult result = cow.interact(player, InteractionHand.MAIN_HAND, cow.position());

            helper.assertTrue(
                    result.consumesAction(),
                    Component.literal("Expected feeding a breeding item to consume the player action.")
            );
            helper.assertTrue(
                    cow.getCustomName() != null && "love mode entity".equals(cow.getCustomName().getString()),
                    Component.literal("Expected love mode event script to expose event-entity.")
            );
            helper.assertTrue(
                    player.getCustomName() != null && "love cause".equals(player.getCustomName().getString()),
                    Component.literal("Expected love mode event script to expose event-player.")
            );
            helper.assertTrue(
                    helper.getLevel().getBlockState(markerAbsolute).is(Blocks.PINK_CONCRETE),
                    Component.literal("Expected love mode event script to mark the block under the player.")
            );
            helper.assertTrue(
                    cow.isInLove(),
                    Component.literal("Expected the cow to enter love mode after the interaction.")
            );
            runtime.clearScripts();
        });
    }

    @GameTest
    public void bucketCatchEventExecutesRealScript(GameTestHelper helper) {
        runWithRuntimeLock(helper, () -> {
            SkriptRuntime runtime = SkriptRuntime.instance();
            runtime.clearScripts();
            runtime.loadFromResource("skript/gametest/event/bucket_catch_marks_player_and_block.sk");

            BlockPos markerRelative = new BlockPos(13, 1, 0);
            BlockPos markerAbsolute = helper.absolutePos(markerRelative);

            ServerPlayer player = helper.makeMockServerPlayerInLevel();
            player.setGameMode(GameType.SURVIVAL);
            player.teleportTo(markerAbsolute.getX() + 0.5D, markerAbsolute.getY() + 1.0D, markerAbsolute.getZ() + 0.5D);
            BlockPos liveMarker = player.blockPosition().below();
            helper.getLevel().setBlockAndUpdate(liveMarker, Blocks.AIR.defaultBlockState());
            player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.WATER_BUCKET));

            Pufferfish pufferfish = (Pufferfish) helper.spawnWithNoFreeWill(EntityType.PUFFERFISH, 13.5F, 2.0F, 0.5F);
            InteractionResult result = pufferfish.interact(player, InteractionHand.MAIN_HAND, pufferfish.position());

            helper.assertTrue(
                    result.consumesAction(),
                    Component.literal("Expected bucket catch interaction to consume the player action.")
            );
            helper.assertTrue(
                    player.getMainHandItem().is(Items.PUFFERFISH_BUCKET),
                    Component.literal("Expected bucket catch interaction to replace the water bucket with a pufferfish bucket.")
            );
            helper.assertTrue(
                    helper.getLevel().getBlockState(liveMarker).is(Blocks.EMERALD_BLOCK),
                    Component.literal("Expected bucket catch script to mark the block under the player at " + liveMarker + ".")
            );
            helper.assertTrue(
                    player.getCustomName() != null && "bucket catcher".equals(player.getCustomName().getString()),
                    Component.literal("Expected bucket catch script to expose the bucketing player as event-player.")
            );
            helper.assertTrue(
                    !pufferfish.isAlive() || pufferfish.isRemoved(),
                    Component.literal("Expected captured pufferfish to be removed from the world.")
            );
            runtime.clearScripts();
        });
    }

    @GameTest
    public void filteredBucketCatchEventExecutesRealScript(GameTestHelper helper) {
        runWithRuntimeLock(helper, () -> {
            SkriptRuntime runtime = SkriptRuntime.instance();
            runtime.clearScripts();
            runtime.loadFromResource("skript/gametest/event/bucket_filtered_catch_names_player.sk");

            ServerPlayer player = helper.makeMockServerPlayerInLevel();
            player.setGameMode(GameType.SURVIVAL);
            player.teleportTo(13.5D, 2.0D, 1.5D);
            player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.WATER_BUCKET));
            BlockPos futureMarker = player.blockPosition().below();

            Pufferfish pufferfish = (Pufferfish) helper.spawnWithNoFreeWill(EntityType.PUFFERFISH, 13.5F, 2.0F, 1.5F);
            InteractionResult result = pufferfish.interact(player, InteractionHand.MAIN_HAND, pufferfish.position());

            helper.assertTrue(
                    result.consumesAction(),
                    Component.literal("Expected filtered bucket catch interaction to consume the player action.")
            );
            helper.assertTrue(
                    player.getCustomName() != null && "puffer catcher".equals(player.getCustomName().getString()),
                    Component.literal(
                            "Expected filtered bucket catch script to match only the pufferfish trigger and resolve future event-item. "
                                    + "actualName="
                                    + (player.getCustomName() == null ? "null" : player.getCustomName().getString())
                                    + ", hand="
                                    + player.getMainHandItem()
                                    + ", futureMarker="
                                    + helper.getLevel().getBlockState(futureMarker)
                    )
            );
            helper.assertTrue(
                    !pufferfish.isAlive() || pufferfish.isRemoved(),
                    Component.literal("Expected filtered bucket catch test to remove the captured pufferfish.")
            );
            runtime.clearScripts();
        });
    }

    @GameTest
    public void fishingLineCastAndStateChangeEventsExecuteRealScript(GameTestHelper helper) {
        runWithRuntimeLock(helper, () -> {
            SkriptRuntime runtime = SkriptRuntime.instance();
            runtime.clearScripts();
            runtime.loadFromResource("skript/gametest/event/fishing_line_cast_and_state_change_marks_player.sk");

            BlockPos markerAbsolute = helper.absolutePos(new BlockPos(6, 1, 1));
            helper.getLevel().setBlockAndUpdate(markerAbsolute, Blocks.AIR.defaultBlockState());

            ServerPlayer player = helper.makeMockServerPlayerInLevel();
            player.setGameMode(GameType.CREATIVE);
            player.teleportTo(markerAbsolute.getX() + 0.5D, markerAbsolute.getY() + 1.0D, markerAbsolute.getZ() + 0.5D);

            new FishingHook(player, helper.getLevel(), 0, 0);

            helper.assertTrue(
                    helper.getLevel().getBlockState(markerAbsolute).is(Blocks.EMERALD_BLOCK),
                    Component.literal("Expected fishing line cast event script to mark the block under the player.")
            );
            helper.assertTrue(
                    player.getCustomName() != null && "state changed".equals(player.getCustomName().getString()),
                    Component.literal("Expected fishing state change script to name the player.")
            );
            runtime.clearScripts();
        });
    }

    @GameTest
    public void fishingEntityHookAndInGroundEventsExecuteRealScript(GameTestHelper helper) {
        runWithRuntimeLock(helper, () -> {
            SkriptRuntime runtime = SkriptRuntime.instance();
            runtime.clearScripts();
            runtime.loadFromResource("skript/gametest/event/fishing_entity_hook_and_in_ground_mark_targets.sk");

            BlockPos markerAbsolute = helper.absolutePos(new BlockPos(7, 1, 1));
            helper.getLevel().setBlockAndUpdate(markerAbsolute, Blocks.AIR.defaultBlockState());

            ServerPlayer player = helper.makeMockServerPlayerInLevel();
            player.setGameMode(GameType.CREATIVE);
            player.teleportTo(markerAbsolute.getX() + 0.5D, markerAbsolute.getY() + 1.0D, markerAbsolute.getZ() + 0.5D);

            FishingHook entityHook = new FishingHook(player, helper.getLevel(), 0, 0);
            ArmorStand armorStand = new ArmorStand(helper.getLevel(), markerAbsolute.getX() + 1.5D, markerAbsolute.getY() + 1.0D, markerAbsolute.getZ() + 0.5D);
            helper.getLevel().addFreshEntity(armorStand);
            PrivateFishingHookAccess.onHitEntity(entityHook, armorStand);

            helper.assertTrue(
                    armorStand.getCustomName() != null && "entity hooked".equals(armorStand.getCustomName().getString()),
                    Component.literal("Expected entity-hooked event script to rename the hooked entity.")
            );

            FishingHook groundHook = new FishingHook(player, helper.getLevel(), 0, 0);
            PrivateFishingHookAccess.onHitBlock(
                    groundHook,
                    new BlockHitResult(
                            Vec3.atCenterOf(markerAbsolute),
                            Direction.UP,
                            markerAbsolute,
                            false
                    )
            );

            helper.assertTrue(
                    helper.getLevel().getBlockState(markerAbsolute).is(Blocks.REDSTONE_BLOCK),
                    Component.literal("Expected bobber-hit-ground event script to mark the block under the player.")
            );
            runtime.clearScripts();
        });
    }

    @GameTest
    public void fishingLuredBiteAndEscapeEventsExecuteRealScript(GameTestHelper helper) {
        runWithRuntimeLock(helper, () -> {
            SkriptRuntime runtime = SkriptRuntime.instance();
            runtime.clearScripts();
            runtime.loadFromResource("skript/gametest/event/fishing_lured_bite_escape_marks_targets.sk");

            BlockPos luredMarkerAbsolute = helper.absolutePos(new BlockPos(8, 1, 1));
            BlockPos escapeMarkerAbsolute = helper.absolutePos(new BlockPos(9, 1, 1));
            helper.getLevel().setBlockAndUpdate(luredMarkerAbsolute, Blocks.AIR.defaultBlockState());
            helper.getLevel().setBlockAndUpdate(escapeMarkerAbsolute, Blocks.AIR.defaultBlockState());

            ServerPlayer player = helper.makeMockServerPlayerInLevel();
            player.setGameMode(GameType.CREATIVE);

            player.teleportTo(luredMarkerAbsolute.getX() + 0.5D, luredMarkerAbsolute.getY() + 1.0D, luredMarkerAbsolute.getZ() + 0.5D);
            FishingHook luredHook = new FishingHook(player, helper.getLevel(), 0, 0);
            PrivateFishingHookAccess.setCurrentState(luredHook, "BOBBING");
            PrivateFishingHookAccess.setTimeUntilLured(luredHook, 0);
            PrivateFishingHookAccess.setTimeUntilHooked(luredHook, 0);
            PrivateFishingHookAccess.setNibble(luredHook, 0);
            PrivateFishingHookAccess.setBiting(luredHook, false);
            PrivateFishingHookAccess.catchingFish(luredHook, luredHook.blockPosition());
            helper.assertTrue(
                    helper.getLevel().getBlockState(luredMarkerAbsolute).is(Blocks.GOLD_BLOCK),
                    Component.literal("Expected fish-approaching event script to mark the block under the player.")
            );

            FishingHook biteHook = new FishingHook(player, helper.getLevel(), 0, 0);
            PrivateFishingHookAccess.setCurrentState(biteHook, "BOBBING");
            PrivateFishingHookAccess.setTimeUntilLured(biteHook, 0);
            PrivateFishingHookAccess.setTimeUntilHooked(biteHook, 1);
            PrivateFishingHookAccess.setNibble(biteHook, 0);
            PrivateFishingHookAccess.setBiting(biteHook, false);
            PrivateFishingHookAccess.catchingFish(biteHook, biteHook.blockPosition());
            helper.assertTrue(
                    biteHook.getCustomName() != null && "fish bite".equals(biteHook.getCustomName().getString()),
                    Component.literal("Expected fish-bite event script to rename the hook.")
            );

            player.teleportTo(escapeMarkerAbsolute.getX() + 0.5D, escapeMarkerAbsolute.getY() + 1.0D, escapeMarkerAbsolute.getZ() + 0.5D);
            FishingHook escapeHook = new FishingHook(player, helper.getLevel(), 0, 0);
            PrivateFishingHookAccess.setCurrentState(escapeHook, "BOBBING");
            PrivateFishingHookAccess.setTimeUntilLured(escapeHook, 0);
            PrivateFishingHookAccess.setTimeUntilHooked(escapeHook, 0);
            PrivateFishingHookAccess.setNibble(escapeHook, 1);
            PrivateFishingHookAccess.setBiting(escapeHook, false);
            PrivateFishingHookAccess.catchingFish(escapeHook, escapeHook.blockPosition());
            helper.assertTrue(
                    helper.getLevel().getBlockState(escapeMarkerAbsolute).is(Blocks.IRON_BLOCK),
                    Component.literal("Expected fish-escape event script to mark the block under the player.")
            );
            runtime.clearScripts();
        });
    }

    @GameTest
    public void fishingCaughtAndReelInEventsExecuteRealScript(GameTestHelper helper) {
        runWithRuntimeLock(helper, () -> {
            SkriptRuntime runtime = SkriptRuntime.instance();
            runtime.clearScripts();
            runtime.loadFromResource("skript/gametest/event/fishing_caught_and_reel_in_marks_targets.sk");

            BlockPos caughtMarkerAbsolute = helper.absolutePos(new BlockPos(10, 1, 1));
            helper.getLevel().setBlockAndUpdate(caughtMarkerAbsolute, Blocks.AIR.defaultBlockState());

            ServerPlayer player = helper.makeMockServerPlayerInLevel();
            player.setGameMode(GameType.CREATIVE);
            player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.FISHING_ROD));
            player.teleportTo(caughtMarkerAbsolute.getX() + 0.5D, caughtMarkerAbsolute.getY() + 1.0D, caughtMarkerAbsolute.getZ() + 0.5D);

            FishingHook caughtHook = new FishingHook(player, helper.getLevel(), 0, 0);
            PrivateFishingHookAccess.setNibble(caughtHook, 1);
            PrivateFishingHookAccess.setTimeUntilLured(caughtHook, 0);
            PrivateFishingHookAccess.setTimeUntilHooked(caughtHook, 0);
            PrivateFishingHookAccess.setBiting(caughtHook, false);
            caughtHook.retrieve(new ItemStack(Items.FISHING_ROD));

            helper.assertTrue(
                    helper.getLevel().getBlockState(caughtMarkerAbsolute).is(Blocks.DIAMOND_BLOCK),
                    Component.literal("Expected fish-caught event script to mark the block under the player.")
            );

            FishingHook reelHook = new FishingHook(player, helper.getLevel(), 0, 0);
            reelHook.retrieve(new ItemStack(Items.FISHING_ROD));

            helper.assertTrue(
                    player.getCustomName() != null && "reeled in".equals(player.getCustomName().getString()),
                    Component.literal("Expected reel-in event script to rename the player.")
            );
            runtime.clearScripts();
        });
    }

    @GameTest
    public void playerInputAnyPressEventExecutesRealScript(GameTestHelper helper) {
        runWithRuntimeLock(helper, () -> {
            SkriptRuntime runtime = SkriptRuntime.instance();
            runtime.clearScripts();
            runtime.loadFromResource("skript/gametest/event/player_input_any_press_marks_block.sk");

            BlockPos markerAbsolute = helper.absolutePos(new BlockPos(9, 1, 0));
            helper.getLevel().setBlockAndUpdate(markerAbsolute, Blocks.AIR.defaultBlockState());

            ServerPlayer player = helper.makeMockServerPlayerInLevel();
            player.setGameMode(GameType.CREATIVE);
            player.teleportTo(markerAbsolute.getX() + 0.5D, markerAbsolute.getY() + 1.0D, markerAbsolute.getZ() + 0.5D);

            sendPlayerInput(player, new Input(true, false, false, false, false, false, false));

            helper.assertTrue(
                    helper.getLevel().getBlockState(markerAbsolute).is(Blocks.DIAMOND_BLOCK),
                    Component.literal("Expected any-key press event script to mark the block under the player.")
            );
            helper.assertTrue(
                    player.getCustomName() == null,
                    Component.literal("Expected any-key press event test not to execute the release handler.")
            );
            runtime.clearScripts();
        });
    }

    @GameTest
    public void playerInputForwardReleaseEventExecutesRealScript(GameTestHelper helper) {
        runWithRuntimeLock(helper, () -> {
            SkriptRuntime runtime = SkriptRuntime.instance();
            runtime.clearScripts();
            runtime.loadFromResource("skript/gametest/event/player_input_forward_release_marks_block.sk");

            BlockPos markerAbsolute = helper.absolutePos(new BlockPos(10, 1, 0));
            helper.getLevel().setBlockAndUpdate(markerAbsolute, Blocks.AIR.defaultBlockState());

            ServerPlayer player = helper.makeMockServerPlayerInLevel();
            player.setGameMode(GameType.CREATIVE);
            player.teleportTo(markerAbsolute.getX() + 0.5D, markerAbsolute.getY() + 1.0D, markerAbsolute.getZ() + 0.5D);

            sendPlayerInput(player, new Input(true, false, false, false, false, false, false));
            sendPlayerInput(player, Input.EMPTY);

            helper.assertTrue(
                    helper.getLevel().getBlockState(markerAbsolute).is(Blocks.EMERALD_BLOCK),
                    Component.literal("Expected forward-key release event script to mark the block under the player.")
            );
            helper.assertTrue(
                    player.getCustomName() == null,
                    Component.literal("Expected forward-key release event test not to execute the jump release handler.")
            );
            runtime.clearScripts();
        });
    }

    @GameTest
    public void playerInputSneakToggleEventExecutesRealScript(GameTestHelper helper) {
        runWithRuntimeLock(helper, () -> {
            SkriptRuntime runtime = SkriptRuntime.instance();
            runtime.clearScripts();
            runtime.loadFromResource("skript/gametest/event/player_input_sneak_toggle_marks_block.sk");

            BlockPos markerAbsolute = helper.absolutePos(new BlockPos(11, 1, 0));
            helper.getLevel().setBlockAndUpdate(markerAbsolute, Blocks.AIR.defaultBlockState());

            ServerPlayer player = helper.makeMockServerPlayerInLevel();
            player.setGameMode(GameType.CREATIVE);
            player.teleportTo(markerAbsolute.getX() + 0.5D, markerAbsolute.getY() + 1.0D, markerAbsolute.getZ() + 0.5D);

            sendPlayerInput(player, new Input(false, false, false, false, false, true, false));

            helper.assertTrue(
                    helper.getLevel().getBlockState(markerAbsolute).is(Blocks.REDSTONE_BLOCK),
                    Component.literal("Expected sneak-key toggle event script to mark the block under the player.")
            );
            helper.assertTrue(
                    player.getCustomName() == null,
                    Component.literal("Expected sneak-key toggle event test not to execute the sprint toggle handler.")
            );
            runtime.clearScripts();
        });
    }

    @GameTest
    public void brewingFuelFilteredEventExecutesRealScript(GameTestHelper helper) {
        runWithRuntimeLock(helper, () -> {
            SkriptRuntime runtime = SkriptRuntime.instance();
            runtime.clearScripts();
            runtime.loadFromResource("skript/gametest/event/brewing_fuel_of_blaze_powder_marks_block.sk");

            BlockPos brewingRelative = new BlockPos(10, 1, 0);
            BlockPos brewingAbsolute = helper.absolutePos(brewingRelative);
            helper.getLevel().setBlockAndUpdate(brewingAbsolute, Blocks.BREWING_STAND.defaultBlockState());

            BrewingStandBlockEntity brewingStand = (BrewingStandBlockEntity) helper.getLevel().getBlockEntity(brewingAbsolute);
            helper.assertTrue(
                    brewingStand != null,
                    Component.literal("Expected brewing stand block entity to exist for filtered brewing fuel test.")
            );
            if (brewingStand == null) {
                throw new IllegalStateException("Brewing stand block entity was not created.");
            }

            for (int slot = 0; slot < 3; slot++) {
                brewingStand.setItem(slot, PotionContents.createItemStack(Items.POTION, Potions.WATER));
            }
            brewingStand.setItem(3, new ItemStack(Items.NETHER_WART));
            brewingStand.setItem(4, new ItemStack(Items.BLAZE_POWDER));
            PrivateBlockEntityAccess.setBrewingFuel(brewingStand, 0);

            BrewingStandBlockEntity.serverTick(
                    helper.getLevel(),
                    brewingAbsolute,
                    helper.getLevel().getBlockState(brewingAbsolute),
                    brewingStand
            );

            helper.assertBlockPresent(Blocks.EMERALD_BLOCK, new BlockPos(10, 2, 0));
            helper.assertTrue(
                    PrivateBlockEntityAccess.brewingFuel(brewingStand) > 0,
                    Component.literal("Expected filtered brewing fuel test to actually consume fuel.")
            );
            runtime.clearScripts();
        });
    }

    @GameTest
    public void brewingFuelMismatchedFilterDoesNotExecuteRealScript(GameTestHelper helper) {
        runWithRuntimeLock(helper, () -> {
            SkriptRuntime runtime = SkriptRuntime.instance();
            runtime.clearScripts();
            runtime.loadFromResource("skript/gametest/event/brewing_fuel_of_redstone_marks_block.sk");

            BlockPos brewingRelative = new BlockPos(11, 1, 0);
            BlockPos brewingAbsolute = helper.absolutePos(brewingRelative);
            helper.getLevel().setBlockAndUpdate(brewingAbsolute, Blocks.BREWING_STAND.defaultBlockState());

            BrewingStandBlockEntity brewingStand = (BrewingStandBlockEntity) helper.getLevel().getBlockEntity(brewingAbsolute);
            helper.assertTrue(
                    brewingStand != null,
                    Component.literal("Expected brewing stand block entity to exist for mismatched brewing fuel test.")
            );
            if (brewingStand == null) {
                throw new IllegalStateException("Brewing stand block entity was not created.");
            }

            for (int slot = 0; slot < 3; slot++) {
                brewingStand.setItem(slot, PotionContents.createItemStack(Items.POTION, Potions.WATER));
            }
            brewingStand.setItem(3, new ItemStack(Items.NETHER_WART));
            brewingStand.setItem(4, new ItemStack(Items.BLAZE_POWDER));
            PrivateBlockEntityAccess.setBrewingFuel(brewingStand, 0);

            BrewingStandBlockEntity.serverTick(
                    helper.getLevel(),
                    brewingAbsolute,
                    helper.getLevel().getBlockState(brewingAbsolute),
                    brewingStand
            );

            helper.assertTrue(
                    helper.getLevel().getBlockState(helper.absolutePos(new BlockPos(11, 2, 0))).is(Blocks.AIR),
                    Component.literal("Expected redstone brewing fuel filter to ignore blaze powder fuel.")
            );
            helper.assertTrue(
                    PrivateBlockEntityAccess.brewingFuel(brewingStand) > 0,
                    Component.literal("Expected mismatched brewing fuel test to still run the live serverTick fuel path.")
            );
            runtime.clearScripts();
        });
    }

    @GameTest
    public void brewingCompleteEventExecutesRealScript(GameTestHelper helper) {
        runWithRuntimeLock(helper, () -> {
            SkriptRuntime runtime = SkriptRuntime.instance();
            runtime.clearScripts();
            runtime.loadFromResource("skript/gametest/event/brewing_complete_clears_results.sk");

            BlockPos brewingRelative = new BlockPos(11, 1, 0);
            BlockPos brewingAbsolute = helper.absolutePos(brewingRelative);
            helper.getLevel().setBlockAndUpdate(brewingAbsolute, Blocks.BREWING_STAND.defaultBlockState());

            BrewingStandBlockEntity brewingStand = (BrewingStandBlockEntity) helper.getLevel().getBlockEntity(brewingAbsolute);
            helper.assertTrue(
                    brewingStand != null,
                    Component.literal("Expected brewing stand block entity to exist for brewing complete event test.")
            );
            if (brewingStand == null) {
                throw new IllegalStateException("Brewing stand block entity was not created.");
            }

            for (int slot = 0; slot < 3; slot++) {
                brewingStand.setItem(slot, PotionContents.createItemStack(Items.POTION, Potions.WATER));
            }
            brewingStand.setItem(3, new ItemStack(Items.NETHER_WART));
            PrivateBlockEntityAccess.setBrewingTime(brewingStand, 1);

            BrewingStandBlockEntity.serverTick(
                    helper.getLevel(),
                    brewingAbsolute,
                    helper.getLevel().getBlockState(brewingAbsolute),
                    brewingStand
            );

            helper.assertBlockPresent(Blocks.LAPIS_BLOCK, new BlockPos(11, 2, 0));
            for (int slot = 0; slot < 3; slot++) {
                helper.assertTrue(
                        brewingStand.getItem(slot).isEmpty(),
                        Component.literal("Expected brewing complete script to clear bottle slot " + slot + ".")
                );
            }
            runtime.clearScripts();
        });
    }

    @GameTest
    public void brewingCompleteItemFilteredEventExecutesRealScript(GameTestHelper helper) {
        runWithRuntimeLock(helper, () -> {
            SkriptRuntime runtime = SkriptRuntime.instance();
            runtime.clearScripts();
            runtime.loadFromResource("skript/gametest/event/brewing_complete_for_potion_marks_block.sk");

            BlockPos brewingRelative = new BlockPos(11, 1, 0);
            BlockPos brewingAbsolute = helper.absolutePos(brewingRelative);
            helper.getLevel().setBlockAndUpdate(brewingAbsolute, Blocks.BREWING_STAND.defaultBlockState());

            BrewingStandBlockEntity brewingStand = (BrewingStandBlockEntity) helper.getLevel().getBlockEntity(brewingAbsolute);
            helper.assertTrue(
                    brewingStand != null,
                    Component.literal("Expected brewing stand block entity to exist for filtered brewing complete item test.")
            );
            if (brewingStand == null) {
                throw new IllegalStateException("Brewing stand block entity was not created.");
            }

            for (int slot = 0; slot < 3; slot++) {
                brewingStand.setItem(slot, PotionContents.createItemStack(Items.POTION, Potions.WATER));
            }
            brewingStand.setItem(3, new ItemStack(Items.NETHER_WART));
            PrivateBlockEntityAccess.setBrewingTime(brewingStand, 1);

            BrewingStandBlockEntity.serverTick(
                    helper.getLevel(),
                    brewingAbsolute,
                    helper.getLevel().getBlockState(brewingAbsolute),
                    brewingStand
            );

            helper.assertBlockPresent(Blocks.DIAMOND_BLOCK, new BlockPos(11, 2, 0));
            helper.assertTrue(
                    brewingStand.getItem(0).is(Items.POTION),
                    Component.literal("Expected filtered brewing complete item test to keep brewed bottle results as potions.")
            );
            runtime.clearScripts();
        });
    }

    @GameTest
    public void brewingCompleteEffectFilteredEventExecutesRealScript(GameTestHelper helper) {
        runWithRuntimeLock(helper, () -> {
            SkriptRuntime runtime = SkriptRuntime.instance();
            runtime.clearScripts();
            runtime.loadFromResource("skript/gametest/event/brewing_complete_for_speed_marks_block.sk");

            BlockPos brewingRelative = new BlockPos(12, 1, 0);
            BlockPos brewingAbsolute = helper.absolutePos(brewingRelative);
            helper.getLevel().setBlockAndUpdate(brewingAbsolute, Blocks.BREWING_STAND.defaultBlockState());

            BrewingStandBlockEntity brewingStand = (BrewingStandBlockEntity) helper.getLevel().getBlockEntity(brewingAbsolute);
            helper.assertTrue(
                    brewingStand != null,
                    Component.literal("Expected brewing stand block entity to exist for filtered brewing complete effect test.")
            );
            if (brewingStand == null) {
                throw new IllegalStateException("Brewing stand block entity was not created.");
            }

            for (int slot = 0; slot < 3; slot++) {
                brewingStand.setItem(slot, PotionContents.createItemStack(Items.POTION, Potions.AWKWARD));
            }
            brewingStand.setItem(3, new ItemStack(Items.SUGAR));
            PrivateBlockEntityAccess.setBrewingTime(brewingStand, 1);

            BrewingStandBlockEntity.serverTick(
                    helper.getLevel(),
                    brewingAbsolute,
                    helper.getLevel().getBlockState(brewingAbsolute),
                    brewingStand
            );

            helper.assertBlockPresent(Blocks.EMERALD_BLOCK, new BlockPos(12, 2, 0));
            PotionContents potionContents = brewingStand.getItem(0).get(DataComponents.POTION_CONTENTS);
            helper.assertTrue(
                    potionContents != null && containsPotionEffect(potionContents, MobEffects.SPEED),
                    Component.literal("Expected filtered brewing complete effect test to brew a speed potion.")
            );
            runtime.clearScripts();
        });
    }

    @GameTest
    public void brewingStartEventExecutesRealScript(GameTestHelper helper) {
        runWithRuntimeLock(helper, () -> {
            SkriptRuntime runtime = SkriptRuntime.instance();
            runtime.clearScripts();
            runtime.loadFromResource("skript/gametest/event/brewing_start_sets_time_and_marks_block.sk");

            BlockPos brewingRelative = new BlockPos(12, 1, 0);
            BlockPos brewingAbsolute = helper.absolutePos(brewingRelative);
            helper.getLevel().setBlockAndUpdate(brewingAbsolute, Blocks.BREWING_STAND.defaultBlockState());

            BrewingStandBlockEntity brewingStand = (BrewingStandBlockEntity) helper.getLevel().getBlockEntity(brewingAbsolute);
            helper.assertTrue(
                    brewingStand != null,
                    Component.literal("Expected brewing stand block entity to exist for brewing start event test.")
            );
            if (brewingStand == null) {
                throw new IllegalStateException("Brewing stand block entity was not created.");
            }

            for (int slot = 0; slot < 3; slot++) {
                brewingStand.setItem(slot, PotionContents.createItemStack(Items.POTION, Potions.WATER));
            }
            brewingStand.setItem(3, new ItemStack(Items.NETHER_WART));
            PrivateBlockEntityAccess.setBrewingFuel(brewingStand, 1);

            BrewingStandBlockEntity.serverTick(
                    helper.getLevel(),
                    brewingAbsolute,
                    helper.getLevel().getBlockState(brewingAbsolute),
                    brewingStand
            );

            helper.assertBlockPresent(Blocks.EMERALD_BLOCK, new BlockPos(12, 2, 0));
            helper.assertTrue(
                    PrivateBlockEntityAccess.brewingTime(brewingStand) == 1,
                    Component.literal("Expected brewing start script to set the brewing time to 1 tick.")
            );
            runtime.clearScripts();
        });
    }

    @GameTest
    public void lootGenerateEventExecutesRealScript(GameTestHelper helper) {
        runWithRuntimeLock(helper, () -> {
            SkriptRuntime runtime = SkriptRuntime.instance();
            runtime.clearScripts();
            runtime.loadFromResource("skript/gametest/event/loot_generate_clears_chest_loot.sk");

            BlockPos chestRelative = new BlockPos(15, 1, 0);
            BlockPos chestAbsolute = helper.absolutePos(chestRelative);
            helper.getLevel().setBlockAndUpdate(chestAbsolute, Blocks.CHEST.defaultBlockState());

            ChestBlockEntity chest = (ChestBlockEntity) helper.getLevel().getBlockEntity(chestAbsolute);
            helper.assertTrue(
                    chest != null,
                    Component.literal("Expected chest block entity to exist for loot generate event test.")
            );
            if (chest == null) {
                throw new IllegalStateException("Chest block entity was not created.");
            }

            chest.setLootTable(BuiltInLootTables.SIMPLE_DUNGEON);
            chest.setLootTableSeed(37L);

            ServerPlayer player = helper.makeMockServerPlayerInLevel();
            player.setGameMode(GameType.CREATIVE);
            player.teleportTo(chestAbsolute.getX() + 0.5D, chestAbsolute.getY() + 1.0D, chestAbsolute.getZ() + 0.5D);

            chest.unpackLootTable(player);

            int nonEmptySlots = 0;
            StringBuilder contents = new StringBuilder();
            for (int slot = 0; slot < chest.getContainerSize(); slot++) {
                ItemStack stack = chest.getItem(slot);
                if (stack.isEmpty()) {
                    continue;
                }
                nonEmptySlots++;
                if (contents.length() > 0) {
                    contents.append(", ");
                }
                contents.append(stack.getCount()).append("x").append(stack.getItem());
            }

            helper.assertTrue(
                    nonEmptySlots == 0,
                    Component.literal("Expected loot generate script to clear all generated loot after deleting loot. Contents: " + contents)
            );
            runtime.clearScripts();
        });
    }

    @GameTest
    public void furnaceFuelBurnEventExecutesRealScript(GameTestHelper helper) {
        runWithRuntimeLock(helper, () -> {
            SkriptRuntime runtime = SkriptRuntime.instance();
            runtime.clearScripts();
            runtime.loadFromResource("skript/gametest/event/furnace_fuel_burn_marks_block.sk");

            AbstractFurnaceBlockEntity furnace = createFurnace(helper, new BlockPos(11, 1, 1));
            furnace.setItem(0, new ItemStack(Items.RAW_IRON));
            furnace.setItem(1, new ItemStack(Items.COAL));

            tickFurnace(helper, furnace);

            helper.assertBlockPresent(Blocks.EMERALD_BLOCK, new BlockPos(11, 2, 1));
            helper.assertTrue(
                    PrivateFurnaceAccess.litTimeRemaining(furnace) == 10,
                    Component.literal("Expected fuel burn script to set the remaining burn time to 10 ticks.")
            );
            helper.assertTrue(
                    PrivateFurnaceAccess.litTotalTime(furnace) == 10,
                    Component.literal("Expected fuel burn script to set the total burn time to 10 ticks.")
            );
            runtime.clearScripts();
        });
    }

    @GameTest
    public void furnaceSmeltingStartEventExecutesRealScript(GameTestHelper helper) {
        runWithRuntimeLock(helper, () -> {
            SkriptRuntime runtime = SkriptRuntime.instance();
            runtime.clearScripts();
            runtime.loadFromResource("skript/gametest/event/furnace_smelting_start_marks_block.sk");

            AbstractFurnaceBlockEntity furnace = createFurnace(helper, new BlockPos(12, 1, 1));
            furnace.setItem(0, new ItemStack(Items.RAW_IRON));
            furnace.setItem(1, new ItemStack(Items.COAL));

            tickFurnace(helper, furnace);

            helper.assertBlockPresent(Blocks.DIAMOND_BLOCK, new BlockPos(12, 2, 1));
            int actualTotalCookTime = PrivateFurnaceAccess.cookingTotalTime(furnace);
            helper.assertTrue(
                    actualTotalCookTime == 40,
                    Component.literal("Expected smelting start script to set the total cook time to 40 ticks. Actual: " + actualTotalCookTime)
            );
            helper.assertTrue(
                    PrivateFurnaceAccess.cookingTimer(furnace) == 1,
                    Component.literal("Expected furnace tick to keep cooking progress moving after the smelting start event.")
            );
            runtime.clearScripts();
        });
    }

    @GameTest
    public void furnaceSmeltEventExecutesRealScript(GameTestHelper helper) {
        runWithRuntimeLock(helper, () -> {
            SkriptRuntime runtime = SkriptRuntime.instance();
            runtime.clearScripts();
            runtime.loadFromResource("skript/gametest/event/furnace_smelt_marks_block.sk");

            AbstractFurnaceBlockEntity furnace = createFurnace(helper, new BlockPos(13, 1, 1));
            furnace.setItem(0, new ItemStack(Items.RAW_IRON));
            furnace.setItem(1, new ItemStack(Items.COAL));
            PrivateFurnaceAccess.setCookingTotalTime(furnace, 1);

            tickFurnace(helper, furnace);

            helper.assertBlockPresent(Blocks.GOLD_BLOCK, new BlockPos(13, 2, 1));
            helper.assertTrue(
                    furnace.getItem(2).is(Items.IRON_INGOT) && furnace.getItem(2).getCount() == 1,
                    Component.literal("Expected furnace smelt bridge to produce one iron ingot after the real furnace tick.")
            );
            runtime.clearScripts();
        });
    }

    @GameTest
    public void furnaceExtractEventExecutesRealScript(GameTestHelper helper) {
        runWithRuntimeLock(helper, () -> {
            SkriptRuntime runtime = SkriptRuntime.instance();
            runtime.clearScripts();
            runtime.loadFromResource("skript/gametest/event/furnace_extract_marks_block.sk");

            AbstractFurnaceBlockEntity furnace = createFurnace(helper, new BlockPos(14, 1, 1));
            furnace.setItem(2, new ItemStack(Items.IRON_INGOT, 3));

            ServerPlayer player = helper.makeMockServerPlayerInLevel();
            player.setGameMode(GameType.CREATIVE);
            player.teleportTo(furnace.getBlockPos().getX() + 1.5D, furnace.getBlockPos().getY() + 1.0D, furnace.getBlockPos().getZ() + 0.5D);

            FurnaceResultSlot resultSlot = new FurnaceResultSlot(player, furnace, 2, 0, 0);
            ItemStack extracted = resultSlot.remove(2);
            resultSlot.onTake(player, extracted);

            helper.assertBlockPresent(Blocks.REDSTONE_BLOCK, new BlockPos(14, 2, 1));
            helper.assertTrue(
                    player.getCustomName() != null && "furnace extractor".equals(player.getCustomName().getString()),
                    Component.literal("Expected furnace extract script to receive the extracting player as event-player.")
            );
            runtime.clearScripts();
        });
    }

    @GameTest
    public void prepareCraftEventExecutesRealScript(GameTestHelper helper) {
        runWithRuntimeLock(helper, () -> {
            SkriptRuntime runtime = SkriptRuntime.instance();
            runtime.clearScripts();
            runtime.loadFromResource("skript/gametest/event/prepare_craft_names_player.sk");

            BlockPos tableAbsolute = helper.absolutePos(new BlockPos(15, 1, 1));
            helper.getLevel().setBlockAndUpdate(tableAbsolute, Blocks.CRAFTING_TABLE.defaultBlockState());

            ServerPlayer player = helper.makeMockServerPlayerInLevel();
            player.setGameMode(GameType.CREATIVE);
            player.teleportTo(tableAbsolute.getX() + 0.5D, tableAbsolute.getY() + 1.0D, tableAbsolute.getZ() + 1.5D);

            CraftingMenu menu = new CraftingMenu(0, player.getInventory(), ContainerLevelAccess.create(helper.getLevel(), tableAbsolute));
            menu.getInputGridSlots().get(0).set(new ItemStack(Items.OAK_PLANKS));
            menu.getInputGridSlots().get(3).set(new ItemStack(Items.OAK_PLANKS));
            menu.slotsChanged(new SimpleContainer(0));

            helper.assertTrue(
                    menu.getResultSlot().getItem().is(Items.STICK) && menu.getResultSlot().getItem().getCount() == 4,
                    Component.literal("Expected the crafting menu preview to produce sticks after the real crafting-grid recompute.")
            );
            helper.assertTrue(
                    player.getCustomName() != null && "prepare craft".equals(player.getCustomName().getString()),
                    Component.literal("Expected prepare-craft event script to rename the player during the real preview path.")
            );
            runtime.clearScripts();
        });
    }

    @GameTest
    public void craftEventExecutesRealScript(GameTestHelper helper) {
        runWithRuntimeLock(helper, () -> {
            SkriptRuntime runtime = SkriptRuntime.instance();
            runtime.clearScripts();
            runtime.loadFromResource("skript/gametest/event/craft_names_player.sk");

            BlockPos tableAbsolute = helper.absolutePos(new BlockPos(16, 1, 1));
            helper.getLevel().setBlockAndUpdate(tableAbsolute, Blocks.CRAFTING_TABLE.defaultBlockState());

            ServerPlayer player = helper.makeMockServerPlayerInLevel();
            player.setGameMode(GameType.CREATIVE);
            player.teleportTo(tableAbsolute.getX() + 0.5D, tableAbsolute.getY() + 1.0D, tableAbsolute.getZ() + 1.5D);

            CraftingMenu menu = new CraftingMenu(0, player.getInventory(), ContainerLevelAccess.create(helper.getLevel(), tableAbsolute));
            menu.getInputGridSlots().get(0).set(new ItemStack(Items.OAK_PLANKS));
            menu.getInputGridSlots().get(3).set(new ItemStack(Items.OAK_PLANKS));
            menu.slotsChanged(menu.getSlot(1).container);

            ResultSlot resultSlot = (ResultSlot) menu.getResultSlot();
            ItemStack crafted = resultSlot.remove(4);
            resultSlot.onTake(player, crafted);

            helper.assertTrue(
                    crafted.is(Items.STICK) && crafted.getCount() == 4,
                    Component.literal("Expected the crafting result slot to return four sticks from the real craft take path.")
            );
            helper.assertTrue(
                    player.getCustomName() != null && "craft".equals(player.getCustomName().getString()),
                    Component.literal("Expected craft event script to rename the player during the real result-take path.")
            );
            runtime.clearScripts();
        });
    }

    @GameTest
    public void stonecuttingEventExecutesRealScript(GameTestHelper helper) {
        runWithRuntimeLock(helper, () -> {
            SkriptRuntime runtime = SkriptRuntime.instance();
            runtime.clearScripts();
            runtime.loadFromResource("skript/gametest/event/stonecutting_names_player.sk");

            BlockPos stonecutterAbsolute = helper.absolutePos(new BlockPos(17, 1, 1));
            helper.getLevel().setBlockAndUpdate(stonecutterAbsolute, Blocks.STONECUTTER.defaultBlockState());

            ServerPlayer player = helper.makeMockServerPlayerInLevel();
            player.setGameMode(GameType.CREATIVE);
            player.teleportTo(stonecutterAbsolute.getX() + 0.5D, stonecutterAbsolute.getY() + 1.0D, stonecutterAbsolute.getZ() + 1.5D);

            StonecutterMenu menu = new StonecutterMenu(0, player.getInventory(), ContainerLevelAccess.create(helper.getLevel(), stonecutterAbsolute));
            menu.getSlot(0).set(new ItemStack(Items.STONE));
            menu.slotsChanged(menu.getSlot(0).container);

            boolean selectedStoneSlab = false;
            for (int i = 0; i < menu.getNumberOfVisibleRecipes(); i++) {
                menu.clickMenuButton(player, i);
                if (menu.getSlot(1).getItem().is(Items.STONE_SLAB)) {
                    selectedStoneSlab = true;
                    break;
                }
            }

            helper.assertTrue(
                    selectedStoneSlab,
                    Component.literal("Expected the real stonecutter menu to expose a stone slab recipe for stone input.")
            );

            ItemStack moved = menu.quickMoveStack(player, 1);

            helper.assertTrue(
                    moved.is(Items.STONE_SLAB) && moved.getCount() > 0,
                    Component.literal("Expected the stonecutter quick-move path to return the selected stone slab output.")
            );
            helper.assertTrue(
                    player.getCustomName() != null && "stonecutting".equals(player.getCustomName().getString()),
                    Component.literal("Expected stonecutting event script to rename the player during the real stonecutter quick-move path.")
            );
            runtime.clearScripts();
        });
    }

    @GameTest
    public void inventoryMoveEventExecutesRealScript(GameTestHelper helper) {
        runWithRuntimeLock(helper, () -> {
            SkriptRuntime runtime = SkriptRuntime.instance();
            runtime.clearScripts();
            Variables.clearAll();
            runtime.loadFromResource("skript/gametest/event/inventory_move_sets_variable.sk");

            BlockPos hopperAbsolute = helper.absolutePos(new BlockPos(19, 1, 1));
            BlockPos chestAbsolute = hopperAbsolute.above();
            helper.getLevel().setBlockAndUpdate(hopperAbsolute, Blocks.HOPPER.defaultBlockState());
            helper.getLevel().setBlockAndUpdate(chestAbsolute, Blocks.CHEST.defaultBlockState());

            HopperBlockEntity hopper = (HopperBlockEntity) helper.getLevel().getBlockEntity(hopperAbsolute);
            ChestBlockEntity chest = (ChestBlockEntity) helper.getLevel().getBlockEntity(chestAbsolute);
            helper.assertTrue(
                    hopper != null && chest != null,
                    Component.literal("Expected hopper and chest block entities to exist for the real inventory move path.")
            );
            if (hopper == null || chest == null) {
                throw new IllegalStateException("Inventory move test containers were not created.");
            }

            chest.setItem(0, new ItemStack(Items.STICK));
            chest.setChanged();

            HopperBlockEntity.pushItemsTick(
                    helper.getLevel(),
                    hopperAbsolute,
                    helper.getLevel().getBlockState(hopperAbsolute),
                    hopper
            );

            helper.assertTrue(
                    hopper.getItem(0).is(Items.STICK),
                    Component.literal("Expected the real hopper pull path to move the stick into the hopper inventory.")
            );
            helper.assertTrue(
                    Boolean.TRUE.equals(Variables.getVariable("gametest::inventory_move_seen", null, false)),
                    Component.literal("Expected the real hopper transfer path to execute the inventory move script.")
            );
            runtime.clearScripts();
            Variables.clearAll();
        });
    }

    @GameTest
    public void playerLeashEventExecutesRealScript(GameTestHelper helper) {
        runWithRuntimeLock(helper, () -> {
            SkriptRuntime runtime = SkriptRuntime.instance();
            runtime.clearScripts();
            runtime.loadFromResource("skript/gametest/event/player_leash_names_player.sk");

            ServerPlayer leashHolder = helper.makeMockServerPlayerInLevel();
            Cow cow = createCow(helper, false);
            leashHolder.setCustomName(null);
            cow.setLeashedTo(leashHolder, true);

            helper.assertTrue(
                    leashHolder.getCustomName() != null && "leasher".equals(leashHolder.getCustomName().getString()),
                    Component.literal("Expected player leash event script to rename the event-player during the real attach path.")
            );
            runtime.clearScripts();
        });
    }

    @GameTest
    public void furnaceFilteredFuelBurnEventExecutesRealScript(GameTestHelper helper) {
        runWithRuntimeLock(helper, () -> {
            SkriptRuntime runtime = SkriptRuntime.instance();
            runtime.clearScripts();
            runtime.loadFromResource("skript/gametest/event/furnace_filtered_fuel_burn_marks_block.sk");

            helper.getLevel().setBlockAndUpdate(helper.absolutePos(new BlockPos(1, 2, 2)), Blocks.AIR.defaultBlockState());
            helper.getLevel().setBlockAndUpdate(helper.absolutePos(new BlockPos(2, 2, 2)), Blocks.AIR.defaultBlockState());

            AbstractFurnaceBlockEntity matchingBurn = createFurnace(helper, new BlockPos(1, 1, 2));
            matchingBurn.setItem(0, new ItemStack(Items.RAW_COPPER));
            matchingBurn.setItem(1, new ItemStack(Items.COAL));
            tickFurnace(helper, matchingBurn);
            helper.assertBlockPresent(Blocks.EMERALD_BLOCK, new BlockPos(1, 2, 2));

            AbstractFurnaceBlockEntity nonMatchingBurn = createFurnace(helper, new BlockPos(2, 1, 2));
            nonMatchingBurn.setItem(0, new ItemStack(Items.RAW_COPPER));
            nonMatchingBurn.setItem(1, new ItemStack(Items.BLAZE_ROD));
            tickFurnace(helper, nonMatchingBurn);
            helper.assertTrue(
                    helper.getLevel().getBlockState(helper.absolutePos(new BlockPos(2, 2, 2))).is(Blocks.AIR),
                    Component.literal("Expected non-matching fuel burn filter to leave the marker block untouched.")
            );

            runtime.clearScripts();
        });
    }

    @GameTest
    public void furnaceFilteredSmeltingStartEventExecutesRealScript(GameTestHelper helper) {
        runWithRuntimeLock(helper, () -> {
            SkriptRuntime runtime = SkriptRuntime.instance();
            runtime.clearScripts();
            runtime.loadFromResource("skript/gametest/event/furnace_filtered_smelting_start_marks_block.sk");

            helper.getLevel().setBlockAndUpdate(helper.absolutePos(new BlockPos(3, 2, 2)), Blocks.AIR.defaultBlockState());
            helper.getLevel().setBlockAndUpdate(helper.absolutePos(new BlockPos(4, 2, 2)), Blocks.AIR.defaultBlockState());

            AbstractFurnaceBlockEntity matchingStart = createFurnace(helper, new BlockPos(3, 1, 2));
            matchingStart.setItem(0, new ItemStack(Items.RAW_IRON));
            matchingStart.setItem(1, new ItemStack(Items.BLAZE_ROD));
            tickFurnace(helper, matchingStart);
            helper.assertBlockPresent(Blocks.DIAMOND_BLOCK, new BlockPos(3, 2, 2));

            AbstractFurnaceBlockEntity nonMatchingStart = createFurnace(helper, new BlockPos(4, 1, 2));
            nonMatchingStart.setItem(0, new ItemStack(Items.RAW_COPPER));
            nonMatchingStart.setItem(1, new ItemStack(Items.BLAZE_ROD));
            tickFurnace(helper, nonMatchingStart);
            helper.assertTrue(
                    helper.getLevel().getBlockState(helper.absolutePos(new BlockPos(4, 2, 2))).is(Blocks.AIR),
                    Component.literal("Expected non-matching smelting-start filter to leave the marker block untouched.")
            );

            runtime.clearScripts();
        });
    }

    @GameTest
    public void furnaceFilteredSmeltEventExecutesRealScript(GameTestHelper helper) {
        runWithRuntimeLock(helper, () -> {
            SkriptRuntime runtime = SkriptRuntime.instance();
            runtime.clearScripts();
            runtime.loadFromResource("skript/gametest/event/furnace_filtered_smelt_marks_block.sk");

            helper.getLevel().setBlockAndUpdate(helper.absolutePos(new BlockPos(5, 2, 2)), Blocks.AIR.defaultBlockState());
            helper.getLevel().setBlockAndUpdate(helper.absolutePos(new BlockPos(6, 2, 2)), Blocks.AIR.defaultBlockState());

            AbstractFurnaceBlockEntity matchingSmelt = createFurnace(helper, new BlockPos(5, 1, 2));
            matchingSmelt.setItem(0, new ItemStack(Items.RAW_IRON));
            matchingSmelt.setItem(1, new ItemStack(Items.BLAZE_ROD));
            PrivateFurnaceAccess.setCookingTotalTime(matchingSmelt, 1);
            tickFurnace(helper, matchingSmelt);
            helper.assertBlockPresent(Blocks.GOLD_BLOCK, new BlockPos(5, 2, 2));

            AbstractFurnaceBlockEntity nonMatchingSmelt = createFurnace(helper, new BlockPos(6, 1, 2));
            nonMatchingSmelt.setItem(0, new ItemStack(Items.RAW_COPPER));
            nonMatchingSmelt.setItem(1, new ItemStack(Items.BLAZE_ROD));
            PrivateFurnaceAccess.setCookingTotalTime(nonMatchingSmelt, 1);
            tickFurnace(helper, nonMatchingSmelt);
            helper.assertTrue(
                    helper.getLevel().getBlockState(helper.absolutePos(new BlockPos(6, 2, 2))).is(Blocks.AIR),
                    Component.literal("Expected non-matching smelt filter to leave the marker block untouched.")
            );

            runtime.clearScripts();
        });
    }

    @GameTest
    public void furnaceFilteredExtractEventExecutesRealScript(GameTestHelper helper) {
        runWithRuntimeLock(helper, () -> {
            SkriptRuntime runtime = SkriptRuntime.instance();
            runtime.clearScripts();
            runtime.loadFromResource("skript/gametest/event/furnace_filtered_extract_marks_block.sk");

            helper.getLevel().setBlockAndUpdate(helper.absolutePos(new BlockPos(7, 2, 2)), Blocks.AIR.defaultBlockState());
            helper.getLevel().setBlockAndUpdate(helper.absolutePos(new BlockPos(8, 2, 2)), Blocks.AIR.defaultBlockState());

            ServerPlayer player = helper.makeMockServerPlayerInLevel();
            player.setGameMode(GameType.CREATIVE);

            AbstractFurnaceBlockEntity matchingExtract = createFurnace(helper, new BlockPos(7, 1, 2));
            matchingExtract.setItem(2, new ItemStack(Items.IRON_INGOT, 3));
            player.teleportTo(matchingExtract.getBlockPos().getX() + 1.5D, matchingExtract.getBlockPos().getY() + 1.0D, matchingExtract.getBlockPos().getZ() + 0.5D);
            FurnaceResultSlot matchingResultSlot = new FurnaceResultSlot(player, matchingExtract, 2, 0, 0);
            ItemStack extractedIron = matchingResultSlot.remove(2);
            matchingResultSlot.onTake(player, extractedIron);
            helper.assertBlockPresent(Blocks.REDSTONE_BLOCK, new BlockPos(7, 2, 2));

            AbstractFurnaceBlockEntity nonMatchingExtract = createFurnace(helper, new BlockPos(8, 1, 2));
            nonMatchingExtract.setItem(2, new ItemStack(Items.GOLD_INGOT, 3));
            player.teleportTo(nonMatchingExtract.getBlockPos().getX() + 1.5D, nonMatchingExtract.getBlockPos().getY() + 1.0D, nonMatchingExtract.getBlockPos().getZ() + 0.5D);
            FurnaceResultSlot nonMatchingResultSlot = new FurnaceResultSlot(player, nonMatchingExtract, 2, 0, 0);
            ItemStack extractedGold = nonMatchingResultSlot.remove(2);
            nonMatchingResultSlot.onTake(player, extractedGold);
            helper.assertTrue(
                    helper.getLevel().getBlockState(helper.absolutePos(new BlockPos(8, 2, 2))).is(Blocks.AIR),
                    Component.literal("Expected non-matching furnace extract filter to leave the marker block untouched.")
            );

            runtime.clearScripts();
        });
    }

    @GameTest
    public void potionEffectAddedEventExecutesRealScript(GameTestHelper helper) {
        runWithRuntimeLock(helper, () -> {
            SkriptRuntime runtime = SkriptRuntime.instance();
            runtime.clearScripts();
            runtime.loadFromResource("skript/gametest/event/potion_effect_added_names_entity.sk");

            Cow cow = createCow(helper, false);
            cow.addEffect(new MobEffectInstance(MobEffects.POISON, 200, 0));

            helper.assertTrue(
                    cow.getCustomName() != null && "poison added".equals(cow.getCustomName().getString()),
                    Component.literal("Expected entity potion add event script to rename the entity.")
            );
            runtime.clearScripts();
        });
    }

    @GameTest
    public void potionEffectAddedEventAcceptsNamespacedEffectId(GameTestHelper helper) {
        runWithRuntimeLock(helper, () -> {
            SkriptRuntime runtime = SkriptRuntime.instance();
            runtime.clearScripts();
            runtime.loadFromResource("skript/gametest/event/potion_effect_added_namespaced_id_names_entity.sk");

            Cow cow = createCow(helper, false);
            cow.addEffect(new MobEffectInstance(MobEffects.POISON, 200, 0));

            helper.assertTrue(
                    cow.getCustomName() != null && "namespaced poison added".equals(cow.getCustomName().getString()),
                    Component.literal("Expected entity potion add event script to match an explicit namespaced effect id.")
            );
            runtime.clearScripts();
        });
    }

    @GameTest
    public void potionEffectDueToPotionDrinkExecutesRealScript(GameTestHelper helper) {
        runWithRuntimeLock(helper, () -> {
            SkriptRuntime runtime = SkriptRuntime.instance();
            runtime.clearScripts();
            runtime.loadFromResource("skript/gametest/event/potion_effect_due_to_potion_drink_names_entity.sk");

            ServerPlayer player = helper.makeMockServerPlayerInLevel();
            player.setGameMode(GameType.SURVIVAL);
            player.setCustomName(null);
            player.setItemInHand(InteractionHand.MAIN_HAND, PotionContents.createItemStack(Items.POTION, Potions.POISON));
            player.getItemInHand(InteractionHand.MAIN_HAND).finishUsingItem(helper.getLevel(), player);

            helper.assertTrue(
                    player.getCustomName() != null && "potion drink".equals(player.getCustomName().getString()),
                    Component.literal("Expected potion-drink cause filter to rename the affected player. Actual name: "
                            + (player.getCustomName() == null ? "null" : player.getCustomName().getString()))
            );
            runtime.clearScripts();
        });
    }

    @GameTest
    public void potionEffectDueToAreaEffectCloudExecutesRealScript(GameTestHelper helper) {
        runWithRuntimeLock(helper, () -> {
            SkriptRuntime runtime = SkriptRuntime.instance();
            runtime.clearScripts();
            runtime.loadFromResource("skript/gametest/event/potion_effect_due_to_area_effect_cloud_names_entity.sk");

            Cow cow = createCow(helper, false);
            cow.setCustomName(null);

            AreaEffectCloud cloud = new AreaEffectCloud(helper.getLevel(), cow.getX(), cow.getY(), cow.getZ());
            cloud.setRadius(2.0F);
            cloud.setWaitTime(0);
            cloud.setDuration(40);
            cloud.setPotionContents(new PotionContents(Potions.POISON));
            helper.getLevel().addFreshEntity(cloud);
            for (int i = 0; i < 6; i++) {
                cloud.tick();
            }

            helper.assertTrue(
                    cow.getCustomName() != null && "area effect cloud".equals(cow.getCustomName().getString()),
                    Component.literal("Expected area-effect-cloud cause filter to rename the affected entity. Actual name: "
                            + (cow.getCustomName() == null ? "null" : cow.getCustomName().getString()))
            );
            runtime.clearScripts();
        });
    }

    @GameTest
    public void areaCloudEffectExecutesRealScript(GameTestHelper helper) {
        runWithRuntimeLock(helper, () -> {
            SkriptRuntime runtime = SkriptRuntime.instance();
            runtime.clearScripts();
            runtime.loadFromResource("skript/gametest/event/area_cloud_effect_names_affected_entities.sk");

            Cow cow = createCow(helper, false);
            cow.setCustomName(null);

            AreaEffectCloud cloud = new AreaEffectCloud(helper.getLevel(), cow.getX(), cow.getY(), cow.getZ());
            cloud.setRadius(2.0F);
            cloud.setWaitTime(0);
            cloud.setDuration(40);
            cloud.setPotionContents(new PotionContents(Potions.POISON));
            helper.getLevel().addFreshEntity(cloud);
            for (int i = 0; i < 6; i++) {
                cloud.tick();
            }

            helper.assertTrue(
                    cow.getCustomName() != null && "affected entity".equals(cow.getCustomName().getString()),
                    Component.literal("Expected area cloud effect event to expose the affected entities list.")
            );
            runtime.clearScripts();
        });
    }

    @GameTest
    public void potionEffectDueToAreaEffectCloudDoesNotMatchPotionDrink(GameTestHelper helper) {
        runWithRuntimeLock(helper, () -> {
            SkriptRuntime runtime = SkriptRuntime.instance();
            runtime.clearScripts();
            runtime.loadFromResource("skript/gametest/event/potion_effect_due_to_area_effect_cloud_names_entity.sk");

            ServerPlayer player = helper.makeMockServerPlayerInLevel();
            player.setGameMode(GameType.SURVIVAL);
            player.setCustomName(null);
            player.setItemInHand(InteractionHand.MAIN_HAND, PotionContents.createItemStack(Items.POTION, Potions.POISON));
            player.getItemInHand(InteractionHand.MAIN_HAND).finishUsingItem(helper.getLevel(), player);

            helper.assertTrue(
                    player.getCustomName() == null,
                    Component.literal("Expected area-effect-cloud cause filter to ignore potion-drink events. Actual name: "
                            + (player.getCustomName() == null ? "null" : player.getCustomName().getString()))
            );
            runtime.clearScripts();
        });
    }

    @GameTest
    public void potionEffectDueToFoodExecutesRealScript(GameTestHelper helper) {
        runWithRuntimeLock(helper, () -> {
            SkriptRuntime runtime = SkriptRuntime.instance();
            runtime.clearScripts();
            runtime.loadFromResource("skript/gametest/event/potion_effect_due_to_food_names_entity.sk");

            ServerPlayer player = helper.makeMockServerPlayerInLevel();
            player.setGameMode(GameType.SURVIVAL);
            player.setCustomName(null);
            player.getFoodData().setFoodLevel(10);
            player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.PUFFERFISH));
            player.getItemInHand(InteractionHand.MAIN_HAND).finishUsingItem(helper.getLevel(), player);

            helper.assertTrue(
                    player.getCustomName() != null && "food".equals(player.getCustomName().getString()),
                    Component.literal("Expected food cause filter to rename the affected player. Actual name: "
                            + (player.getCustomName() == null ? "null" : player.getCustomName().getString()))
            );
            runtime.clearScripts();
        });
    }

    @GameTest
    public void potionEffectDueToMilkExecutesRealScript(GameTestHelper helper) {
        runWithRuntimeLock(helper, () -> {
            SkriptRuntime runtime = SkriptRuntime.instance();
            runtime.clearScripts();
            runtime.loadFromResource("skript/gametest/event/potion_effect_due_to_milk_names_entity.sk");

            ServerPlayer player = helper.makeMockServerPlayerInLevel();
            player.setGameMode(GameType.SURVIVAL);
            player.setCustomName(null);
            player.addEffect(new MobEffectInstance(MobEffects.POISON, 200, 0));
            player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.MILK_BUCKET));
            player.getItemInHand(InteractionHand.MAIN_HAND).finishUsingItem(helper.getLevel(), player);

            helper.assertTrue(
                    player.getCustomName() != null && "milk".equals(player.getCustomName().getString()),
                    Component.literal("Expected milk cause filter to rename the affected player. Actual name: "
                            + (player.getCustomName() == null ? "null" : player.getCustomName().getString()))
            );
            runtime.clearScripts();
        });
    }

    @GameTest(skyAccess = true)
    public void beaconActivationExecutesRealScript(GameTestHelper helper) {
        BlockPos beaconPos = helper.absolutePos(new BlockPos(0, 1, 0));
        helper.getLevel().setBlockAndUpdate(beaconPos, Blocks.BEACON.defaultBlockState());
        helper.getLevel().setBlockAndUpdate(beaconPos.above(), Blocks.AIR.defaultBlockState());
        invokeBeaconTick(helper, beaconPos);

        runWithRuntimeLock(helper, () -> {
            SkriptRuntime runtime = SkriptRuntime.instance();
            runtime.clearScripts();
            runtime.loadFromResource("skript/gametest/event/beacon_activation_marks_block.sk");
            buildSingleTierBeaconBase(helper, beaconPos);
            invokeBeaconTick(helper, beaconPos);

            helper.assertTrue(
                    PrivateBeaconAccess.levels(beaconAt(helper, beaconPos)) > 0,
                    Component.literal("Expected the live beacon tick path to activate the beacon after building a base.")
            );
            helper.assertTrue(
                    helper.getLevel().getBlockState(beaconPos.above()).is(Blocks.EMERALD_BLOCK),
                    Component.literal("Expected beacon activation event to mark the block above the beacon.")
            );
            runtime.clearScripts();
        });
    }

    @GameTest(skyAccess = true)
    public void primaryBeaconEffectExecutesRealScript(GameTestHelper helper) {
        BlockPos beaconPos = helper.absolutePos(new BlockPos(2, 1, 2));
        helper.getLevel().setBlockAndUpdate(beaconPos, Blocks.BEACON.defaultBlockState());
        helper.getLevel().setBlockAndUpdate(beaconPos.above(), Blocks.AIR.defaultBlockState());
        buildSingleTierBeaconBase(helper, beaconPos);
        invokeBeaconTick(helper, beaconPos);

        ServerPlayer player = helper.makeMockServerPlayerInLevel();
        player.setGameMode(GameType.SURVIVAL);
        player.removeAllEffects();
        player.teleportTo(beaconPos.getX() + 0.5D, beaconPos.getY() + 1.0D, beaconPos.getZ() + 0.5D);

        runWithRuntimeLock(helper, () -> {
            SkriptRuntime runtime = SkriptRuntime.instance();
            runtime.clearScripts();
            runtime.loadFromResource("skript/gametest/event/primary_beacon_effect_marks_block.sk");

            BeaconBlockEntity beacon = beaconAt(helper, beaconPos);
            helper.assertTrue(
                    PrivateBeaconAccess.levels(beacon) > 0,
                    Component.literal("Expected the live beacon tick path to activate the beacon before applying effects.")
            );
            PrivateBeaconAccess.setPrimaryPower(beacon, MobEffects.SPEED);
            beacon.setChanged();
            invokeBeaconApplyEffects(
                    helper,
                    beaconPos,
                    PrivateBeaconAccess.levels(beacon),
                    PrivateBeaconAccess.primaryPower(beacon),
                    null
            );

            helper.assertTrue(
                    player.hasEffect(MobEffects.SPEED),
                    Component.literal("Expected beacon applyEffects to grant the configured primary effect to the nearby player.")
            );
            helper.assertTrue(
                    helper.getLevel().getBlockState(beaconPos.above()).is(Blocks.GOLD_BLOCK),
                    Component.literal("Expected primary beacon effect event to mark the block above the beacon.")
            );
            runtime.clearScripts();
        });
    }

    @GameTest
    public void potionEffectDueToBeaconExecutesRealScript(GameTestHelper helper) {
        runWithRuntimeLock(helper, () -> {
            SkriptRuntime runtime = SkriptRuntime.instance();
            runtime.clearScripts();
            runtime.loadFromResource("skript/gametest/event/potion_effect_due_to_beacon_names_entity.sk");

            ServerPlayer player = helper.makeMockServerPlayerInLevel();
            player.setGameMode(GameType.SURVIVAL);
            player.setCustomName(null);
            BlockPos beaconPos = helper.absolutePos(new BlockPos(2, 1, 2));
            player.teleportTo(beaconPos.getX() + 0.5D, beaconPos.getY() + 1.0D, beaconPos.getZ() + 0.5D);

            invokeBeaconApplyEffects(helper, beaconPos, 1, MobEffects.SPEED, null);

            helper.assertTrue(
                    player.getCustomName() != null && "beacon".equals(player.getCustomName().getString()),
                    Component.literal("Expected beacon cause filter to rename the affected player. Actual name: "
                            + (player.getCustomName() == null ? "null" : player.getCustomName().getString()))
            );
            runtime.clearScripts();
        });
    }

    @GameTest
    public void potionEffectDueToConduitExecutesRealScript(GameTestHelper helper) {
        runWithRuntimeLock(helper, () -> {
            SkriptRuntime runtime = SkriptRuntime.instance();
            runtime.clearScripts();
            runtime.loadFromResource("skript/gametest/event/potion_effect_due_to_conduit_names_entity.sk");

            ServerPlayer player = helper.makeMockServerPlayerInLevel();
            player.setGameMode(GameType.SURVIVAL);
            player.setCustomName(null);
            BlockPos conduitPos = helper.absolutePos(new BlockPos(4, 1, 4));
            BlockPos waterPos = conduitPos.above();
            helper.getLevel().setBlockAndUpdate(waterPos, Blocks.WATER.defaultBlockState());
            helper.getLevel().setBlockAndUpdate(waterPos.above(), Blocks.WATER.defaultBlockState());
            player.teleportTo(waterPos.getX() + 0.5D, waterPos.getY(), waterPos.getZ() + 0.5D);
            refreshWaterState(player);

            invokeConduitApplyEffects(helper, conduitPos, 7);

            helper.assertTrue(
                    player.getCustomName() != null && "conduit".equals(player.getCustomName().getString()),
                    Component.literal("Expected conduit cause filter to rename the affected player. Actual name: "
                            + (player.getCustomName() == null ? "null" : player.getCustomName().getString()))
            );
            runtime.clearScripts();
        });
    }

    @GameTest
    public void potionEffectDueToCommandExecutesRealScript(GameTestHelper helper) {
        runWithRuntimeLock(helper, () -> {
            SkriptRuntime runtime = SkriptRuntime.instance();
            runtime.clearScripts();
            runtime.loadFromResource("skript/gametest/event/potion_effect_due_to_command_names_entity.sk");

            ServerPlayer player = helper.makeMockServerPlayerInLevel();
            player.setGameMode(GameType.SURVIVAL);
            player.setCustomName(null);

            helper.getLevel().getServer()
                    .getCommands()
                    .performPrefixedCommand(player.createCommandSourceStack().withPermission(LevelBasedPermissionSet.OWNER), "effect give @s poison 5 0 true");

            helper.assertTrue(
                    player.getCustomName() != null && "command".equals(player.getCustomName().getString()),
                    Component.literal("Expected command cause filter to rename the affected player. Actual name: "
                            + (player.getCustomName() == null ? "null" : player.getCustomName().getString()))
            );
            runtime.clearScripts();
        });
    }

    @GameTest
    public void potionEffectDueToAttackExecutesRealScript(GameTestHelper helper) {
        runWithRuntimeLock(helper, () -> {
            SkriptRuntime runtime = SkriptRuntime.instance();
            runtime.clearScripts();
            runtime.loadFromResource("skript/gametest/event/potion_effect_due_to_attack_names_entity.sk");

            Pufferfish pufferfish = (Pufferfish) helper.spawnWithNoFreeWill(EntityType.PUFFERFISH, 0.5F, 1.0F, 0.5F);
            Cow cow = createCow(helper, false);
            cow.setCustomName(null);
            pufferfish.setPuffState(2);
            invokePufferfishTouch(pufferfish, helper, cow);

            helper.assertTrue(
                    cow.getCustomName() != null && "attack".equals(cow.getCustomName().getString()),
                    Component.literal("Expected attack cause filter to rename the affected entity. Actual name: "
                            + (cow.getCustomName() == null ? "null" : cow.getCustomName().getString()))
            );
            runtime.clearScripts();
        });
    }

    @GameTest
    public void potionEffectDueToArrowExecutesRealScript(GameTestHelper helper) {
        runWithRuntimeLock(helper, () -> {
            SkriptRuntime runtime = SkriptRuntime.instance();
            runtime.clearScripts();
            runtime.loadFromResource("skript/gametest/event/potion_effect_due_to_arrow_names_entity.sk");

            ServerPlayer player = helper.makeMockServerPlayerInLevel();
            player.setGameMode(GameType.SURVIVAL);
            Cow cow = createCow(helper, false);
            cow.setCustomName(null);

            Arrow arrow = new Arrow(helper.getLevel(), player, new ItemStack(Items.ARROW), new ItemStack(Items.BOW));
            arrow.addEffect(new MobEffectInstance(MobEffects.POISON, 200, 0));
            invokeArrowPostHurtEffects(arrow, cow);

            helper.assertTrue(
                    cow.getCustomName() != null && "arrow".equals(cow.getCustomName().getString()),
                    Component.literal("Expected arrow cause filter to rename the affected entity. Actual name: "
                            + (cow.getCustomName() == null ? "null" : cow.getCustomName().getString()))
            );
            runtime.clearScripts();
        });
    }

    @GameTest
    public void fireworkExplosionExecutesRealScript(GameTestHelper helper) {
        AtomicBoolean loaded = new AtomicBoolean(false);
        helper.succeedWhen(() -> {
            SkriptRuntime runtime = SkriptRuntime.instance();
            if (!loaded.get()) {
                if (!RUNTIME_LOCK.compareAndSet(false, true)) {
                    return;
                }
                runtime.clearScripts();
                runtime.loadFromResource("skript/gametest/event/firework_explosion_marks_block.sk");

                ItemStack rocketStack = new ItemStack(Items.FIREWORK_ROCKET);
                rocketStack.set(
                        DataComponents.FIREWORKS,
                        new Fireworks(
                                1,
                                List.of(new FireworkExplosion(
                                        FireworkExplosion.Shape.SMALL_BALL,
                                        IntArrayList.of(0xFF0000),
                                        IntArrayList.of(),
                                        false,
                                        false
                                ))
                        )
                );
                FireworkRocketEntity firework = new FireworkRocketEntity(helper.getLevel(), 0.5D, 1.0D, 0.5D, rocketStack);
                helper.getLevel().addFreshEntity(firework);
                setIntField(firework, "life", 0);
                setIntField(firework, "lifetime", 1);
                loaded.set(true);
                return;
            }

            helper.assertBlockPresent(Blocks.EMERALD_BLOCK, new BlockPos(0, 2, 0));
            runtime.clearScripts();
            Variables.clearAll();
            RUNTIME_LOCK.set(false);
        });
    }

    @GameTest
    public void entityTargetAndUntargetExecuteRealScript(GameTestHelper helper) {
        runWithRuntimeLock(helper, () -> {
            SkriptRuntime runtime = SkriptRuntime.instance();
            Variables.clearAll();
            runtime.clearScripts();
            runtime.loadFromResource("skript/gametest/event/entity_target_and_untarget_mark_entity.sk");

            Spider spider = (Spider) helper.spawnWithNoFreeWill(EntityType.SPIDER, 0.5F, 1.0F, 0.5F);
            ServerPlayer player = helper.makeMockServerPlayerInLevel();
            player.teleportTo(2.5D, 1.0D, 0.5D);

            spider.setTarget(player);
            helper.assertTrue(
                    spider.getCustomName() != null && "targeted".equals(spider.getCustomName().getString()),
                    Component.literal("Expected entity target event to rename the mob when a real target is assigned.")
            );

            spider.setCustomName(null);
            spider.setTarget(null);
            helper.assertTrue(
                    spider.getCustomName() != null && "untargeted".equals(spider.getCustomName().getString()),
                    Component.literal("Expected entity untarget event to rename the mob when its target is cleared.")
            );

            runtime.clearScripts();
            Variables.clearAll();
        });
    }

    @GameTest
    public void explosionExecutesRealScript(GameTestHelper helper) {
        AtomicBoolean loaded = new AtomicBoolean(false);
        helper.succeedWhen(() -> {
            SkriptRuntime runtime = SkriptRuntime.instance();
            if (!loaded.get()) {
                if (!RUNTIME_LOCK.compareAndSet(false, true)) {
                    return;
                }
                runtime.clearScripts();
                runtime.loadFromResource("skript/gametest/event/explosion_marks_block_types.sk");

                helper.getLevel().setBlockAndUpdate(helper.absolutePos(new BlockPos(1, 1, 0)), Blocks.STONE.defaultBlockState());
                helper.getLevel().setBlockAndUpdate(helper.absolutePos(new BlockPos(2, 1, 0)), Blocks.DIRT.defaultBlockState());
                helper.getLevel().setBlockAndUpdate(helper.absolutePos(new BlockPos(8, 1, 0)), Blocks.AIR.defaultBlockState());
                helper.getLevel().setBlockAndUpdate(helper.absolutePos(new BlockPos(9, 1, 0)), Blocks.AIR.defaultBlockState());

                spawnPrimedTnt(helper, 1.5D, 1.0D, 1.5D);
                loaded.set(true);
                return;
            }

            helper.assertBlockPresent(Blocks.REDSTONE_BLOCK, new BlockPos(8, 1, 0));
            helper.assertBlockPresent(Blocks.REDSTONE_BLOCK, new BlockPos(9, 1, 0));
            runtime.clearScripts();
            Variables.clearAll();
            RUNTIME_LOCK.set(false);
        });
    }

    @GameTest
    public void explosionPrimeProducerExecutesRealScript(GameTestHelper helper) {
        runWithRuntimeLock(helper, () -> {
            SkriptRuntime runtime = SkriptRuntime.instance();
            Variables.clearAll();
            runtime.clearScripts();
            runtime.loadFromResource("skript/gametest/event/explosion_prime_records_default_radius.sk");

            Creeper creeper = (Creeper) helper.spawnWithNoFreeWill(EntityType.CREEPER, 4.5F, 1.0F, 0.5F);
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

            Object value = Variables.getVariable("gametest::explosion_prime_radius", null, false);
            helper.assertTrue(
                    value instanceof Number number && Math.abs(number.floatValue() - 3.0F) < 0.001F,
                    Component.literal("Expected real explosion prime producer to expose the default creeper radius.")
            );
            helper.assertTrue(
                    !creeper.isAlive(),
                    Component.literal("Expected the ignited creeper to reach its real explosion path.")
            );
            runtime.clearScripts();
            Variables.clearAll();
        });
    }

    @GameTest
    public void helmetChangeExecutesRealScript(GameTestHelper helper) {
        runWithRuntimeLock(helper, () -> {
            SkriptRuntime runtime = SkriptRuntime.instance();
            Variables.clearAll();
            runtime.clearScripts();
            runtime.loadFromResource("skript/gametest/event/helmet_change_names_player.sk");

            ServerPlayer player = helper.makeMockServerPlayerInLevel();
            player.setItemSlot(EquipmentSlot.HEAD, new ItemStack(Items.DIAMOND_HELMET));

            helper.assertTrue(
                    player.getCustomName() != null && "helmet changed".equals(player.getCustomName().getString()),
                    Component.literal("Expected helmet change event to rename the player when a real armor slot changes.")
            );

            runtime.clearScripts();
            Variables.clearAll();
        });
    }

    @GameTest
    public void entityPortalExecutesRealScript(GameTestHelper helper) {
        runWithRuntimeLock(helper, () -> {
            SkriptRuntime runtime = SkriptRuntime.instance();
            Variables.clearAll();
            runtime.clearScripts();
            runtime.loadFromResource("skript/gametest/event/entity_portal_names_entity.sk");

            BlockPos portalPos = helper.absolutePos(new BlockPos(0, 1, 0));
            helper.getLevel().setBlockAndUpdate(portalPos, Blocks.END_PORTAL.defaultBlockState());

            Zombie zombie = (Zombie) helper.spawnWithNoFreeWill(EntityType.ZOMBIE, 0.5F, 1.0F, 0.5F);
            invokeEndPortalEntityInside(helper, portalPos, zombie);
            zombie.tick();

            helper.assertTrue(
                    zombie.getCustomName() != null && "portal entity".equals(zombie.getCustomName().getString()),
                    Component.literal("Expected entity portal event to rename the entity after the portal travel path runs.")
            );

            runtime.clearScripts();
            Variables.clearAll();
        });
    }

    @GameTest
    public void explosionYieldExecutesRealScript(GameTestHelper helper) {
        AtomicBoolean loaded = new AtomicBoolean(false);
        BlockPos dirtPos = new BlockPos(2, 1, 0);
        BlockPos markerPos = new BlockPos(8, 1, 0);
        AABB dropBox = AABB.encapsulatingFullBlocks(
                helper.absolutePos(new BlockPos(0, 0, -1)),
                helper.absolutePos(new BlockPos(4, 3, 2))
        );
        helper.succeedWhen(() -> {
            SkriptRuntime runtime = SkriptRuntime.instance();
            if (!loaded.get()) {
                if (!RUNTIME_LOCK.compareAndSet(false, true)) {
                    return;
                }
                runtime.clearScripts();
                runtime.loadFromResource("skript/gametest/event/explosion_zero_yield_marks_block.sk");
                helper.getLevel().getGameRules().set(GameRules.TNT_EXPLOSION_DROP_DECAY, false, helper.getLevel().getServer());
                helper.getLevel().setBlockAndUpdate(helper.absolutePos(dirtPos), Blocks.DIRT.defaultBlockState());
                helper.getLevel().setBlockAndUpdate(helper.absolutePos(markerPos), Blocks.AIR.defaultBlockState());

                spawnPrimedTnt(helper, 1.5D, 1.0D, 1.5D);
                loaded.set(true);
                return;
            }

            if (!helper.getBlockState(markerPos).is(Blocks.EMERALD_BLOCK)) {
                return;
            }
            if (!helper.getBlockState(dirtPos).is(Blocks.AIR)) {
                return;
            }

            List<ItemEntity> nearbyDrops = helper.getLevel().getEntitiesOfClass(ItemEntity.class, dropBox);
            helper.assertTrue(
                    nearbyDrops.stream().noneMatch(item -> item.getItem().is(Items.DIRT)),
                    Component.literal("Expected explosion block yield mutation to suppress TNT dirt drops.")
            );
            runtime.clearScripts();
            Variables.clearAll();
            RUNTIME_LOCK.set(false);
        });
    }

    @GameTest
    public void piglinBarterEventExecutesRealScript(GameTestHelper helper) {
        AtomicBoolean loaded = new AtomicBoolean(false);
        helper.succeedWhen(() -> {
            SkriptRuntime runtime = SkriptRuntime.instance();
            if (!loaded.get()) {
                if (!RUNTIME_LOCK.compareAndSet(false, true)) {
                    return;
                }
                runtime.clearScripts();
                runtime.loadFromResource("skript/gametest/event/piglin_barter_mutates_drops_and_marks_input.sk");

                Piglin piglin = new Piglin(EntityType.PIGLIN, helper.getLevel());
                piglin.setPos(0.5D, 1.0D, 0.5D);
                piglin.setBaby(false);
                piglin.setPersistenceRequired();
                helper.getLevel().addFreshEntity(piglin);

                ItemEntity gold = new ItemEntity(helper.getLevel(), 0.5D, 1.0D, 0.75D, new ItemStack(Items.GOLD_INGOT));
                gold.setPickUpDelay(0);
                helper.getLevel().addFreshEntity(gold);
                loaded.set(true);
                return;
            }
            if (!helper.getBlockState(new BlockPos(0, 2, 0)).is(Blocks.EMERALD_BLOCK)) {
                return;
            }

            List<ItemEntity> nearbyDrops = helper.getLevel().getEntitiesOfClass(
                    ItemEntity.class,
                    AABB.encapsulatingFullBlocks(
                            helper.absolutePos(new BlockPos(0, 0, 0)).offset(-1, 0, -1),
                            helper.absolutePos(new BlockPos(1, 3, 1)).offset(1, 1, 1)
                    )
            );
            if (nearbyDrops.stream().noneMatch(item -> item.getItem().is(Items.STICK))) {
                return;
            }

            helper.assertTrue(
                    nearbyDrops.size() == 1 && nearbyDrops.getFirst().getItem().is(Items.STICK),
                    Component.literal("Expected piglin barter script to replace barter drops with a single stick drop.")
            );
            runtime.clearScripts();
            Variables.clearAll();
            RUNTIME_LOCK.set(false);
        });
    }

    @GameTest
    public void deathEventMutatesDropsAndXpThroughRealDeathFlow(GameTestHelper helper) {
        AtomicBoolean loaded = new AtomicBoolean(false);
        AABB deathBox = AABB.encapsulatingFullBlocks(
                helper.absolutePos(new BlockPos(-1, 0, -1)),
                helper.absolutePos(new BlockPos(2, 3, 2))
        );
        helper.succeedWhen(() -> {
            SkriptRuntime runtime = SkriptRuntime.instance();
            if (!loaded.get()) {
                if (!RUNTIME_LOCK.compareAndSet(false, true)) {
                    return;
                }
                Variables.clearAll();
                runtime.clearScripts();
                runtime.loadFromResource("skript/gametest/event/death_mutates_drops_and_xp.sk");

                Cow cow = createCow(helper, false);
                cow.hurtServer(helper.getLevel(), helper.getLevel().damageSources().genericKill(), cow.getHealth());
                loaded.set(true);
                return;
            }

            List<ItemEntity> drops = helper.getLevel().getEntitiesOfClass(ItemEntity.class, deathBox);
            int totalExperience = helper.getLevel().getEntitiesOfClass(ExperienceOrb.class, deathBox)
                    .stream()
                    .mapToInt(ExperienceOrb::getValue)
                    .sum();
            if (drops.isEmpty() || totalExperience == 0) {
                return;
            }

            helper.assertTrue(
                    drops.size() == 1 && drops.getFirst().getItem().is(Items.DIAMOND),
                    Component.literal("Expected death script to replace real cow drops with a single diamond.")
            );
            helper.assertTrue(
                    totalExperience == 7,
                    Component.literal("Expected death script to replace dropped experience with 7 total xp, got " + totalExperience + ".")
            );
            runtime.clearScripts();
            Variables.clearAll();
            RUNTIME_LOCK.set(false);
        });
    }

    @GameTest
    public void playerEggThrowProducerExecutesRealScript(GameTestHelper helper) {
        AtomicBoolean loaded = new AtomicBoolean(false);
        AtomicInteger initialPigCount = new AtomicInteger(-1);
        BlockPos pigBoxMin = helper.absolutePos(new BlockPos(-1, 0, -1));
        BlockPos pigBoxMax = helper.absolutePos(new BlockPos(2, 4, 2));
        AABB pigBox = new AABB(
                pigBoxMin.getX(),
                pigBoxMin.getY(),
                pigBoxMin.getZ(),
                pigBoxMax.getX() + 1.0D,
                pigBoxMax.getY() + 1.0D,
                pigBoxMax.getZ() + 1.0D
        );
        helper.succeedWhen(() -> {
            SkriptRuntime runtime = SkriptRuntime.instance();
            if (!loaded.get()) {
                if (!RUNTIME_LOCK.compareAndSet(false, true)) {
                    return;
                }
                initialPigCount.set(helper.getLevel().getEntitiesOfClass(Pig.class, pigBox).size());
                Variables.clearAll();
                runtime.clearScripts();
                runtime.loadFromResource("skript/gametest/event/player_egg_throw_hatches_pigs.sk");

                ServerPlayer player = helper.makeMockServerPlayerInLevel();
                player.setGameMode(GameType.SURVIVAL);
                player.teleportTo(0.5D, 3.0D, 0.5D);

                ThrownEgg egg = (ThrownEgg) EntityType.EGG.create(helper.getLevel(), EntitySpawnReason.TRIGGERED);
                helper.assertTrue(egg != null, Component.literal("Expected egg entity type to create a thrown egg."));
                if (egg == null) {
                    throw new IllegalStateException("Thrown egg type creation returned null.");
                }
                egg.setOwner(player);
                egg.setPos(0.5D, 3.0D, 0.5D);
                egg.setDeltaMovement(0.0D, -0.75D, 0.0D);
                helper.getLevel().addFreshEntity(egg);
                loaded.set(true);
                return;
            }

            helper.assertTrue(
                    helper.getLevel().getEntitiesOfClass(Pig.class, pigBox).size() == initialPigCount.get() + 3,
                    Component.literal("Expected player egg throw script to hatch exactly three pigs from a real egg collision.")
            );
            runtime.clearScripts();
            Variables.clearAll();
            RUNTIME_LOCK.set(false);
        });
    }

    @GameTest
    public void potionEffectDueToUnknownExecutesRealScript(GameTestHelper helper) {
        runWithRuntimeLock(helper, () -> {
            SkriptRuntime runtime = SkriptRuntime.instance();
            runtime.clearScripts();
            runtime.loadFromResource("skript/gametest/event/potion_effect_due_to_unknown_names_entity.sk");

            Cow cow = createCow(helper, false);
            cow.setCustomName(null);
            cow.addEffect(new MobEffectInstance(MobEffects.POISON, 200, 0));

            helper.assertTrue(
                    cow.getCustomName() != null && "unknown".equals(cow.getCustomName().getString()),
                    Component.literal("Expected unknown cause filter to rename the affected entity. Actual name: "
                            + (cow.getCustomName() == null ? "null" : cow.getCustomName().getString()))
            );
            runtime.clearScripts();
        });
    }

    @GameTest
    public void potionEffectDueToPotionSplashExecutesRealScript(GameTestHelper helper) {
        runWithRuntimeLock(helper, () -> {
            SkriptRuntime runtime = SkriptRuntime.instance();
            runtime.clearScripts();
            runtime.loadFromResource("skript/gametest/event/potion_effect_due_to_potion_splash_names_entity.sk");

            ServerPlayer player = helper.makeMockServerPlayerInLevel();
            player.setGameMode(GameType.SURVIVAL);
            Cow cow = createCow(helper, false);
            cow.setCustomName(null);

            ItemStack splashStack = PotionContents.createItemStack(Items.SPLASH_POTION, Potions.POISON);
            ThrownSplashPotion splashPotion = new ThrownSplashPotion(helper.getLevel(), player, splashStack);
            splashPotion.setPos(cow.getX(), cow.getY(), cow.getZ());
            helper.getLevel().addFreshEntity(splashPotion);
            splashPotion.onHitAsPotion(helper.getLevel(), splashStack, new EntityHitResult(cow));

            helper.assertTrue(
                    cow.getCustomName() != null && "potion splash".equals(cow.getCustomName().getString()),
                    Component.literal("Expected splash-potion cause filter to rename the affected entity. Actual name: "
                            + (cow.getCustomName() == null ? "null" : cow.getCustomName().getString()))
            );
            runtime.clearScripts();
        });
    }

    @GameTest
    public void potionEffectClearDueToCommandExecutesRealScript(GameTestHelper helper) {
        runWithRuntimeLock(helper, () -> {
            SkriptRuntime runtime = SkriptRuntime.instance();
            runtime.clearScripts();
            runtime.loadFromResource("skript/gametest/event/potion_effect_clear_due_to_command_names_entity.sk");

            ServerPlayer player = helper.makeMockServerPlayerInLevel();
            player.setGameMode(GameType.SURVIVAL);
            player.setCustomName(null);
            player.addEffect(new MobEffectInstance(MobEffects.POISON, 200, 0));

            helper.getLevel().getServer()
                    .getCommands()
                    .performPrefixedCommand(player.createCommandSourceStack().withPermission(LevelBasedPermissionSet.OWNER), "effect clear @s");

            helper.assertTrue(
                    player.getCustomName() != null && "command clear".equals(player.getCustomName().getString()),
                    Component.literal("Expected command clear cause filter to rename the affected player. Actual name: "
                            + (player.getCustomName() == null ? "null" : player.getCustomName().getString()))
            );
            runtime.clearScripts();
        });
    }

    @GameTest
    public void potionEffectDueToTotemExecutesRealScript(GameTestHelper helper) {
        runWithRuntimeLock(helper, () -> {
            SkriptRuntime runtime = SkriptRuntime.instance();
            runtime.clearScripts();
            runtime.loadFromResource("skript/gametest/event/potion_effect_due_to_totem_names_entity.sk");

            ServerPlayer player = helper.makeMockServerPlayerInLevel();
            player.setGameMode(GameType.SURVIVAL);
            player.setCustomName(null);
            player.setHealth(1.0F);
            player.setItemInHand(InteractionHand.OFF_HAND, new ItemStack(Items.TOTEM_OF_UNDYING));

            boolean survived = invokeTotemDeathProtection(player, helper.getLevel().damageSources().mobAttack(createCow(helper, false)));

            helper.assertTrue(survived, Component.literal("Expected totem protection invocation to succeed."));
            helper.assertTrue(
                    player.getCustomName() != null && "totem".equals(player.getCustomName().getString()),
                    Component.literal("Expected totem cause filter to rename the affected player. Actual name: "
                            + (player.getCustomName() == null ? "null" : player.getCustomName().getString()))
            );
            runtime.clearScripts();
        });
    }

    @GameTest
    public void potionEffectDueToWitherRoseExecutesRealScript(GameTestHelper helper) {
        runWithRuntimeLock(helper, () -> {
            SkriptRuntime runtime = SkriptRuntime.instance();
            runtime.clearScripts();
            runtime.loadFromResource("skript/gametest/event/potion_effect_due_to_wither_rose_names_entity.sk");

            Cow cow = createCow(helper, false);
            cow.setCustomName(null);
            BlockPos rosePos = helper.absolutePos(new BlockPos(12, 1, 12));
            helper.getLevel().setBlockAndUpdate(rosePos, Blocks.WITHER_ROSE.defaultBlockState());

            invokeWitherRoseEntityInside(helper, rosePos, cow);

            helper.assertTrue(
                    cow.getCustomName() != null && "wither rose".equals(cow.getCustomName().getString()),
                    Component.literal("Expected wither-rose cause filter to rename the affected entity. Actual name: "
                            + (cow.getCustomName() == null ? "null" : cow.getCustomName().getString()))
            );
            runtime.clearScripts();
        });
    }

    @GameTest
    public void potionEffectDueToConversionExecutesRealScript(GameTestHelper helper) {
        runWithRuntimeLock(helper, () -> {
            SkriptRuntime runtime = SkriptRuntime.instance();
            runtime.clearScripts();
            runtime.loadFromResource("skript/gametest/event/potion_effect_due_to_conversion_names_entity.sk");

            ZombieVillager zombieVillager = (ZombieVillager) helper.spawnWithNoFreeWill(EntityType.ZOMBIE_VILLAGER, 0.5F, 1.0F, 0.5F);
            invokeZombieVillagerFinishConversion(helper, zombieVillager);

            List<Villager> villagers = helper.getLevel().getEntitiesOfClass(
                    Villager.class,
                    new AABB(zombieVillager.blockPosition()).inflate(4.0D)
            );

            helper.assertTrue(
                    villagers.stream().anyMatch(villager ->
                            villager.getCustomName() != null && "conversion".equals(villager.getCustomName().getString())),
                    Component.literal("Expected conversion cause filter to rename the converted villager.")
            );
            runtime.clearScripts();
        });
    }

    @GameTest
    public void potionEffectDueToAxolotlExecutesRealScript(GameTestHelper helper) {
        runWithRuntimeLock(helper, () -> {
            SkriptRuntime runtime = SkriptRuntime.instance();
            runtime.clearScripts();
            runtime.loadFromResource("skript/gametest/event/potion_effect_due_to_axolotl_names_entity.sk");

            Axolotl axolotl = (Axolotl) helper.spawnWithNoFreeWill(EntityType.AXOLOTL, 0.5F, 1.0F, 0.5F);
            ServerPlayer player = helper.makeMockServerPlayerInLevel();
            player.setGameMode(GameType.SURVIVAL);
            player.setCustomName(null);

            invokeAxolotlSupportingEffects(axolotl, player);

            helper.assertTrue(
                    player.getCustomName() != null && "axolotl".equals(player.getCustomName().getString()),
                    Component.literal("Expected axolotl cause filter to rename the affected player. Actual name: "
                            + (player.getCustomName() == null ? "null" : player.getCustomName().getString()))
            );
            runtime.clearScripts();
        });
    }

    @GameTest
    public void potionEffectDueToWardenExecutesRealScript(GameTestHelper helper) {
        runWithRuntimeLock(helper, () -> {
            SkriptRuntime runtime = SkriptRuntime.instance();
            runtime.clearScripts();
            runtime.loadFromResource("skript/gametest/event/potion_effect_due_to_warden_names_entity.sk");

            Warden warden = (Warden) helper.spawnWithNoFreeWill(EntityType.WARDEN, 0.5F, 1.0F, 0.5F);
            ServerPlayer player = helper.makeMockServerPlayerInLevel();
            player.setGameMode(GameType.SURVIVAL);
            player.setCustomName(null);
            player.teleportTo(warden.getX(), warden.getY(), warden.getZ() + 1.0D);

            Warden.applyDarknessAround(helper.getLevel(), warden.position(), warden, 20);

            helper.assertTrue(
                    player.getCustomName() != null && "warden".equals(player.getCustomName().getString()),
                    Component.literal("Expected warden cause filter to rename the affected player. Actual name: "
                            + (player.getCustomName() == null ? "null" : player.getCustomName().getString()))
            );
            runtime.clearScripts();
        });
    }

    @GameTest
    public void potionEffectDueToSpiderSpawnExecutesRealScript(GameTestHelper helper) {
        runWithRuntimeLock(helper, () -> {
            SkriptRuntime runtime = SkriptRuntime.instance();
            runtime.clearScripts();
            runtime.loadFromResource("skript/gametest/event/potion_effect_due_to_spider_spawn_names_entity.sk");

            Spider spider = (Spider) helper.spawnWithNoFreeWill(EntityType.SPIDER, 0.5F, 1.0F, 0.5F);
            spider.setCustomName(null);

            invokeSpiderFinalizeSpawn(helper, spider);

            helper.assertTrue(
                    spider.getCustomName() != null && "spider spawn".equals(spider.getCustomName().getString()),
                    Component.literal("Expected spider-spawn cause filter to rename the affected entity. Actual name: "
                            + (spider.getCustomName() == null ? "null" : spider.getCustomName().getString()))
            );
            runtime.clearScripts();
        });
    }

    @GameTest
    public void potionEffectDueToVillagerTradeExecutesRealScript(GameTestHelper helper) {
        runWithRuntimeLock(helper, () -> {
            SkriptRuntime runtime = SkriptRuntime.instance();
            runtime.clearScripts();
            runtime.loadFromResource("skript/gametest/event/potion_effect_due_to_villager_trade_names_entity.sk");

            Villager villager = (Villager) helper.spawnWithNoFreeWill(EntityType.VILLAGER, 0.5F, 1.0F, 0.5F);
            villager.setCustomName(null);

            invokeVillagerRewardTradeXp(villager, new MerchantOffer(new ItemCost(Items.EMERALD), new ItemStack(Items.BREAD), 0, 2, 0.05F));
            setIntField(villager, "updateMerchantTimer", 1);
            invokeVillagerCustomServerAiStep(helper, villager);

            helper.assertTrue(
                    villager.getCustomName() != null && "villager trade".equals(villager.getCustomName().getString()),
                    Component.literal("Expected villager-trade cause filter to rename the affected villager. Actual name: "
                            + (villager.getCustomName() == null ? "null" : villager.getCustomName().getString()))
            );
            runtime.clearScripts();
        });
    }

    @GameTest
    public void potionEffectDueToExpirationExecutesRealScript(GameTestHelper helper) {
        runWithRuntimeLock(helper, () -> {
            SkriptRuntime runtime = SkriptRuntime.instance();
            runtime.clearScripts();
            runtime.loadFromResource("skript/gametest/event/potion_effect_due_to_expiration_names_entity.sk");

            Cow cow = createCow(helper, false);
            cow.setCustomName(null);
            cow.addEffect(new MobEffectInstance(MobEffects.POISON, 1, 0));
            cow.tick();
            cow.tick();

            helper.assertTrue(
                    cow.getCustomName() != null && "expiration".equals(cow.getCustomName().getString()),
                    Component.literal("Expected expiration cause filter to rename the affected entity. Actual name: "
                            + (cow.getCustomName() == null ? "null" : cow.getCustomName().getString()))
            );
            runtime.clearScripts();
        });
    }

    @GameTest
    public void potionEffectDueToDolphinExecutesRealScript(GameTestHelper helper) {
        runWithRuntimeLock(helper, () -> {
            SkriptRuntime runtime = SkriptRuntime.instance();
            runtime.clearScripts();
            runtime.loadFromResource("skript/gametest/event/potion_effect_due_to_dolphin_names_entity.sk");

            Dolphin dolphin = (Dolphin) helper.spawnWithNoFreeWill(EntityType.DOLPHIN, 0.5F, 1.0F, 0.5F);
            ServerPlayer player = helper.makeMockServerPlayerInLevel();
            player.setGameMode(GameType.SURVIVAL);
            player.setCustomName(null);

            invokeDolphinSwimWithPlayerGoalStart(dolphin, player);

            helper.assertTrue(
                    player.getCustomName() != null && "dolphin".equals(player.getCustomName().getString()),
                    Component.literal("Expected dolphin cause filter to rename the affected player. Actual name: "
                            + (player.getCustomName() == null ? "null" : player.getCustomName().getString()))
            );
            runtime.clearScripts();
        });
    }

    @GameTest
    public void potionEffectDueToTurtleHelmetExecutesRealScript(GameTestHelper helper) {
        runWithRuntimeLock(helper, () -> {
            SkriptRuntime runtime = SkriptRuntime.instance();
            runtime.clearScripts();
            runtime.loadFromResource("skript/gametest/event/potion_effect_due_to_turtle_helmet_names_entity.sk");

            ServerPlayer player = helper.makeMockServerPlayerInLevel();
            player.setGameMode(GameType.SURVIVAL);
            player.setCustomName(null);
            player.setItemSlot(EquipmentSlot.HEAD, new ItemStack(Items.TURTLE_HELMET));

            invokePlayerTurtleHelmetTick(player);

            helper.assertTrue(
                    player.getCustomName() != null && "turtle helmet".equals(player.getCustomName().getString()),
                    Component.literal("Expected turtle-helmet cause filter to rename the affected player. Actual name: "
                            + (player.getCustomName() == null ? "null" : player.getCustomName().getString()))
            );
            runtime.clearScripts();
        });
    }

    @GameTest
    public void potionEffectDueToIllusionExecutesRealScript(GameTestHelper helper) {
        runWithRuntimeLock(helper, () -> {
            SkriptRuntime runtime = SkriptRuntime.instance();
            runtime.clearScripts();
            runtime.loadFromResource("skript/gametest/event/potion_effect_due_to_illusion_names_entity.sk");

            Illusioner illusioner = (Illusioner) helper.spawnWithNoFreeWill(EntityType.ILLUSIONER, 0.5F, 1.0F, 0.5F);
            illusioner.setCustomName(null);

            invokeIllusionerMirrorSpell(illusioner);

            helper.assertTrue(
                    illusioner.getCustomName() != null && "illusion".equals(illusioner.getCustomName().getString()),
                    Component.literal("Expected illusion cause filter to rename the affected illusioner. Actual name: "
                            + (illusioner.getCustomName() == null ? "null" : illusioner.getCustomName().getString()))
            );
            runtime.clearScripts();
        });
    }

    @GameTest
    public void potionEffectDueToPluginExecutesRealScript(GameTestHelper helper) {
        runWithRuntimeLock(helper, () -> {
            SkriptRuntime runtime = SkriptRuntime.instance();
            runtime.clearScripts();
            runtime.loadFromResource("skript/gametest/event/potion_effect_due_to_plugin_names_entity.sk");

            Cow cow = createCow(helper, false);
            cow.setCustomName(null);

            ServerPlayer player = helper.makeMockServerPlayerInLevel();
            player.setGameMode(GameType.CREATIVE);
            player.teleportTo(cow.getX(), cow.getY() + 1.0D, cow.getZ());

            InteractionResult result = UseEntityCallback.EVENT.invoker().interact(
                    player,
                    helper.getLevel(),
                    InteractionHand.MAIN_HAND,
                    cow,
                    new EntityHitResult(cow)
            );

            helper.assertTrue(
                    result == InteractionResult.PASS,
                    Component.literal("Expected plugin-cause setup script to keep Fabric callback flow in PASS state.")
            );
            helper.assertTrue(
                    cow.getCustomName() != null && "plugin".equals(cow.getCustomName().getString()),
                    Component.literal("Expected plugin cause filter to rename the affected entity. Actual name: "
                            + (cow.getCustomName() == null ? "null" : cow.getCustomName().getString()))
            );
            runtime.clearScripts();
        });
    }

    @GameTest
    public void potionEffectDueToDeathExecutesRealScript(GameTestHelper helper) {
        runWithRuntimeLock(helper, () -> {
            SkriptRuntime runtime = SkriptRuntime.instance();
            runtime.clearScripts();
            runtime.loadFromResource("skript/gametest/event/potion_effect_due_to_death_names_entity.sk");

            Cow cow = createCow(helper, false);
            cow.setCustomName(null);
            cow.addEffect(new MobEffectInstance(MobEffects.POISON, 200, 0));
            cow.remove(Entity.RemovalReason.KILLED);

            helper.assertTrue(
                    cow.getCustomName() != null && "death".equals(cow.getCustomName().getString()),
                    Component.literal("Expected death cause filter to rename the removed entity. Actual name: "
                            + (cow.getCustomName() == null ? "null" : cow.getCustomName().getString()))
            );
            runtime.clearScripts();
        });
    }

    @GameTest
    public void potionEffectChangedEventExecutesRealScript(GameTestHelper helper) {
        runWithRuntimeLock(helper, () -> {
            Cow cow = createCow(helper, false);
            cow.addEffect(new MobEffectInstance(MobEffects.POISON, 200, 0));

            SkriptRuntime runtime = SkriptRuntime.instance();
            runtime.clearScripts();
            runtime.loadFromResource("skript/gametest/event/potion_effect_changed_names_entity.sk");

            cow.addEffect(new MobEffectInstance(MobEffects.POISON, 200, 1));

            helper.assertTrue(
                    cow.getCustomName() != null && "poison changed".equals(cow.getCustomName().getString()),
                    Component.literal("Expected entity potion change event script to rename the entity. Actual name: "
                            + (cow.getCustomName() == null ? "null" : cow.getCustomName().getString()))
            );
            runtime.clearScripts();
        });
    }

    @GameTest
    public void potionEffectRemovedEventExecutesRealScript(GameTestHelper helper) {
        runWithRuntimeLock(helper, () -> {
            Cow cow = createCow(helper, false);
            cow.addEffect(new MobEffectInstance(MobEffects.POISON, 200, 0));

            SkriptRuntime runtime = SkriptRuntime.instance();
            runtime.clearScripts();
            runtime.loadFromResource("skript/gametest/event/potion_effect_removed_names_entity.sk");

            boolean removed = cow.removeEffect(MobEffects.POISON);

            helper.assertTrue(
                    removed,
                    Component.literal("Expected explicit potion removal to succeed.")
            );
            helper.assertTrue(
                    cow.getCustomName() != null && "poison removed".equals(cow.getCustomName().getString()),
                    Component.literal("Expected entity potion remove event script to rename the entity. Actual name: "
                            + (cow.getCustomName() == null ? "null" : cow.getCustomName().getString()))
            );
            runtime.clearScripts();
        });
    }

    @GameTest
    public void potionEffectClearedEventExecutesRealScript(GameTestHelper helper) {
        runWithRuntimeLock(helper, () -> {
            Cow cow = createCow(helper, false);
            cow.addEffect(new MobEffectInstance(MobEffects.POISON, 200, 0));
            cow.addEffect(new MobEffectInstance(MobEffects.SPEED, 200, 0));

            SkriptRuntime runtime = SkriptRuntime.instance();
            runtime.clearScripts();
            runtime.loadFromResource("skript/gametest/event/potion_effect_cleared_names_entity.sk");

            boolean cleared = cow.removeAllEffects();

            helper.assertTrue(
                    cleared,
                    Component.literal("Expected clear-all potion removal to succeed.")
            );
            helper.assertTrue(
                    cow.getCustomName() != null && "poison cleared".equals(cow.getCustomName().getString()),
                    Component.literal("Expected entity potion clear event script to rename the entity. Actual name: "
                            + (cow.getCustomName() == null ? "null" : cow.getCustomName().getString()))
            );
            runtime.clearScripts();
        });
    }

    @GameTest
    public void zombieSpawnEventExecutesRealScript(GameTestHelper helper) {
        runWithRuntimeLock(helper, () -> {
            SkriptRuntime runtime = SkriptRuntime.instance();
            runtime.clearScripts();
            runtime.loadFromResource("skript/gametest/event/spawning_of_zombie_names_entity.sk");

            Zombie zombie = new Zombie(EntityType.ZOMBIE, helper.getLevel());
            zombie.setPos(0.5D, 1.0D, 0.5D);
            helper.getLevel().addFreshEntity(zombie);

            helper.assertTrue(
                    zombie.getCustomName() != null && "spawned zombie".equals(zombie.getCustomName().getString()),
                    Component.literal("Expected zombie spawn event script to rename the spawned zombie.")
            );
            runtime.clearScripts();
        });
    }

    @GameTest
    public void zombieTransformDueToCuringExecutesRealScript(GameTestHelper helper) {
        runWithRuntimeLock(helper, () -> {
            SkriptRuntime runtime = SkriptRuntime.instance();
            runtime.clearScripts();
            runtime.loadFromResource("skript/gametest/event/zombie_transforming_due_to_curing_names_entity.sk");

            ZombieVillager zombieVillager = (ZombieVillager) helper.spawnWithNoFreeWill(EntityType.ZOMBIE_VILLAGER, 0.5F, 1.0F, 0.5F);
            invokeZombieVillagerFinishConversion(helper, zombieVillager);

            helper.assertTrue(
                    zombieVillager.getCustomName() != null && "transformed zombie".equals(zombieVillager.getCustomName().getString()),
                    Component.literal("Expected zombie curing transform event script to rename the transforming zombie.")
            );
            runtime.clearScripts();
        });
    }

    @GameTest
    public void experienceOrbSpawnEventExecutesRealScript(GameTestHelper helper) {
        runWithRuntimeLock(helper, () -> {
            SkriptRuntime runtime = SkriptRuntime.instance();
            runtime.clearScripts();
            runtime.loadFromResource("skript/gametest/event/experience_orb_spawn_marks_block.sk");

            ExperienceOrb orb = new ExperienceOrb(helper.getLevel(), 0.5D, 1.0D, 0.5D, 5);
            helper.getLevel().addFreshEntity(orb);

            helper.assertBlockPresent(Blocks.LAPIS_BLOCK, new BlockPos(6, 1, 0));
            runtime.clearScripts();
        });
    }

    @GameTest
    public void itemSpawnOfAppleExecutesRealScript(GameTestHelper helper) {
        runWithRuntimeLock(helper, () -> {
            SkriptRuntime runtime = SkriptRuntime.instance();
            runtime.clearScripts();
            runtime.loadFromResource("skript/gametest/event/item_spawn_of_apple_names_item.sk");

            ItemEntity itemEntity = new ItemEntity(helper.getLevel(), 0.5D, 1.0D, 0.5D, new ItemStack(Items.APPLE));
            helper.getLevel().addFreshEntity(itemEntity);

            ItemStack spawnedApple = itemEntity.getItem();
            helper.assertTrue(
                    spawnedApple.getCustomName() != null && "spawned apple".equals(spawnedApple.getCustomName().getString()),
                    Component.literal("Expected apple item spawn event script to rename the spawned item.")
            );
            runtime.clearScripts();
        });
    }

    @GameTest
    public void itemDespawnOfAppleExecutesRealScript(GameTestHelper helper) {
        runWithRuntimeLock(helper, () -> {
            SkriptRuntime runtime = SkriptRuntime.instance();
            runtime.clearScripts();
            runtime.loadFromResource("skript/gametest/event/item_despawn_of_apple_marks_block.sk");

            ItemEntity itemEntity = new ItemEntity(helper.getLevel(), 0.5D, 1.0D, 0.5D, new ItemStack(Items.APPLE));
            helper.getLevel().addFreshEntity(itemEntity);
            setIntField(itemEntity, "age", 5999);

            itemEntity.tick();

            helper.assertTrue(
                    itemEntity.isRemoved(),
                    Component.literal("Expected item despawn test setup to remove the aged item entity on tick.")
            );
            helper.assertBlockPresent(Blocks.EMERALD_BLOCK, new BlockPos(6, 1, 0));
            runtime.clearScripts();
        });
    }

    @GameTest
    public void itemMergeOfAppleExecutesRealScript(GameTestHelper helper) {
        runWithRuntimeLock(helper, () -> {
            SkriptRuntime runtime = SkriptRuntime.instance();
            runtime.clearScripts();
            runtime.loadFromResource("skript/gametest/event/item_merge_of_apple_marks_block.sk");

            ItemEntity source = new ItemEntity(helper.getLevel(), 0.5D, 1.0D, 0.5D, new ItemStack(Items.APPLE));
            ItemEntity target = new ItemEntity(helper.getLevel(), 0.5D, 1.0D, 0.5D, new ItemStack(Items.APPLE));
            helper.getLevel().addFreshEntity(source);
            helper.getLevel().addFreshEntity(target);
            setIntField(source, "age", 40);
            setIntField(target, "age", 40);

            source.tick();
            if (!target.isRemoved()) {
                target.tick();
            }

            helper.assertBlockPresent(Blocks.GOLD_BLOCK, new BlockPos(7, 1, 0));
            runtime.clearScripts();
        });
    }

    @GameTest
    public void useItemBridgeExecutesLoadedScript(GameTestHelper helper) {
        runWithRuntimeLock(helper, () -> {
            SkriptRuntime runtime = SkriptRuntime.instance();
            runtime.clearScripts();
            runtime.loadFromResource("skript/gametest/event/use_item_renames_item.sk");

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

    private void spawnPrimedTnt(GameTestHelper helper, double x, double y, double z) {
        PrimedTnt primedTnt = new PrimedTnt(helper.getLevel(), x, y, z, null);
        primedTnt.setNoGravity(true);
        primedTnt.setDeltaMovement(Vec3.ZERO);
        primedTnt.setFuse(1);
        helper.getLevel().addFreshEntity(primedTnt);
    }

    private void buildSingleTierBeaconBase(GameTestHelper helper, BlockPos beaconPos) {
        BlockPos baseCenter = beaconPos.below();
        for (int x = -1; x <= 1; x++) {
            for (int z = -1; z <= 1; z++) {
                helper.getLevel().setBlockAndUpdate(baseCenter.offset(x, 0, z), Blocks.IRON_BLOCK.defaultBlockState());
            }
        }
    }

    @GameTest
    public void cancelDamageEventPreventsHurt(GameTestHelper helper) {
        runWithRuntimeLock(helper, () -> {
            SkriptRuntime runtime = SkriptRuntime.instance();
            runtime.clearScripts();
            Variables.clearAll();
            runtime.loadFromResource("skript/gametest/event/cancel_damage_sets_variable.sk");

            Zombie zombie = helper.getLevel().getEntitiesOfClass(Zombie.class,
                    helper.getLevel().getWorldBorder().getCollisionShape().bounds()).stream().findFirst().orElse(null);
            if (zombie == null) {
                zombie = new Zombie(EntityType.ZOMBIE, helper.getLevel());
                zombie.setPos(helper.absolutePos(new BlockPos(0, 2, 0)).getCenter());
                helper.getLevel().addFreshEntity(zombie);
            }

            float healthBefore = zombie.getHealth();

            // Dispatch damage via the Fabric callback
            boolean allowed = ServerLivingEntityEvents.ALLOW_DAMAGE.invoker().allowDamage(
                    zombie,
                    zombie.damageSources().generic(),
                    5.0f
            );

            helper.assertTrue(
                    Boolean.TRUE.equals(Variables.getVariable("gametest::cancel_damage_ran", null, false)),
                    Component.literal("Expected the on-damage script to execute and set the variable.")
            );
            helper.assertTrue(
                    !allowed,
                    Component.literal("Expected ALLOW_DAMAGE to return false when script cancels the event.")
            );
            runtime.clearScripts();
        });
    }

}
