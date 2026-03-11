package org.skriptlang.skript.fabric.runtime;

import java.util.Collections;
import java.util.Collection;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import net.fabricmc.fabric.api.event.player.AttackEntityCallback;
import net.fabricmc.fabric.api.event.player.AttackBlockCallback;
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.fabricmc.fabric.api.event.player.UseEntityCallback;
import net.fabricmc.fabric.api.event.player.UseItemCallback;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerEntityEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.entity.player.Input;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Interaction;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.projectile.FishingHook;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.level.block.entity.BrewingStandBlockEntity;
import net.minecraft.world.level.block.entity.AbstractFurnaceBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.entity.Mob;
import org.jetbrains.annotations.Nullable;
import ch.njol.skript.events.FabricEventCompatHandles;
import ch.njol.skript.events.FabricPlayerEventHandles;
import org.skriptlang.skript.bukkit.loottables.LootTable;
import org.skriptlang.skript.bukkit.potion.util.SkriptPotionEffect;
import org.skriptlang.skript.fabric.compat.FabricBreedingItemSource;
import org.skriptlang.skript.fabric.compat.FabricLocation;
import org.skriptlang.skript.fabric.compat.FabricInteractionState;
import org.skriptlang.skript.fabric.compat.PrivateFurnaceAccess;

public final class SkriptFabricEventBridge {

    private static final Map<net.minecraft.world.level.storage.loot.LootTable, ResourceKey<net.minecraft.world.level.storage.loot.LootTable>> LOOT_TABLE_KEYS =
            Collections.synchronizedMap(new IdentityHashMap<>());
    private static volatile boolean registered;

    private SkriptFabricEventBridge() {
    }

    public static void register() {
        if (registered) {
            return;
        }
        synchronized (SkriptFabricEventBridge.class) {
            if (registered) {
                return;
            }
            ServerTickEvents.END_SERVER_TICK.register(SkriptFabricEventBridge::dispatchServerTick);
            PlayerBlockBreakEvents.AFTER.register(SkriptFabricEventBridge::dispatchBlockBreak);
            AttackEntityCallback.EVENT.register(SkriptFabricEventBridge::dispatchAttackEntity);
            AttackBlockCallback.EVENT.register(SkriptFabricEventBridge::dispatchAttackBlock);
            UseBlockCallback.EVENT.register(SkriptFabricEventBridge::dispatchUseBlock);
            UseEntityCallback.EVENT.register(SkriptFabricEventBridge::dispatchUseEntity);
            UseItemCallback.EVENT.register(SkriptFabricEventBridge::dispatchUseItem);
            ServerEntityEvents.ENTITY_LOAD.register(SkriptFabricEventBridge::dispatchEntityLoad);
            ServerLivingEntityEvents.ALLOW_DAMAGE.register(SkriptFabricEventBridge::dispatchDamage);
            ServerLivingEntityEvents.AFTER_DEATH.register(SkriptFabricEventBridge::dispatchDeath);
            ServerLivingEntityEvents.MOB_CONVERSION.register(SkriptFabricEventBridge::dispatchEntityTransform);
            registered = true;
        }
    }

    private static void dispatchServerTick(MinecraftServer server) {
        SkriptFabricTaskScheduler.tick(server);
        SkriptRuntime.instance().dispatch(new org.skriptlang.skript.lang.event.SkriptEvent(
                FabricRuntimeHandle.SERVER_TICK,
                server,
                server.overworld(),
                null
        ));
    }

    private static void dispatchBlockBreak(Level level, net.minecraft.world.entity.player.Player player, BlockPos pos, BlockState state, BlockEntity blockEntity) {
        if (!(level instanceof ServerLevel serverLevel) || !(player instanceof ServerPlayer serverPlayer)) {
            return;
        }
        dispatchCompatBlock(serverLevel, pos.immutable(), FabricEventCompatHandles.BlockAction.BREAK, state, null, true, serverPlayer);
        SkriptRuntime.instance().dispatch(new org.skriptlang.skript.lang.event.SkriptEvent(
                new FabricBlockBreakHandle(serverLevel, serverPlayer, pos.immutable(), state, blockEntity),
                serverLevel.getServer(),
                serverLevel,
                serverPlayer
        ));
    }

    private static InteractionResult dispatchAttackBlock(
            net.minecraft.world.entity.player.Player player,
            Level level,
            InteractionHand hand,
            BlockPos pos,
            net.minecraft.core.Direction direction
    ) {
        if (!(level instanceof ServerLevel serverLevel) || !(player instanceof ServerPlayer serverPlayer)) {
            return InteractionResult.PASS;
        }
        dispatchClick(serverLevel, pos.immutable(), FabricEventCompatHandles.ClickType.LEFT, null, level.getBlockState(pos), player.getItemInHand(hand), serverPlayer);
        return InteractionResult.PASS;
    }

    private static InteractionResult dispatchUseBlock(net.minecraft.world.entity.player.Player player, Level level, InteractionHand hand, BlockHitResult hitResult) {
        if (!(level instanceof ServerLevel serverLevel) || !(player instanceof ServerPlayer serverPlayer)) {
            return InteractionResult.PASS;
        }
        dispatchClick(
                serverLevel,
                hitResult.getBlockPos().immutable(),
                FabricEventCompatHandles.ClickType.RIGHT,
                null,
                serverLevel.getBlockState(hitResult.getBlockPos()),
                player.getItemInHand(hand),
                serverPlayer
        );
        SkriptRuntime.instance().dispatch(new org.skriptlang.skript.lang.event.SkriptEvent(
                new FabricUseBlockHandle(serverLevel, serverPlayer, hand, hitResult),
                serverLevel.getServer(),
                serverLevel,
                serverPlayer
        ));
        return InteractionResult.PASS;
    }

    private static InteractionResult dispatchAttackEntity(
            net.minecraft.world.entity.player.Player player,
            Level level,
            InteractionHand hand,
            Entity entity,
            EntityHitResult hitResult
    ) {
        if (!(level instanceof ServerLevel serverLevel) || !(player instanceof ServerPlayer serverPlayer)) {
            return InteractionResult.PASS;
        }
        if (entity instanceof Interaction interaction) {
            FabricInteractionState.recordAttack(interaction, serverPlayer);
        }
        dispatchClick(serverLevel, entity.blockPosition().immutable(), FabricEventCompatHandles.ClickType.LEFT, entity, null, player.getItemInHand(hand), serverPlayer);
        SkriptRuntime.instance().dispatch(new org.skriptlang.skript.lang.event.SkriptEvent(
                new FabricAttackEntityHandle(serverLevel, serverPlayer, hand, entity, hitResult),
                serverLevel.getServer(),
                serverLevel,
                serverPlayer
        ));
        return InteractionResult.PASS;
    }

    private static InteractionResult dispatchUseEntity(
            net.minecraft.world.entity.player.Player player,
            Level level,
            InteractionHand hand,
            Entity entity,
            EntityHitResult hitResult
    ) {
        if (!(level instanceof ServerLevel serverLevel) || !(player instanceof ServerPlayer serverPlayer)) {
            return InteractionResult.PASS;
        }
        if (entity instanceof Interaction interaction) {
            FabricInteractionState.recordInteract(interaction, serverPlayer);
        }
        dispatchClick(serverLevel, entity.blockPosition().immutable(), FabricEventCompatHandles.ClickType.RIGHT, entity, null, player.getItemInHand(hand), serverPlayer);
        SkriptRuntime.instance().dispatch(new org.skriptlang.skript.lang.event.SkriptEvent(
                new FabricUseEntityHandle(serverLevel, serverPlayer, hand, entity, hitResult),
                serverLevel.getServer(),
                serverLevel,
                serverPlayer
        ));
        return InteractionResult.PASS;
    }

    private static InteractionResult dispatchUseItem(
            net.minecraft.world.entity.player.Player player,
            Level level,
            InteractionHand hand
    ) {
        if (!(level instanceof ServerLevel serverLevel) || !(player instanceof ServerPlayer serverPlayer)) {
            return InteractionResult.PASS;
        }
        SkriptRuntime.instance().dispatch(new org.skriptlang.skript.lang.event.SkriptEvent(
                new FabricUseItemHandle(serverLevel, serverPlayer, hand),
                serverLevel.getServer(),
                serverLevel,
                serverPlayer
        ));
        return InteractionResult.PASS;
    }

    private static void dispatchEntityLoad(Entity entity, ServerLevel level) {
        if (!(entity instanceof net.minecraft.world.entity.player.Player)) {
            SkriptRuntime.instance().dispatch(new org.skriptlang.skript.lang.event.SkriptEvent(
                    new FabricEventCompatHandles.EntityLifecycle(entity, true),
                    level.getServer(),
                    level,
                    entity instanceof ServerPlayer serverPlayer ? serverPlayer : null
            ));
        }
        if (entity instanceof ItemEntity itemEntity) {
            dispatchCompatItem(level, itemEntity.blockPosition().immutable(), FabricEventCompatHandles.ItemAction.SPAWN, itemEntity.getItem(), false, null);
        } else if (entity instanceof ExperienceOrb orb) {
            dispatchExperienceSpawn(level, orb);
        }
    }

    private static boolean dispatchDamage(LivingEntity entity, DamageSource source, float amount) {
        if (!(entity.level() instanceof ServerLevel serverLevel)) {
            return true;
        }
        SkriptRuntime.instance().dispatch(new org.skriptlang.skript.lang.event.SkriptEvent(
                new FabricDamageHandle(serverLevel, entity, source, amount),
                serverLevel.getServer(),
                serverLevel,
                entity instanceof ServerPlayer serverPlayer ? serverPlayer : null
        ));
        return true;
    }

    private static void dispatchDeath(LivingEntity entity, DamageSource source) {
        if (!(entity.level() instanceof ServerLevel serverLevel)) {
            return;
        }
        SkriptRuntime.instance().dispatch(new org.skriptlang.skript.lang.event.SkriptEvent(
                new FabricEventCompatHandles.EntityLifecycle(entity, false),
                serverLevel.getServer(),
                serverLevel,
                entity instanceof ServerPlayer serverPlayer ? serverPlayer : null
        ));
    }

    private static void dispatchEntityTransform(Mob previous, Mob converted, net.minecraft.world.entity.ConversionParams conversionParams) {
        if (!(previous.level() instanceof ServerLevel serverLevel)) {
            return;
        }
        String reason = conversionParams.type().name().toLowerCase(java.util.Locale.ENGLISH);
        SkriptRuntime.instance().dispatch(new org.skriptlang.skript.lang.event.SkriptEvent(
                new FabricEventCompatHandles.EntityTransform(previous, reason),
                serverLevel.getServer(),
                serverLevel,
                null
        ));
    }

    public static void dispatchBrewingFuel(ServerLevel level, BlockPos pos, BrewingStandBlockEntity brewingStand, boolean willConsume) {
        SkriptRuntime.instance().dispatch(new org.skriptlang.skript.lang.event.SkriptEvent(
                new FabricBrewingFuelHandle(level, pos.immutable(), brewingStand, willConsume),
                level.getServer(),
                level,
                null
        ));
    }

    public static void dispatchBrewingStart(ServerLevel level, BlockPos pos, BrewingStandBlockEntity brewingStand) {
        SkriptRuntime.instance().dispatch(new org.skriptlang.skript.lang.event.SkriptEvent(
                new FabricBrewingStartHandle(level, pos.immutable(), brewingStand),
                level.getServer(),
                level,
                null
        ));
    }

    public static void dispatchBrewingComplete(
            ServerLevel level,
            BlockPos pos,
            BrewingStandBlockEntity brewingStand,
            List<ItemStack> results
    ) {
        SkriptRuntime.instance().dispatch(new org.skriptlang.skript.lang.event.SkriptEvent(
                new FabricBrewingCompleteHandle(level, pos.immutable(), brewingStand, results),
                level.getServer(),
                level,
                null
        ));
    }

    public static void dispatchFishing(ServerLevel level, ServerPlayer player, FishingHook hook, boolean lureApplied) {
        dispatchFishing(level, player, hook, null, lureApplied, FabricFishingEventState.FISHING);
    }

    public static void dispatchFishing(
            ServerLevel level,
            ServerPlayer player,
            FishingHook hook,
            @Nullable Entity eventEntity,
            boolean lureApplied,
            FabricFishingEventState state
    ) {
        SkriptRuntime.instance().dispatch(new org.skriptlang.skript.lang.event.SkriptEvent(
                new FabricFishingHandle(level, player, hook, eventEntity, lureApplied, state),
                level.getServer(),
                level,
                player
        ));
    }

    public static @Nullable ItemEntity findNearestCaughtFishEntity(ServerLevel level, FishingHook hook) {
        return level.getEntitiesOfClass(ItemEntity.class, hook.getBoundingBox().inflate(2.0D)).stream()
                .min((left, right) -> Double.compare(left.distanceToSqr(hook), right.distanceToSqr(hook)))
                .orElse(null);
    }

    public static void dispatchBucketCatch(
            ServerLevel level,
            ServerPlayer player,
            LivingEntity entity,
            ItemStack originalBucket,
            ItemStack entityBucket
    ) {
        SkriptRuntime.instance().dispatch(new org.skriptlang.skript.lang.event.SkriptEvent(
                new FabricBucketCatchHandle(level, player, entity, originalBucket, entityBucket),
                level.getServer(),
                level,
                player
        ));
    }

    public static void dispatchBookEdit(ServerPlayer player, ItemStack previous, ItemStack current, boolean signing) {
        ServerLevel level = player.level();
        SkriptRuntime.instance().dispatch(new org.skriptlang.skript.lang.event.SkriptEvent(
                new FabricEventCompatHandles.BookEdit(previous.copy(), current.copy(), signing),
                level.getServer(),
                level,
                player
        ));
    }

    public static void dispatchBeaconEffect(
            ServerLevel level,
            BlockPos pos,
            boolean primary,
            @Nullable net.minecraft.core.Holder<net.minecraft.world.effect.MobEffect> effect
    ) {
        SkriptRuntime.instance().dispatch(new org.skriptlang.skript.lang.event.SkriptEvent(
                new FabricEventCompatHandles.BeaconEffect(level, pos.immutable(), primary, FabricEventCompatHandles.effectName(effect)),
                level.getServer(),
                level,
                null
        ));
    }

    public static void dispatchBeaconToggle(ServerLevel level, BlockPos pos, boolean activated) {
        SkriptRuntime.instance().dispatch(new org.skriptlang.skript.lang.event.SkriptEvent(
                new FabricEventCompatHandles.BeaconToggle(level, pos.immutable(), activated),
                level.getServer(),
                level,
                null
        ));
    }

    public static void dispatchHealing(ServerLevel level, LivingEntity entity, @Nullable String reason, float amount) {
        SkriptRuntime.instance().dispatch(new org.skriptlang.skript.lang.event.SkriptEvent(
                new FabricEventCompatHandles.Healing(entity, reason, amount),
                level.getServer(),
                level,
                entity instanceof ServerPlayer serverPlayer ? serverPlayer : null
        ));
    }

    public static void dispatchExperienceSpawn(ServerLevel level, ExperienceOrb orb) {
        SkriptRuntime.instance().dispatch(new org.skriptlang.skript.lang.event.SkriptEvent(
                new FabricEventCompatHandles.ExperienceSpawn(orb.getValue()),
                level.getServer(),
                level,
                null
        ));
    }

    public static void dispatchBlockPlace(
            ServerLevel level,
            BlockPos pos,
            @Nullable BlockState state,
            @Nullable ItemStack itemStack,
            @Nullable ServerPlayer player
    ) {
        dispatchCompatBlock(level, pos, FabricEventCompatHandles.BlockAction.PLACE, state, itemStack, false, player);
    }

    public static void dispatchEntityBlockChange(
            ServerLevel level,
            BlockPos pos,
            Entity entity,
            @Nullable BlockState from,
            @Nullable BlockState to
    ) {
        SkriptRuntime.instance().dispatch(new org.skriptlang.skript.lang.event.SkriptEvent(
                new FabricEventCompatHandles.EntityBlockChange(level, pos.immutable(), entity, from, to),
                level.getServer(),
                level,
                entity instanceof ServerPlayer serverPlayer ? serverPlayer : null
        ));
    }

    public static void dispatchGrow(
            ServerLevel level,
            BlockPos pos,
            @Nullable BlockState from,
            @Nullable BlockState to,
            @Nullable String structureType
    ) {
        SkriptRuntime.instance().dispatch(new org.skriptlang.skript.lang.event.SkriptEvent(
                new FabricEventCompatHandles.Grow(level, pos.immutable(), from, to, structureType),
                level.getServer(),
                level,
                null
        ));
    }

    public static void dispatchPlantGrowth(
            ServerLevel level,
            BlockPos pos,
            @Nullable BlockState from,
            @Nullable BlockState to
    ) {
        SkriptRuntime.instance().dispatch(new org.skriptlang.skript.lang.event.SkriptEvent(
                new FabricEventCompatHandles.PlantGrowth(level, pos.immutable(), from, to),
                level.getServer(),
                level,
                null
        ));
    }

    public static void dispatchPressurePlate(ServerLevel level, BlockPos pos, boolean tripwire) {
        SkriptRuntime.instance().dispatch(new org.skriptlang.skript.lang.event.SkriptEvent(
                new FabricEventCompatHandles.PressurePlate(level, pos.immutable(), tripwire),
                level.getServer(),
                level,
                null
        ));
    }

    public static void dispatchVehicleCollision(
            ServerLevel level,
            BlockPos pos,
            Entity vehicle,
            @Nullable BlockState blockState,
            @Nullable Entity entity
    ) {
        SkriptRuntime.instance().dispatch(new org.skriptlang.skript.lang.event.SkriptEvent(
                new FabricEventCompatHandles.VehicleCollision(level, pos.immutable(), vehicle, blockState, entity),
                level.getServer(),
                level,
                vehicle instanceof ServerPlayer serverPlayer ? serverPlayer : null
        ));
    }

    private static void dispatchCompatBlock(
            ServerLevel level,
            BlockPos pos,
            FabricEventCompatHandles.BlockAction action,
            @Nullable BlockState state,
            @Nullable ItemStack itemStack,
            boolean dropped,
            @Nullable ServerPlayer player
    ) {
        SkriptRuntime.instance().dispatch(new org.skriptlang.skript.lang.event.SkriptEvent(
                new FabricEventCompatHandles.Block(level, pos, action, state, itemStack == null ? null : itemStack.copy(), dropped),
                level.getServer(),
                level,
                player
        ));
    }

    private static void dispatchCompatItem(
            ServerLevel level,
            BlockPos pos,
            FabricEventCompatHandles.ItemAction action,
            ItemStack itemStack,
            boolean entityEvent,
            @Nullable ServerPlayer player
    ) {
        SkriptRuntime.instance().dispatch(new org.skriptlang.skript.lang.event.SkriptEvent(
                new FabricEventCompatHandles.Item(level, pos, action, itemStack.copy(), entityEvent),
                level.getServer(),
                level,
                player
        ));
    }

    private static void dispatchClick(
            ServerLevel level,
            BlockPos pos,
            FabricEventCompatHandles.ClickType clickType,
            @Nullable Entity entity,
            @Nullable BlockState blockState,
            ItemStack tool,
            @Nullable ServerPlayer player
    ) {
        SkriptRuntime.instance().dispatch(new org.skriptlang.skript.lang.event.SkriptEvent(
                new FabricEventCompatHandles.Click(
                        level,
                        pos,
                        clickType,
                        entity,
                        blockState,
                        tool.isEmpty() ? null : tool.copyWithCount(1)
                ),
                level.getServer(),
                level,
                player
        ));
    }

    public static void dispatchPotionEffect(
            ServerLevel level,
            LivingEntity entity,
            @Nullable MobEffectInstance currentEffect,
            @Nullable MobEffectInstance previousEffect,
            FabricPotionEffectAction action
    ) {
        dispatchPotionEffect(level, entity, currentEffect, previousEffect, action, FabricPotionEffectCause.UNKNOWN);
    }

    public static void dispatchPotionEffect(
            ServerLevel level,
            LivingEntity entity,
            @Nullable MobEffectInstance currentEffect,
            @Nullable MobEffectInstance previousEffect,
            FabricPotionEffectAction action,
            FabricPotionEffectCause cause
    ) {
        SkriptPotionEffect current = currentEffect == null ? null : SkriptPotionEffect.fromInstance(currentEffect, entity);
        SkriptPotionEffect previous = previousEffect == null ? null : SkriptPotionEffect.fromInstance(previousEffect);
        SkriptRuntime.instance().dispatch(new org.skriptlang.skript.lang.event.SkriptEvent(
                new FabricPotionEffectHandle(level, entity, current, previous, action, cause),
                level.getServer(),
                level,
                entity instanceof ServerPlayer serverPlayer ? serverPlayer : null
        ));
    }

    public static void dispatchLoveModeEnter(ServerLevel level, Animal entity, @Nullable ServerPlayer player) {
        SkriptRuntime.instance().dispatch(new org.skriptlang.skript.lang.event.SkriptEvent(
                new FabricLoveModeEnterHandle(level, entity, player),
                level.getServer(),
                level,
                player
        ));
    }

    public static void rememberLootTableKey(
            net.minecraft.world.level.storage.loot.LootTable lootTable,
            ResourceKey<net.minecraft.world.level.storage.loot.LootTable> key
    ) {
        LOOT_TABLE_KEYS.put(lootTable, key);
    }

    public static void dispatchLootGenerate(
            LootContext context,
            net.minecraft.world.level.storage.loot.LootTable lootTable,
            List<ItemStack> loot
    ) {
        ResourceKey<net.minecraft.world.level.storage.loot.LootTable> key = resolveLootTableKey(context, lootTable);
        if (key == null) {
            return;
        }

        ServerLevel level = context.getLevel();
        Entity contextEntity = context.getOptionalParameter(LootContextParams.THIS_ENTITY);
        ServerPlayer looter = resolveLootingPlayer(context, contextEntity);
        Entity lootedEntity = contextEntity instanceof ServerPlayer && contextEntity == looter ? null : contextEntity;
        Vec3 origin = context.getOptionalParameter(LootContextParams.ORIGIN);

        SkriptRuntime.instance().dispatch(new org.skriptlang.skript.lang.event.SkriptEvent(
                new FabricLootGenerateHandle(
                        new LootTable(key),
                        loot,
                        new FabricLocation(level, origin != null ? origin : Vec3.ZERO),
                        looter,
                        lootedEntity
                ),
                level.getServer(),
                level,
                looter
        ));
    }

    public static void dispatchPlayerInput(ServerPlayer player, Input currentInput, Input previousInput) {
        ServerLevel level = player.level();
        SkriptRuntime.instance().dispatch(new org.skriptlang.skript.lang.event.SkriptEvent(
                new FabricPlayerInputHandle(level, player, normalizeInput(previousInput), normalizeInput(currentInput)),
                level.getServer(),
                level,
                player
        ));
    }

    public static void dispatchPlayerMove(
            ServerPlayer player,
            Vec3 fromPosition,
            Vec3 toPosition,
            float fromYaw,
            float fromPitch,
            float toYaw,
            float toPitch
    ) {
        ServerLevel level = player.level();
        SkriptRuntime.instance().dispatch(new org.skriptlang.skript.lang.event.SkriptEvent(
                FabricPlayerEventHandles.move(
                        player,
                        new FabricLocation(level, fromPosition),
                        new FabricLocation(level, toPosition),
                        fromYaw,
                        fromPitch,
                        toYaw,
                        toPitch
                ),
                level.getServer(),
                level,
                player
        ));
    }

    public static void dispatchPlayerCommandSend(ServerPlayer player, Collection<String> commands) {
        ServerLevel level = player.level();
        SkriptRuntime.instance().dispatch(new org.skriptlang.skript.lang.event.SkriptEvent(
                FabricPlayerEventHandles.commandSend(commands),
                level.getServer(),
                level,
                player
        ));
    }

    public static void dispatchResourcePackResponse(ServerPlayer player, @Nullable String status) {
        ServerLevel level = player.level();
        SkriptRuntime.instance().dispatch(new org.skriptlang.skript.lang.event.SkriptEvent(
                new FabricEventCompatHandles.ResourcePackResponse(status),
                level.getServer(),
                level,
                player
        ));
    }

    public static void dispatchCommand(ServerPlayer player, String command) {
        ServerLevel level = player.level();
        SkriptRuntime.instance().dispatch(new org.skriptlang.skript.lang.event.SkriptEvent(
                FabricPlayerEventHandles.command(command),
                level.getServer(),
                level,
                player
        ));
    }

    public static void dispatchLevelChange(ServerPlayer player, int oldLevel, int newLevel) {
        ServerLevel level = player.level();
        SkriptRuntime.instance().dispatch(new org.skriptlang.skript.lang.event.SkriptEvent(
                FabricPlayerEventHandles.level(oldLevel, newLevel),
                level.getServer(),
                level,
                player
        ));
    }

    public static void dispatchTeleport(Entity entity, ServerLevel level, Vec3 fromPosition, Vec3 toPosition) {
        SkriptRuntime.instance().dispatch(new org.skriptlang.skript.lang.event.SkriptEvent(
                FabricPlayerEventHandles.teleport(
                        entity,
                        new FabricLocation(level, fromPosition),
                        new FabricLocation(level, toPosition)
                ),
                level.getServer(),
                level,
                entity instanceof ServerPlayer player ? player : null
        ));
    }

    public static void dispatchSpectate(
            ServerPlayer player,
            FabricPlayerEventHandles.SpectateAction action,
            @Nullable Entity currentTarget,
            @Nullable Entity newTarget
    ) {
        ServerLevel level = player.level();
        SkriptRuntime.instance().dispatch(new org.skriptlang.skript.lang.event.SkriptEvent(
                FabricPlayerEventHandles.spectate(action, currentTarget, newTarget),
                level.getServer(),
                level,
                player
        ));
    }

    public static void dispatchExperienceChange(ServerPlayer player, int amount) {
        ServerLevel level = player.level();
        SkriptRuntime.instance().dispatch(new org.skriptlang.skript.lang.event.SkriptEvent(
                FabricPlayerEventHandles.experienceChange(player, amount),
                level.getServer(),
                level,
                player
        ));
    }

    private static Input normalizeInput(Input input) {
        return input != null ? input : Input.EMPTY;
    }

    public static void dispatchBreeding(
            ServerLevel level,
            Animal mother,
            Animal father,
            AgeableMob offspring,
            @org.jetbrains.annotations.Nullable Player breeder
    ) {
        ItemStack bredWith = ItemStack.EMPTY;
        if (mother instanceof FabricBreedingItemSource source) {
            bredWith = source.skript$getLastLoveItem();
        }
        if (bredWith.isEmpty() && father instanceof FabricBreedingItemSource source) {
            bredWith = source.skript$getLastLoveItem();
        }
        if (bredWith.isEmpty() && breeder != null) {
            ItemStack mainHand = breeder.getMainHandItem();
            if (!mainHand.isEmpty()) {
                bredWith = mainHand.copyWithCount(1);
            } else {
                ItemStack offHand = breeder.getOffhandItem();
                if (!offHand.isEmpty()) {
                    bredWith = offHand.copyWithCount(1);
                }
            }
        }
        SkriptRuntime.instance().dispatch(new org.skriptlang.skript.lang.event.SkriptEvent(
                new FabricBreedingHandle(level, mother, father, offspring, breeder, bredWith),
                level.getServer(),
                level,
                breeder instanceof ServerPlayer serverPlayer ? serverPlayer : null
        ));
    }

    public static void dispatchFurnaceBurn(
            ServerLevel level,
            BlockPos pos,
            AbstractFurnaceBlockEntity furnace,
            ItemStack source,
            ItemStack fuel
    ) {
        dispatchFurnace(level, pos, furnace, FabricFurnaceEventHandle.Kind.BURN, source, fuel, furnace.getItem(2).copy(), 0, null);
    }

    public static void dispatchFurnaceSmeltingStart(
            ServerLevel level,
            BlockPos pos,
            AbstractFurnaceBlockEntity furnace,
            ItemStack source,
            ItemStack fuel
    ) {
        dispatchFurnace(level, pos, furnace, FabricFurnaceEventHandle.Kind.START_SMELT, source, fuel, furnace.getItem(2).copy(), 0, null);
    }

    public static void dispatchFurnaceSmelt(
            ServerLevel level,
            BlockPos pos,
            AbstractFurnaceBlockEntity furnace,
            ItemStack source,
            ItemStack fuel,
            ItemStack result
    ) {
        dispatchFurnace(level, pos, furnace, FabricFurnaceEventHandle.Kind.SMELT, source, fuel, result, 0, null);
    }

    public static void dispatchFurnaceExtract(
            ServerPlayer player,
            AbstractFurnaceBlockEntity furnace,
            ItemStack result,
            int itemAmount
    ) {
        ServerLevel level = player.level();
        dispatchFurnace(
                level,
                furnace.getBlockPos(),
                furnace,
                FabricFurnaceEventHandle.Kind.EXTRACT,
                furnace.getItem(0).copy(),
                furnace.getItem(1).copy(),
                result,
                itemAmount,
                player
        );
    }

    private static void dispatchFurnace(
            ServerLevel level,
            BlockPos pos,
            AbstractFurnaceBlockEntity furnace,
            FabricFurnaceEventHandle.Kind kind,
            ItemStack source,
            ItemStack fuel,
            ItemStack result,
            int itemAmount,
            @org.jetbrains.annotations.Nullable ServerPlayer player
    ) {
        SkriptRuntime.instance().dispatch(new org.skriptlang.skript.lang.event.SkriptEvent(
                new FabricFurnaceHandle(
                        kind,
                        level,
                        pos.immutable(),
                        furnace,
                        source.copy(),
                        fuel.copy(),
                        result.copy(),
                        itemAmount,
                        PrivateFurnaceAccess.litTimeRemaining(furnace),
                        PrivateFurnaceAccess.cookingTotalTime(furnace)
                ),
                level.getServer(),
                level,
                player
        ));
    }

    private static @Nullable ResourceKey<net.minecraft.world.level.storage.loot.LootTable> resolveLootTableKey(
            LootContext context,
            net.minecraft.world.level.storage.loot.LootTable lootTable
    ) {
        ResourceKey<net.minecraft.world.level.storage.loot.LootTable> remembered = LOOT_TABLE_KEYS.get(lootTable);
        if (remembered != null) {
            return remembered;
        }

        MinecraftServer server = context.getLevel().getServer();
        if (server == null) {
            return null;
        }

        return server.reloadableRegistries().lookup().lookup(Registries.LOOT_TABLE)
                .flatMap(registry -> registry.listElements()
                        .filter(holder -> holder.value() == lootTable)
                        .findFirst()
                        .map(Holder.Reference::key))
                .map(key -> {
                    LOOT_TABLE_KEYS.put(lootTable, key);
                    return key;
                })
                .orElse(null);
    }

    private static @Nullable ServerPlayer resolveLootingPlayer(LootContext context, @Nullable Entity contextEntity) {
        Player lastDamagePlayer = context.getOptionalParameter(LootContextParams.LAST_DAMAGE_PLAYER);
        if (lastDamagePlayer instanceof ServerPlayer serverPlayer) {
            return serverPlayer;
        }

        Entity attackingEntity = context.getOptionalParameter(LootContextParams.ATTACKING_ENTITY);
        if (attackingEntity instanceof ServerPlayer serverPlayer) {
            return serverPlayer;
        }

        if (contextEntity instanceof ServerPlayer serverPlayer) {
            return serverPlayer;
        }

        return null;
    }
}
