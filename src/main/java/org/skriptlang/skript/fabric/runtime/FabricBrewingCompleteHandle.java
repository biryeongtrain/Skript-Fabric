package org.skriptlang.skript.fabric.runtime;

import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BrewingStandBlockEntity;

public record FabricBrewingCompleteHandle(
        ServerLevel level,
        BlockPos position,
        BrewingStandBlockEntity brewingStand,
        List<ItemStack> results
) implements FabricBrewingCompleteEventHandle {
}
