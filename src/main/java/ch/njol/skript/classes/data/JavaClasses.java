package ch.njol.skript.classes.data;

import ch.njol.skript.classes.ClassInfo;
import ch.njol.skript.classes.Parser;
import ch.njol.skript.lang.ParseContext;
import ch.njol.skript.lang.util.SimpleLiteral;
import ch.njol.skript.localization.Language;
import ch.njol.skript.registrations.Classes;
import java.math.BigDecimal;
import java.util.Locale;
import java.util.UUID;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.jetbrains.annotations.Nullable;

/**
 * Serializer-free subset of upstream Java class registrations.
 *
 * <p>This keeps the old parser/class-info surface available without depending
 * on Bukkit registrations or the removed Yggdrasil serializer stack.</p>
 */
public final class JavaClasses {

    public static final int VARIABLENAME_NUMBERACCURACY = 8;
    public static final String SCIENTIFIC_PATTERN = "(?:[eE][+-]?\\d+)?";
    public static final String INTEGER_NUMBER_PATTERN = "-?\\d+(_\\d+)*";
    public static final String DECIMAL_NUMBER_PATTERN = "-?\\d+(_\\d+)*(?>\\.\\d+(_\\d+)*)?%?";
    public static final Pattern INTEGER_PATTERN =
            Pattern.compile("(?<num>%s%s)(?: (?:in )?(?:(?<rad>rad(?:ian)?)|deg(?:ree)?)s?)?"
                    .formatted(INTEGER_NUMBER_PATTERN, SCIENTIFIC_PATTERN));
    public static final Pattern DECIMAL_PATTERN =
            Pattern.compile("(?<num>%s%s)(?: (?:in )?(?:(?<rad>rad(?:ian)?)|deg(?:ree)?)s?)?"
                    .formatted(DECIMAL_NUMBER_PATTERN, SCIENTIFIC_PATTERN));

    private JavaClasses() {
    }

    public static void register() {
        registerObjectClass();
        registerNumberClasses();
        registerBooleanClass();
        registerStringClass();
        registerUuidClass();
    }

    private static void registerObjectClass() {
        if (Classes.getExactClassInfo(Object.class) != null) {
            return;
        }
        Classes.registerClassInfo(new ClassInfo<>(Object.class, "object")
                .user("objects?")
                .name("Object")
                .description("The supertype of all registered values.")
                .since("1.0"));
    }

    private static void registerNumberClasses() {
        registerIfMissing(Number.class, new ClassInfo<>(Number.class, "number")
                .user("num(ber)?s?")
                .name("Number")
                .defaultExpression(new SimpleLiteral<>(1, true))
                .parser(new NumberParser()));
        registerIfMissing(Long.class, new ClassInfo<>(Long.class, "long")
                .user("int(eger)?s?")
                .name(ClassInfo.NO_DOC)
                .before("integer", "short", "byte")
                .defaultExpression(new SimpleLiteral<>(1L, true))
                .parser(new WholeNumberParser<>(Long.class, Long::parseLong)));
        registerIfMissing(Integer.class, new ClassInfo<>(Integer.class, "integer")
                .name(ClassInfo.NO_DOC)
                .defaultExpression(new SimpleLiteral<>(1, true))
                .parser(new WholeNumberParser<>(Integer.class, Integer::parseInt)));
        registerIfMissing(Double.class, new ClassInfo<>(Double.class, "double")
                .name(ClassInfo.NO_DOC)
                .defaultExpression(new SimpleLiteral<>(1D, true))
                .after("long")
                .before("float", "integer", "short", "byte")
                .parser(new DecimalNumberParser<>(Double.class, Double::parseDouble)));
        registerIfMissing(Float.class, new ClassInfo<>(Float.class, "float")
                .name(ClassInfo.NO_DOC)
                .defaultExpression(new SimpleLiteral<>(1F, true))
                .parser(new DecimalNumberParser<>(Float.class, Float::parseFloat)));
        registerIfMissing(Short.class, new ClassInfo<>(Short.class, "short")
                .name(ClassInfo.NO_DOC)
                .defaultExpression(new SimpleLiteral<>((short) 1, true))
                .parser(new WholeNumberParser<>(Short.class, Short::parseShort)));
        registerIfMissing(Byte.class, new ClassInfo<>(Byte.class, "byte")
                .name(ClassInfo.NO_DOC)
                .defaultExpression(new SimpleLiteral<>((byte) 1, true))
                .parser(new WholeNumberParser<>(Byte.class, Byte::parseByte)));
    }

    private static void registerBooleanClass() {
        registerIfMissing(Boolean.class, new ClassInfo<>(Boolean.class, "boolean")
                .user("booleans?")
                .name("Boolean")
                .parser(new BooleanParser()));
    }

    private static void registerStringClass() {
        registerIfMissing(String.class, new ClassInfo<>(String.class, "string")
                .user("(text|string)s?")
                .name("Text")
                .parser(new StringParser()));
    }

    private static void registerUuidClass() {
        registerIfMissing(UUID.class, new ClassInfo<>(UUID.class, "uuid")
                .user("uuids?")
                .name("UUID")
                .parser(new UuidParser()));
    }

    private static <T> void registerIfMissing(Class<T> type, ClassInfo<T> info) {
        if (Classes.getExactClassInfo(type) == null) {
            Classes.registerClassInfo(info);
        }
    }

    private static @Nullable String normalizedNumber(String input, Pattern pattern) {
        Matcher matcher = pattern.matcher(input);
        if (!matcher.matches() || matcher.group("rad") != null) {
            return null;
        }
        return matcher.group("num").replace("_", "");
    }

    private static @Nullable Double parseDecimal(String input) {
        Matcher matcher = DECIMAL_PATTERN.matcher(input);
        if (!matcher.matches()) {
            return null;
        }
        String number = matcher.group("num").replace("_", "");
        try {
            double parsed;
            if (number.endsWith("%")) {
                parsed = Double.parseDouble(number.substring(0, number.length() - 1)) / 100.0D;
            } else {
                parsed = Double.parseDouble(number);
            }
            if (matcher.group("rad") != null) {
                parsed = Math.toDegrees(parsed);
            }
            return Double.isFinite(parsed) ? parsed : null;
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    private static String formatNumber(Number number) {
        if (number instanceof Byte || number instanceof Short || number instanceof Integer || number instanceof Long) {
            return number.toString();
        }
        BigDecimal decimal = BigDecimal.valueOf(number.doubleValue()).stripTrailingZeros();
        return decimal.scale() < 0 ? decimal.setScale(0).toPlainString() : decimal.toPlainString();
    }

    private static boolean isQuotedCorrectly(String input) {
        if (input.length() < 2 || input.charAt(0) != '"' || input.charAt(input.length() - 1) != '"') {
            return false;
        }
        for (int index = 1; index < input.length() - 1; index++) {
            if (input.charAt(index) != '"') {
                continue;
            }
            if (index + 1 >= input.length() - 1 || input.charAt(index + 1) != '"') {
                return false;
            }
            index++;
        }
        return true;
    }

    private static final class NumberParser extends Parser<Number> {

        @Override
        public @Nullable Number parse(String input, ParseContext context) {
            String normalizedInteger = normalizedNumber(input, INTEGER_PATTERN);
            if (normalizedInteger != null) {
                try {
                    return Integer.parseInt(normalizedInteger);
                } catch (NumberFormatException ignored) {
                }
            }
            return parseDecimal(input);
        }

        @Override
        public String toString(Number object, int flags) {
            return formatNumber(object);
        }

        @Override
        public String toVariableNameString(Number object) {
            return formatNumber(object);
        }
    }

    private static final class WholeNumberParser<T extends Number> extends Parser<T> {

        private final Class<T> type;
        private final Function<String, T> parser;

        private WholeNumberParser(Class<T> type, Function<String, T> parser) {
            this.type = type;
            this.parser = parser;
        }

        @Override
        public @Nullable T parse(String input, ParseContext context) {
            String normalized = normalizedNumber(input, INTEGER_PATTERN);
            if (normalized == null) {
                return null;
            }
            try {
                return parser.apply(normalized);
            } catch (NumberFormatException ex) {
                return null;
            }
        }

        @Override
        public String toString(T object, int flags) {
            return type.cast(object).toString();
        }

        @Override
        public String toVariableNameString(T object) {
            return type.cast(object).toString();
        }
    }

    private static final class DecimalNumberParser<T extends Number> extends Parser<T> {

        private final Class<T> type;
        private final Function<String, T> parser;

        private DecimalNumberParser(Class<T> type, Function<String, T> parser) {
            this.type = type;
            this.parser = parser;
        }

        @Override
        public @Nullable T parse(String input, ParseContext context) {
            Double parsed = parseDecimal(input);
            if (parsed == null) {
                return null;
            }
            try {
                T converted = parser.apply(Double.toString(parsed));
                return Double.isFinite(converted.doubleValue()) ? converted : null;
            } catch (NumberFormatException ex) {
                return null;
            }
        }

        @Override
        public String toString(T object, int flags) {
            return formatNumber(type.cast(object));
        }

        @Override
        public String toVariableNameString(T object) {
            return formatNumber(type.cast(object));
        }
    }

    private static final class BooleanParser extends Parser<Boolean> {

        private static final String DEFAULT_TRUE_PATTERN = "true|yes|on";
        private static final String DEFAULT_FALSE_PATTERN = "false|no|off";

        @Override
        public @Nullable Boolean parse(String input, ParseContext context) {
            if (matches("boolean.true.pattern", DEFAULT_TRUE_PATTERN, input)) {
                return Boolean.TRUE;
            }
            if (matches("boolean.false.pattern", DEFAULT_FALSE_PATTERN, input)) {
                return Boolean.FALSE;
            }
            return null;
        }

        @Override
        public String toString(Boolean object, int flags) {
            return object
                    ? localizedValue("boolean.true.name", "true")
                    : localizedValue("boolean.false.name", "false");
        }

        @Override
        public String toVariableNameString(Boolean object) {
            return object.toString();
        }

        private static boolean matches(String key, String fallback, String input) {
            String pattern = localizedValue(key, fallback);
            return Pattern.compile(pattern, Pattern.CASE_INSENSITIVE).matcher(input).matches();
        }

        private static String localizedValue(String key, String fallback) {
            String localized = Language.get_(key);
            return localized == null || localized.isBlank() ? fallback : localized;
        }
    }

    private static final class StringParser extends Parser<String> {

        @Override
        public @Nullable String parse(String input, ParseContext context) {
            return switch (context) {
                case CONFIG, COMMAND, PARSE -> input;
                case SCRIPT, EVENT -> isQuotedCorrectly(input)
                        ? input.substring(1, input.length() - 1).replace("\"\"", "\"")
                        : null;
                case DEFAULT -> null;
            };
        }

        @Override
        public boolean canParse(ParseContext context) {
            return context != ParseContext.DEFAULT;
        }

        @Override
        public String toString(String object, int flags) {
            return object;
        }

        @Override
        public String getDebugMessage(String object) {
            return '"' + object + '"';
        }

        @Override
        public String toVariableNameString(String object) {
            return object;
        }
    }

    private static final class UuidParser extends Parser<UUID> {

        @Override
        public @Nullable UUID parse(String input, ParseContext context) {
            try {
                return UUID.fromString(input.toLowerCase(Locale.ENGLISH));
            } catch (IllegalArgumentException ex) {
                return null;
            }
        }

        @Override
        public String toString(UUID object, int flags) {
            return object.toString();
        }

        @Override
        public String toVariableNameString(UUID object) {
            return object.toString();
        }
    }
}
