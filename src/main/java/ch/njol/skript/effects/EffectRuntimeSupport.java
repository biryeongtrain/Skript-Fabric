package ch.njol.skript.effects;

import ch.njol.skript.registrations.Classes;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import ch.njol.skript.util.StringMode;
import java.util.Locale;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.fabric.compat.FabricLocation;
import org.skriptlang.skript.fabric.placeholder.SkriptTextPlaceholders;
import org.skriptlang.skript.lang.event.SkriptEvent;

final class EffectRuntimeSupport {

    private EffectRuntimeSupport() {
    }

    static ServerPlayer[] playersOrEvent(@Nullable ServerPlayer[] players, SkriptEvent event) {
        if (players != null && players.length > 0) {
            return players;
        }
        return event.player() == null ? new ServerPlayer[0] : new ServerPlayer[]{event.player()};
    }

    static FabricLocation locationOf(ServerPlayer player) {
        return new FabricLocation(player.level(), player.position());
    }

    static Component componentOf(@Nullable Object value, @Nullable SkriptEvent event) {
        if (value == null) {
            return Component.empty();
        }
        if (value instanceof String string) {
            return SkriptTextPlaceholders.resolveComponent(string, event);
        }
        return Component.literal(stringOf(value));
    }

    static String stringOf(@Nullable Object value) {
        if (value == null) {
            return "";
        }
        return value instanceof String string ? string : Classes.toString(value, StringMode.MESSAGE);
    }

    static @Nullable ResourceLocation parseResourceLocation(@Nullable String input) {
        if (input == null || input.isBlank()) {
            return null;
        }
        try {
            return input.contains(":")
                    ? ResourceLocation.parse(input)
                    : ResourceLocation.withDefaultNamespace(input);
        } catch (IllegalArgumentException ignored) {
            return null;
        }
    }

    static Holder<SoundEvent> soundOf(ResourceLocation id) {
        return BuiltInRegistries.SOUND_EVENT.get(id)
                .<Holder<SoundEvent>>map(reference -> reference)
                .orElseGet(() -> Holder.direct(SoundEvent.createVariableRangeEvent(id)));
    }

    static SoundSource soundSource(@Nullable String input) {
        if (input == null || input.isBlank()) {
            return SoundSource.MASTER;
        }
        String normalized = input.trim().toLowerCase(Locale.ENGLISH);
        for (SoundSource value : SoundSource.values()) {
            if (value.getName().equalsIgnoreCase(normalized) || value.name().equalsIgnoreCase(normalized)) {
                return value;
            }
        }
        return SoundSource.MASTER;
    }

    static ServerPlayer[] worldPlayers(@Nullable ServerLevel level) {
        if (level == null) {
            return new ServerPlayer[0];
        }
        return level.players().toArray(ServerPlayer[]::new);
    }

    static @Nullable Object invokeCompatible(@Nullable Object target, String methodName, Object... args) {
        return invokeCompatible(target, new String[]{methodName}, args);
    }

    static @Nullable Object invokeCompatible(@Nullable Object target, String[] methodNames, Object... args) {
        if (target == null) {
            return null;
        }
        Class<?> type = target.getClass();
        for (String methodName : methodNames) {
            Method method = findCompatibleMethod(type, methodName, args);
            if (method == null) {
                continue;
            }
            try {
                method.setAccessible(true);
                return method.invoke(target, args);
            } catch (ReflectiveOperationException ignored) {
            }
        }
        return null;
    }

    static boolean setBooleanField(@Nullable Object target, String fieldName, boolean value) {
        if (target == null) {
            return false;
        }
        Class<?> current = target.getClass();
        while (current != null) {
            try {
                Field field = current.getDeclaredField(fieldName);
                field.setAccessible(true);
                if (field.getType() == boolean.class || field.getType() == Boolean.class) {
                    field.setBoolean(target, value);
                    return true;
                }
            } catch (ReflectiveOperationException ignored) {
            }
            current = current.getSuperclass();
        }
        return false;
    }

    static boolean setField(@Nullable Object target, String fieldName, @Nullable Object value) {
        if (target == null) {
            return false;
        }
        Class<?> current = target.getClass();
        while (current != null) {
            try {
                Field field = current.getDeclaredField(fieldName);
                field.setAccessible(true);
                field.set(target, value);
                return true;
            } catch (ReflectiveOperationException ignored) {
            }
            current = current.getSuperclass();
        }
        return false;
    }

    private static @Nullable Method findCompatibleMethod(Class<?> type, String methodName, Object[] args) {
        for (Method method : type.getMethods()) {
            if (method.getName().equals(methodName) && isCompatible(method.getParameterTypes(), args)) {
                return method;
            }
        }
        Class<?> current = type;
        while (current != null) {
            for (Method method : current.getDeclaredMethods()) {
                if (method.getName().equals(methodName) && isCompatible(method.getParameterTypes(), args)) {
                    return method;
                }
            }
            current = current.getSuperclass();
        }
        return null;
    }

    private static boolean isCompatible(Class<?>[] parameterTypes, Object[] args) {
        if (parameterTypes.length != args.length) {
            return false;
        }
        for (int i = 0; i < parameterTypes.length; i++) {
            if (!isCompatible(parameterTypes[i], args[i])) {
                return false;
            }
        }
        return true;
    }

    private static boolean isCompatible(Class<?> parameterType, @Nullable Object argument) {
        if (argument == null) {
            return !parameterType.isPrimitive();
        }
        Class<?> wrapped = wrap(parameterType);
        return wrapped.isInstance(argument);
    }

    private static Class<?> wrap(Class<?> type) {
        if (!type.isPrimitive()) {
            return type;
        }
        if (type == boolean.class) {
            return Boolean.class;
        }
        if (type == byte.class) {
            return Byte.class;
        }
        if (type == short.class) {
            return Short.class;
        }
        if (type == int.class) {
            return Integer.class;
        }
        if (type == long.class) {
            return Long.class;
        }
        if (type == float.class) {
            return Float.class;
        }
        if (type == double.class) {
            return Double.class;
        }
        if (type == char.class) {
            return Character.class;
        }
        return type;
    }
}
