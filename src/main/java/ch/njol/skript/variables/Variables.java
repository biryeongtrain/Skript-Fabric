package ch.njol.skript.variables;

import ch.njol.skript.lang.Variable;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Pattern;
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

    public static boolean caseInsensitiveVariables = true;
    private static final Pattern VARIABLE_NAME_SPLIT_PATTERN = Pattern.compile(Pattern.quote(Variable.SEPARATOR));

    private static final Comparator<String> VARIABLE_NAME_COMPARATOR = (first, second) -> {
        if (first == null) {
            return second == null ? 0 : -1;
        }
        if (second == null) {
            return 1;
        }

        int firstIndex = 0;
        int secondIndex = 0;
        boolean lastNumberNegative = false;
        boolean afterDecimalPoint = false;
        while (firstIndex < first.length() && secondIndex < second.length()) {
            char firstChar = first.charAt(firstIndex);
            char secondChar = second.charAt(secondIndex);
            if (Character.isDigit(firstChar) && Character.isDigit(secondChar)) {
                int firstEnd = findLastDigit(first, firstIndex);
                int secondEnd = findLastDigit(second, secondIndex);
                int firstLeadingZeros = 0;
                int secondLeadingZeros = 0;

                if (!afterDecimalPoint) {
                    while (firstIndex < firstEnd - 1 && first.charAt(firstIndex) == '0') {
                        firstIndex++;
                        firstLeadingZeros++;
                    }
                    while (secondIndex < secondEnd - 1 && second.charAt(secondIndex) == '0') {
                        secondIndex++;
                        secondLeadingZeros++;
                    }
                }

                boolean previousNegative = lastNumberNegative;
                lastNumberNegative = firstIndex - firstLeadingZeros > 0
                        && first.charAt(firstIndex - firstLeadingZeros - 1) == '-';
                int sign = (lastNumberNegative || previousNegative) ? -1 : 1;

                if (!afterDecimalPoint && firstEnd - firstIndex != secondEnd - secondIndex) {
                    return ((firstEnd - firstIndex) - (secondEnd - secondIndex)) * sign;
                }

                while (firstIndex < firstEnd && secondIndex < secondEnd) {
                    char firstDigit = first.charAt(firstIndex);
                    char secondDigit = second.charAt(secondIndex);
                    if (firstDigit != secondDigit) {
                        return (firstDigit - secondDigit) * sign;
                    }
                    firstIndex++;
                    secondIndex++;
                }

                if (afterDecimalPoint && firstEnd - firstIndex != secondEnd - secondIndex) {
                    return ((firstEnd - firstIndex) - (secondEnd - secondIndex)) * sign;
                }
                if (firstLeadingZeros != secondLeadingZeros) {
                    return (firstLeadingZeros - secondLeadingZeros) * sign;
                }

                afterDecimalPoint = true;
                continue;
            }

            if (firstChar != secondChar) {
                return firstChar - secondChar;
            }
            if (firstChar != '.') {
                lastNumberNegative = false;
                afterDecimalPoint = false;
            }
            firstIndex++;
            secondIndex++;
        }

        if (firstIndex < first.length()) {
            return lastNumberNegative ? -1 : 1;
        }
        if (secondIndex < second.length()) {
            return lastNumberNegative ? 1 : -1;
        }
        return 0;
    };

    private static final Map<String, Object> GLOBAL_VARIABLES = new ConcurrentHashMap<>();
    private static final Map<Object, Map<String, Object>> LOCAL_VARIABLES =
            Collections.synchronizedMap(new WeakHashMap<>());

    private Variables() {
    }

    public static @Nullable Object getVariable(String name, @Nullable SkriptEvent event, boolean local) {
        if (name == null || name.isBlank()) {
            return null;
        }
        String normalized = normalizeName(name);
        Map<String, Object> map = local ? localMap(event, false) : GLOBAL_VARIABLES;
        return getVariableFromMap(normalized, map);
    }

    public static void setVariable(String name, @Nullable Object value, @Nullable SkriptEvent event, boolean local) {
        if (name == null || name.isBlank()) {
            return;
        }
        String normalized = normalizeName(name);
        Map<String, Object> map = local ? localMap(event, true) : GLOBAL_VARIABLES;
        if (value == null) {
            map.remove(normalized);
        } else {
            map.put(normalized, value);
        }
    }

    public static void removePrefix(String prefix, @Nullable SkriptEvent event, boolean local) {
        if (prefix == null || prefix.isBlank()) {
            return;
        }
        String normalized = normalizeName(prefix);
        Map<String, Object> map = local ? localMap(event, false) : GLOBAL_VARIABLES;
        if (map.isEmpty()) {
            return;
        }
        map.keySet().removeIf(key -> key.startsWith(normalized));
    }

    public static Map<String, Object> getVariablesWithPrefix(String prefix, @Nullable SkriptEvent event, boolean local) {
        if (prefix == null || prefix.isBlank()) {
            return Map.of();
        }
        String normalized = normalizeName(prefix);
        Object rawValue = getVariableFromMap(normalized + "*", local ? localMap(event, false) : GLOBAL_VARIABLES);
        if (!(rawValue instanceof Map<?, ?> rawMap)) {
            return Map.of();
        }
        Map<String, Object> matches = new TreeMap<>(VARIABLE_NAME_COMPARATOR);
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

    public static void withLocalVariables(
            @Nullable SkriptEvent source,
            @Nullable SkriptEvent target,
            Runnable action
    ) {
        Object sourceKey = eventScopeKey(source);
        Object targetKey = eventScopeKey(target);
        if (sourceKey.equals(targetKey)) {
            action.run();
            return;
        }
        Map<String, Object> sourceLocals = LOCAL_VARIABLES.get(sourceKey);
        if (sourceLocals == null || sourceLocals.isEmpty()) {
            LOCAL_VARIABLES.remove(targetKey);
        } else {
            LOCAL_VARIABLES.put(targetKey, new ConcurrentHashMap<>(sourceLocals));
        }
        try {
            action.run();
        } finally {
            Map<String, Object> targetLocals = LOCAL_VARIABLES.get(targetKey);
            if (targetLocals == null || targetLocals.isEmpty()) {
                LOCAL_VARIABLES.remove(sourceKey);
            } else {
                LOCAL_VARIABLES.put(sourceKey, new ConcurrentHashMap<>(targetLocals));
            }
            LOCAL_VARIABLES.remove(targetKey);
        }
    }

    private static Map<String, Object> localMap(@Nullable SkriptEvent event, boolean create) {
        Object key = eventScopeKey(event);
        if (!create) {
            return LOCAL_VARIABLES.getOrDefault(key, Map.of());
        }
        return LOCAL_VARIABLES.computeIfAbsent(key, ignored -> new ConcurrentHashMap<>());
    }

    public static String[] splitVariableName(String name) {
        return VARIABLE_NAME_SPLIT_PATTERN.split(name);
    }

    private static Object eventScopeKey(@Nullable SkriptEvent event) {
        if (event == null) {
            return Variables.class;
        }
        return event.handle() != null ? event.handle() : event;
    }

    private static int findLastDigit(String input, int start) {
        int index = start;
        while (index < input.length() && Character.isDigit(input.charAt(index))) {
            index++;
        }
        return index;
    }

    private static @Nullable Object getVariableFromMap(String name, Map<String, Object> map) {
        if (!name.endsWith("*")) {
            return map.get(name);
        }

        String prefix = name.substring(0, name.length() - 1);
        TreeMap<String, Object> listValue = null;
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            if (!entry.getKey().startsWith(prefix)) {
                continue;
            }

            String suffix = entry.getKey().substring(prefix.length());
            if (suffix.isEmpty()) {
                continue;
            }
            if (listValue == null) {
                listValue = new TreeMap<>(VARIABLE_NAME_COMPARATOR);
            }
            insertListValue(listValue, suffix, entry.getValue());
        }
        return listValue;
    }

    @SuppressWarnings("unchecked")
    private static void insertListValue(TreeMap<String, Object> parent, String suffix, Object value) {
        String[] parts = splitVariableName(suffix);
        TreeMap<String, Object> current = parent;
        for (int index = 0; index < parts.length; index++) {
            String part = parts[index];
            boolean last = index == parts.length - 1;
            Object child = current.get(part);

            if (last) {
                if (child instanceof TreeMap<?, ?> childMap) {
                    ((TreeMap<String, Object>) childMap).put(null, value);
                } else {
                    current.put(part, value);
                }
                return;
            }

            if (child instanceof TreeMap<?, ?> childMap) {
                current = (TreeMap<String, Object>) childMap;
                continue;
            }

            TreeMap<String, Object> next = new TreeMap<>(VARIABLE_NAME_COMPARATOR);
            if (child != null) {
                next.put(null, child);
            }
            current.put(part, next);
            current = next;
        }
    }

    private static String normalizeName(String name) {
        if (!caseInsensitiveVariables) {
            return name;
        }
        return name.toLowerCase(Locale.ENGLISH);
    }
}
