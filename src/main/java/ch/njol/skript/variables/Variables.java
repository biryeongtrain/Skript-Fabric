package ch.njol.skript.variables;

import ch.njol.skript.lang.Variable;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.WeakHashMap;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.lang.event.SkriptEvent;

/**
 * Minimal variable storage bridge for legacy {@code ch.njol.skript.lang.Variable}.
 * Global and local variables are stored separately; local scope is keyed by event handle.
 */
public final class Variables {

    public static boolean caseInsensitiveVariables = true;
    private static final Map<Object, VariablesMap> LOCAL_VARIABLES =
            Collections.synchronizedMap(new WeakHashMap<>());
    private static final VariablesMap GLOBAL_VARIABLES = new VariablesMap();

    private Variables() {
    }

    public static @Nullable Object getVariable(String name, @Nullable SkriptEvent event, boolean local) {
        if (name == null || name.isBlank()) {
            return null;
        }
        String normalized = normalizeName(name);
        VariablesMap map = local ? localMap(event, false) : GLOBAL_VARIABLES;
        synchronized (map) {
            return map.getVariable(normalized);
        }
    }

    public static void setVariable(String name, @Nullable Object value, @Nullable SkriptEvent event, boolean local) {
        if (name == null || name.isBlank()) {
            return;
        }
        String normalized = normalizeName(name);
        if (normalized.endsWith(Variable.SEPARATOR + "*") && value == null) {
            removePrefix(normalized.substring(0, normalized.length() - 1), event, local);
            return;
        }
        VariablesMap map = local ? localMap(event, true) : GLOBAL_VARIABLES;
        synchronized (map) {
            map.setVariable(normalized, value);
        }
    }

    public static void removePrefix(String prefix, @Nullable SkriptEvent event, boolean local) {
        if (prefix == null || prefix.isBlank()) {
            return;
        }
        String normalized = normalizeName(prefix);
        VariablesMap map = local ? localMap(event, false) : GLOBAL_VARIABLES;
        synchronized (map) {
            map.setVariable(normalized + "*", null);
        }
    }

    public static Map<String, Object> getVariablesWithPrefix(String prefix, @Nullable SkriptEvent event, boolean local) {
        if (prefix == null || prefix.isBlank()) {
            return Map.of();
        }
        String normalized = normalizeName(prefix);
        VariablesMap map = local ? localMap(event, false) : GLOBAL_VARIABLES;
        Object rawValue;
        synchronized (map) {
            rawValue = map.getVariable(normalized + "*");
        }
        if (!(rawValue instanceof Map<?, ?> rawMap)) {
            return Map.of();
        }
        Map<String, Object> matches = new java.util.TreeMap<>(VariablesMap.VARIABLE_NAME_COMPARATOR);
        for (Map.Entry<?, ?> entry : rawMap.entrySet()) {
            if (!(entry.getKey() instanceof String childKey)) {
                continue;
            }
            Object childValue = entry.getValue();
            if (childValue instanceof Map<?, ?> sublist) {
                childValue = sublist.get(null);
            }
            if (childValue != null) {
                matches.put(normalized + childKey, childValue);
            }
        }
        return new LinkedHashMap<>(matches);
    }

    public static void clearAll() {
        GLOBAL_VARIABLES.clear();
        LOCAL_VARIABLES.clear();
    }

    public static @Nullable Object removeLocals(@Nullable SkriptEvent event) {
        Object key = eventScopeKey(event);
        synchronized (LOCAL_VARIABLES) {
            VariablesMap removed = LOCAL_VARIABLES.remove(key);
            return removed == null ? null : removed.copy();
        }
    }

    public static void setLocalVariables(@Nullable SkriptEvent event, @Nullable Object variables) {
        Object key = eventScopeKey(event);
        synchronized (LOCAL_VARIABLES) {
            if (!(variables instanceof VariablesMap map)) {
                LOCAL_VARIABLES.remove(key);
                return;
            }
            LOCAL_VARIABLES.put(key, map.copy());
        }
    }

    public static void withLocalVariables(
            @Nullable SkriptEvent source,
            @Nullable SkriptEvent target,
            Runnable action
    ) {
        Object sourceKey = eventScopeKey(source);
        Object targetKey = eventScopeKey(target);
        VariablesMap sourceLocals;
        synchronized (LOCAL_VARIABLES) {
            sourceLocals = LOCAL_VARIABLES.get(sourceKey);
            if (sourceLocals == null
                    || (sourceLocals.hashMap.isEmpty() && sourceLocals.treeMap.isEmpty())) {
                LOCAL_VARIABLES.remove(targetKey);
            } else {
                LOCAL_VARIABLES.put(targetKey, sourceLocals.copy());
            }
        }
        try {
            action.run();
        } finally {
            synchronized (LOCAL_VARIABLES) {
                VariablesMap targetLocals = LOCAL_VARIABLES.get(targetKey);
                if (targetLocals == null
                        || (targetLocals.hashMap.isEmpty() && targetLocals.treeMap.isEmpty())) {
                    LOCAL_VARIABLES.remove(sourceKey);
                } else {
                    LOCAL_VARIABLES.put(sourceKey, targetLocals.copy());
                }
                LOCAL_VARIABLES.remove(targetKey);
            }
        }
    }

    private static VariablesMap localMap(@Nullable SkriptEvent event, boolean create) {
        Object key = eventScopeKey(event);
        synchronized (LOCAL_VARIABLES) {
            if (!create) {
                VariablesMap map = LOCAL_VARIABLES.get(key);
                return map == null ? new VariablesMap() : map;
            }
            return LOCAL_VARIABLES.computeIfAbsent(key, ignored -> new VariablesMap());
        }
    }

    public static String[] splitVariableName(String name) {
        return name.split(java.util.regex.Pattern.quote(Variable.SEPARATOR));
    }

    private static Object eventScopeKey(@Nullable SkriptEvent event) {
        if (event == null) {
            return Variables.class;
        }
        return event.handle() != null ? event.handle() : event;
    }

    private static String normalizeName(String name) {
        if (!caseInsensitiveVariables) {
            return name;
        }
        return name.toLowerCase(Locale.ENGLISH);
    }
}
