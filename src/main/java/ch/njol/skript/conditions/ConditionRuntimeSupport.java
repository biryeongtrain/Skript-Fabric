package ch.njol.skript.conditions;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.fabric.runtime.SkriptRuntime;
import org.skriptlang.skript.lang.script.Script;

final class ConditionRuntimeSupport {

    private ConditionRuntimeSupport() {
    }

    static @Nullable Object invokeCompatible(@Nullable Object target, String... methodNames) {
        if (target == null) {
            return null;
        }
        Class<?> current = target.getClass();
        for (String methodName : methodNames) {
            Method method = findCompatibleMethod(current, methodName);
            if (method == null) {
                continue;
            }
            try {
                method.setAccessible(true);
                return method.invoke(target);
            } catch (ReflectiveOperationException ignored) {
            }
        }
        return null;
    }

    static boolean booleanMethod(@Nullable Object target, boolean fallback, String... methodNames) {
        Object result = invokeCompatible(target, methodNames);
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

    private static @Nullable Method findCompatibleMethod(Class<?> type, String methodName) {
        for (Method method : type.getMethods()) {
            if (method.getName().equals(methodName) && method.getParameterCount() == 0) {
                return method;
            }
        }
        Class<?> current = type;
        while (current != null) {
            for (Method method : current.getDeclaredMethods()) {
                if (method.getName().equals(methodName) && method.getParameterCount() == 0) {
                    return method;
                }
            }
            current = current.getSuperclass();
        }
        return null;
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
