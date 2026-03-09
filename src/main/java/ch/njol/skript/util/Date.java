package ch.njol.skript.util;

import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.TimeZone;

public class Date implements Comparable<Date> {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ISO_OFFSET_DATE_TIME.withZone(ZoneOffset.UTC);

    private long timestamp;

    public static Date now() {
        return new Date();
    }

    public static Date fromJavaDate(java.util.Date date) {
        if (date instanceof java.sql.Timestamp timestamp) {
            return new Date(timestamp.getTime());
        }
        if (date instanceof java.util.Date) {
            return new Date(date.getTime());
        }
        throw new IllegalArgumentException("Unsupported date type: " + date.getClass().getName());
    }

    public Date() {
        this(System.currentTimeMillis());
    }

    public Date(long timestamp) {
        this.timestamp = timestamp;
    }

    public Date(long timestamp, TimeZone zone) {
        this(timestamp - zone.getOffset(timestamp));
    }

    public long getTime() {
        return timestamp;
    }

    public void setTime(long timestamp) {
        this.timestamp = timestamp;
    }

    public void add(Timespan other) {
        setTime(getTime() + other.getAs(Timespan.TimePeriod.MILLISECOND));
    }

    public void subtract(Timespan other) {
        setTime(getTime() - other.getAs(Timespan.TimePeriod.MILLISECOND));
    }

    public Timespan difference(Date other) {
        return new Timespan(Math.abs(getTime() - other.getTime()));
    }

    public Date plus(Timespan other) {
        return new Date(getTime() + other.getAs(Timespan.TimePeriod.MILLISECOND));
    }

    public Date minus(Timespan other) {
        return new Date(getTime() - other.getAs(Timespan.TimePeriod.MILLISECOND));
    }

    @Deprecated(since = "2.10.0", forRemoval = true)
    public long getTimestamp() {
        return getTime();
    }

    @Override
    public int compareTo(Date other) {
        return Long.compare(getTime(), other.getTime());
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        if (object instanceof Date other) {
            return getTime() == other.getTime();
        }
        if (object instanceof java.util.Date other) {
            return getTime() == other.getTime();
        }
        return false;
    }

    @Override
    public int hashCode() {
        return 31 + Long.hashCode(getTime());
    }

    @Override
    public String toString() {
        return FORMATTER.format(Instant.ofEpochMilli(getTime()));
    }
}
