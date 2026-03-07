package org.skriptlang.skript.fabric.compat;

import java.lang.reflect.Field;
import net.minecraft.world.level.block.entity.BrewingStandBlockEntity;

public final class PrivateBlockEntityAccess {

    private static final Field BREWING_FUEL = lookup(BrewingStandBlockEntity.class, "fuel", int.class);
    private static final Field BREWING_TIME = lookup(BrewingStandBlockEntity.class, "brewTime", int.class);

    private PrivateBlockEntityAccess() {
    }

    public static int brewingFuel(BrewingStandBlockEntity brewingStand) {
        try {
            return BREWING_FUEL.getInt(brewingStand);
        } catch (IllegalAccessException exception) {
            throw new IllegalStateException("Failed to read brewing stand fuel via reflection.", exception);
        }
    }

    public static void setBrewingFuel(BrewingStandBlockEntity brewingStand, int fuel) {
        try {
            BREWING_FUEL.setInt(brewingStand, fuel);
        } catch (IllegalAccessException exception) {
            throw new IllegalStateException("Failed to write brewing stand fuel via reflection.", exception);
        }
    }

    public static int brewingTime(BrewingStandBlockEntity brewingStand) {
        try {
            return BREWING_TIME.getInt(brewingStand);
        } catch (IllegalAccessException exception) {
            throw new IllegalStateException("Failed to read brewing stand brew time via reflection.", exception);
        }
    }

    public static void setBrewingTime(BrewingStandBlockEntity brewingStand, int brewingTime) {
        try {
            BREWING_TIME.setInt(brewingStand, brewingTime);
        } catch (IllegalAccessException exception) {
            throw new IllegalStateException("Failed to write brewing stand brew time via reflection.", exception);
        }
    }

    private static Field lookup(Class<?> owner, String name, Class<?> expectedType) {
        try {
            Field field = owner.getDeclaredField(name);
            field.setAccessible(true);
            if (!expectedType.isAssignableFrom(field.getType()) && field.getType() != expectedType) {
                throw new IllegalStateException("Unexpected field type for " + owner.getName() + "#" + name);
            }
            return field;
        } catch (NoSuchFieldException exception) {
            throw new IllegalStateException("Missing expected Minecraft field " + owner.getName() + "#" + name, exception);
        }
    }
}
