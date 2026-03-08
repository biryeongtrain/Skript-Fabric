package ch.njol.skript.registrations;

import ch.njol.skript.SkriptAPIException;
import ch.njol.skript.classes.ClassInfo;
import ch.njol.skript.lang.ParseContext;
import ch.njol.skript.util.StringMode;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.lang.properties.Property;

public final class Classes {

    private static final Map<Class<?>, ClassInfo<?>> REGISTERED_INFOS = new ConcurrentHashMap<>();
    private static final Map<Class<?>, ClassInfo<?>> SUPER_CLASS_CACHE = new ConcurrentHashMap<>();
    private static final Map<String, ClassInfo<?>> INFOS_BY_CODE_NAME = new ConcurrentHashMap<>();
    private static final Map<String, List<ClassInfo<?>>> REGISTERED_LITERAL_PATTERNS = new ConcurrentHashMap<>();
    private static final CopyOnWriteArrayList<ClassInfo<?>> REGISTRATION_ORDER = new CopyOnWriteArrayList<>();

    private Classes() {
    }

    @SuppressWarnings("unchecked")
    public static <T> ClassInfo<T> getSuperClassInfo(Class<T> type) {
        ClassInfo<?> exact = REGISTERED_INFOS.get(type);
        if (exact != null) {
            return (ClassInfo<T>) exact;
        }
        ClassInfo<?> cached = SUPER_CLASS_CACHE.get(type);
        if (cached != null) {
            return (ClassInfo<T>) cached;
        }
        for (ClassInfo<?> info : REGISTRATION_ORDER) {
            if (info.getC().isAssignableFrom(type)) {
                SUPER_CLASS_CACHE.put(type, info);
                return (ClassInfo<T>) info;
            }
        }
        ClassInfo<T> info = new ClassInfo<>(type);
        SUPER_CLASS_CACHE.put(type, info);
        return info;
    }

    public static void registerClassInfo(ClassInfo<?> info) {
        ClassInfo<?> existingByClass = REGISTERED_INFOS.putIfAbsent(info.getC(), info);
        if (existingByClass != null) {
            throw new IllegalArgumentException("Class info already registered for " + info.getC().getName());
        }
        ClassInfo<?> existingByCodeName = INFOS_BY_CODE_NAME.putIfAbsent(info.getCodeName(), info);
        if (existingByCodeName != null) {
            REGISTERED_INFOS.remove(info.getC(), info);
            throw new IllegalArgumentException(
                    "Code name '" + info.getCodeName() + "' is already used by " + existingByCodeName.getC().getName()
            );
        }
        REGISTRATION_ORDER.add(info);
        for (String pattern : info.getLiteralPatterns()) {
            REGISTERED_LITERAL_PATTERNS
                    .computeIfAbsent(pattern, ignored -> new CopyOnWriteArrayList<>())
                    .add(info);
        }
    }

    public static void clearClassInfos() {
        REGISTERED_INFOS.clear();
        SUPER_CLASS_CACHE.clear();
        INFOS_BY_CODE_NAME.clear();
        REGISTERED_LITERAL_PATTERNS.clear();
        REGISTRATION_ORDER.clear();
    }

    public static List<ClassInfo<?>> getClassInfos() {
        return List.copyOf(REGISTRATION_ORDER);
    }

    public static ClassInfo<?> getClassInfo(String codeName) {
        ClassInfo<?> info = getClassInfoNoError(codeName);
        if (info == null) {
            throw new SkriptAPIException("No class info found for " + codeName);
        }
        return info;
    }

    public static @Nullable ClassInfo<?> getClassInfoNoError(@Nullable String codeName) {
        if (codeName == null || codeName.isBlank()) {
            return null;
        }
        return INFOS_BY_CODE_NAME.get(codeName.toLowerCase(Locale.ENGLISH));
    }

    public static @Nullable ClassInfo<?> getClassInfoFromUserInput(@Nullable String userInput) {
        if (userInput == null || userInput.isBlank()) {
            return null;
        }
        String normalized = normalizeUserInput(userInput);
        ClassInfo<?> exact = INFOS_BY_CODE_NAME.get(normalized);
        if (exact != null) {
            return exact;
        }
        if (normalized.length() > 1 && normalized.endsWith("s")) {
            return INFOS_BY_CODE_NAME.get(normalized.substring(0, normalized.length() - 1));
        }
        return null;
    }

    public static boolean isPluralClassInfoUserInput(@Nullable String userInput, @Nullable ClassInfo<?> classInfo) {
        if (userInput == null || userInput.isBlank() || classInfo == null) {
            return false;
        }
        String normalized = normalizeUserInput(userInput);
        return normalized.length() > 1
                && normalized.endsWith("s")
                && normalizeUserInput(classInfo.getCodeName()).equals(normalized.substring(0, normalized.length() - 1));
    }

    @SuppressWarnings("unchecked")
    public static <T> @Nullable ClassInfo<T> getExactClassInfo(@Nullable Class<T> type) {
        return type == null ? null : (ClassInfo<T>) REGISTERED_INFOS.get(type);
    }

    public static Set<ClassInfo<?>> getClassInfosByProperty(Property<?> property) {
        return REGISTERED_INFOS.values().stream()
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
        String normalized = text.trim().toLowerCase(Locale.ENGLISH);
        List<ClassInfo<?>> explicitMatches = REGISTERED_LITERAL_PATTERNS.get(normalized);
        if (explicitMatches != null && !explicitMatches.isEmpty()) {
            return List.copyOf(explicitMatches);
        }
        List<ClassInfo<?>> matches = new ArrayList<>();
        for (ClassInfo<?> info : REGISTRATION_ORDER) {
            ClassInfo.Parser<?> parser = info.getParser();
            if (parser == null || !parser.canParse(ParseContext.DEFAULT) || parser.parse(text, ParseContext.DEFAULT) == null) {
                continue;
            }
            matches.add(info);
        }
        return matches.isEmpty() ? null : List.copyOf(matches);
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

        ClassInfo<?> exactInfo = REGISTERED_INFOS.get(type);
        if (exactInfo != null) {
            ClassInfo.Parser<?> parser = exactInfo.getParser();
            if (parser != null && parser.canParse(context)) {
                Object parsed = parser.parse(text, context);
                if (type.isInstance(parsed)) {
                    return (T) parsed;
                }
            }
        }

        for (ClassInfo<?> info : REGISTRATION_ORDER) {
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

    private static String normalizeUserInput(String input) {
        StringBuilder normalized = new StringBuilder(input.length());
        for (int i = 0; i < input.length(); i++) {
            char character = input.charAt(i);
            if (Character.isWhitespace(character) || character == '-' || character == '_') {
                continue;
            }
            normalized.append(Character.toLowerCase(character));
        }
        return normalized.toString();
    }
}
