package ch.njol.skript.log;

import java.util.logging.Level;

public final class LogEntry {

    private final Level level;
    private final ErrorQuality quality;
    private final String message;

    public LogEntry(Level level, String message) {
        this(level, ErrorQuality.SEMANTIC_ERROR, message);
    }

    public LogEntry(Level level, ErrorQuality quality, String message) {
        this.level = level == null ? Level.INFO : level;
        this.quality = quality == null ? ErrorQuality.GENERIC : quality;
        this.message = message == null ? "" : message;
    }

    public static LogEntry severe(String message) {
        return new LogEntry(Level.SEVERE, message);
    }

    public Level getLevel() {
        return level;
    }

    public ErrorQuality getQuality() {
        return quality;
    }

    public String getMessage() {
        return message;
    }

    public String toFormattedString() {
        return message;
    }
}
