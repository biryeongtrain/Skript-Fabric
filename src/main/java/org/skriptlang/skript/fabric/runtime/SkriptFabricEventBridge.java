package org.skriptlang.skript.fabric.runtime;

import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.fabricmc.fabric.api.event.player.AttackEntityCallback;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.fabricmc.fabric.api.event.player.UseEntityCallback;
import net.fabricmc.fabric.api.event.player.UseItemCallback;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.world.level.Level;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.entity.player.Input;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Interaction;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.projectile.FishingHook;
import net.minecraft.world.level.block.entity.BrewingStandBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import org.skriptlang.skript.fabric.compat.FabricInteractionState;

public final class SkriptFabricEventBridge {

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
            UseBlockCallback.EVENT.register(SkriptFabricEventBridge::dispatchUseBlock);
            UseEntityCallback.EVENT.register(SkriptFabricEventBridge::dispatchUseEntity);
            UseItemCallback.EVENT.register(SkriptFabricEventBridge::dispatchUseItem);
            ServerLivingEntityEvents.ALLOW_DAMAGE.register(SkriptFabricEventBridge::dispatchDamage);
            registered = true;
        }
    }

    private static void dispatchServerTick(MinecraftServer server) {
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
        SkriptRuntime.instance().dispatch(new org.skriptlang.skript.lang.event.SkriptEvent(
                new FabricBlockBreakHandle(serverLevel, serverPlayer, pos.immutable(), state, blockEntity),
                serverLevel.getServer(),
                serverLevel,
                serverPlayer
        ));
    }

    private static InteractionResult dispatchUseBlock(net.minecraft.world.entity.player.Player player, Level level, InteractionHand hand, BlockHitResult hitResult) {
        if (!(level instanceof ServerLevel serverLevel) || !(player instanceof ServerPlayer serverPlayer)) {
            return InteractionResult.PASS;
        }
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

    public static void dispatchBrewingFuel(ServerLevel level, BlockPos pos, BrewingStandBlockEntity brewingStand, boolean willConsume) {
        SkriptRuntime.instance().dispatch(new org.skriptlang.skript.lang.event.SkriptEvent(
                new FabricBrewingFuelHandle(level, pos.immutable(), brewingStand, willConsume),
                level.getServer(),
                level,
                null
        ));
    }

    public static void dispatchFishing(ServerLevel level, ServerPlayer player, FishingHook hook, boolean lureApplied) {
        SkriptRuntime.instance().dispatch(new org.skriptlang.skript.lang.event.SkriptEvent(
                new FabricFishingHandle(level, player, hook, lureApplied),
                level.getServer(),
                level,
                player
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

    private static Input normalizeInput(Input input) {
        return input != null ? input : Input.EMPTY;
    }
}
