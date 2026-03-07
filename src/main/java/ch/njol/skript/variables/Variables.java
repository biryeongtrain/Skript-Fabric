package ch.njol.skript.variables;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.TreeMap;
import java.util.WeakHashMap;
import java.util.concurrent.ConcurrentHashMap;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.lang.event.SkriptEvent;

/**
 * Minimal variable storage bridge for legacy {@code ch.njol.skript.lang.Variable}.
 * Global and local variables are stored separately; local scope is keyed by event handle.
 */
public final class Variables {

    private static final Map<String, Object> GLOBAL_VARIABLES = new ConcurrentHashMap<>();
    private static final Map<Object, Map<String, Object>> LOCAL_VARIABLES =
            Collections.synchronizedMap(new WeakHashMap<>());

    private Variables() {
    }

    public static @Nullable Object getVariable(String name, @Nullable SkriptEvent event, boolean local) {
        if (name == null || name.isBlank()) {
            return null;
        }
        return local ? localMap(event, false).get(name) : GLOBAL_VARIABLES.get(name);
    }

    public static void setVariable(String name, @Nullable Object value, @Nullable SkriptEvent event, boolean local) {
        if (name == null || name.isBlank()) {
            return;
        }
        Map<String, Object> map = local ? localMap(event, true) : GLOBAL_VARIABLES;
        if (value == null) {
            map.remove(name);
        } else {
            map.put(name, value);
        }
    }

    public static void removePrefix(String prefix, @Nullable SkriptEvent event, boolean local) {
        if (prefix == null || prefix.isBlank()) {
            return;
        }
        Map<String, Object> map = local ? localMap(event, false) : GLOBAL_VARIABLES;
        if (map.isEmpty()) {
            return;
        }
        map.keySet().removeIf(key -> key.startsWith(prefix));
    }

    public static Map<String, Object> getVariablesWithPrefix(String prefix, @Nullable SkriptEvent event, boolean local) {
        if (prefix == null || prefix.isBlank()) {
            return Map.of();
        }
        Map<String, Object> map = local ? localMap(event, false) : GLOBAL_VARIABLES;
        Map<String, Object> matches = new TreeMap<>();
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            if (entry.getKey().startsWith(prefix)) {
                matches.put(entry.getKey(), entry.getValue());
            }
        }
        return new LinkedHashMap<>(matches);
    }

    public static void clearAll() {
        GLOBAL_VARIABLES.clear();
        LOCAL_VARIABLES.clear();
    }

    public static void withLocalVariables(
            @Nullable SkriptEvent source,
            @Nullable SkriptEvent target,
            Runnable action
    ) {
        Object sourceKey = eventScopeKey(source);
        Object targetKey = eventScopeKey(target);
        Map<String, Object> sourceLocals = LOCAL_VARIABLES.get(sourceKey);
        Map<String, Object> backup = LOCAL_VARIABLES.get(targetKey);
        if (sourceLocals != null && !sourceLocals.isEmpty()) {
            LOCAL_VARIABLES.put(targetKey, new ConcurrentHashMap<>(sourceLocals));
        } else {
            LOCAL_VARIABLES.remove(targetKey);
        }
        try {
            action.run();
        } finally {
            if (backup == null || backup.isEmpty()) {
                LOCAL_VARIABLES.remove(targetKey);
            } else {
                LOCAL_VARIABLES.put(targetKey, backup);
            }
        }
    }

    private static Map<String, Object> localMap(@Nullable SkriptEvent event, boolean create) {
        Object key = eventScopeKey(event);
        if (!create) {
            return LOCAL_VARIABLES.getOrDefault(key, Map.of());
        }
        return LOCAL_VARIABLES.computeIfAbsent(key, ignored -> new ConcurrentHashMap<>());
    }

    private static Object eventScopeKey(@Nullable SkriptEvent event) {
        if (event == null) {
            return Variables.class;
        }
        return event.handle() != null ? event.handle() : event;
    }
}
