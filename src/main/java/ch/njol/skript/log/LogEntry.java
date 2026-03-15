package ch.njol.skript.log;

import ch.njol.skript.config.Config;
import ch.njol.skript.config.Node;
import java.util.logging.Level;
import org.jetbrains.annotations.Nullable;

public final class LogEntry {

    private final Level level;
    private final ErrorQuality quality;
    private final String message;
    private final @Nullable Node node;

    public LogEntry(Level level, String message) {
        this(level, ErrorQuality.SEMANTIC_ERROR, message, SkriptLogger.getNode());
    }

    public LogEntry(Level level, ErrorQuality quality, String message) {
        this(level, quality, message, SkriptLogger.getNode());
    }

    public LogEntry(Level level, String message, @Nullable Node node) {
        this(level, ErrorQuality.SEMANTIC_ERROR, message, node);
    }

    public LogEntry(Level level, ErrorQuality quality, String message, @Nullable Node node) {
        this.level = level == null ? Level.INFO : level;
        this.quality = quality == null ? ErrorQuality.GENERIC : quality;
        this.message = message == null ? "" : message;
        this.node = node;
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

    public @Nullable Node getNode() {
        return node;
    }

    @Override
    public String toString() {
        if (node == null || level.intValue() < Level.WARNING.intValue()) {
            return message;
        }
        Config config = node.getConfig();
        if (config == null) {
            return message;
        }
        String key = node.getKey();
        return message + " (" + config.getFileName() + ", line " + node.getLine()
                + ": " + (key != null ? key.trim() : "") + ")";
    }

    public String toFormattedString() {
        if (node == null || level.intValue() < Level.WARNING.intValue()) {
            return message;
        }
        Config config = node.getConfig();
        if (config == null) {
            return message;
        }
        String key = node.getKey();
        String sourceLine = key != null ? key.trim() : "";
        String fileName = config.getFileName();
        int lineNumber = node.getLine();
        if (level.intValue() >= Level.SEVERE.intValue()) {
            // Error format: file/line header, error message, source line
            return "Line " + lineNumber + ": (" + fileName + ")\n"
                    + "\t" + message + "\n"
                    + "\tLine " + lineNumber + ": " + sourceLine;
        }
        // Warning format
        return "Line " + lineNumber + ": (" + fileName + ")\n"
                + "\t" + message + "\n"
                + "\tLine " + lineNumber + ": " + sourceLine;
    }
}
