package org.skriptlang.skript.fabric.compat;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public record FabricBlock(ServerLevel level, BlockPos position) {

    public BlockState state() {
        return level.getBlockState(position);
    }

    public Block block() {
        return state().getBlock();
    }

    public @Nullable BlockEntity blockEntity() {
        return level.getBlockEntity(position);
    }
}
