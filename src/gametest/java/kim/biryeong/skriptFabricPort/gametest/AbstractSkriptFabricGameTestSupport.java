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
import net.minecraft.network.protocol.common.ServerboundResourcePackPacket;
import net.minecraft.network.protocol.game.ServerboundAcceptTeleportationPacket;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;
import net.minecraft.network.protocol.game.ServerboundPlayerLoadedPacket;
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
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.item.equipment.Equippable;
import net.minecraft.world.item.trading.ItemCost;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraft.world.level.block.BasePressurePlateBlock;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.TripWireBlock;
import net.minecraft.world.level.block.WitherRoseBlock;
import net.minecraft.world.level.block.entity.AbstractFurnaceBlockEntity;
import net.minecraft.world.level.block.entity.BeaconBlockEntity;
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
import org.skriptlang.skript.fabric.runtime.SkriptRuntime;
import org.skriptlang.skript.lang.properties.Property;
import org.skriptlang.skript.lang.properties.handlers.WXYZHandler;
import kim.biryeong.skriptFabric.mixin.ServerGamePacketListenerImplAccessor;
import ch.njol.util.Kleenean;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.skriptlang.skript.fabric.runtime.GameTestRuntimeContext;

public abstract class AbstractSkriptFabricGameTestSupport {

    protected static final AtomicBoolean RUNTIME_LOCK = new AtomicBoolean(false);

    protected void runWithRuntimeLock(GameTestHelper helper, LockedRuntimeBody body) {
        helper.succeedWhen(() -> {
            if (!RUNTIME_LOCK.compareAndSet(false, true)) {
                return;
            }
            SkriptRuntime runtime = SkriptRuntime.instance();
            try {
                runtime.clearScripts();
                Variables.clearAll();
                GameTestRuntimeContext.withHelper(helper, body::run);
            } finally {
                runtime.clearScripts();
                Variables.clearAll();
                RUNTIME_LOCK.set(false);
            }
        });
    }

    protected void assertUseEntityScriptSetsMarker(
            GameTestHelper helper,
            String scriptPath,
            net.minecraft.world.entity.Entity entity,
            BlockPos playerMarkerRelative,
            net.minecraft.world.level.block.Block expectedBlock
    ) {
        assertUseEntityScriptSetsMarker(helper, scriptPath, entity, playerMarkerRelative, expectedBlock, null);
    }

    protected void assertUseEntityScriptSetsMarker(
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

    protected void assertUseEntityScriptNamesEntity(
            GameTestHelper helper,
            String scriptPath,
            net.minecraft.world.entity.Entity entity,
            String expectedName
    ) {
        assertUseEntityScriptNamesEntity(helper, scriptPath, entity, expectedName, null);
    }

    protected void assertUseEntityScriptNamesEntity(
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

    protected void assertUseEntityScriptDoesNotNameEntity(
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

    protected void assertAttackEntityScriptNamesEntity(
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

    protected void assertUseItemScriptNamesItem(
            GameTestHelper helper,
            String scriptPath,
            ItemStack itemStack,
            String expectedName
    ) {
        assertUseItemScriptNamesItem(helper, scriptPath, itemStack, expectedName, null);
    }

    protected void assertUseItemScriptNamesItem(
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

    protected FishingHook createFishingHook(GameTestHelper helper) {
        ServerPlayer player = helper.makeMockServerPlayerInLevel();
        player.setGameMode(GameType.CREATIVE);
        return new FishingHook(player, helper.getLevel(), 0, 0);
    }

    protected void assertFishingScriptSetsMarker(
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

    protected Display.TextDisplay createTextDisplay(GameTestHelper helper, byte flags) {
        Display.TextDisplay textDisplay = new Display.TextDisplay(EntityType.TEXT_DISPLAY, helper.getLevel());
        textDisplay.setPos(0.5D, 1.0D, 0.5D);
        PrivateEntityAccess.setTextDisplayFlags(textDisplay, flags);
        helper.getLevel().addFreshEntity(textDisplay);
        return textDisplay;
    }

    protected Display.ItemDisplay createItemDisplay(GameTestHelper helper, ItemDisplayContext transform) {
        Display.ItemDisplay itemDisplay = new Display.ItemDisplay(EntityType.ITEM_DISPLAY, helper.getLevel());
        itemDisplay.setPos(0.5D, 1.0D, 0.5D);
        itemDisplay.getSlot(0).set(new ItemStack(Items.STICK));
        PrivateEntityAccess.setItemDisplayTransform(itemDisplay, transform);
        helper.getLevel().addFreshEntity(itemDisplay);
        return itemDisplay;
    }

    protected Interaction createInteraction(GameTestHelper helper, boolean responsive) {
        Interaction interaction = new Interaction(EntityType.INTERACTION, helper.getLevel());
        interaction.setPos(0.5D, 1.0D, 0.5D);
        PrivateEntityAccess.setInteractionResponse(interaction, responsive);
        helper.getLevel().addFreshEntity(interaction);
        return interaction;
    }

    protected MinecartChest createChestMinecart(GameTestHelper helper, boolean withLootTable) {
        MinecartChest chestMinecart = new MinecartChest(EntityType.CHEST_MINECART, helper.getLevel());
        chestMinecart.setPos(0.5D, 1.0D, 0.5D);
        if (withLootTable) {
            chestMinecart.setLootTable(BuiltInLootTables.SIMPLE_DUNGEON, 1L);
        }
        helper.getLevel().addFreshEntity(chestMinecart);
        return chestMinecart;
    }

    protected Cow createCow(GameTestHelper helper, boolean baby) {
        Cow cow = (Cow) helper.spawnWithNoFreeWill(EntityType.COW, 0.5F, 1.0F, 0.5F);
        cow.setBaby(baby);
        return cow;
    }

    protected AbstractFurnaceBlockEntity createFurnace(GameTestHelper helper, BlockPos relativePos) {
        BlockPos absolutePos = helper.absolutePos(relativePos);
        helper.getLevel().setBlockAndUpdate(absolutePos, Blocks.FURNACE.defaultBlockState());
        AbstractFurnaceBlockEntity furnace = (AbstractFurnaceBlockEntity) helper.getLevel().getBlockEntity(absolutePos);
        helper.assertTrue(furnace != null, Component.literal("Expected furnace block entity to exist for furnace event test."));
        if (furnace == null) {
            throw new IllegalStateException("Furnace block entity was not created.");
        }
        return furnace;
    }

    protected void tickFurnace(GameTestHelper helper, AbstractFurnaceBlockEntity furnace) {
        AbstractFurnaceBlockEntity.serverTick(
                helper.getLevel(),
                furnace.getBlockPos(),
                helper.getLevel().getBlockState(furnace.getBlockPos()),
                furnace
        );
    }

    protected BeaconBlockEntity beaconAt(GameTestHelper helper, BlockPos pos) {
        BeaconBlockEntity beacon = helper.getLevel().getBlockEntity(pos) instanceof BeaconBlockEntity found ? found : null;
        helper.assertTrue(beacon != null, Component.literal("Expected beacon block entity to exist for beacon event test."));
        if (beacon == null) {
            throw new IllegalStateException("Beacon block entity was not created.");
        }
        return beacon;
    }

    @SuppressWarnings("unchecked")
    protected void invokeBeaconApplyEffects(
            GameTestHelper helper,
            BlockPos pos,
            int levels,
            Holder<net.minecraft.world.effect.MobEffect> primaryPower,
            Holder<net.minecraft.world.effect.MobEffect> secondaryPower
    ) {
        try {
            Method method = BeaconBlockEntity.class.getDeclaredMethod(
                    "applyEffects",
                    net.minecraft.world.level.Level.class,
                    BlockPos.class,
                    int.class,
                    Holder.class,
                    Holder.class
            );
            method.setAccessible(true);
            method.invoke(null, helper.getLevel(), pos, levels, primaryPower, secondaryPower);
        } catch (ReflectiveOperationException exception) {
            throw new IllegalStateException("Failed to invoke BeaconBlockEntity.applyEffects for GameTest.", exception);
        }
    }

    protected void invokeBeaconTick(GameTestHelper helper, BlockPos pos) {
        try {
            Method method = BeaconBlockEntity.class.getDeclaredMethod(
                    "tick",
                    net.minecraft.world.level.Level.class,
                    BlockPos.class,
                    net.minecraft.world.level.block.state.BlockState.class,
                    BeaconBlockEntity.class
            );
            method.setAccessible(true);
            method.invoke(null, helper.getLevel(), pos, helper.getLevel().getBlockState(pos), beaconAt(helper, pos));
        } catch (ReflectiveOperationException exception) {
            throw new IllegalStateException("Failed to invoke BeaconBlockEntity.tick for GameTest.", exception);
        }
    }

    protected void invokeConduitApplyEffects(GameTestHelper helper, BlockPos pos, int effectBlockCount) {
        try {
            Method method = ConduitBlockEntity.class.getDeclaredMethod(
                    "applyEffects",
                    net.minecraft.world.level.Level.class,
                    BlockPos.class,
                    List.class
            );
            method.setAccessible(true);
            List<BlockPos> effectBlocks = new ArrayList<>();
            for (int i = 0; i < effectBlockCount; i++) {
                effectBlocks.add(pos);
            }
            method.invoke(null, helper.getLevel(), pos, effectBlocks);
        } catch (ReflectiveOperationException exception) {
            throw new IllegalStateException("Failed to invoke ConduitBlockEntity.applyEffects for GameTest.", exception);
        }
    }

    protected void sendPlayerInput(ServerPlayer player, Input input) {
        player.connection.handlePlayerInput(new ServerboundPlayerInputPacket(input));
    }

    protected void sendPlayerMove(ServerPlayer player, Vec3 position, float yaw, float pitch) {
        preparePlayerForMovementPackets(player);
        player.connection.resetPosition();
        player.connection.handleMovePlayer(new ServerboundMovePlayerPacket.PosRot(position, yaw, pitch, true, false));
    }

    protected void sendResourcePackResponse(ServerPlayer player, ServerboundResourcePackPacket.Action action) {
        player.connection.handleResourcePackResponse(new ServerboundResourcePackPacket(UUID.randomUUID(), action));
    }

    protected void preparePlayerForMovementPackets(ServerPlayer player) {
        if (!player.hasClientLoaded()) {
            player.connection.handleAcceptPlayerLoad(new ServerboundPlayerLoadedPacket());
        }
        ServerGamePacketListenerImplAccessor accessor = (ServerGamePacketListenerImplAccessor) player.connection;
        if (accessor.skript$getAwaitingPositionFromClient() != null) {
            player.connection.handleAcceptTeleportPacket(
                    new ServerboundAcceptTeleportationPacket(accessor.skript$getAwaitingTeleport())
            );
        }
    }

    protected void invokeArrowPostHurtEffects(Arrow arrow, LivingEntity target) {
        try {
            Method method = Arrow.class.getDeclaredMethod("doPostHurtEffects", LivingEntity.class);
            method.setAccessible(true);
            method.invoke(arrow, target);
        } catch (ReflectiveOperationException exception) {
            throw new IllegalStateException("Failed to invoke Arrow.doPostHurtEffects for GameTest.", exception);
        }
    }

    protected void invokePufferfishTouch(Pufferfish pufferfish, GameTestHelper helper, Mob target) {
        try {
            Method method = Pufferfish.class.getDeclaredMethod("touch", net.minecraft.server.level.ServerLevel.class, Mob.class);
            method.setAccessible(true);
            method.invoke(pufferfish, helper.getLevel(), target);
        } catch (ReflectiveOperationException exception) {
            throw new IllegalStateException("Failed to invoke Pufferfish.touch for GameTest.", exception);
        }
    }

    protected void invokeWitherRoseEntityInside(GameTestHelper helper, BlockPos pos, Entity entity) {
        try {
            Method method = WitherRoseBlock.class.getDeclaredMethod(
                    "entityInside",
                    net.minecraft.world.level.block.state.BlockState.class,
                    net.minecraft.world.level.Level.class,
                    BlockPos.class,
                    net.minecraft.world.entity.Entity.class,
                    InsideBlockEffectApplier.class
            );
            method.setAccessible(true);
            method.invoke(
                    Blocks.WITHER_ROSE,
                    helper.getLevel().getBlockState(pos),
                    helper.getLevel(),
                    pos,
                    entity,
                    InsideBlockEffectApplier.NOOP
            );
        } catch (ReflectiveOperationException exception) {
            throw new IllegalStateException("Failed to invoke WitherRoseBlock.entityInside for GameTest.", exception);
        }
    }

    protected void invokePressurePlateEntityInside(GameTestHelper helper, BlockPos pos, Entity entity) {
        try {
            Method method = BasePressurePlateBlock.class.getDeclaredMethod(
                    "entityInside",
                    net.minecraft.world.level.block.state.BlockState.class,
                    net.minecraft.world.level.Level.class,
                    BlockPos.class,
                    net.minecraft.world.entity.Entity.class,
                    InsideBlockEffectApplier.class
            );
            method.setAccessible(true);
            method.invoke(
                    helper.getLevel().getBlockState(pos).getBlock(),
                    helper.getLevel().getBlockState(pos),
                    helper.getLevel(),
                    pos,
                    entity,
                    InsideBlockEffectApplier.NOOP
            );
        } catch (ReflectiveOperationException exception) {
            throw new IllegalStateException("Failed to invoke BasePressurePlateBlock.entityInside for GameTest.", exception);
        }
    }

    protected void invokeTripWireEntityInside(GameTestHelper helper, BlockPos pos, Entity entity) {
        try {
            Method method = TripWireBlock.class.getDeclaredMethod(
                    "entityInside",
                    net.minecraft.world.level.block.state.BlockState.class,
                    net.minecraft.world.level.Level.class,
                    BlockPos.class,
                    net.minecraft.world.entity.Entity.class,
                    InsideBlockEffectApplier.class
            );
            method.setAccessible(true);
            method.invoke(
                    helper.getLevel().getBlockState(pos).getBlock(),
                    helper.getLevel().getBlockState(pos),
                    helper.getLevel(),
                    pos,
                    entity,
                    InsideBlockEffectApplier.NOOP
            );
        } catch (ReflectiveOperationException exception) {
            throw new IllegalStateException("Failed to invoke TripWireBlock.entityInside for GameTest.", exception);
        }
    }

    protected void invokeEndPortalEntityInside(GameTestHelper helper, BlockPos pos, Entity entity) {
        try {
            Method method = net.minecraft.world.level.block.EndPortalBlock.class.getDeclaredMethod(
                    "entityInside",
                    net.minecraft.world.level.block.state.BlockState.class,
                    net.minecraft.world.level.Level.class,
                    BlockPos.class,
                    net.minecraft.world.entity.Entity.class,
                    InsideBlockEffectApplier.class
            );
            method.setAccessible(true);
            method.invoke(
                    helper.getLevel().getBlockState(pos).getBlock(),
                    helper.getLevel().getBlockState(pos),
                    helper.getLevel(),
                    pos,
                    entity,
                    InsideBlockEffectApplier.NOOP
            );
        } catch (ReflectiveOperationException exception) {
            throw new IllegalStateException("Failed to invoke EndPortalBlock.entityInside for GameTest.", exception);
        }
    }

    protected boolean invokeTotemDeathProtection(LivingEntity entity, DamageSource damageSource) {
        try {
            Method method = LivingEntity.class.getDeclaredMethod("checkTotemDeathProtection", DamageSource.class);
            method.setAccessible(true);
            return (boolean) method.invoke(entity, damageSource);
        } catch (ReflectiveOperationException exception) {
            throw new IllegalStateException("Failed to invoke LivingEntity.checkTotemDeathProtection for GameTest.", exception);
        }
    }

    protected void invokeZombieVillagerFinishConversion(GameTestHelper helper, ZombieVillager zombieVillager) {
        try {
            Method method = ZombieVillager.class.getDeclaredMethod("finishConversion", net.minecraft.server.level.ServerLevel.class);
            method.setAccessible(true);
            method.invoke(zombieVillager, helper.getLevel());
        } catch (ReflectiveOperationException exception) {
            throw new IllegalStateException("Failed to invoke ZombieVillager.finishConversion for GameTest.", exception);
        }
    }

    protected void invokeAxolotlSupportingEffects(Axolotl axolotl, ServerPlayer player) {
        try {
            Method method = Axolotl.class.getDeclaredMethod("applySupportingEffects", net.minecraft.world.entity.player.Player.class);
            method.setAccessible(true);
            method.invoke(axolotl, player);
        } catch (ReflectiveOperationException exception) {
            throw new IllegalStateException("Failed to invoke Axolotl.applySupportingEffects for GameTest.", exception);
        }
    }

    protected void invokeDolphinSwimWithPlayerGoalStart(Dolphin dolphin, ServerPlayer player) {
        try {
            Class<?> goalClass = Class.forName("net.minecraft.world.entity.animal.Dolphin$DolphinSwimWithPlayerGoal");
            var constructor = goalClass.getDeclaredConstructor(Dolphin.class, double.class);
            constructor.setAccessible(true);
            Object goal = constructor.newInstance(dolphin, 1.0D);
            Field playerField = goalClass.getDeclaredField("player");
            playerField.setAccessible(true);
            playerField.set(goal, player);
            Method method = goalClass.getDeclaredMethod("start");
            method.setAccessible(true);
            method.invoke(goal);
        } catch (ReflectiveOperationException exception) {
            throw new IllegalStateException("Failed to invoke DolphinSwimWithPlayerGoal.start for GameTest.", exception);
        }
    }

    protected void invokePlayerTurtleHelmetTick(ServerPlayer player) {
        try {
            Method method = net.minecraft.world.entity.player.Player.class.getDeclaredMethod("turtleHelmetTick");
            method.setAccessible(true);
            method.invoke(player);
        } catch (ReflectiveOperationException exception) {
            throw new IllegalStateException("Failed to invoke Player.turtleHelmetTick for GameTest.", exception);
        }
    }

    protected void invokeIllusionerMirrorSpell(Illusioner illusioner) {
        try {
            Class<?> goalClass = Class.forName("net.minecraft.world.entity.monster.Illusioner$IllusionerMirrorSpellGoal");
            var constructor = goalClass.getDeclaredConstructor(Illusioner.class);
            constructor.setAccessible(true);
            Object goal = constructor.newInstance(illusioner);
            Method method = goalClass.getDeclaredMethod("performSpellCasting");
            method.setAccessible(true);
            method.invoke(goal);
        } catch (ReflectiveOperationException exception) {
            throw new IllegalStateException("Failed to invoke IllusionerMirrorSpellGoal.performSpellCasting for GameTest.", exception);
        }
    }

    protected void invokeSpiderFinalizeSpawn(GameTestHelper helper, Spider spider) {
        try {
            Class<?> groupDataClass = Class.forName("net.minecraft.world.entity.monster.Spider$SpiderEffectsGroupData");
            Object groupData = groupDataClass.getDeclaredConstructor().newInstance();
            Field effectField = groupDataClass.getDeclaredField("effect");
            effectField.setAccessible(true);
            effectField.set(groupData, MobEffects.SPEED);

            Method method = Spider.class.getDeclaredMethod(
                    "finalizeSpawn",
                    net.minecraft.world.level.ServerLevelAccessor.class,
                    net.minecraft.world.DifficultyInstance.class,
                    EntitySpawnReason.class,
                    net.minecraft.world.entity.SpawnGroupData.class
            );
            method.setAccessible(true);
            method.invoke(
                    spider,
                    helper.getLevel(),
                    helper.getLevel().getCurrentDifficultyAt(spider.blockPosition()),
                    EntitySpawnReason.NATURAL,
                    groupData
            );
        } catch (ReflectiveOperationException exception) {
            throw new IllegalStateException("Failed to invoke Spider.finalizeSpawn for GameTest.", exception);
        }
    }

    protected void invokeVillagerRewardTradeXp(Villager villager, MerchantOffer offer) {
        try {
            Method method = Villager.class.getDeclaredMethod("rewardTradeXp", MerchantOffer.class);
            method.setAccessible(true);
            method.invoke(villager, offer);
        } catch (ReflectiveOperationException exception) {
            throw new IllegalStateException("Failed to invoke Villager.rewardTradeXp for GameTest.", exception);
        }
    }

    protected void invokeVillagerCustomServerAiStep(GameTestHelper helper, Villager villager) {
        try {
            Method method = Villager.class.getDeclaredMethod("customServerAiStep", net.minecraft.server.level.ServerLevel.class);
            method.setAccessible(true);
            method.invoke(villager, helper.getLevel());
        } catch (ReflectiveOperationException exception) {
            throw new IllegalStateException("Failed to invoke Villager.customServerAiStep for GameTest.", exception);
        }
    }

    protected void refreshWaterState(ServerPlayer player) {
        try {
            Method method = net.minecraft.world.entity.Entity.class.getDeclaredMethod("updateInWaterStateAndDoWaterCurrentPushing");
            method.setAccessible(true);
            method.invoke(player);
        } catch (ReflectiveOperationException exception) {
            throw new IllegalStateException("Failed to refresh player water state for GameTest.", exception);
        }
    }

    protected boolean containsPotionEffect(PotionContents potionContents, net.minecraft.core.Holder<net.minecraft.world.effect.MobEffect> effect) {
        for (MobEffectInstance instance : potionContents.getAllEffects()) {
            if (instance.getEffect().value() == effect.value()) {
                return true;
            }
        }
        return false;
    }

    protected Path writeTempScript(String prefix, String source) {
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
    protected <T> Expression<? extends T> parseExpressionInEvent(
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

    protected Condition parseConditionInEvent(String condition, Class<?>... eventClasses) {
        ParserInstance parser = ParserInstance.get();
        String previousEventName = parser.getCurrentEventName();
        Class<?>[] previousEventClasses = parser.getCurrentEventClasses();
        try {
            parser.setCurrentEvent("gametest", eventClasses);
            return Condition.parse(condition, null);
        } finally {
            if (previousEventName == null) {
                parser.deleteCurrentEvent();
            } else {
                parser.setCurrentEvent(previousEventName, previousEventClasses);
            }
        }
    }

    protected void setFishingHookOpenWater(FishingHook hook, boolean openWater) {
        try {
            Field openWaterField = FishingHook.class.getDeclaredField("openWater");
            openWaterField.setAccessible(true);
            openWaterField.setBoolean(hook, openWater);
        } catch (ReflectiveOperationException exception) {
            throw new IllegalStateException("Failed to set fishing hook open water state for test setup.", exception);
        }
    }

    protected void setFishingHookedEntity(FishingHook hook, net.minecraft.world.entity.Entity entity) {
        try {
            Field hookedInField = FishingHook.class.getDeclaredField("hookedIn");
            hookedInField.setAccessible(true);
            hookedInField.set(hook, entity);
        } catch (ReflectiveOperationException exception) {
            throw new IllegalStateException("Failed to set fishing hook hooked entity for test setup.", exception);
        }
    }

    protected void setIntField(Object target, String fieldName, int value) {
        try {
            Field field = findField(target.getClass(), fieldName);
            field.setAccessible(true);
            field.setInt(target, value);
        } catch (ReflectiveOperationException exception) {
            throw new IllegalStateException("Failed to set integer field '" + fieldName + "' for GameTest.", exception);
        }
    }

    protected void setBooleanField(Object target, String fieldName, boolean value) {
        try {
            Field field = findField(target.getClass(), fieldName);
            field.setAccessible(true);
            field.setBoolean(target, value);
        } catch (ReflectiveOperationException exception) {
            throw new IllegalStateException("Failed to set boolean field '" + fieldName + "' for GameTest.", exception);
        }
    }

    private Field findField(Class<?> type, String fieldName) throws NoSuchFieldException {
        Class<?> current = type;
        while (current != null) {
            try {
                return current.getDeclaredField(fieldName);
            } catch (NoSuchFieldException ignored) {
                current = current.getSuperclass();
            }
        }
        throw new NoSuchFieldException(fieldName);
    }

    protected ItemStack createEquippableTestItem(
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

    protected String describeEquippable(@org.jetbrains.annotations.Nullable Equippable equippable) {
        if (equippable == null) {
            return "null";
        }
        return "damageOnHurt=" + equippable.damageOnHurt()
                + ", dispensable=" + equippable.dispensable()
                + ", equipOnInteract=" + equippable.equipOnInteract()
                + ", canBeSheared=" + equippable.canBeSheared()
                + ", swappable=" + equippable.swappable();
    }

    protected Trigger getOnlyLoadedTrigger(SkriptRuntime runtime) {
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

    protected String describeTriggerItems(Trigger trigger) {
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

    protected Object getFirstTriggerItem(Trigger trigger) {
        try {
            Field firstField = TriggerSection.class.getDeclaredField("first");
            firstField.setAccessible(true);
            return firstField.get(trigger);
        } catch (ReflectiveOperationException exception) {
            throw new IllegalStateException("Failed to read first trigger item.", exception);
        }
    }

    protected Object getTriggerItem(Trigger trigger, int index) {
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

    protected String describeExpressionFields(Object syntax, org.skriptlang.skript.lang.event.SkriptEvent event) {
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

    protected boolean skriptPatternMatches(String input, String pattern) {
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
    protected interface LockedRuntimeBody {
        void run();
    }
}
