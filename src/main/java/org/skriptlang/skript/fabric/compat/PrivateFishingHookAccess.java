package org.skriptlang.skript.fabric.compat;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.projectile.FishingHook;
import org.jetbrains.annotations.Nullable;

public final class PrivateFishingHookAccess {

    private static final Field TIME_UNTIL_HOOKED = lookup(FishingHook.class, "timeUntilHooked", int.class);
    private static final Field HOOKED_IN = lookup(FishingHook.class, "hookedIn", Entity.class);
    private static final Method PULL_ENTITY = lookup(FishingHook.class, "pullEntity", void.class, Entity.class);

    private PrivateFishingHookAccess() {
    }

    public static int timeUntilHooked(FishingHook hook) {
        try {
            return TIME_UNTIL_HOOKED.getInt(hook);
        } catch (IllegalAccessException exception) {
            throw new IllegalStateException("Failed to read fishing hook bite time via reflection.", exception);
        }
    }

    public static void setTimeUntilHooked(FishingHook hook, int value) {
        try {
            TIME_UNTIL_HOOKED.setInt(hook, value);
        } catch (IllegalAccessException exception) {
            throw new IllegalStateException("Failed to write fishing hook bite time via reflection.", exception);
        }
    }

    public static void pullEntity(FishingHook hook, Entity entity) {
        try {
            PULL_ENTITY.invoke(hook, entity);
        } catch (IllegalAccessException | InvocationTargetException exception) {
            throw new IllegalStateException("Failed to pull a hooked entity via reflection.", exception);
        }
    }

    public static @Nullable Entity hookedIn(FishingHook hook) {
        try {
            return (Entity) HOOKED_IN.get(hook);
        } catch (IllegalAccessException exception) {
            throw new IllegalStateException("Failed to read hooked entity via reflection.", exception);
        }
    }

    public static void setHookedIn(FishingHook hook, @Nullable Entity entity) {
        try {
            HOOKED_IN.set(hook, entity);
        } catch (IllegalAccessException exception) {
            throw new IllegalStateException("Failed to write hooked entity via reflection.", exception);
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

    private static Method lookup(Class<?> owner, String name, Class<?> expectedReturnType, Class<?>... parameterTypes) {
        try {
            Method method = owner.getDeclaredMethod(name, parameterTypes);
            method.setAccessible(true);
            if (expectedReturnType != void.class && !expectedReturnType.isAssignableFrom(method.getReturnType())) {
                throw new IllegalStateException("Unexpected return type for " + owner.getName() + "#" + name);
            }
            return method;
        } catch (NoSuchMethodException exception) {
            throw new IllegalStateException("Missing expected Minecraft method " + owner.getName() + "#" + name, exception);
        }
    }
}
