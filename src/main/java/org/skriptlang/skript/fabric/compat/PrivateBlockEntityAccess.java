package org.skriptlang.skript.fabric.compat;

import kim.biryeong.skriptFabric.mixin.BrewingStandBlockEntityAccessor;
import net.minecraft.world.level.block.entity.BrewingStandBlockEntity;

public final class PrivateBlockEntityAccess {

    private PrivateBlockEntityAccess() {
    }

    public static int brewingFuel(BrewingStandBlockEntity brewingStand) {
        return accessor(brewingStand).skript$getFuel();
    }

    public static void setBrewingFuel(BrewingStandBlockEntity brewingStand, int fuel) {
        accessor(brewingStand).skript$setFuel(fuel);
    }

    public static int brewingTime(BrewingStandBlockEntity brewingStand) {
        return accessor(brewingStand).skript$getBrewTime();
    }

    public static void setBrewingTime(BrewingStandBlockEntity brewingStand, int brewingTime) {
        accessor(brewingStand).skript$setBrewTime(brewingTime);
    }

    private static BrewingStandBlockEntityAccessor accessor(BrewingStandBlockEntity brewingStand) {
        return (BrewingStandBlockEntityAccessor) brewingStand;
    }
}
