package org.skriptlang.skript.fabric.runtime;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.phys.BlockHitResult;

public record FabricUseBlockHandle(
        ServerLevel level,
        ServerPlayer player,
        InteractionHand hand,
        BlockHitResult hitResult
) implements FabricBlockEventHandle {

    @Override
    public BlockPos position() {
        return hitResult.getBlockPos().immutable();
    }
}
