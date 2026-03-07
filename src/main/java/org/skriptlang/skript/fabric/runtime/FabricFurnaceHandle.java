package org.skriptlang.skript.fabric.runtime;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.AbstractFurnaceBlockEntity;

public record FabricFurnaceHandle(
        Kind kind,
        ServerLevel level,
        BlockPos position,
        AbstractFurnaceBlockEntity furnace,
        ItemStack source,
        ItemStack fuel,
        ItemStack result,
        int itemAmount,
        int burnTime,
        int totalCookTime
) implements FabricFurnaceEventHandle {
}
