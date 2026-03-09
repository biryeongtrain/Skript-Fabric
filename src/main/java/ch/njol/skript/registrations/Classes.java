package ch.njol.skript.registrations;

import ch.njol.skript.SkriptAPIException;
import ch.njol.skript.classes.ClassInfo;
import ch.njol.skript.lang.DefaultExpression;
import ch.njol.skript.lang.ParseContext;
import ch.njol.skript.log.ParseLogHandler;
import ch.njol.skript.log.SkriptLogger;
import ch.njol.skript.util.StringMode;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.lang.converter.ConverterInfo;
import org.skriptlang.skript.lang.converter.Converters;
import org.skriptlang.skript.lang.properties.Property;

public final class Classes {

    private static final Map<Class<?>, ClassInfo<?>> REGISTERED_INFOS = new ConcurrentHashMap<>();
    private static final Map<Class<?>, ClassInfo<?>> SUPER_CLASS_CACHE = new ConcurrentHashMap<>();
    private static final Map<String, ClassInfo<?>> INFOS_BY_CODE_NAME = new ConcurrentHashMap<>();
    private static final Map<String, List<ClassInfo<?>>> REGISTERED_LITERAL_PATTERNS = new ConcurrentHashMap<>();
    private static final CopyOnWriteArrayList<ClassInfo<?>> REGISTRATION_ORDER = new CopyOnWriteArrayList<>();
    private static volatile List<ClassInfo<?>> SORTED_INFOS = List.of();

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
        for (ClassInfo<?> info : getSortedClassInfos()) {
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
        SUPER_CLASS_CACHE.clear();
        SORTED_INFOS = List.of();
    }

    public static void clearClassInfos() {
        REGISTERED_INFOS.clear();
        SUPER_CLASS_CACHE.clear();
        INFOS_BY_CODE_NAME.clear();
        REGISTERED_LITERAL_PATTERNS.clear();
        REGISTRATION_ORDER.clear();
        SORTED_INFOS = List.of();
    }

    public static List<ClassInfo<?>> getClassInfos() {
        return getSortedClassInfos();
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
        return INFOS_BY_CODE_NAME.get(codeName);
    }

    public static @Nullable ClassInfo<?> getClassInfoFromUserInput(@Nullable String userInput) {
        if (userInput == null || userInput.isBlank()) {
            return null;
        }
        String lowerCaseInput = userInput.trim().toLowerCase(Locale.ENGLISH);
        for (ClassInfo<?> info : getSortedClassInfos()) {
            if (info.matchesUserInput(lowerCaseInput)) {
                return info;
            }
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

    public static @Nullable DefaultExpression<?> getDefaultExpression(String codeName) {
        return getClassInfo(codeName).getDefaultExpression();
    }

    public static <T> @Nullable DefaultExpression<T> getDefaultExpression(Class<T> type) {
        ClassInfo<T> info = getExactClassInfo(type);
        return info == null ? null : info.getDefaultExpression();
    }

    public static boolean isPluralClassInfoUserInput(@Nullable String userInput, @Nullable ClassInfo<?> classInfo) {
        if (userInput == null || userInput.isBlank() || classInfo == null) {
            return false;
        }
        String lowerCaseInput = userInput.trim().toLowerCase(Locale.ENGLISH);
        if (classInfo.matchesUserInput(lowerCaseInput)) {
            if (lowerCaseInput.endsWith("ies") && classInfo.matchesUserInput(lowerCaseInput.substring(0, lowerCaseInput.length() - 3) + "y")) {
                return true;
            }
            if (lowerCaseInput.endsWith("es") && classInfo.matchesUserInput(lowerCaseInput.substring(0, lowerCaseInput.length() - 2))) {
                return true;
            }
            if (lowerCaseInput.endsWith("s") && classInfo.matchesUserInput(lowerCaseInput.substring(0, lowerCaseInput.length() - 1))) {
                return true;
            }
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
            return toString((Object) null, StringMode.MESSAGE);
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
            // Upstream returns literal-pattern matches in registration order.
            // Do not reorder by class-info specificity/dependencies here.
            return List.copyOf(explicitMatches);
        }
        // Upstream behavior only considers explicitly registered literal patterns here.
        // Parser-based fallback belongs in Classes.parse(...) and getParser(...), not in pattern info lookup.
        return null;
    }

    @SuppressWarnings("unchecked")
    public static <T> @Nullable ClassInfo.Parser<? extends T> getParser(Class<T> type) {
        List<ClassInfo<?>> infos = getSortedClassInfos();
        for (int index = infos.size() - 1; index >= 0; index--) {
            ClassInfo<?> info = infos.get(index);
            ClassInfo.Parser<?> parser = info.getParser();
            if (parser != null && type.isAssignableFrom(info.getC())) {
                return (ClassInfo.Parser<? extends T>) parser;
            }
        }
        for (ConverterInfo<?, ?> converterInfo : Converters.getConverterInfos()) {
            if (!type.isAssignableFrom(converterInfo.getTo())) {
                continue;
            }
            for (int index = infos.size() - 1; index >= 0; index--) {
                ClassInfo<?> info = infos.get(index);
                ClassInfo.Parser<?> parser = info.getParser();
                if (parser == null || !converterInfo.getFrom().isAssignableFrom(info.getC())) {
                    continue;
                }
                return createConvertedParser(
                        (ClassInfo.Parser<Object>) parser,
                        (ConverterInfo<Object, ?>) converterInfo,
                        type
                );
            }
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    public static <T> @Nullable T parse(String text, Class<T> type, ParseContext context) {
        try (ParseLogHandler log = SkriptLogger.startParseLogHandler()) {
            T parsed = parseSimple(text, type, context);
            if (parsed != null) {
                log.printLog();
                return parsed;
            }

            for (ConverterInfo<?, ?> converterInfo : Converters.getConverterInfos()) {
                if (!type.isAssignableFrom(converterInfo.getTo())) {
                    continue;
                }

                clearParseLog(log);
                Object source = parseSimple(text, (Class<Object>) converterInfo.getFrom(), context);
                if (source == null) {
                    continue;
                }

                Object converted = ((ConverterInfo<Object, ?>) converterInfo).getConverter().convert(source);
                if (type.isInstance(converted)) {
                    log.printLog();
                    return (T) converted;
                }
            }

            log.printError();
            return null;
        }
    }

    public static <T> @Nullable T parseSimple(String text, Class<T> type, ParseContext context) {
        try (ParseLogHandler log = SkriptLogger.startParseLogHandler()) {
            T parsed = parsePrimitive(text, type);
            if (parsed != null) {
                log.printLog();
                return parsed;
            }

            ClassInfo<T> exactInfo = getExactClassInfo(type);
            parsed = parseWithClassInfo(text, type, context, exactInfo, log);
            if (parsed != null) {
                log.printLog();
                return parsed;
            }

            for (ClassInfo<?> info : getSortedClassInfos()) {
                if (info == exactInfo || !type.isAssignableFrom(info.getC())) {
                    continue;
                }

                parsed = parseWithClassInfo(text, type, context, info, log);
                if (parsed != null) {
                    log.printLog();
                    return parsed;
                }
            }

            log.printError();
            return null;
        }
    }

    @SuppressWarnings("unchecked")
    private static <T> @Nullable T parsePrimitive(String text, Class<T> type) {
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
        return null;
    }

    @SuppressWarnings("unchecked")
    private static <T> @Nullable T parseWithClassInfo(
            String text,
            Class<T> type,
            ParseContext context,
            @Nullable ClassInfo<?> info,
            ParseLogHandler log
    ) {
        if (info == null) {
            return null;
        }

        ClassInfo.Parser<?> parser = info.getParser();
        if (parser == null || !parser.canParse(context)) {
            return null;
        }

        clearParseLog(log);
        Object parsed = parser.parse(text, context);
        return type.isInstance(parsed) ? (T) parsed : null;
    }

    @SuppressWarnings("unchecked")
    public static <T> T clone(T value) {
        if (value == null) {
            return null;
        }
        if (value.getClass().isArray()) {
            int length = Array.getLength(value);
            Object clone = Array.newInstance(value.getClass().getComponentType(), length);
            for (int index = 0; index < length; index++) {
                Array.set(clone, index, clone(Array.get(value, index)));
            }
            @SuppressWarnings("unchecked")
            T cast = (T) clone;
            return cast;
        }
        if (value instanceof Cloneable) {
            try {
                return (T) value.getClass().getMethod("clone").invoke(value);
            } catch (ReflectiveOperationException ignored) {
            }
        }
        return value;
    }

    private static <F, T> ClassInfo.Parser<T> createConvertedParser(
            ClassInfo.Parser<? extends F> parser,
            ConverterInfo<F, ?> converterInfo,
            Class<T> type
    ) {
        return new ClassInfo.Parser<>() {
            @Override
            public boolean canParse(ParseContext context) {
                return parser.canParse(context);
            }

            @SuppressWarnings("unchecked")
            @Override
            public @Nullable T parse(String input, ParseContext context) {
                F parsed = parser.parse(input, context);
                if (parsed == null) {
                    return null;
                }
                Object converted = ((ConverterInfo<F, Object>) converterInfo).getConverter().convert(parsed);
                return type.isInstance(converted) ? (T) converted : null;
            }
        };
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

    private static List<ClassInfo<?>> orderBySortedClassInfos(List<ClassInfo<?>> matches) {
        Set<ClassInfo<?>> remaining = new LinkedHashSet<>(matches);
        List<ClassInfo<?>> ordered = new ArrayList<>(remaining.size());
        for (ClassInfo<?> info : getSortedClassInfos()) {
            if (remaining.remove(info)) {
                ordered.add(info);
            }
        }
        if (!remaining.isEmpty()) {
            ordered.addAll(remaining);
        }
        return List.copyOf(ordered);
    }

    private static List<ClassInfo<?>> getSortedClassInfos() {
        List<ClassInfo<?>> cached = SORTED_INFOS;
        if (!cached.isEmpty() || REGISTRATION_ORDER.isEmpty()) {
            return cached;
        }
        synchronized (Classes.class) {
            if (!SORTED_INFOS.isEmpty() || REGISTRATION_ORDER.isEmpty()) {
                return SORTED_INFOS;
            }
            SORTED_INFOS = sortClassInfos();
            return SORTED_INFOS;
        }
    }

    private static void clearParseLog(ParseLogHandler log) {
        log.clear();
        log.clearError();
    }

    private static List<ClassInfo<?>> sortClassInfos() {
        List<ClassInfo<?>> remaining = new ArrayList<>(REGISTRATION_ORDER);
        Map<String, ClassInfo<?>> byCodeName = new ConcurrentHashMap<>();
        for (ClassInfo<?> info : remaining) {
            byCodeName.put(info.getCodeName(), info);
        }

        Map<ClassInfo<?>, Set<String>> dependencies = new ConcurrentHashMap<>();
        for (ClassInfo<?> info : remaining) {
            dependencies.put(info, new LinkedHashSet<>(info.after()));
        }

        for (ClassInfo<?> info : remaining) {
            Set<String> before = info.before();
            if (before == null) {
                continue;
            }
            for (String codeName : before) {
                ClassInfo<?> target = byCodeName.get(codeName);
                if (target != null && target != info) {
                    dependencies.get(target).add(info.getCodeName());
                }
            }
        }

        for (ClassInfo<?> info : remaining) {
            for (ClassInfo<?> other : remaining) {
                if (info == other) {
                    continue;
                }
                if (info.getC().isAssignableFrom(other.getC())) {
                    dependencies.get(info).add(other.getCodeName());
                }
            }
        }

        for (ClassInfo<?> info : remaining) {
            dependencies.get(info).removeIf(codeName -> !byCodeName.containsKey(codeName) || info.getCodeName().equals(codeName));
        }

        List<ClassInfo<?>> sorted = new ArrayList<>(remaining.size());
        boolean changed = true;
        while (changed) {
            changed = false;
            for (int index = 0; index < remaining.size(); index++) {
                ClassInfo<?> info = remaining.get(index);
                if (!dependencies.get(info).isEmpty()) {
                    continue;
                }
                sorted.add(info);
                remaining.remove(index);
                for (Set<String> after : dependencies.values()) {
                    after.remove(info.getCodeName());
                }
                index--;
                changed = true;
            }
        }

        if (!remaining.isEmpty()) {
            String circular = remaining.stream()
                    .map(info -> info.getCodeName() + " (after: " + String.join(", ", dependencies.get(info)) + ")")
                    .collect(Collectors.joining(", "));
            throw new IllegalStateException("ClassInfos with circular dependencies detected: " + circular);
        }

        return List.copyOf(sorted);
    }
}
