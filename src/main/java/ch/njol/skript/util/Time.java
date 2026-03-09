package ch.njol.skript.util;

import ch.njol.skript.Skript;
import ch.njol.skript.localization.Message;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.lang.util.Cyclical;

public class Time implements Cyclical<Integer> {

    private static final int TICKS_PER_HOUR = 1000;
    private static final int TICKS_PER_DAY = 24 * TICKS_PER_HOUR;
    private static final double TICKS_PER_MINUTE = 1000.0 / 60.0;
    private static final int HOUR_ZERO = 6 * TICKS_PER_HOUR;

    private static final Pattern DAY_TIME_PATTERN =
            Pattern.compile("(\\d?\\d)(:(\\d\\d))? ?(am|pm)", Pattern.CASE_INSENSITIVE);
    private static final Pattern TIME_PATTERN = Pattern.compile("\\d?\\d:\\d\\d", Pattern.CASE_INSENSITIVE);

    private static final Message M_ERROR_24_HOURS = new Message("time.errors.24 hours");
    private static final Message M_ERROR_12_HOURS = new Message("time.errors.12 hours");
    private static final Message M_ERROR_60_MINUTES = new Message("time.errors.60 minutes");

    private final int time;

    public Time() {
        this(0);
    }

    public Time(int time) {
        this.time = Math.floorMod(time, TICKS_PER_DAY);
    }

    public int getTicks() {
        return time;
    }

    public int getTime() {
        return (time + HOUR_ZERO) % TICKS_PER_DAY;
    }

    public int getHour() {
        int hour = (getTime()) / TICKS_PER_HOUR;
        return hour >= 24 ? hour - 24 : hour;
    }

    public int getMinute() {
        return (int) Math.round(((time + HOUR_ZERO) % TICKS_PER_HOUR) / TICKS_PER_MINUTE);
    }

    @Override
    public String toString() {
        return toString(time);
    }

    public static String toString(int ticks) {
        assert 0 <= ticks && ticks < TICKS_PER_DAY;
        int current = (ticks + HOUR_ZERO) % TICKS_PER_DAY;
        int hours = current / TICKS_PER_HOUR;
        int minutes = (int) Math.round((current % TICKS_PER_HOUR) / TICKS_PER_MINUTE);
        if (minutes >= 60) {
            hours = (hours + 1) % 24;
            minutes -= 60;
        }
        return hours + ":" + (minutes < 10 ? "0" : "") + minutes;
    }

    @Nullable
    public static Time parse(String input) {
        if (TIME_PATTERN.matcher(input).matches()) {
            int hours = Utils.parseInt(input.split(":")[0]);
            if (hours == 24) {
                hours = 0;
            } else if (hours > 24) {
                Skript.error(String.valueOf(M_ERROR_24_HOURS));
                return null;
            }

            int minutes = Utils.parseInt(input.split(":")[1]);
            if (minutes >= 60) {
                Skript.error(String.valueOf(M_ERROR_60_MINUTES));
                return null;
            }

            return new Time((int) Math.round(hours * TICKS_PER_HOUR - HOUR_ZERO + minutes * TICKS_PER_MINUTE));
        }

        Matcher matcher = DAY_TIME_PATTERN.matcher(input);
        if (!matcher.matches()) {
            return null;
        }

        int hours = Utils.parseInt(matcher.group(1));
        if (hours == 12) {
            hours = 0;
        } else if (hours > 12) {
            Skript.error(String.valueOf(M_ERROR_12_HOURS));
            return null;
        }

        int minutes = 0;
        if (matcher.group(3) != null) {
            minutes = Utils.parseInt(matcher.group(3));
        }
        if (minutes >= 60) {
            Skript.error(String.valueOf(M_ERROR_60_MINUTES));
            return null;
        }

        if ("pm".equalsIgnoreCase(matcher.group(4))) {
            hours += 12;
        }
        return new Time((int) Math.round(hours * TICKS_PER_HOUR - HOUR_ZERO + minutes * TICKS_PER_MINUTE));
    }

    @Override
    public int hashCode() {
        return time;
    }

    @Override
    public boolean equals(@Nullable Object object) {
        if (this == object) {
            return true;
        }
        if (!(object instanceof Time other)) {
            return false;
        }
        return time == other.time;
    }

    @Override
    public Integer getMaximum() {
        return TICKS_PER_DAY;
    }

    @Override
    public Integer getMinimum() {
        return 0;
    }
}
