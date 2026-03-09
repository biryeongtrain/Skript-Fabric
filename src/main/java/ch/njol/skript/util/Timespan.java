package ch.njol.skript.util;

import ch.njol.skript.lang.ParseContext;
import ch.njol.skript.localization.GeneralWords;
import ch.njol.skript.localization.Language;
import ch.njol.skript.localization.Noun;
import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.time.temporal.Temporal;
import java.time.temporal.TemporalAmount;
import java.time.temporal.TemporalUnit;
import java.time.temporal.UnsupportedTemporalTypeException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class Timespan implements Comparable<Timespan>, TemporalAmount {

    private static final Pattern CLOCK_PATTERN = Pattern.compile("^(\\d+):(\\d\\d)(:\\d\\d){0,2}(?:\\.(\\d{1,4}))?$");
    private static final Pattern NUMBER_PATTERN = Pattern.compile("^\\d+(?:\\.\\d+)?$");
    private static final Pattern SHORT_FORM_PATTERN = Pattern.compile("^(\\d+(?:\\.\\d+)?)([a-zA-Z]+)$");
    private static final Pattern SPLIT_PATTERN = Pattern.compile("[:.]");
    private static final Noun FOREVER_NAME = new Noun("time.forever");
    private static final List<TimePeriod> DISPLAY_PERIODS = List.of(
            TimePeriod.YEAR,
            TimePeriod.MONTH,
            TimePeriod.WEEK,
            TimePeriod.DAY,
            TimePeriod.HOUR,
            TimePeriod.MINUTE,
            TimePeriod.SECOND
    );

    private final long millis;

    public Timespan() {
        this(0L);
    }

    public Timespan(long millis) {
        this.millis = Math.max(0L, millis);
    }

    public Timespan(TimePeriod period, long amount) {
        this(safeMultiply(Math.max(0L, amount), period.millis()));
    }

    public static Timespan fromDuration(Duration duration) {
        return new Timespan(duration.toMillis());
    }

    public static Timespan infinite() {
        return new Timespan(Long.MAX_VALUE);
    }

    public static Timespan parse(String value) {
        return parse(value, ParseContext.DEFAULT);
    }

    public static Timespan parse(String value, ParseContext context) {
        if (value == null) {
            return null;
        }
        String input = value.trim();
        if (input.isEmpty()) {
            return null;
        }

        String normalized = input.toLowerCase(Locale.ENGLISH);
        if (normalized.equals("forever") || normalized.equals("eternity") || normalized.equals("infinite")) {
            return infinite();
        }
        String foreverKey = FOREVER_NAME.toString(false).trim().toLowerCase(Locale.ENGLISH);
        if (!foreverKey.equals("time.forever") && foreverKey.equals(normalized)) {
            return infinite();
        }

        Matcher clockMatcher = CLOCK_PATTERN.matcher(input);
        if (clockMatcher.matches()) {
            return parseClockForm(input);
        }

        String[] parts = normalized.split("\\s+");
        long totalMillis = 0L;
        boolean minecraftTime = false;
        boolean timeModeSeen = false;

        for (int i = 0; i < parts.length; i++) {
            String part = trimPunctuation(parts[i]);
            if (part.isEmpty()) {
                continue;
            }
            if (part.equals(GeneralWords.and.toString().toLowerCase(Locale.ENGLISH))) {
                if (i == 0 || i == parts.length - 1) {
                    return null;
                }
                continue;
            }

            double amount = 1.0;
            if (Noun.isIndefiniteArticle(part)) {
                if (i == parts.length - 1) {
                    return null;
                }
                part = trimPunctuation(parts[++i]);
            } else if (NUMBER_PATTERN.matcher(part).matches()) {
                if (i == parts.length - 1) {
                    return null;
                }
                amount = parseNumber(part);
                part = trimPunctuation(parts[++i]);
            }

            if (context == ParseContext.COMMAND) {
                Matcher shortFormMatcher = SHORT_FORM_PATTERN.matcher(part);
                if (shortFormMatcher.matches()) {
                    amount = parseNumber(shortFormMatcher.group(1));
                    part = shortFormMatcher.group(2).toLowerCase(Locale.ENGLISH);
                }
            }

            String lowerPart = part.toLowerCase(Locale.ENGLISH);
            if (matchesLanguageList("time.real", lowerPart)) {
                if (i == parts.length - 1 || (timeModeSeen && minecraftTime)) {
                    return null;
                }
                timeModeSeen = true;
                part = trimPunctuation(parts[++i]);
                lowerPart = part.toLowerCase(Locale.ENGLISH);
            } else if (matchesLanguageList("time.minecraft", lowerPart)) {
                if (i == parts.length - 1 || (timeModeSeen && !minecraftTime)) {
                    return null;
                }
                minecraftTime = true;
                timeModeSeen = true;
                part = trimPunctuation(parts[++i]);
                lowerPart = part.toLowerCase(Locale.ENGLISH);
            }

            TimePeriod period = TimePeriod.fromToken(lowerPart);
            if (period == null) {
                return null;
            }

            if (minecraftTime && period != TimePeriod.TICK) {
                amount /= 72.0;
            }

            totalMillis = safeAdd(totalMillis, safeScale(period.millis(), amount));
        }

        return new Timespan(totalMillis);
    }

    private static Timespan parseClockForm(String input) {
        String[] segments = SPLIT_PATTERN.split(input);
        boolean hasMillis = input.contains(".");
        long[] units = hasMillis
                ? new long[]{TimePeriod.DAY.millis(), TimePeriod.HOUR.millis(), TimePeriod.MINUTE.millis(), TimePeriod.SECOND.millis(), 1L}
                : new long[]{TimePeriod.DAY.millis(), TimePeriod.HOUR.millis(), TimePeriod.MINUTE.millis(), TimePeriod.SECOND.millis()};
        int offset = units.length - segments.length;

        long total = 0L;
        for (int i = 0; i < segments.length; i++) {
            total = safeAdd(total, safeMultiply(Long.parseLong(segments[i]), units[offset + i]));
        }
        return new Timespan(total);
    }

    private static boolean matchesLanguageList(String key, String value) {
        if (!Language.keyExists(key)) {
            return false;
        }
        for (String candidate : Language.getList(key)) {
            if (candidate.equalsIgnoreCase(value)) {
                return true;
            }
        }
        return false;
    }

    private static String trimPunctuation(String value) {
        if (value.endsWith(",")) {
            return value.substring(0, value.length() - 1);
        }
        return value;
    }

    private static double parseNumber(String value) {
        try {
            return Double.parseDouble(value);
        } catch (NumberFormatException exception) {
            throw new IllegalArgumentException("Invalid timespan number: " + value, exception);
        }
    }

    private static long safeScale(long unitMillis, double amount) {
        if (Double.isInfinite(amount)) {
            return Long.MAX_VALUE;
        }
        if (Double.isNaN(amount) || amount <= 0.0) {
            return 0L;
        }
        double scaled = unitMillis * amount;
        if (scaled >= Long.MAX_VALUE) {
            return Long.MAX_VALUE;
        }
        return Math.round(scaled);
    }

    private static long safeMultiply(long left, long right) {
        if (left == 0L || right == 0L) {
            return 0L;
        }
        if (left > Long.MAX_VALUE / right) {
            return Long.MAX_VALUE;
        }
        return left * right;
    }

    private static long safeAdd(long left, long right) {
        if (left == Long.MAX_VALUE || right == Long.MAX_VALUE) {
            return Long.MAX_VALUE;
        }
        if (Long.MAX_VALUE - left < right) {
            return Long.MAX_VALUE;
        }
        return left + right;
    }

    public static String toString(long millis) {
        return toString(millis, 0);
    }

    public static String toString(long millis, int flags) {
        if (millis == Long.MAX_VALUE) {
            return FOREVER_NAME.toString(false);
        }
        if (millis >= TimePeriod.TICK.millis() && millis < TimePeriod.SECOND.millis() && millis % TimePeriod.TICK.millis() == 0L) {
            return TimePeriod.TICK.format(millis / TimePeriod.TICK.millis());
        }
        for (int i = 0; i < DISPLAY_PERIODS.size(); i++) {
            TimePeriod period = DISPLAY_PERIODS.get(i);
            if (millis >= period.millis()) {
                long whole = millis / period.millis();
                long remainder = millis % period.millis();
                String current = period.format(whole);
                if (remainder <= 0L || i == DISPLAY_PERIODS.size() - 1) {
                    return current;
                }
                TimePeriod next = DISPLAY_PERIODS.get(i + 1);
                if (remainder < next.millis()) {
                    return current;
                }
                return current + " " + GeneralWords.and + " " + toString(remainder, flags);
            }
        }
        return TimePeriod.MILLISECOND.format(millis);
    }

    public boolean isInfinite() {
        return millis == Long.MAX_VALUE;
    }

    public long getAs(TimePeriod period) {
        return millis / period.millis();
    }

    public long millis() {
        return millis;
    }

    public Duration getDuration() {
        return Duration.ofMillis(millis);
    }

    public Timespan add(Timespan other) {
        if (isInfinite() || other.isInfinite()) {
            return infinite();
        }
        return new Timespan(safeAdd(millis, other.millis));
    }

    public Timespan subtract(Timespan other) {
        if (isInfinite() || other.isInfinite()) {
            return infinite();
        }
        return new Timespan(Math.max(0L, millis - other.millis));
    }

    public Timespan multiply(double scalar) {
        if (scalar < 0.0) {
            throw new IllegalArgumentException("scalar must be >= 0");
        }
        if (isInfinite() || Double.isInfinite(scalar)) {
            return infinite();
        }
        if (Double.isNaN(scalar)) {
            return new Timespan(0L);
        }
        double scaled = millis * scalar;
        if (scaled >= Long.MAX_VALUE) {
            return infinite();
        }
        return new Timespan((long) scaled);
    }

    public Timespan divide(double scalar) {
        if (scalar < 0.0) {
            throw new IllegalArgumentException("scalar must be >= 0");
        }
        if (isInfinite()) {
            return infinite();
        }
        double value = millis / scalar;
        if (Double.isNaN(value)) {
            return new Timespan(0L);
        }
        if (Double.isInfinite(value)) {
            return infinite();
        }
        return new Timespan((long) value);
    }

    public double divide(Timespan other) {
        if (isInfinite()) {
            return other.isInfinite() ? Double.NaN : Double.POSITIVE_INFINITY;
        }
        if (other.isInfinite()) {
            return 0.0;
        }
        return millis / (double) other.millis;
    }

    public Timespan difference(Timespan other) {
        if (isInfinite() || other.isInfinite()) {
            return infinite();
        }
        return new Timespan(Math.abs(millis - other.millis));
    }

    @Override
    public long get(TemporalUnit unit) {
        if (unit instanceof TimePeriod period) {
            return getAs(period);
        }
        if (!(unit instanceof ChronoUnit chronoUnit)) {
            throw new UnsupportedTemporalTypeException("Unsupported unit: " + unit);
        }
        return switch (chronoUnit) {
            case MILLIS -> getAs(TimePeriod.MILLISECOND);
            case SECONDS -> getAs(TimePeriod.SECOND);
            case MINUTES -> getAs(TimePeriod.MINUTE);
            case HOURS -> getAs(TimePeriod.HOUR);
            case DAYS -> getAs(TimePeriod.DAY);
            default -> throw new UnsupportedTemporalTypeException("Unsupported unit: " + unit);
        };
    }

    @Override
    public List<TemporalUnit> getUnits() {
        return List.of(
                TimePeriod.YEAR,
                TimePeriod.MONTH,
                TimePeriod.WEEK,
                TimePeriod.DAY,
                TimePeriod.HOUR,
                TimePeriod.MINUTE,
                TimePeriod.SECOND,
                TimePeriod.TICK,
                TimePeriod.MILLISECOND
        );
    }

    @Override
    public Temporal addTo(Temporal temporal) {
        return temporal.plus(millis, ChronoUnit.MILLIS);
    }

    @Override
    public Temporal subtractFrom(Temporal temporal) {
        return temporal.minus(millis, ChronoUnit.MILLIS);
    }

    @Override
    public int compareTo(Timespan other) {
        return Long.compare(millis, other == null ? millis : other.millis);
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        if (!(object instanceof Timespan timespan)) {
            return false;
        }
        return millis == timespan.millis;
    }

    @Override
    public int hashCode() {
        return Objects.hash(millis);
    }

    @Override
    public String toString() {
        return toString(millis);
    }

    public String toString(int flags) {
        return toString(millis, flags);
    }

    public enum TimePeriod implements TemporalUnit {
        MILLISECOND(1L, "millisecond", "milliseconds", "ms", "ms"),
        TICK(50L, "tick", "ticks", "tick", "ticks"),
        SECOND(1_000L, "second", "seconds", "sec", "secs"),
        MINUTE(60_000L, "minute", "minutes", "min", "mins"),
        HOUR(3_600_000L, "hour", "hours", "hr", "hrs"),
        DAY(86_400_000L, "day", "days", "day", "days"),
        WEEK(604_800_000L, "week", "weeks", "week", "weeks"),
        MONTH(2_592_000_000L, "month", "months", "month", "months"),
        YEAR(31_536_000_000L, "year", "years", "year", "years");

        private final long millis;
        private final Noun fullName;
        private final Noun shortName;
        private final List<String> fallbackTokens;

        TimePeriod(long millis, String singular, String plural, String shortSingular, String shortPlural) {
            this.millis = millis;
            String baseKey = "time." + name().toLowerCase(Locale.ENGLISH);
            this.fullName = new Noun(baseKey + ".full");
            this.shortName = new Noun(baseKey + ".short");
            this.fallbackTokens = buildFallbackTokens(singular, plural, shortSingular, shortPlural);
        }

        private static List<String> buildFallbackTokens(String singular, String plural, String shortSingular, String shortPlural) {
            List<String> tokens = new ArrayList<>();
            tokens.add(singular);
            tokens.add(plural);
            tokens.add(shortSingular);
            tokens.add(shortPlural);
            if (singular.length() > 1) {
                tokens.add(String.valueOf(singular.charAt(0)));
            }
            return tokens;
        }

        public long millis() {
            return millis;
        }

        public long getTime() {
            return millis;
        }

        public String singular() {
            return fallbackTokens.get(0);
        }

        public String plural() {
            return fallbackTokens.get(1);
        }

        public String getFullForm() {
            return resolveName(fullName, singular(), plural(), false);
        }

        public String getShortForm() {
            return resolveName(shortName, fallbackTokens.get(2), fallbackTokens.get(3), false);
        }

        public String format(double amount) {
            return resolveName(fullName, singular(), plural(), true, amount);
        }

        public static TimePeriod fromToken(String token) {
            if (token == null) {
                return null;
            }
            String normalized = token.trim().toLowerCase(Locale.ENGLISH);
            for (TimePeriod period : values()) {
                for (String candidate : period.fallbackTokens) {
                    if (candidate.equals(normalized)) {
                        return period;
                    }
                }
                if (matchesNoun(period.fullName, normalized) || matchesNoun(period.shortName, normalized)) {
                    return period;
                }
            }
            return null;
        }

        private static boolean matchesNoun(Noun noun, String token) {
            String singular = noun.getSingular().toLowerCase(Locale.ENGLISH);
            String plural = noun.getPlural().toLowerCase(Locale.ENGLISH);
            return !singular.startsWith("time.") && (singular.equals(token) || plural.equals(token));
        }

        private static String resolveName(Noun noun, String singularFallback, String pluralFallback, boolean withAmount, double amount) {
            String singular = noun.getSingular();
            String plural = noun.getPlural();
            if (singular.startsWith("time.") || plural.startsWith("time.")) {
                String chosen = amount == 1.0 ? singularFallback : pluralFallback;
                return withAmount ? formatAmount(amount) + " " + chosen : chosen;
            }
            return withAmount ? noun.withAmount(amount) : singular;
        }

        private static String resolveName(Noun noun, String singularFallback, String pluralFallback, boolean withAmount) {
            return resolveName(noun, singularFallback, pluralFallback, withAmount, 1.0);
        }

        @Override
        public Duration getDuration() {
            return Duration.ofMillis(millis);
        }

        @Override
        public boolean isDurationEstimated() {
            return false;
        }

        @Override
        public boolean isDateBased() {
            return this == DAY || this == WEEK || this == MONTH || this == YEAR;
        }

        @Override
        public boolean isTimeBased() {
            return !isDateBased();
        }

        @Override
        public <R extends Temporal> R addTo(R temporal, long amount) {
            return (R) temporal.plus(amount, this);
        }

        @Override
        public long between(Temporal temporal1Inclusive, Temporal temporal2Exclusive) {
            return temporal1Inclusive.until(temporal2Exclusive, this);
        }
    }

    private static String formatAmount(double amount) {
        if (amount == Math.rint(amount)) {
            return Long.toString((long) amount);
        }
        return Double.toString(amount);
    }
}
