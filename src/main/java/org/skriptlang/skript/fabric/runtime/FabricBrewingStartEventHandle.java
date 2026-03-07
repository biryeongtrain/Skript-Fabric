package org.skriptlang.skript.fabric.runtime;

import net.minecraft.world.level.block.entity.BrewingStandBlockEntity;

public interface FabricBrewingStartEventHandle extends FabricBlockEventHandle {

    BrewingStandBlockEntity brewingStand();
}
