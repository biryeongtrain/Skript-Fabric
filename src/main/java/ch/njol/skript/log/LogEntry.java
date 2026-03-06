package ch.njol.skript.log;

import java.util.logging.Level;

public final class LogEntry {

    private final Level level;
    private final String message;

    public LogEntry(Level level, String message) {
        this.level = level == null ? Level.INFO : level;
        this.message = message == null ? "" : message;
    }

    public static LogEntry severe(String message) {
        return new LogEntry(Level.SEVERE, message);
    }

    public Level getLevel() {
        return level;
    }

    public String getMessage() {
        return message;
    }
}
