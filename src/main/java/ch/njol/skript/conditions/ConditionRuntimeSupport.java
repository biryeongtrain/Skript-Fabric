package ch.njol.skript.conditions;

import ch.njol.skript.entity.EntityData;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import net.minecraft.world.entity.EntityType;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.fabric.runtime.SkriptRuntime;
import org.skriptlang.skript.lang.script.Script;

final class ConditionRuntimeSupport {

    private ConditionRuntimeSupport() {
    }

    static @Nullable Object invokeCompatible(@Nullable Object target, String... methodNames) {
        return invokeCompatible(target, new Object[0], methodNames);
    }

    static @Nullable Object invokeCompatible(@Nullable Object target, Object[] args, String... methodNames) {
        if (target == null) {
            return null;
        }
        Class<?> current = target.getClass();
        for (String methodName : methodNames) {
            Method method = findCompatibleMethod(current, methodName, args);
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

    static boolean booleanMethod(@Nullable Object target, boolean fallback, String... methodNames) {
        Object result = invokeCompatible(target, methodNames);
        return result instanceof Boolean value ? value : fallback;
    }

    static boolean booleanMethod(@Nullable Object target, Object[] args, boolean fallback, String... methodNames) {
        Object result = invokeCompatible(target, args, methodNames);
        return result instanceof Boolean value ? value : fallback;
    }

    static boolean booleanField(@Nullable Object target, boolean fallback, String... fieldNames) {
        if (target == null) {
            return fallback;
        }
        Class<?> current = target.getClass();
        while (current != null) {
            for (String fieldName : fieldNames) {
                try {
                    Field field = current.getDeclaredField(fieldName);
                    field.setAccessible(true);
                    Object value = field.get(target);
                    if (value instanceof Boolean bool) {
                        return bool;
                    }
                } catch (ReflectiveOperationException ignored) {
                }
            }
            current = current.getSuperclass();
        }
        return fallback;
    }

    static List<Script> loadedScripts() {
        try {
            Field field = SkriptRuntime.class.getDeclaredField("scripts");
            field.setAccessible(true);
            Object value = field.get(SkriptRuntime.instance());
            if (value instanceof List<?> list) {
                List<Script> scripts = new ArrayList<>(list.size());
                for (Object entry : list) {
                    if (entry instanceof Script script) {
                        scripts.add(script);
                    }
                }
                return scripts;
            }
        } catch (ReflectiveOperationException ignored) {
        }
        return List.of();
    }

    static boolean isLoadedScript(@Nullable Script script) {
        return script != null && loadedScripts().contains(script);
    }

    static boolean isLoadedScriptNamed(String input) {
        if (input == null || input.isBlank()) {
            return false;
        }
        String normalized = normalizeScriptName(input);
        for (Script script : loadedScripts()) {
            if (matchesScriptName(script, normalized)) {
                return true;
            }
        }
        return false;
    }

    static String normalizeToken(String input) {
        return input.toLowerCase(Locale.ENGLISH)
                .replace('-', ' ')
                .replace('_', ' ')
                .replaceAll("\\s+", " ")
                .trim();
    }

    static boolean hasEnabledMod(@Nullable String input) {
        if (input == null || input.isBlank()) {
            return false;
        }
        FabricLoader loader = FabricLoader.getInstance();
        String normalized = normalizeToken(input);
        String compact = normalized.replace(" ", "");
        String dashed = normalized.replace(' ', '-');
        if (loader.isModLoaded(normalized) || loader.isModLoaded(compact) || loader.isModLoaded(dashed)) {
            return true;
        }
        for (ModContainer container : loader.getAllMods()) {
            if (normalized.equals(normalizeToken(container.getMetadata().getId()))
                    || normalized.equals(normalizeToken(container.getMetadata().getName()))) {
                return true;
            }
        }
        return false;
    }

    static boolean isSpawnable(@Nullable EntityData<?> entityData) {
        EntityType<?> entityType = exactEntityType(entityData);
        return entityType != null && entityType.canSummon();
    }

    private static @Nullable EntityType<?> exactEntityType(@Nullable EntityData<?> entityData) {
        Object result = invokeCompatible(entityData, "getMinecraftType");
        return result instanceof EntityType<?> entityType ? entityType : null;
    }

    private static @Nullable Method findCompatibleMethod(Class<?> type, String methodName, Object[] args) {
        for (Method method : type.getMethods()) {
            if (isCompatibleMethod(method, methodName, args)) {
                return method;
            }
        }
        Class<?> current = type;
        while (current != null) {
            for (Method method : current.getDeclaredMethods()) {
                if (isCompatibleMethod(method, methodName, args)) {
                    return method;
                }
            }
            current = current.getSuperclass();
        }
        return null;
    }

    private static boolean isCompatibleMethod(Method method, String methodName, Object[] args) {
        if (!method.getName().equals(methodName) || method.getParameterCount() != args.length) {
            return false;
        }
        Class<?>[] parameterTypes = method.getParameterTypes();
        for (int i = 0; i < parameterTypes.length; i++) {
            Object arg = args[i];
            if (arg == null) {
                continue;
            }
            if (!wrap(parameterTypes[i]).isInstance(arg)) {
                return false;
            }
        }
        return true;
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

    private static boolean matchesScriptName(Script script, String normalized) {
        return normalized.equals(normalizeScriptName(script.name()))
                || normalized.equals(normalizeScriptName(script.nameAndPath()))
                || normalized.equals(normalizeScriptName(script.getConfig().getFileName()));
    }

    private static String normalizeScriptName(@Nullable String name) {
        if (name == null) {
            return "";
        }
        String normalized = name.replace('\\', '/').trim();
        if (normalized.endsWith(".sk")) {
            normalized = normalized.substring(0, normalized.length() - 3);
        }
        return normalized.toLowerCase(Locale.ENGLISH);
    }
}
