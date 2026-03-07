package org.skriptlang.skript.fabric.runtime;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.entity.BrewingStandBlockEntity;

public record FabricBrewingStartHandle(
        ServerLevel level,
        BlockPos position,
        BrewingStandBlockEntity brewingStand
) implements FabricBrewingStartEventHandle {
}
