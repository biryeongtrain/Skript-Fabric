package ch.njol.skript.util;

import java.util.Locale;
import java.util.Objects;

public final class Timespan {

    public enum TimePeriod {
        TICK(50L, "tick", "ticks"),
        SECOND(1000L, "second", "seconds"),
        MINUTE(60_000L, "minute", "minutes"),
        HOUR(3_600_000L, "hour", "hours");

        private final long millis;
        private final String singular;
        private final String plural;

        TimePeriod(long millis, String singular, String plural) {
            this.millis = millis;
            this.singular = singular;
            this.plural = plural;
        }

        public long millis() {
            return millis;
        }

        public String singular() {
            return singular;
        }

        public String plural() {
            return plural;
        }

        public static TimePeriod fromToken(String token) {
            if (token == null) {
                return null;
            }
            String normalized = token.trim().toLowerCase(Locale.ENGLISH);
            return switch (normalized) {
                case "tick", "ticks", "t" -> TICK;
                case "second", "seconds", "sec", "secs", "s" -> SECOND;
                case "minute", "minutes", "min", "mins", "m" -> MINUTE;
                case "hour", "hours", "hr", "hrs", "h" -> HOUR;
                default -> null;
            };
        }
    }

    private final long millis;

    public Timespan(TimePeriod period, long amount) {
        this.millis = Math.max(0L, amount) * period.millis();
    }

    public long getAs(TimePeriod period) {
        return millis / period.millis();
    }

    public long millis() {
        return millis;
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
        long ticks = getAs(TimePeriod.TICK);
        return ticks + " " + (ticks == 1L ? "tick" : "ticks");
    }
}
