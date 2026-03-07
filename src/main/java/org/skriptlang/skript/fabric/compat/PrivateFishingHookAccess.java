package org.skriptlang.skript.fabric.compat;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.projectile.FishingHook;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import org.jetbrains.annotations.Nullable;

public final class PrivateFishingHookAccess {

    private static final Field TIME_UNTIL_HOOKED = lookup(FishingHook.class, "timeUntilHooked", int.class);
    private static final Field TIME_UNTIL_LURED = lookup(FishingHook.class, "timeUntilLured", int.class);
    private static final Field NIBBLE = lookup(FishingHook.class, "nibble", int.class);
    private static final Field BITING = lookup(FishingHook.class, "biting", boolean.class);
    private static final Field CURRENT_STATE = lookup(FishingHook.class, "currentState", Enum.class);
    private static final Field HOOKED_IN = lookup(FishingHook.class, "hookedIn", Entity.class);
    private static final Field LURE_SPEED = lookup(FishingHook.class, "lureSpeed", int.class);
    private static final Method ON_HIT_ENTITY = lookup(FishingHook.class, "onHitEntity", void.class, EntityHitResult.class);
    private static final Method ON_HIT_BLOCK = lookup(FishingHook.class, "onHitBlock", void.class, BlockHitResult.class);
    private static final Method CATCHING_FISH = lookup(FishingHook.class, "catchingFish", void.class, BlockPos.class);
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

    public static int timeUntilLured(FishingHook hook) {
        try {
            return TIME_UNTIL_LURED.getInt(hook);
        } catch (IllegalAccessException exception) {
            throw new IllegalStateException("Failed to read fishing hook lure time via reflection.", exception);
        }
    }

    public static void setTimeUntilLured(FishingHook hook, int value) {
        try {
            TIME_UNTIL_LURED.setInt(hook, value);
        } catch (IllegalAccessException exception) {
            throw new IllegalStateException("Failed to write fishing hook lure time via reflection.", exception);
        }
    }

    public static int nibble(FishingHook hook) {
        try {
            return NIBBLE.getInt(hook);
        } catch (IllegalAccessException exception) {
            throw new IllegalStateException("Failed to read fishing hook nibble time via reflection.", exception);
        }
    }

    public static void setNibble(FishingHook hook, int value) {
        try {
            NIBBLE.setInt(hook, value);
        } catch (IllegalAccessException exception) {
            throw new IllegalStateException("Failed to write fishing hook nibble time via reflection.", exception);
        }
    }

    public static boolean biting(FishingHook hook) {
        try {
            return BITING.getBoolean(hook);
        } catch (IllegalAccessException exception) {
            throw new IllegalStateException("Failed to read fishing hook biting state via reflection.", exception);
        }
    }

    public static void setBiting(FishingHook hook, boolean value) {
        try {
            BITING.setBoolean(hook, value);
        } catch (IllegalAccessException exception) {
            throw new IllegalStateException("Failed to write fishing hook biting state via reflection.", exception);
        }
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    public static void setCurrentState(FishingHook hook, String stateName) {
        try {
            Class<? extends Enum> enumClass = (Class<? extends Enum>) CURRENT_STATE.getType();
            CURRENT_STATE.set(hook, Enum.valueOf(enumClass, stateName));
        } catch (IllegalAccessException exception) {
            throw new IllegalStateException("Failed to write fishing hook internal state via reflection.", exception);
        }
    }

    public static boolean lureApplied(FishingHook hook) {
        try {
            return LURE_SPEED.getInt(hook) > 0;
        } catch (IllegalAccessException exception) {
            throw new IllegalStateException("Failed to read fishing hook lure bonus via reflection.", exception);
        }
    }

    public static void pullEntity(FishingHook hook, Entity entity) {
        try {
            PULL_ENTITY.invoke(hook, entity);
        } catch (IllegalAccessException | InvocationTargetException exception) {
            throw new IllegalStateException("Failed to pull a hooked entity via reflection.", exception);
        }
    }

    public static void onHitEntity(FishingHook hook, Entity entity) {
        try {
            ON_HIT_ENTITY.invoke(hook, new EntityHitResult(entity));
        } catch (IllegalAccessException | InvocationTargetException exception) {
            throw new IllegalStateException("Failed to invoke fishing hook entity-hit handling via reflection.", exception);
        }
    }

    public static void onHitBlock(FishingHook hook, BlockHitResult hitResult) {
        try {
            ON_HIT_BLOCK.invoke(hook, hitResult);
        } catch (IllegalAccessException | InvocationTargetException exception) {
            throw new IllegalStateException("Failed to invoke fishing hook block-hit handling via reflection.", exception);
        }
    }

    public static void catchingFish(FishingHook hook, BlockPos position) {
        try {
            CATCHING_FISH.invoke(hook, position);
        } catch (IllegalAccessException | InvocationTargetException exception) {
            throw new IllegalStateException("Failed to invoke fishing hook catch loop via reflection.", exception);
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
