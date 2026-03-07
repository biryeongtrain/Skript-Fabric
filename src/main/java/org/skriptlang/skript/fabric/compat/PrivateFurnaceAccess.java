package org.skriptlang.skript.fabric.compat;

import java.lang.reflect.Field;
import net.minecraft.world.level.block.entity.AbstractFurnaceBlockEntity;

public final class PrivateFurnaceAccess {

    private static final Field LIT_TIME_REMAINING = lookup("litTimeRemaining");
    private static final Field LIT_TOTAL_TIME = lookup("litTotalTime");
    private static final Field COOKING_TIMER = lookup("cookingTimer");
    private static final Field COOKING_TOTAL_TIME = lookup("cookingTotalTime");

    private PrivateFurnaceAccess() {
    }

    public static int litTimeRemaining(AbstractFurnaceBlockEntity furnace) {
        return getInt(LIT_TIME_REMAINING, furnace);
    }

    public static void setLitTimeRemaining(AbstractFurnaceBlockEntity furnace, int value) {
        setInt(LIT_TIME_REMAINING, furnace, value);
    }

    public static int litTotalTime(AbstractFurnaceBlockEntity furnace) {
        return getInt(LIT_TOTAL_TIME, furnace);
    }

    public static void setLitTotalTime(AbstractFurnaceBlockEntity furnace, int value) {
        setInt(LIT_TOTAL_TIME, furnace, value);
    }

    public static int cookingTimer(AbstractFurnaceBlockEntity furnace) {
        return getInt(COOKING_TIMER, furnace);
    }

    public static void setCookingTimer(AbstractFurnaceBlockEntity furnace, int value) {
        setInt(COOKING_TIMER, furnace, value);
    }

    public static int cookingTotalTime(AbstractFurnaceBlockEntity furnace) {
        return getInt(COOKING_TOTAL_TIME, furnace);
    }

    public static void setCookingTotalTime(AbstractFurnaceBlockEntity furnace, int value) {
        setInt(COOKING_TOTAL_TIME, furnace, value);
    }

    private static int getInt(Field field, Object target) {
        try {
            return field.getInt(target);
        } catch (IllegalAccessException exception) {
            throw new IllegalStateException("Failed to access furnace state.", exception);
        }
    }

    private static void setInt(Field field, Object target, int value) {
        try {
            field.setInt(target, value);
        } catch (IllegalAccessException exception) {
            throw new IllegalStateException("Failed to mutate furnace state.", exception);
        }
    }

    private static Field lookup(String name) {
        try {
            Field field = AbstractFurnaceBlockEntity.class.getDeclaredField(name);
            field.setAccessible(true);
            return field;
        } catch (NoSuchFieldException exception) {
            throw new IllegalStateException("Missing furnace field " + name, exception);
        }
    }
}
