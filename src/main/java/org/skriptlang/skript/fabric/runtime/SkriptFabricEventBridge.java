package org.skriptlang.skript.fabric.runtime;

import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.fabricmc.fabric.api.event.player.UseEntityCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.world.level.Level;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;

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
            UseBlockCallback.EVENT.register(SkriptFabricEventBridge::dispatchUseBlock);
            UseEntityCallback.EVENT.register(SkriptFabricEventBridge::dispatchUseEntity);
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
        SkriptRuntime.instance().dispatch(new org.skriptlang.skript.lang.event.SkriptEvent(
                new FabricUseEntityHandle(serverLevel, serverPlayer, hand, entity, hitResult),
                serverLevel.getServer(),
                serverLevel,
                serverPlayer
        ));
        return InteractionResult.PASS;
    }
}
