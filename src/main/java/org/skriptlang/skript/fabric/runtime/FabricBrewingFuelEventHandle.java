package org.skriptlang.skript.fabric.runtime;

import net.minecraft.world.level.block.entity.BrewingStandBlockEntity;

public interface FabricBrewingFuelEventHandle extends FabricBlockEventHandle {

    BrewingStandBlockEntity brewingStand();

    boolean willConsume();

    void setWillConsume(boolean willConsume);
}
