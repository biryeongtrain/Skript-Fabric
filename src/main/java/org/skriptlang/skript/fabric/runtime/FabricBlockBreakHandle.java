package org.skriptlang.skript.fabric.runtime;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public record FabricBlockBreakHandle(
        ServerLevel level,
        ServerPlayer player,
        BlockPos position,
        BlockState state,
        @Nullable BlockEntity blockEntity
) implements FabricBlockEventHandle {
}
