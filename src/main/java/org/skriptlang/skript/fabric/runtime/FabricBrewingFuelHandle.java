package org.skriptlang.skript.fabric.runtime;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.entity.BrewingStandBlockEntity;

public final class FabricBrewingFuelHandle implements FabricBrewingFuelEventHandle {

    private final ServerLevel level;
    private final BlockPos position;
    private final BrewingStandBlockEntity brewingStand;
    private boolean willConsume;

    public FabricBrewingFuelHandle(
            ServerLevel level,
            BlockPos position,
            BrewingStandBlockEntity brewingStand,
            boolean willConsume
    ) {
        this.level = level;
        this.position = position;
        this.brewingStand = brewingStand;
        this.willConsume = willConsume;
    }

    @Override
    public ServerLevel level() {
        return level;
    }

    @Override
    public BlockPos position() {
        return position;
    }

    @Override
    public BrewingStandBlockEntity brewingStand() {
        return brewingStand;
    }

    @Override
    public boolean willConsume() {
        return willConsume;
    }

    @Override
    public void setWillConsume(boolean willConsume) {
        this.willConsume = willConsume;
    }
}
