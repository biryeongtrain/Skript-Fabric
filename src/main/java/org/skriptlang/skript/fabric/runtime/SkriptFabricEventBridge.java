package org.skriptlang.skript.fabric.runtime;

import com.mojang.authlib.GameProfile;
import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Collection;
import java.util.IdentityHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLevelEvents;
import net.fabricmc.fabric.api.event.player.AttackEntityCallback;
import net.fabricmc.fabric.api.event.player.AttackBlockCallback;
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.fabricmc.fabric.api.event.player.UseEntityCallback;
import net.fabricmc.fabric.api.event.player.UseItemCallback;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerEntityEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerChunkEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.projectile.throwableitemprojectile.ThrownEgg;
import net.minecraft.world.level.Level;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.entity.player.Input;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Interaction;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.projectile.FishingHook;
import net.minecraft.world.entity.projectile.FireworkRocketEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.level.block.entity.BrewingStandBlockEntity;
import net.minecraft.world.level.block.entity.AbstractFurnaceBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.core.BlockPos;
import net.minecraft.world.Container;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.monster.piglin.Piglin;
import org.jetbrains.annotations.Nullable;
import ch.njol.skript.events.FabricEventCompatHandles;
import ch.njol.skript.events.FabricPlayerEventHandles;
import org.skriptlang.skript.bukkit.loottables.LootTable;
import org.skriptlang.skript.bukkit.potion.util.SkriptPotionEffect;
import org.skriptlang.skript.fabric.compat.FabricBlock;
import org.skriptlang.skript.fabric.compat.FabricBreedingItemSource;
import org.skriptlang.skript.fabric.compat.FabricLocation;
import org.skriptlang.skript.fabric.compat.FabricInteractionState;
import org.skriptlang.skript.fabric.compat.PrivateFurnaceAccess;

public final class SkriptFabricEventBridge {

    private static final Map<net.minecraft.world.level.storage.loot.LootTable, ResourceKey<net.minecraft.world.level.storage.loot.LootTable>> LOOT_TABLE_KEYS =
            Collections.synchronizedMap(new IdentityHashMap<>());
    private static final @Nullable Constructor<?> PLAYER_RESPAWN_EFFECT_HANDLE_CTOR = resolvePlayerRespawnEffectHandleCtor();
    private static final @Nullable Method PLAYER_RESPAWN_EFFECT_LOCATION = resolvePlayerRespawnEffectMethod("respawnLocation");
    private static final @Nullable Constructor<?> ENTITY_DEATH_EFFECT_HANDLE_CTOR = resolveEntityDeathEffectHandleCtor();
    private static final @Nullable Method ENTITY_DEATH_EFFECT_DROPS = resolveEntityDeathEffectMethod("drops");
    private static final @Nullable Method ENTITY_DEATH_EFFECT_DROPPED_EXP = resolveEntityDeathEffectMethod("droppedExp");
    private static final @Nullable Constructor<?> EXPLOSION_PRIME_EFFECT_HANDLE_CTOR = resolveExplosionPrimeEffectHandleCtor();
    private static final @Nullable Method EXPLOSION_PRIME_EFFECT_RADIUS = resolveExplosionPrimeEffectMethod("radius");
    private static final @Nullable Constructor<?> HANGING_BREAK_EFFECT_HANDLE_CTOR = resolveHangingBreakEffectHandleCtor();
    private static final @Nullable Constructor<?> HANGING_PLACE_EFFECT_HANDLE_CTOR = resolveHangingPlaceEffectHandleCtor();
    private static final @Nullable Constructor<?> ELYTRA_BOOST_EFFECT_HANDLE_CTOR = resolveElytraBoostEffectHandleCtor();
    private static final @Nullable Method ELYTRA_BOOST_SHOULD_CONSUME = resolveElytraBoostEffectMethod("shouldConsume");
    private static final ThreadLocal<@Nullable DeathCapture> ACTIVE_DEATH_CAPTURE = new ThreadLocal<>();
    private static final ThreadLocal<Float> MODIFIED_DAMAGE = new ThreadLocal<>();
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
            ServerLevelEvents.LOAD.register(SkriptFabricEventBridge::dispatchWorldLoad);
            ServerLevelEvents.UNLOAD.register(SkriptFabricEventBridge::dispatchWorldUnload);
            ServerChunkEvents.CHUNK_LOAD.register((level, chunk, generated) -> dispatchChunkLoad(level, chunk));
            ServerChunkEvents.CHUNK_UNLOAD.register(SkriptFabricEventBridge::dispatchChunkUnload);
            ServerPlayConnectionEvents.DISCONNECT.register((handler, server) -> dispatchQuit(handler.getPlayer()));
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
        SkriptRuntime.instance().dispatch(new org.skriptlang.skript.lang.event.SkriptEvent(
                new FabricScheduledTickHandle(null, server.overworld().getGameTime(), server.overworld().getGameTime()),
                server,
                null,
                null
        ));
        for (ServerLevel level : server.getAllLevels()) {
            SkriptRuntime.instance().dispatch(new org.skriptlang.skript.lang.event.SkriptEvent(
                    new FabricScheduledTickHandle(level, level.getGameTime(), level.getGameTime()),
                    server,
                    level,
                    null
            ));
        }
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
        boolean cancelled = dispatchClick(serverLevel, pos.immutable(), FabricEventCompatHandles.ClickType.LEFT, null, level.getBlockState(pos), player.getItemInHand(hand), serverPlayer);
        return cancelled ? InteractionResult.FAIL : InteractionResult.PASS;
    }

    private static InteractionResult dispatchUseBlock(net.minecraft.world.entity.player.Player player, Level level, InteractionHand hand, BlockHitResult hitResult) {
        if (!(level instanceof ServerLevel serverLevel) || !(player instanceof ServerPlayer serverPlayer)) {
            return InteractionResult.PASS;
        }
        boolean cancelled = dispatchClick(
                serverLevel,
                hitResult.getBlockPos().immutable(),
                FabricEventCompatHandles.ClickType.RIGHT,
                null,
                serverLevel.getBlockState(hitResult.getBlockPos()),
                player.getItemInHand(hand),
                serverPlayer
        );
        org.skriptlang.skript.lang.event.SkriptEvent event = new org.skriptlang.skript.lang.event.SkriptEvent(
                new FabricUseBlockHandle(serverLevel, serverPlayer, hand, hitResult),
                serverLevel.getServer(),
                serverLevel,
                serverPlayer
        );
        SkriptRuntime.instance().dispatch(event);
        return (cancelled || event.isCancelled()) ? InteractionResult.FAIL : InteractionResult.PASS;
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
        boolean cancelled = dispatchClick(serverLevel, entity.blockPosition().immutable(), FabricEventCompatHandles.ClickType.LEFT, entity, null, player.getItemInHand(hand), serverPlayer);
        org.skriptlang.skript.lang.event.SkriptEvent event = new org.skriptlang.skript.lang.event.SkriptEvent(
                new FabricAttackEntityHandle(serverLevel, serverPlayer, hand, entity, hitResult),
                serverLevel.getServer(),
                serverLevel,
                serverPlayer
        );
        SkriptRuntime.instance().dispatch(event);
        return (cancelled || event.isCancelled()) ? InteractionResult.FAIL : InteractionResult.PASS;
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
        boolean cancelled = dispatchClick(serverLevel, entity.blockPosition().immutable(), FabricEventCompatHandles.ClickType.RIGHT, entity, null, player.getItemInHand(hand), serverPlayer);
        org.skriptlang.skript.lang.event.SkriptEvent event = new org.skriptlang.skript.lang.event.SkriptEvent(
                new FabricUseEntityHandle(serverLevel, serverPlayer, hand, entity, hitResult),
                serverLevel.getServer(),
                serverLevel,
                serverPlayer
        );
        SkriptRuntime.instance().dispatch(event);
        return (cancelled || event.isCancelled()) ? InteractionResult.FAIL : InteractionResult.PASS;
    }

    private static InteractionResult dispatchUseItem(
            net.minecraft.world.entity.player.Player player,
            Level level,
            InteractionHand hand
    ) {
        if (!(level instanceof ServerLevel serverLevel) || !(player instanceof ServerPlayer serverPlayer)) {
            return InteractionResult.PASS;
        }
        boolean cancelled = dispatchClick(serverLevel, serverPlayer.blockPosition().immutable(), FabricEventCompatHandles.ClickType.RIGHT, null, null, player.getItemInHand(hand), serverPlayer);
        org.skriptlang.skript.lang.event.SkriptEvent event = new org.skriptlang.skript.lang.event.SkriptEvent(
                new FabricUseItemHandle(serverLevel, serverPlayer, hand),
                serverLevel.getServer(),
                serverLevel,
                serverPlayer
        );
        SkriptRuntime.instance().dispatch(event);
        return (cancelled || event.isCancelled()) ? InteractionResult.FAIL : InteractionResult.PASS;
    }

    private static void dispatchEntityLoad(Entity entity, ServerLevel level) {
        if (!(entity instanceof net.minecraft.world.entity.player.Player)) {
            net.minecraft.world.entity.EntitySpawnReason captured = SpawnReasonCapture.consume();
            ch.njol.skript.events.SpawnReason reason = captured != null
                    ? ch.njol.skript.events.SpawnReason.fromMinecraft(captured) : null;
            SkriptRuntime.instance().dispatch(new org.skriptlang.skript.lang.event.SkriptEvent(
                    new FabricEventCompatHandles.EntityLifecycle(entity, true, reason),
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
        FabricDamageHandle handle = new FabricDamageHandle(serverLevel, entity, source, amount);
        org.skriptlang.skript.lang.event.SkriptEvent event = new org.skriptlang.skript.lang.event.SkriptEvent(
                handle,
                serverLevel.getServer(),
                serverLevel,
                entity instanceof ServerPlayer serverPlayer ? serverPlayer : null
        );
        SkriptRuntime.instance().dispatch(event);
        if (event.isCancelled()) return false;
        if (handle.amount() != amount) {
            MODIFIED_DAMAGE.set(handle.amount());
        }
        return true;
    }

    public static @Nullable Float consumeModifiedDamage() {
        Float val = MODIFIED_DAMAGE.get();
        MODIFIED_DAMAGE.remove();
        return val;
    }

    private static void dispatchDeath(LivingEntity entity, DamageSource source) {
        if (!(entity.level() instanceof ServerLevel serverLevel)) {
            return;
        }
        SkriptRuntime.instance().dispatch(new org.skriptlang.skript.lang.event.SkriptEvent(
                new FabricEventCompatHandles.EntityLifecycle(entity, false, null),
                serverLevel.getServer(),
                serverLevel,
                entity instanceof ServerPlayer serverPlayer ? serverPlayer : null
        ));
    }

    public static void beginDeathCapture(LivingEntity entity, ServerLevel level, DamageSource source) {
        ACTIVE_DEATH_CAPTURE.set(new DeathCapture(entity, level, source));
    }

    public static boolean captureDeathDrop(Entity entity, ItemStack stack) {
        DeathCapture capture = ACTIVE_DEATH_CAPTURE.get();
        if (capture == null || capture.entity() != entity || stack.isEmpty()) {
            return false;
        }
        capture.drops().add(stack.copy());
        return true;
    }

    public static boolean captureDeathExperience(LivingEntity entity, int amount) {
        DeathCapture capture = ACTIVE_DEATH_CAPTURE.get();
        if (capture == null || capture.entity() != entity) {
            return false;
        }
        capture.addDroppedExp(amount);
        return true;
    }

    public static void finishDeathCapture(LivingEntity entity) {
        DeathCapture capture = ACTIVE_DEATH_CAPTURE.get();
        ACTIVE_DEATH_CAPTURE.remove();
        if (capture == null || capture.entity() != entity) {
            return;
        }

        Object handle = createEntityDeathHandle(entity, capture.drops(), capture.droppedExp());
        SkriptRuntime.instance().dispatch(new org.skriptlang.skript.lang.event.SkriptEvent(
                handle,
                capture.level().getServer(),
                capture.level(),
                entity instanceof ServerPlayer serverPlayer ? serverPlayer : null
        ));

        for (ItemStack drop : entityDeathDrops(handle)) {
            if (!drop.isEmpty()) {
                entity.spawnAtLocation(capture.level(), drop.copy());
            }
        }

        int droppedExp = entityDeathDroppedExp(handle);
        if (droppedExp > 0) {
            ExperienceOrb.award(capture.level(), entity.position(), droppedExp);
        }
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

    public static void dispatchAreaCloudEffect(ServerLevel level, List<LivingEntity> affectedEntities) {
        SkriptRuntime.instance().dispatch(new org.skriptlang.skript.lang.event.SkriptEvent(
                new FabricEventCompatHandles.AreaEffectCloudApply(List.copyOf(affectedEntities)),
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

    public static void dispatchPrepareCraft(ServerLevel level, BlockPos pos, ServerPlayer player, ItemStack result) {
        dispatchCompatItem(
                level,
                pos.immutable(),
                FabricEventCompatHandles.ItemAction.PREPARE_CRAFT,
                result,
                false,
                player
        );
    }

    public static void dispatchCraft(ServerPlayer player, ItemStack result) {
        dispatchCompatItem(
                player.level(),
                player.blockPosition().immutable(),
                FabricEventCompatHandles.ItemAction.CRAFT,
                result,
                false,
                player
        );
    }

    public static void dispatchStonecutting(ServerLevel level, BlockPos pos, ServerPlayer player, ItemStack result) {
        dispatchCompatItem(
                level,
                pos.immutable(),
                FabricEventCompatHandles.ItemAction.STONECUTTING,
                result,
                false,
                player
        );
    }

    public static void dispatchInventoryMove(Container source, Container destination, ItemStack movedStack) {
        ServerLevel level = inventoryMoveLevel(destination);
        BlockPos pos = inventoryMovePos(destination);
        if (level == null || pos == null) {
            level = inventoryMoveLevel(source);
            pos = inventoryMovePos(source);
        }
        if (level == null || pos == null || movedStack.isEmpty()) {
            return;
        }
        dispatchInventoryMove(level, pos, movedStack);
    }

    public static void dispatchInventoryMove(ServerLevel level, BlockPos pos, ItemStack movedStack) {
        if (movedStack.isEmpty()) {
            return;
        }
        dispatchCompatItem(
                level,
                pos.immutable(),
                FabricEventCompatHandles.ItemAction.INVENTORY_MOVE,
                movedStack,
                false,
                null
        );
    }

    public static boolean dispatchInventoryMoveEvent(Container source, Container destination, Container initiator) {
        ServerLevel level = inventoryMoveLevel(destination);
        if (level == null) {
            level = inventoryMoveLevel(source);
        }
        if (level == null) {
            return false;
        }
        org.skriptlang.skript.lang.event.SkriptEvent event = new org.skriptlang.skript.lang.event.SkriptEvent(
                new FabricInventoryMoveHandle(source, destination, initiator),
                level.getServer(),
                level,
                null
        );
        SkriptRuntime.instance().dispatch(event);
        return event.isCancelled();
    }

    public static boolean dispatchReadyArrow(ServerPlayer player, ItemStack bow, ItemStack arrow) {
        if (!(player.level() instanceof ServerLevel level)) {
            return false;
        }
        org.skriptlang.skript.lang.event.SkriptEvent event = new org.skriptlang.skript.lang.event.SkriptEvent(
                new FabricReadyArrowHandle(bow, arrow),
                level.getServer(),
                level,
                player
        );
        SkriptRuntime.instance().dispatch(event);
        return event.isCancelled();
    }

    public static void dispatchEnchantPrepare(
            ServerLevel level,
            ServerPlayer player,
            ItemStack item,
            int enchantmentBonus,
            @Nullable java.util.List<net.minecraft.world.item.enchantment.EnchantmentInstance> offers
    ) {
        SkriptRuntime.instance().dispatch(new org.skriptlang.skript.lang.event.SkriptEvent(
                new FabricEventCompatHandles.EnchantPrepare(item.copy(), enchantmentBonus, offers),
                level.getServer(),
                level,
                player
        ));
    }

    public static void dispatchEnchantApply(
            ServerLevel level,
            ServerPlayer player,
            ItemStack item,
            java.util.List<net.minecraft.world.item.enchantment.EnchantmentInstance> enchantments,
            int cost
    ) {
        SkriptRuntime.instance().dispatch(new org.skriptlang.skript.lang.event.SkriptEvent(
                new FabricEventCompatHandles.EnchantApply(item.copy(), enchantments, cost),
                level.getServer(),
                level,
                player
        ));
    }

    public static FabricEventCompatHandles.Mending dispatchMending(
            ServerLevel level,
            LivingEntity entity,
            ItemStack item,
            int repairAmount,
            @Nullable ExperienceOrb experienceOrb
    ) {
        FabricEventCompatHandles.Mending handle = new FabricEventCompatHandles.Mending(entity, item.copy(), repairAmount, experienceOrb);
        org.skriptlang.skript.lang.event.SkriptEvent event = new org.skriptlang.skript.lang.event.SkriptEvent(
                handle,
                level.getServer(),
                level,
                entity instanceof ServerPlayer serverPlayer ? serverPlayer : null
        );
        SkriptRuntime.instance().dispatch(event);
        if (event.isCancelled()) {
            handle.setRepairAmount(0);
        }
        return handle;
    }

    public static FabricChatHandle dispatchChat(
            ServerPlayer player,
            Component message,
            Set<ServerPlayer> recipients
    ) {
        ServerLevel level = (ServerLevel) player.level();
        FabricChatHandle handle = new FabricChatHandle(player, message, "<%1$s> %2$s", recipients);
        org.skriptlang.skript.lang.event.SkriptEvent event = new org.skriptlang.skript.lang.event.SkriptEvent(
                handle,
                level.getServer(),
                level,
                player
        );
        SkriptRuntime.instance().dispatch(event);
        handle.setCancelled(event.isCancelled());
        return handle;
    }

    public static void dispatchInventoryClick(ServerPlayer player, ItemStack clickedStack) {
        if (clickedStack.isEmpty()) {
            return;
        }
        dispatchCompatItem(
                player.level(),
                player.blockPosition().immutable(),
                FabricEventCompatHandles.ItemAction.INVENTORY_CLICK,
                clickedStack,
                false,
                player
        );
    }

    public static void dispatchEntityShootBow(ServerLevel level, LivingEntity entity, @Nullable ItemStack consumable, float force) {
        SkriptRuntime.instance().dispatch(new org.skriptlang.skript.lang.event.SkriptEvent(
                new FabricEventCompatHandles.EntityShootBow(entity, consumable == null ? null : consumable.copy(), force),
                level.getServer(),
                level,
                entity instanceof ServerPlayer serverPlayer ? serverPlayer : null
        ));
    }

    public static void dispatchPiglinBarter(ServerLevel level, Piglin piglin, @Nullable ItemStack input, List<ItemStack> outcome) {
        SkriptRuntime.instance().dispatch(new org.skriptlang.skript.lang.event.SkriptEvent(
                new FabricEventCompatHandles.PiglinBarter(input, outcome),
                level.getServer(),
                level,
                null
        ));
    }

    public static boolean dispatchPlayerEggThrow(ThrownEgg egg, HitResult hitResult) {
        if (!(egg.level() instanceof ServerLevel level)) {
            return false;
        }
        if (!(egg.getOwner() instanceof ServerPlayer player)) {
            return false;
        }

        MutableEggThrowHandle handle = MutableEggThrowHandle.create(egg);
        SkriptRuntime.instance().dispatch(new org.skriptlang.skript.lang.event.SkriptEvent(
                handle,
                level.getServer(),
                level,
                player
        ));

        if (handle.hatching()) {
            spawnEggHatches(level, egg, handle.hatches(), handle.hatchingType());
        }

        level.broadcastEntityEvent(egg, (byte) 3);
        egg.discard();
        return true;
    }

    public static void dispatchEntityUnleash(ServerLevel level, Entity entity, @Nullable Entity actor, boolean dropLeash) {
        SkriptRuntime.instance().dispatch(new org.skriptlang.skript.lang.event.SkriptEvent(
                new FabricEntityUnleashHandle(entity, actor, dropLeash),
                level.getServer(),
                level,
                actor instanceof ServerPlayer serverPlayer ? serverPlayer : null
        ));
    }

    public static void dispatchEntityLeash(ServerLevel level, Entity entity, @Nullable Entity actor) {
        ServerPlayer serverPlayer = actor instanceof ServerPlayer player ? player : null;
        SkriptRuntime.instance().dispatch(new org.skriptlang.skript.lang.event.SkriptEvent(
                new FabricEventCompatHandles.Leash(
                        entity,
                        serverPlayer != null
                                ? FabricEventCompatHandles.LeashAction.PLAYER_LEASH
                                : FabricEventCompatHandles.LeashAction.LEASH
                ),
                level.getServer(),
                level,
                serverPlayer
        ));
    }

    public static void dispatchHangingBreak(ServerLevel level, Entity entity, @Nullable Entity remover) {
        ServerPlayer player = remover instanceof ServerPlayer serverPlayer ? serverPlayer : null;
        SkriptRuntime.instance().dispatch(new org.skriptlang.skript.lang.event.SkriptEvent(
                createHangingBreakHandle(entity, remover),
                level.getServer(),
                level,
                player
        ));
    }

    public static void dispatchHangingPlace(ServerLevel level, Entity entity, @Nullable Entity placer) {
        ServerPlayer player = placer instanceof ServerPlayer serverPlayer ? serverPlayer : null;
        SkriptRuntime.instance().dispatch(new org.skriptlang.skript.lang.event.SkriptEvent(
                createHangingPlaceHandle(entity),
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
        FabricEventCompatHandles.ExperienceSpawn handle = new FabricEventCompatHandles.ExperienceSpawn(orb.getValue());
        org.skriptlang.skript.lang.event.SkriptEvent event = new org.skriptlang.skript.lang.event.SkriptEvent(
                handle,
                level.getServer(),
                level,
                null
        );
        SkriptRuntime.instance().dispatch(event);
        if (handle.amount() != orb.getValue()) {
            ((kim.biryeong.skriptFabric.mixin.ExperienceOrbAccessor) orb).skript$setValue(handle.amount());
        }
    }

    public static void dispatchFirework(ServerLevel level, FireworkRocketEntity firework) {
        SkriptRuntime.instance().dispatch(new org.skriptlang.skript.lang.event.SkriptEvent(
                new FabricEventCompatHandles.Firework(fireworkColors(firework)),
                level.getServer(),
                level,
                null
        ));
    }

    public static boolean dispatchElytraBoost(ServerLevel level, ServerPlayer player, FireworkRocketEntity firework) {
        Object handle = createElytraBoostHandle(true);
        SkriptRuntime.instance().dispatch(new org.skriptlang.skript.lang.event.SkriptEvent(
                handle,
                level.getServer(),
                level,
                player
        ));
        return elytraBoostShouldConsume(handle);
    }

    public static FabricEventCompatHandles.Explosion dispatchExplosion(ServerLevel level, Entity source, List<BlockPos> explodedPositions) {
        List<FabricBlock> explodedBlocks = new ArrayList<>(explodedPositions.size());
        for (BlockPos explodedPosition : explodedPositions) {
            explodedBlocks.add(new FabricBlock(level, explodedPosition.immutable()));
        }
        FabricEventCompatHandles.Explosion handle = new FabricEventCompatHandles.Explosion(explodedBlocks);
        SkriptRuntime.instance().dispatch(new org.skriptlang.skript.lang.event.SkriptEvent(
                handle,
                level.getServer(),
                level,
                source instanceof ServerPlayer serverPlayer ? serverPlayer : null
        ));
        return handle;
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

    public static void dispatchBlockBurn(ServerLevel level, BlockPos pos, @Nullable BlockState state) {
        dispatchCompatBlock(level, pos, FabricEventCompatHandles.BlockAction.BURN, state, null, false, null);
    }

    public static void dispatchBlockFade(ServerLevel level, BlockPos pos, @Nullable BlockState state) {
        dispatchCompatBlock(level, pos, FabricEventCompatHandles.BlockAction.FADE, state, null, false, null);
    }

    public static void dispatchBlockForm(ServerLevel level, BlockPos pos, @Nullable BlockState state) {
        dispatchCompatBlock(level, pos, FabricEventCompatHandles.BlockAction.FORM, state, null, false, null);
    }

    public static void dispatchBlockDrop(
            ServerLevel level,
            BlockPos pos,
            @Nullable BlockState state,
            @Nullable ServerPlayer player
    ) {
        ItemStack itemStack = null;
        if (state != null) {
            net.minecraft.world.item.Item item = state.getBlock().asItem();
            if (item != null) {
                itemStack = new ItemStack(item);
            }
        }
        dispatchCompatBlock(level, pos, FabricEventCompatHandles.BlockAction.DROP, state, itemStack, true, player);
    }

    public static void dispatchHarvestBlock(ServerLevel level, BlockState state, @Nullable ServerPlayer player) {
        SkriptRuntime.instance().dispatch(new org.skriptlang.skript.lang.event.SkriptEvent(
                new FabricEventCompatHandles.HarvestBlock(state),
                level.getServer(),
                level,
                player
        ));
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

    public static void dispatchBlockFertilize(ServerLevel level, java.util.List<FabricBlock> blocks) {
        SkriptRuntime.instance().dispatch(new org.skriptlang.skript.lang.event.SkriptEvent(
                new FabricEventCompatHandles.BlockFertilize(List.copyOf(blocks)),
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

    public static void dispatchPlayerItemDrop(ServerPlayer player, ItemEntity itemEntity) {
        if (!(itemEntity.level() instanceof ServerLevel level)) {
            return;
        }
        dispatchCompatItem(
                level,
                itemEntity.blockPosition().immutable(),
                FabricEventCompatHandles.ItemAction.DROP,
                itemEntity.getItem(),
                false,
                player
        );
    }

    public static void dispatchPlayerItemPickup(ServerPlayer player, ItemEntity itemEntity, ItemStack itemStack) {
        if (!(itemEntity.level() instanceof ServerLevel level)) {
            return;
        }
        dispatchCompatItem(
                level,
                itemEntity.blockPosition().immutable(),
                FabricEventCompatHandles.ItemAction.PICKUP,
                itemStack,
                false,
                player
        );
    }

    public static void dispatchItemDespawn(ServerLevel level, BlockPos pos, ItemStack itemStack) {
        dispatchCompatItem(
                level,
                pos.immutable(),
                FabricEventCompatHandles.ItemAction.DESPAWN,
                itemStack,
                false,
                null
        );
    }

    public static void dispatchItemMerge(ServerLevel level, BlockPos pos, ItemStack itemStack) {
        dispatchCompatItem(
                level,
                pos.immutable(),
                FabricEventCompatHandles.ItemAction.MERGE,
                itemStack,
                false,
                null
        );
    }

    public static void dispatchItemDispense(ServerLevel level, BlockPos pos, ItemStack itemStack) {
        dispatchCompatItem(
                level,
                pos.immutable(),
                FabricEventCompatHandles.ItemAction.DISPENSE,
                itemStack,
                false,
                null
        );
    }

    public static boolean dispatchPlayerItemConsume(ServerPlayer player, ItemStack itemStack) {
        return dispatchCompatItem(
                (ServerLevel) player.level(),
                player.blockPosition().immutable(),
                FabricEventCompatHandles.ItemAction.CONSUME,
                itemStack,
                false,
                player
        );
    }

    public static void dispatchWorldSave(ServerLevel level) {
        SkriptRuntime.instance().dispatch(new org.skriptlang.skript.lang.event.SkriptEvent(
                new FabricEventCompatHandles.World(level, FabricEventCompatHandles.WorldAction.SAVE),
                level.getServer(),
                level,
                null
        ));
    }

    public static void dispatchWorldInit(MinecraftServer server, ServerLevel level) {
        SkriptRuntime.instance().dispatch(new org.skriptlang.skript.lang.event.SkriptEvent(
                new FabricEventCompatHandles.World(level, FabricEventCompatHandles.WorldAction.INIT),
                server,
                level,
                null
        ));
    }

    public static void dispatchWorldLoad(MinecraftServer server, ServerLevel level) {
        SkriptRuntime.instance().dispatch(new org.skriptlang.skript.lang.event.SkriptEvent(
                new FabricEventCompatHandles.World(level, FabricEventCompatHandles.WorldAction.LOAD),
                server,
                level,
                null
        ));
    }

    public static void dispatchWorldUnload(MinecraftServer server, ServerLevel level) {
        SkriptRuntime.instance().dispatch(new org.skriptlang.skript.lang.event.SkriptEvent(
                new FabricEventCompatHandles.World(level, FabricEventCompatHandles.WorldAction.UNLOAD),
                server,
                level,
                null
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

    private static boolean dispatchCompatItem(
            ServerLevel level,
            BlockPos pos,
            FabricEventCompatHandles.ItemAction action,
            ItemStack itemStack,
            boolean entityEvent,
            @Nullable ServerPlayer player
    ) {
        org.skriptlang.skript.lang.event.SkriptEvent event = new org.skriptlang.skript.lang.event.SkriptEvent(
                new FabricEventCompatHandles.Item(level, pos, action, itemStack.copy(), entityEvent),
                level.getServer(),
                level,
                player
        );
        SkriptRuntime.instance().dispatch(event);
        return event.isCancelled();
    }

    private static @Nullable ServerLevel inventoryMoveLevel(Container container) {
        if (container instanceof BlockEntity blockEntity && blockEntity.getLevel() instanceof ServerLevel level) {
            return level;
        }
        return null;
    }

    private static @Nullable BlockPos inventoryMovePos(Container container) {
        if (container instanceof BlockEntity blockEntity) {
            return blockEntity.getBlockPos();
        }
        return null;
    }

    private static boolean dispatchClick(
            ServerLevel level,
            BlockPos pos,
            FabricEventCompatHandles.ClickType clickType,
            @Nullable Entity entity,
            @Nullable BlockState blockState,
            ItemStack tool,
            @Nullable ServerPlayer player
    ) {
        org.skriptlang.skript.lang.event.SkriptEvent event = new org.skriptlang.skript.lang.event.SkriptEvent(
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
        );
        SkriptRuntime.instance().dispatch(event);
        return event.isCancelled();
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
        dispatchMoveOn(player, level, fromPosition, toPosition);
    }

    static void dispatchMoveOn(ServerPlayer player, ServerLevel level, Vec3 fromPosition, Vec3 toPosition) {
        BlockPos fromBlock = moveOnBlockPos(fromPosition);
        BlockPos toBlock = moveOnBlockPos(toPosition);
        if (fromBlock.equals(toBlock)) {
            return;
        }
        BlockState blockState = level.getBlockState(toBlock);
        if (blockState.isAir()) {
            return;
        }
        SkriptRuntime.instance().dispatch(new org.skriptlang.skript.lang.event.SkriptEvent(
                new FabricEventCompatHandles.MoveOn(blockState),
                level.getServer(),
                level,
                player
        ));
    }

    static BlockPos moveOnBlockPos(Vec3 position) {
        return BlockPos.containing(position.x, Math.ceil(position.y) - 1.0D, position.z);
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

    public static void dispatchResourcePackResponse(ServerPlayer player, @Nullable FabricEventCompatHandles.ResourcePackState status) {
        ServerLevel level = player.level();
        SkriptRuntime.instance().dispatch(new org.skriptlang.skript.lang.event.SkriptEvent(
                new FabricEventCompatHandles.ResourcePackResponse(status),
                level.getServer(),
                level,
                player
        ));
    }

    public static void dispatchFirstJoin(ServerPlayer player, boolean firstJoin) {
        ServerLevel level = player.level();
        SkriptRuntime.instance().dispatch(new org.skriptlang.skript.lang.event.SkriptEvent(
                FabricPlayerEventHandles.firstJoin(firstJoin),
                level.getServer(),
                level,
                player
        ));
    }

    public static void dispatchJoin(ServerPlayer player) {
        ServerLevel level = player.level();
        SkriptRuntime.instance().dispatch(new org.skriptlang.skript.lang.event.SkriptEvent(
                FabricPlayerEventHandles.join(),
                level.getServer(),
                level,
                player
        ));
    }

    public static void dispatchConnect(ServerPlayer player) {
        ServerLevel level = player.level();
        SkriptRuntime.instance().dispatch(new org.skriptlang.skript.lang.event.SkriptEvent(
                FabricPlayerEventHandles.connect(),
                level.getServer(),
                level,
                player
        ));
    }

    public static void dispatchKick(ServerPlayer player, @Nullable net.minecraft.network.chat.Component reason) {
        ServerLevel level = player.level();
        SkriptRuntime.instance().dispatch(new org.skriptlang.skript.lang.event.SkriptEvent(
                FabricPlayerEventHandles.kick(reason),
                level.getServer(),
                level,
                player
        ));
    }

    public static void dispatchQuit(ServerPlayer player) {
        ServerLevel level = player.level();
        SkriptRuntime.instance().dispatch(new org.skriptlang.skript.lang.event.SkriptEvent(
                FabricPlayerEventHandles.quit(),
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

    public static void dispatchGameMode(ServerPlayer player, GameType mode) {
        ServerLevel level = player.level();
        SkriptRuntime.instance().dispatch(new org.skriptlang.skript.lang.event.SkriptEvent(
                new FabricEventCompatHandles.GameMode(mode),
                level.getServer(),
                level,
                player
        ));
    }

    public static void dispatchPortal(Entity entity) {
        if (!(entity.level() instanceof ServerLevel level)) {
            return;
        }
        SkriptRuntime.instance().dispatch(new org.skriptlang.skript.lang.event.SkriptEvent(
                new FabricEventCompatHandles.Portal(entity, entity instanceof ServerPlayer),
                level.getServer(),
                level,
                entity instanceof ServerPlayer player ? player : null
        ));
    }

    public static void dispatchRespawn(ServerPlayer previousPlayer, ServerPlayer player, boolean alive, Entity.RemovalReason removalReason) {
        ServerLevel level = player.level();
        FabricLocation defaultLocation = new FabricLocation(level, player.position());
        boolean anchorSpawn = false;
        boolean bedSpawn = false;

        Object respawn = invokeNoArg(previousPlayer, "getRespawn");
        if (respawn != null) {
            Object respawnData = invokeNoArg(respawn, "respawnData");
            if (respawnData == null) {
                respawnData = respawn;
            }
            Object rawDimension = invokeNoArg(respawnData, "dimension");
            Object rawPos = invokeNoArg(respawnData, "pos");
            if (rawDimension instanceof ResourceKey<?> dimensionKey && rawPos instanceof BlockPos blockPos) {
                @SuppressWarnings("unchecked")
                ServerLevel respawnLevel = player.level().getServer().getLevel((ResourceKey<Level>) dimensionKey);
                if (respawnLevel != null) {
                    BlockState blockState = respawnLevel.getBlockState(blockPos);
                    anchorSpawn = blockState.is(Blocks.RESPAWN_ANCHOR);
                    bedSpawn = blockState.is(BlockTags.BEDS);
                }
            }
        }

        String reason = alive ? "end_portal" : removalReason == Entity.RemovalReason.KILLED ? "death" : null;
        Object handle = createPlayerRespawnHandle(defaultLocation, bedSpawn, anchorSpawn, reason);
        SkriptRuntime.instance().dispatch(new org.skriptlang.skript.lang.event.SkriptEvent(
                handle,
                level.getServer(),
                level,
                player
        ));

        if (handle instanceof FabricEventCompatHandles.PlayerRespawn compatHandle && compatHandle.respawnLocation() != null) {
            applyRespawnLocation(player, compatHandle.respawnLocation());
            return;
        }

        FabricLocation updatedLocation = playerRespawnLocation(handle);
        if (updatedLocation != null) {
            applyRespawnLocation(player, updatedLocation);
        }
    }

    public static void dispatchTeleport(Entity entity, ServerLevel level, Vec3 fromPosition, Vec3 toPosition, @Nullable ch.njol.skript.events.TeleportCause cause) {
        SkriptRuntime.instance().dispatch(new org.skriptlang.skript.lang.event.SkriptEvent(
                FabricPlayerEventHandles.teleport(
                        entity,
                        new FabricLocation(level, fromPosition),
                        new FabricLocation(level, toPosition),
                        cause
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

    public static void dispatchExperienceCooldownChange(ServerPlayer player, @Nullable String reason) {
        ServerLevel level = player.level();
        SkriptRuntime.instance().dispatch(new org.skriptlang.skript.lang.event.SkriptEvent(
                new FabricEventCompatHandles.ExperienceCooldownChange(reason),
                level.getServer(),
                level,
                player
        ));
    }

    public static boolean isFirstJoin(Path playerDataDirectory, GameProfile profile) {
        return profile.id() != null && !Files.exists(playerDataDirectory.resolve(profile.id() + ".dat"));
    }

    public static void dispatchArmorChange(ServerPlayer player, FabricEventCompatHandles.ArmorSlot slot) {
        ServerLevel level = player.level();
        SkriptRuntime.instance().dispatch(new org.skriptlang.skript.lang.event.SkriptEvent(
                new FabricEventCompatHandles.PlayerArmorChange(slot),
                level.getServer(),
                level,
                player
        ));
    }

    public static void dispatchWeatherChange(ServerLevel level, boolean rain, boolean thunder) {
        SkriptRuntime.instance().dispatch(new org.skriptlang.skript.lang.event.SkriptEvent(
                new FabricEventCompatHandles.WeatherChange(rain, thunder),
                level.getServer(),
                level,
                null
        ));
    }

    public static void dispatchEntityTarget(ServerLevel level, Mob entity, @Nullable LivingEntity target) {
        SkriptRuntime.instance().dispatch(new org.skriptlang.skript.lang.event.SkriptEvent(
                new FabricEventCompatHandles.EntityTarget(entity, target),
                level.getServer(),
                level,
                null
        ));
    }

    private static Input normalizeInput(Input input) {
        return input != null ? input : Input.EMPTY;
    }

    private static @Nullable java.util.Set<Integer> fireworkColors(FireworkRocketEntity firework) {
        Object fireworks = firework.getItem().get(DataComponents.FIREWORKS);
        if (fireworks == null) {
            return null;
        }
        Object explosions = invokeNoArg(fireworks, "explosions");
        if (!(explosions instanceof Iterable<?> iterable)) {
            return null;
        }
        java.util.Set<Integer> colors = new LinkedHashSet<>();
        for (Object explosion : iterable) {
            appendColorValues(colors, invokeNoArg(explosion, "colors"));
        }
        return colors.isEmpty() ? null : colors;
    }

    private static void appendColorValues(java.util.Set<Integer> colors, @Nullable Object source) {
        if (source == null) {
            return;
        }
        if (source instanceof Iterable<?> iterable) {
            for (Object value : iterable) {
                if (value instanceof Number number) {
                    colors.add(number.intValue());
                }
            }
            return;
        }
        if (source.getClass().isArray()) {
            int length = Array.getLength(source);
            for (int index = 0; index < length; index++) {
                Object value = Array.get(source, index);
                if (value instanceof Number number) {
                    colors.add(number.intValue());
                }
            }
        }
    }

    private static @Nullable Object invokeNoArg(Object target, String methodName) {
        try {
            Method method = target.getClass().getMethod(methodName);
            method.setAccessible(true);
            return method.invoke(target);
        } catch (ReflectiveOperationException ignored) {
            return null;
        }
    }

    private static void applyRespawnLocation(ServerPlayer player, FabricLocation location) {
        if (location.level() != null && location.level() != player.level()) {
            return;
        }
        player.snapTo(
                location.position().x,
                location.position().y,
                location.position().z,
                player.getYRot(),
                player.getXRot()
        );
    }

    private static Object createPlayerRespawnHandle(
            FabricLocation defaultLocation,
            boolean bedSpawn,
            boolean anchorSpawn,
            @Nullable String reason
    ) {
        if (PLAYER_RESPAWN_EFFECT_HANDLE_CTOR == null) {
            return new FabricEventCompatHandles.PlayerRespawn(defaultLocation, bedSpawn, anchorSpawn, reason);
        }
        try {
            return PLAYER_RESPAWN_EFFECT_HANDLE_CTOR.newInstance(defaultLocation, bedSpawn, anchorSpawn, reason);
        } catch (ReflectiveOperationException ignored) {
            return new FabricEventCompatHandles.PlayerRespawn(defaultLocation, bedSpawn, anchorSpawn, reason);
        }
    }

    private static @Nullable FabricLocation playerRespawnLocation(Object handle) {
        if (PLAYER_RESPAWN_EFFECT_LOCATION == null || !PLAYER_RESPAWN_EFFECT_LOCATION.getDeclaringClass().isInstance(handle)) {
            return null;
        }
        try {
            Object value = PLAYER_RESPAWN_EFFECT_LOCATION.invoke(handle);
            return value instanceof FabricLocation location ? location : null;
        } catch (ReflectiveOperationException ignored) {
            return null;
        }
    }

    private static @Nullable Constructor<?> resolvePlayerRespawnEffectHandleCtor() {
        try {
            Class<?> type = Class.forName("ch.njol.skript.effects.FabricEffectEventHandles$PlayerRespawn");
            Constructor<?> constructor = type.getDeclaredConstructor(FabricLocation.class, boolean.class, boolean.class, String.class);
            constructor.setAccessible(true);
            return constructor;
        } catch (ReflectiveOperationException ignored) {
            return null;
        }
    }

    private static @Nullable Method resolvePlayerRespawnEffectMethod(String methodName) {
        try {
            Class<?> type = Class.forName("ch.njol.skript.effects.FabricEffectEventHandles$PlayerRespawn");
            Method method = type.getMethod(methodName);
            method.setAccessible(true);
            return method;
        } catch (ReflectiveOperationException ignored) {
            return null;
        }
    }

    private static Object createEntityDeathHandle(LivingEntity entity, List<ItemStack> drops, int droppedExp) {
        if (ENTITY_DEATH_EFFECT_HANDLE_CTOR == null) {
            throw new IllegalStateException("Entity death effect handle constructor is unavailable.");
        }
        try {
            return ENTITY_DEATH_EFFECT_HANDLE_CTOR.newInstance(entity, new ArrayList<>(drops), droppedExp);
        } catch (ReflectiveOperationException exception) {
            throw new IllegalStateException("Failed to create entity death effect handle.", exception);
        }
    }

    @SuppressWarnings("unchecked")
    private static List<ItemStack> entityDeathDrops(Object handle) {
        if (ENTITY_DEATH_EFFECT_DROPS == null || !ENTITY_DEATH_EFFECT_DROPS.getDeclaringClass().isInstance(handle)) {
            throw new IllegalStateException("Entity death effect drops accessor is unavailable.");
        }
        try {
            Object value = ENTITY_DEATH_EFFECT_DROPS.invoke(handle);
            if (!(value instanceof List<?> list)) {
                return List.of();
            }
            return (List<ItemStack>) list;
        } catch (ReflectiveOperationException exception) {
            throw new IllegalStateException("Failed to read entity death drops.", exception);
        }
    }

    private static int entityDeathDroppedExp(Object handle) {
        if (ENTITY_DEATH_EFFECT_DROPPED_EXP == null || !ENTITY_DEATH_EFFECT_DROPPED_EXP.getDeclaringClass().isInstance(handle)) {
            throw new IllegalStateException("Entity death effect dropped-exp accessor is unavailable.");
        }
        try {
            Object value = ENTITY_DEATH_EFFECT_DROPPED_EXP.invoke(handle);
            return value instanceof Number number ? number.intValue() : 0;
        } catch (ReflectiveOperationException exception) {
            throw new IllegalStateException("Failed to read entity death dropped exp.", exception);
        }
    }

    private static @Nullable Constructor<?> resolveEntityDeathEffectHandleCtor() {
        try {
            Class<?> type = Class.forName("ch.njol.skript.effects.FabricEffectEventHandles$EntityDeath");
            Constructor<?> constructor = type.getDeclaredConstructor(LivingEntity.class, List.class, int.class);
            constructor.setAccessible(true);
            return constructor;
        } catch (ReflectiveOperationException ignored) {
            return null;
        }
    }

    private static @Nullable Method resolveEntityDeathEffectMethod(String methodName) {
        try {
            Class<?> type = Class.forName("ch.njol.skript.effects.FabricEffectEventHandles$EntityDeath");
            Method method = type.getMethod(methodName);
            method.setAccessible(true);
            return method;
        } catch (ReflectiveOperationException ignored) {
            return null;
        }
    }

    public static float dispatchExplosionPrime(ServerLevel level, float radius, boolean causesFire) {
        Object handle = createExplosionPrimeHandle(radius, causesFire);
        SkriptRuntime.instance().dispatch(new org.skriptlang.skript.lang.event.SkriptEvent(
                handle,
                level.getServer(),
                level,
                null
        ));
        return explosionPrimeRadius(handle);
    }

    private static Object createExplosionPrimeHandle(float radius, boolean causesFire) {
        if (EXPLOSION_PRIME_EFFECT_HANDLE_CTOR == null) {
            throw new IllegalStateException("Explosion prime effect handle constructor is unavailable.");
        }
        try {
            return EXPLOSION_PRIME_EFFECT_HANDLE_CTOR.newInstance(radius, causesFire);
        } catch (ReflectiveOperationException exception) {
            throw new IllegalStateException("Failed to create explosion prime effect handle.", exception);
        }
    }

    private static float explosionPrimeRadius(Object handle) {
        if (EXPLOSION_PRIME_EFFECT_RADIUS == null || !EXPLOSION_PRIME_EFFECT_RADIUS.getDeclaringClass().isInstance(handle)) {
            throw new IllegalStateException("Explosion prime effect radius accessor is unavailable.");
        }
        try {
            Object value = EXPLOSION_PRIME_EFFECT_RADIUS.invoke(handle);
            return value instanceof Number number ? number.floatValue() : 0.0F;
        } catch (ReflectiveOperationException exception) {
            throw new IllegalStateException("Failed to read explosion prime radius.", exception);
        }
    }

    private static @Nullable Constructor<?> resolveExplosionPrimeEffectHandleCtor() {
        try {
            Class<?> type = Class.forName("ch.njol.skript.effects.FabricEffectEventHandles$ExplosionPrime");
            Constructor<?> constructor = type.getDeclaredConstructor(float.class, boolean.class);
            constructor.setAccessible(true);
            return constructor;
        } catch (ReflectiveOperationException ignored) {
            return null;
        }
    }

    private static @Nullable Method resolveExplosionPrimeEffectMethod(String methodName) {
        try {
            Class<?> type = Class.forName("ch.njol.skript.effects.FabricEffectEventHandles$ExplosionPrime");
            Method method = type.getMethod(methodName);
            method.setAccessible(true);
            return method;
        } catch (ReflectiveOperationException ignored) {
            return null;
        }
    }

    private static Object createHangingBreakHandle(Entity entity, @Nullable Entity remover) {
        if (HANGING_BREAK_EFFECT_HANDLE_CTOR == null) {
            throw new IllegalStateException("Hanging break effect handle constructor is unavailable.");
        }
        try {
            return HANGING_BREAK_EFFECT_HANDLE_CTOR.newInstance(entity, remover);
        } catch (ReflectiveOperationException exception) {
            throw new IllegalStateException("Failed to create hanging break effect handle.", exception);
        }
    }

    private static Object createHangingPlaceHandle(Entity entity) {
        if (HANGING_PLACE_EFFECT_HANDLE_CTOR == null) {
            throw new IllegalStateException("Hanging place effect handle constructor is unavailable.");
        }
        try {
            return HANGING_PLACE_EFFECT_HANDLE_CTOR.newInstance(entity);
        } catch (ReflectiveOperationException exception) {
            throw new IllegalStateException("Failed to create hanging place effect handle.", exception);
        }
    }

    private static @Nullable Constructor<?> resolveHangingBreakEffectHandleCtor() {
        try {
            Class<?> type = Class.forName("ch.njol.skript.effects.FabricEffectEventHandles$HangingBreak");
            Constructor<?> constructor = type.getDeclaredConstructor(Entity.class, Entity.class);
            constructor.setAccessible(true);
            return constructor;
        } catch (ReflectiveOperationException ignored) {
            return null;
        }
    }

    private static @Nullable Constructor<?> resolveHangingPlaceEffectHandleCtor() {
        try {
            Class<?> type = Class.forName("ch.njol.skript.effects.FabricEffectEventHandles$HangingPlace");
            Constructor<?> constructor = type.getDeclaredConstructor(Entity.class);
            constructor.setAccessible(true);
            return constructor;
        } catch (ReflectiveOperationException ignored) {
            return null;
        }
    }

    private static @Nullable Constructor<?> resolveElytraBoostEffectHandleCtor() {
        try {
            Class<?> type = Class.forName("ch.njol.skript.effects.FabricEffectEventHandles$PlayerElytraBoost");
            Constructor<?> constructor = type.getDeclaredConstructor(boolean.class);
            constructor.setAccessible(true);
            return constructor;
        } catch (ReflectiveOperationException ignored) {
            return null;
        }
    }

    private static @Nullable Method resolveElytraBoostEffectMethod(String methodName) {
        try {
            Class<?> type = Class.forName("ch.njol.skript.effects.FabricEffectEventHandles$PlayerElytraBoost");
            Method method = type.getDeclaredMethod(methodName);
            method.setAccessible(true);
            return method;
        } catch (ReflectiveOperationException ignored) {
            return null;
        }
    }

    private static Object createElytraBoostHandle(boolean shouldConsume) {
        if (ELYTRA_BOOST_EFFECT_HANDLE_CTOR == null) {
            throw new IllegalStateException("Elytra boost effect handle constructor is unavailable.");
        }
        try {
            return ELYTRA_BOOST_EFFECT_HANDLE_CTOR.newInstance(shouldConsume);
        } catch (ReflectiveOperationException exception) {
            throw new IllegalStateException("Failed to create elytra boost effect handle.", exception);
        }
    }

    private static boolean elytraBoostShouldConsume(Object handle) {
        if (ELYTRA_BOOST_SHOULD_CONSUME == null || !ELYTRA_BOOST_SHOULD_CONSUME.getDeclaringClass().isInstance(handle)) {
            return true;
        }
        try {
            Object value = ELYTRA_BOOST_SHOULD_CONSUME.invoke(handle);
            return value instanceof Boolean bool ? bool : true;
        } catch (ReflectiveOperationException exception) {
            return true;
        }
    }

    private static final class DeathCapture {

        private final LivingEntity entity;
        private final ServerLevel level;
        @SuppressWarnings("unused")
        private final DamageSource source;
        private final List<ItemStack> drops = new ArrayList<>();
        private int droppedExp;

        private DeathCapture(LivingEntity entity, ServerLevel level, DamageSource source) {
            this.entity = entity;
            this.level = level;
            this.source = source;
        }

        private LivingEntity entity() {
            return entity;
        }

        private ServerLevel level() {
            return level;
        }

        private List<ItemStack> drops() {
            return drops;
        }

        private int droppedExp() {
            return droppedExp;
        }

        private void addDroppedExp(int amount) {
            droppedExp = Math.max(0, droppedExp + amount);
        }
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

    private static void spawnEggHatches(
            ServerLevel level,
            ThrownEgg egg,
            byte hatches,
            @Nullable EntityType<?> hatchingType
    ) {
        int hatchCount = Math.max(0, hatches);
        EntityType<?> type = hatchingType == null ? EntityType.CHICKEN : hatchingType;
        for (int index = 0; index < hatchCount; index++) {
            Entity entity = type.create(level, net.minecraft.world.entity.EntitySpawnReason.TRIGGERED);
            if (entity == null) {
                continue;
            }
            entity.setPos(egg.getX(), egg.getY(), egg.getZ());
            entity.setYRot(egg.getYRot());
            if (entity instanceof AgeableMob ageableMob) {
                ageableMob.setAge(-24000);
            }
            level.addFreshEntity(entity);
        }
    }

    private static final class MutableEggThrowHandle implements FabricEggThrowEventHandle {

        private final ThrownEgg egg;
        private boolean hatching;
        private byte hatches;
        private @Nullable EntityType<?> hatchingType;

        private MutableEggThrowHandle(ThrownEgg egg, boolean hatching, byte hatches, @Nullable EntityType<?> hatchingType) {
            this.egg = egg;
            this.hatching = hatching;
            this.hatches = hatches;
            this.hatchingType = hatchingType;
        }

        private static MutableEggThrowHandle create(ThrownEgg egg) {
            boolean hatching = egg.level().getRandom().nextInt(8) == 0;
            byte hatches = 0;
            if (hatching) {
                hatches = (byte) (egg.level().getRandom().nextInt(32) == 0 ? 4 : 1);
            }
            return new MutableEggThrowHandle(egg, hatching, hatches, EntityType.CHICKEN);
        }

        @Override
        public ThrownEgg egg() {
            return egg;
        }

        @Override
        public boolean hatching() {
            return hatching;
        }

        @Override
        public void setHatching(boolean hatching) {
            this.hatching = hatching;
        }

        @Override
        public byte hatches() {
            return hatches;
        }

        @Override
        public void setHatches(byte hatches) {
            this.hatches = (byte) Math.max(0, hatches);
        }

        @Override
        public @Nullable EntityType<?> hatchingType() {
            return hatchingType;
        }

        @Override
        public void setHatchingType(EntityType<?> hatchingType) {
            this.hatchingType = hatchingType;
        }
    }

    public static void dispatchJump(ServerPlayer player) {
        ServerLevel level = player.level();
        SkriptRuntime.instance().dispatch(new org.skriptlang.skript.lang.event.SkriptEvent(
                new FabricEventCompatHandles.Jump(),
                level.getServer(),
                level,
                player
        ));
    }

    public static void dispatchHandItemSwap(ServerPlayer player) {
        ServerLevel level = player.level();
        SkriptRuntime.instance().dispatch(new org.skriptlang.skript.lang.event.SkriptEvent(
                new FabricEventCompatHandles.HandItemSwap(),
                level.getServer(),
                level,
                player
        ));
    }

    public static void dispatchSneakToggle(ServerPlayer player, boolean sneaking) {
        ServerLevel level = player.level();
        SkriptRuntime.instance().dispatch(new org.skriptlang.skript.lang.event.SkriptEvent(
                new FabricEventCompatHandles.SneakToggle(sneaking),
                level.getServer(),
                level,
                player
        ));
    }

    public static void dispatchSprintToggle(ServerPlayer player, boolean sprinting) {
        ServerLevel level = player.level();
        SkriptRuntime.instance().dispatch(new org.skriptlang.skript.lang.event.SkriptEvent(
                new FabricEventCompatHandles.SprintToggle(sprinting),
                level.getServer(),
                level,
                player
        ));
    }

    public static void dispatchFlightToggle(ServerPlayer player, boolean flying) {
        ServerLevel level = player.level();
        SkriptRuntime.instance().dispatch(new org.skriptlang.skript.lang.event.SkriptEvent(
                new FabricEventCompatHandles.FlightToggle(flying),
                level.getServer(),
                level,
                player
        ));
    }

    public static void dispatchGlideToggle(ServerPlayer player) {
        ServerLevel level = player.level();
        SkriptRuntime.instance().dispatch(new org.skriptlang.skript.lang.event.SkriptEvent(
                new FabricEventCompatHandles.GlideToggle(),
                level.getServer(),
                level,
                player
        ));
    }

    public static void dispatchSwimToggle(Entity entity, boolean swimming) {
        if (!(entity.level() instanceof ServerLevel level)) return;
        SkriptRuntime.instance().dispatch(new org.skriptlang.skript.lang.event.SkriptEvent(
                new FabricEventCompatHandles.SwimToggle(entity, swimming),
                level.getServer(),
                level,
                entity instanceof ServerPlayer sp ? sp : null
        ));
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

    public static FabricServerListPingHandle dispatchServerListPing(List<String> currentSample, int protocolVersion) {
        FabricServerListPingHandle handle = new FabricServerListPingHandle(new ArrayList<>(currentSample), protocolVersion);
        SkriptRuntime.instance().dispatch(new org.skriptlang.skript.lang.event.SkriptEvent(
                handle,
                null,
                null,
                null
        ));
        return handle;
    }

    public static void dispatchChunkLoad(ServerLevel level, net.minecraft.world.level.chunk.LevelChunk chunk) {
        SkriptRuntime.instance().dispatch(new org.skriptlang.skript.lang.event.SkriptEvent(
                new FabricEventCompatHandles.ChunkLoad(chunk),
                level.getServer(),
                level,
                null
        ));
    }

    public static void dispatchChunkUnload(ServerLevel level, net.minecraft.world.level.chunk.LevelChunk chunk) {
        SkriptRuntime.instance().dispatch(new org.skriptlang.skript.lang.event.SkriptEvent(
                new FabricEventCompatHandles.ChunkUnload(chunk),
                level.getServer(),
                level,
                null
        ));
    }

    public static void dispatchVehicleCreate(ServerLevel level, Entity vehicle) {
        SkriptRuntime.instance().dispatch(new org.skriptlang.skript.lang.event.SkriptEvent(
                new FabricEventCompatHandles.VehicleCreate(vehicle),
                level.getServer(),
                level,
                null
        ));
    }

    public static void dispatchVehicleDamage(ServerLevel level, Entity vehicle, @Nullable Entity attacker) {
        SkriptRuntime.instance().dispatch(new org.skriptlang.skript.lang.event.SkriptEvent(
                new FabricEventCompatHandles.VehicleDamage(vehicle, attacker),
                level.getServer(),
                level,
                attacker instanceof ServerPlayer sp ? sp : null
        ));
    }

    public static void dispatchVehicleDestroy(ServerLevel level, Entity vehicle, @Nullable Entity attacker) {
        SkriptRuntime.instance().dispatch(new org.skriptlang.skript.lang.event.SkriptEvent(
                new FabricEventCompatHandles.VehicleDestroy(vehicle, attacker),
                level.getServer(),
                level,
                attacker instanceof ServerPlayer sp ? sp : null
        ));
    }

    public static void dispatchVehicleEnter(ServerLevel level, Entity vehicle, Entity passenger) {
        SkriptRuntime.instance().dispatch(new org.skriptlang.skript.lang.event.SkriptEvent(
                new FabricEventCompatHandles.VehicleEnter(vehicle, passenger),
                level.getServer(),
                level,
                passenger instanceof ServerPlayer sp ? sp : null
        ));
    }

    public static void dispatchVehicleExit(ServerLevel level, Entity vehicle, Entity passenger) {
        SkriptRuntime.instance().dispatch(new org.skriptlang.skript.lang.event.SkriptEvent(
                new FabricEventCompatHandles.VehicleExit(vehicle, passenger),
                level.getServer(),
                level,
                passenger instanceof ServerPlayer sp ? sp : null
        ));
    }

    public static void dispatchEntityMount(ServerLevel level, Entity entity, Entity vehicle) {
        SkriptRuntime.instance().dispatch(new org.skriptlang.skript.lang.event.SkriptEvent(
                new FabricEventCompatHandles.EntityMount(entity, vehicle),
                level.getServer(),
                level,
                entity instanceof ServerPlayer sp ? sp : null
        ));
    }

    public static void dispatchEntityDismount(ServerLevel level, Entity entity, Entity vehicle) {
        SkriptRuntime.instance().dispatch(new org.skriptlang.skript.lang.event.SkriptEvent(
                new FabricEventCompatHandles.EntityDismount(entity, vehicle),
                level.getServer(),
                level,
                entity instanceof ServerPlayer sp ? sp : null
        ));
    }

    public static void dispatchResurrectAttempt(ServerLevel level, LivingEntity entity) {
        SkriptRuntime.instance().dispatch(new org.skriptlang.skript.lang.event.SkriptEvent(
                new FabricEventCompatHandles.ResurrectAttempt(entity),
                level.getServer(),
                level,
                entity instanceof ServerPlayer sp ? sp : null
        ));
    }

    public static void dispatchPlayerWorldChange(ServerPlayer player, ServerLevel fromLevel) {
        SkriptRuntime.instance().dispatch(new org.skriptlang.skript.lang.event.SkriptEvent(
                new FabricEventCompatHandles.PlayerWorldChange(player),
                fromLevel.getServer(),
                fromLevel,
                player
        ));
    }

    public static void dispatchSheepRegrowWool(ServerLevel level, Entity sheep) {
        SkriptRuntime.instance().dispatch(new org.skriptlang.skript.lang.event.SkriptEvent(
                new FabricEventCompatHandles.SheepRegrowWool(sheep),
                level.getServer(),
                level,
                null
        ));
    }

    public static void dispatchSlimeSplit(ServerLevel level, Entity slime) {
        SkriptRuntime.instance().dispatch(new org.skriptlang.skript.lang.event.SkriptEvent(
                new FabricEventCompatHandles.SlimeSplit(slime),
                level.getServer(),
                level,
                null
        ));
    }

    public static void dispatchBellRing(ServerLevel level, net.minecraft.core.BlockPos pos) {
        SkriptRuntime.instance().dispatch(new org.skriptlang.skript.lang.event.SkriptEvent(
                new FabricEventCompatHandles.BellRing(level, pos.immutable()),
                level.getServer(),
                level,
                null
        ));
    }

    public static void dispatchBellResonate(ServerLevel level, net.minecraft.core.BlockPos pos) {
        SkriptRuntime.instance().dispatch(new org.skriptlang.skript.lang.event.SkriptEvent(
                new FabricEventCompatHandles.BellResonate(level, pos.immutable()),
                level.getServer(),
                level,
                null
        ));
    }

    public static void dispatchBatToggleSleep(ServerLevel level, Entity bat, boolean resting) {
        SkriptRuntime.instance().dispatch(new org.skriptlang.skript.lang.event.SkriptEvent(
                new FabricEventCompatHandles.BatToggleSleep(bat, resting),
                level.getServer(),
                level,
                null
        ));
    }

    public static void dispatchToolChange(ServerPlayer player, int previousSlot, int newSlot) {
        if (!(player.level() instanceof ServerLevel level)) return;
        SkriptRuntime.instance().dispatch(new org.skriptlang.skript.lang.event.SkriptEvent(
                new FabricEventCompatHandles.ToolChange(player, previousSlot, newSlot),
                level.getServer(),
                level,
                player
        ));
    }

    public static void dispatchLanguageChange(ServerPlayer player, String language) {
        if (!(player.level() instanceof ServerLevel level)) return;
        SkriptRuntime.instance().dispatch(new org.skriptlang.skript.lang.event.SkriptEvent(
                new FabricEventCompatHandles.LanguageChange(player, language),
                level.getServer(),
                level,
                player
        ));
    }

    public static void dispatchTame(ServerLevel level, net.minecraft.world.entity.TamableAnimal animal, net.minecraft.world.entity.player.Player player) {
        SkriptRuntime.instance().dispatch(new org.skriptlang.skript.lang.event.SkriptEvent(
                new FabricEventCompatHandles.Tame(animal, player),
                level.getServer(),
                level,
                player instanceof ServerPlayer sp ? sp : null
        ));
    }

    public static void dispatchCombust(ServerLevel level, Entity entity, int durationTicks) {
        SkriptRuntime.instance().dispatch(new org.skriptlang.skript.lang.event.SkriptEvent(
                new FabricEventCompatHandles.Combust(entity, durationTicks),
                level.getServer(),
                level,
                entity instanceof ServerPlayer sp ? sp : null
        ));
    }

    public static void dispatchProjectileHit(ServerLevel level, net.minecraft.world.entity.projectile.Projectile projectile, @Nullable Entity hitEntity) {
        SkriptRuntime.instance().dispatch(new org.skriptlang.skript.lang.event.SkriptEvent(
                new FabricEventCompatHandles.ProjectileHit(projectile, hitEntity),
                level.getServer(),
                level,
                null
        ));
    }

    public static void dispatchProjectileLaunch(ServerLevel level, net.minecraft.world.entity.projectile.Projectile projectile) {
        SkriptRuntime.instance().dispatch(new org.skriptlang.skript.lang.event.SkriptEvent(
                new FabricEventCompatHandles.ProjectileLaunch(projectile),
                level.getServer(),
                level,
                null
        ));
    }

    public static void dispatchBedEnter(ServerLevel level, ServerPlayer player) {
        SkriptRuntime.instance().dispatch(new org.skriptlang.skript.lang.event.SkriptEvent(
                new FabricEventCompatHandles.BedEnter(player),
                level.getServer(),
                level,
                player
        ));
    }

    public static void dispatchBedLeave(ServerLevel level, ServerPlayer player) {
        SkriptRuntime.instance().dispatch(new org.skriptlang.skript.lang.event.SkriptEvent(
                new FabricEventCompatHandles.BedLeave(player),
                level.getServer(),
                level,
                player
        ));
    }

    public static void dispatchLightningStrike(ServerLevel level, net.minecraft.world.entity.LightningBolt lightning) {
        SkriptRuntime.instance().dispatch(new org.skriptlang.skript.lang.event.SkriptEvent(
                new FabricEventCompatHandles.LightningStrike(lightning),
                level.getServer(),
                level,
                null
        ));
    }

    public static void dispatchFoodLevelChange(ServerLevel level, ServerPlayer player, int oldLevel, int newLevel) {
        SkriptRuntime.instance().dispatch(new org.skriptlang.skript.lang.event.SkriptEvent(
                new FabricEventCompatHandles.FoodLevelChange(player, oldLevel, newLevel),
                level.getServer(),
                level,
                player
        ));
    }

    public static void dispatchSignChange(ServerLevel level, ServerPlayer player, BlockPos pos, String[] lines, boolean front) {
        SkriptRuntime.instance().dispatch(new org.skriptlang.skript.lang.event.SkriptEvent(
                new FabricEventCompatHandles.SignChange(player, pos.immutable(), lines.clone(), front),
                level.getServer(),
                level,
                player
        ));
    }

    public static void dispatchBlockDamage(ServerLevel level, ServerPlayer player, BlockPos pos) {
        SkriptRuntime.instance().dispatch(new org.skriptlang.skript.lang.event.SkriptEvent(
                new FabricEventCompatHandles.BlockDamage(player, pos.immutable()),
                level.getServer(),
                level,
                player
        ));
    }

    public static void dispatchBucketUse(ServerLevel level, ServerPlayer player, boolean fill) {
        SkriptRuntime.instance().dispatch(new org.skriptlang.skript.lang.event.SkriptEvent(
                new FabricEventCompatHandles.BucketUse(player, fill),
                level.getServer(),
                level,
                player
        ));
    }

    public static void dispatchInventoryOpen(ServerLevel level, ServerPlayer player) {
        SkriptRuntime.instance().dispatch(new org.skriptlang.skript.lang.event.SkriptEvent(
                new FabricEventCompatHandles.InventoryOpen(player),
                level.getServer(),
                level,
                player
        ));
    }

    public static void dispatchInventoryClose(ServerLevel level, ServerPlayer player) {
        SkriptRuntime.instance().dispatch(new org.skriptlang.skript.lang.event.SkriptEvent(
                new FabricEventCompatHandles.InventoryClose(player),
                level.getServer(),
                level,
                player
        ));
    }

    public static void dispatchInventoryDrag(ServerLevel level, ServerPlayer player) {
        SkriptRuntime.instance().dispatch(new org.skriptlang.skript.lang.event.SkriptEvent(
                new FabricEventCompatHandles.InventoryDrag(player),
                level.getServer(),
                level,
                player
        ));
    }

    public static void dispatchLeavesDecay(ServerLevel level, BlockPos pos) {
        SkriptRuntime.instance().dispatch(new org.skriptlang.skript.lang.event.SkriptEvent(
                new FabricEventCompatHandles.LeavesDecay(level, pos.immutable()),
                level.getServer(),
                level,
                null
        ));
    }

    public static void dispatchSpongeAbsorb(ServerLevel level, BlockPos pos) {
        SkriptRuntime.instance().dispatch(new org.skriptlang.skript.lang.event.SkriptEvent(
                new FabricEventCompatHandles.SpongeAbsorb(level, pos.immutable()),
                level.getServer(),
                level,
                null
        ));
    }

    public static void dispatchSpawnChange(ServerLevel level, BlockPos pos) {
        SkriptRuntime.instance().dispatch(new org.skriptlang.skript.lang.event.SkriptEvent(
                new FabricEventCompatHandles.SpawnChange(level, pos.immutable()),
                level.getServer(),
                level,
                null
        ));
    }
}
