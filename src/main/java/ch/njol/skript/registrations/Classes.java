package ch.njol.skript.registrations;

import ch.njol.skript.classes.ClassInfo;
import ch.njol.skript.lang.ParseContext;
import ch.njol.skript.util.StringMode;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.lang.properties.Property;

public final class Classes {

    private static final Map<Class<?>, ClassInfo<?>> INFOS = new ConcurrentHashMap<>();

    private Classes() {
    }

    @SuppressWarnings("unchecked")
    public static <T> ClassInfo<T> getSuperClassInfo(Class<T> type) {
        ClassInfo<?> exact = INFOS.get(type);
        if (exact != null) {
            return (ClassInfo<T>) exact;
        }
        for (Map.Entry<Class<?>, ClassInfo<?>> entry : INFOS.entrySet()) {
            if (entry.getKey().isAssignableFrom(type)) {
                return (ClassInfo<T>) entry.getValue();
            }
        }
        ClassInfo<T> info = new ClassInfo<>(type);
        INFOS.put(type, info);
        return info;
    }

    public static void registerClassInfo(ClassInfo<?> info) {
        INFOS.put(info.getC(), info);
    }

    public static void clearClassInfos() {
        INFOS.clear();
    }

    public static Set<ClassInfo<?>> getClassInfosByProperty(Property<?> property) {
        return INFOS.values().stream()
                .filter(info -> info.hasProperty(property))
                .collect(Collectors.toSet());
    }

    public static String toString(Object value, StringMode mode) {
        return value == null ? "null" : value.toString();
    }

    public static String toString(Object[] values, boolean and) {
        if (values == null || values.length == 0) {
            return "";
        }
        if (values.length == 1) {
            return String.valueOf(values[0]);
        }
        if (values.length == 2) {
            return values[0] + (and ? " and " : " or ") + values[1];
        }
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < values.length; i++) {
            if (i > 0) {
                if (i == values.length - 1) {
                    builder.append(and ? ", and " : ", or ");
                } else {
                    builder.append(", ");
                }
            }
            builder.append(values[i]);
        }
        return builder.toString();
    }

    public static @Nullable List<ClassInfo<?>> getPatternInfos(String text) {
        if (text == null || text.isBlank()) {
            return null;
        }
        List<ClassInfo<?>> matches = INFOS.values().stream()
                .filter(info -> {
                    ClassInfo.Parser<?> parser = info.getParser();
                    return parser != null
                            && parser.canParse(ParseContext.DEFAULT)
                            && parser.parse(text, ParseContext.DEFAULT) != null;
                })
                .toList();
        return matches.isEmpty() ? null : matches;
    }

    @SuppressWarnings("unchecked")
    public static <T> @Nullable T parse(String text, Class<T> type, ParseContext context) {
        if (type == String.class) {
            return (T) text;
        }
        if (type == Integer.class || type == int.class) {
            try {
                return (T) Integer.valueOf(text);
            } catch (NumberFormatException ignored) {
                return null;
            }
        }
        if (type == Double.class || type == double.class) {
            try {
                return (T) Double.valueOf(text);
            } catch (NumberFormatException ignored) {
                return null;
            }
        }
        if (type == Boolean.class || type == boolean.class) {
            if ("true".equalsIgnoreCase(text) || "false".equalsIgnoreCase(text)) {
                return (T) Boolean.valueOf(text);
            }
            return null;
        }

        ClassInfo<?> exactInfo = INFOS.get(type);
        if (exactInfo != null) {
            ClassInfo.Parser<?> parser = exactInfo.getParser();
            if (parser != null && parser.canParse(context)) {
                Object parsed = parser.parse(text, context);
                if (type.isInstance(parsed)) {
                    return (T) parsed;
                }
            }
        }

        for (ClassInfo<?> info : INFOS.values()) {
            if (!type.isAssignableFrom(info.getC())) {
                continue;
            }
            ClassInfo.Parser<?> parser = info.getParser();
            if (parser == null || !parser.canParse(context)) {
                continue;
            }
            Object parsed = parser.parse(text, context);
            if (type.isInstance(parsed)) {
                return (T) parsed;
            }
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    public static <T> T clone(T value) {
        if (value == null) {
            return null;
        }
        if (value instanceof Cloneable) {
            try {
                return (T) value.getClass().getMethod("clone").invoke(value);
            } catch (ReflectiveOperationException ignored) {
            }
        }
        return value;
    }
}
