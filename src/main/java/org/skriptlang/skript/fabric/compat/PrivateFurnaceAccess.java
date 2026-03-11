package org.skriptlang.skript.fabric.compat;

import kim.biryeong.skriptFabric.mixin.AbstractFurnaceBlockEntityAccessor;
import net.minecraft.world.level.block.entity.AbstractFurnaceBlockEntity;

public final class PrivateFurnaceAccess {

    private PrivateFurnaceAccess() {
    }

    public static int litTimeRemaining(AbstractFurnaceBlockEntity furnace) {
        return accessor(furnace).skript$getLitTimeRemaining();
    }

    public static void setLitTimeRemaining(AbstractFurnaceBlockEntity furnace, int value) {
        accessor(furnace).skript$setLitTimeRemaining(value);
    }

    public static int litTotalTime(AbstractFurnaceBlockEntity furnace) {
        return accessor(furnace).skript$getLitTotalTime();
    }

    public static void setLitTotalTime(AbstractFurnaceBlockEntity furnace, int value) {
        accessor(furnace).skript$setLitTotalTime(value);
    }

    public static int cookingTimer(AbstractFurnaceBlockEntity furnace) {
        return accessor(furnace).skript$getCookingTimer();
    }

    public static void setCookingTimer(AbstractFurnaceBlockEntity furnace, int value) {
        accessor(furnace).skript$setCookingTimer(value);
    }

    public static int cookingTotalTime(AbstractFurnaceBlockEntity furnace) {
        return accessor(furnace).skript$getCookingTotalTime();
    }

    public static void setCookingTotalTime(AbstractFurnaceBlockEntity furnace, int value) {
        accessor(furnace).skript$setCookingTotalTime(value);
    }

    private static AbstractFurnaceBlockEntityAccessor accessor(AbstractFurnaceBlockEntity furnace) {
        return (AbstractFurnaceBlockEntityAccessor) furnace;
    }
}
