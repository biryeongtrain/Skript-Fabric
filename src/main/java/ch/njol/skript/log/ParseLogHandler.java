package ch.njol.skript.log;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import org.jetbrains.annotations.Nullable;

public class ParseLogHandler implements AutoCloseable {

    private final List<LogEntry> logEntries = new ArrayList<>();
    private @Nullable LogEntry error;
    private boolean active;

    public ParseLogHandler start() {
        if (!active) {
            SkriptLogger.startLogHandler(this);
            active = true;
        }
        return this;
    }

    public void printError() {
        printError((String) null);
    }

    public void printError(String message, ErrorQuality quality) {
        stop();
        if (error != null && error.getQuality().priority() >= quality.priority()) {
            SkriptLogger.log(error);
            return;
        }
        if (message != null && !message.isBlank()) {
            SkriptLogger.log(new LogEntry(Level.SEVERE, quality, message));
        }
    }

    public void printError(@Nullable String defaultError) {
        stop();
        if (error != null) {
            SkriptLogger.log(error);
            return;
        }
        if (defaultError != null && !defaultError.isBlank()) {
            SkriptLogger.log(LogEntry.severe(defaultError));
        }
    }

    public void printLog() {
        stop();
        for (LogEntry entry : logEntries) {
            SkriptLogger.log(entry);
        }
    }

    public void clear() {
        logEntries.clear();
    }

    public void clearError() {
        error = null;
    }

    public boolean hasError() {
        return error != null;
    }

    public @Nullable LogEntry getError() {
        return error;
    }

    public List<LogEntry> getErrors() {
        List<LogEntry> errors = new ArrayList<>();
        for (LogEntry entry : logEntries) {
            if (entry.getLevel().intValue() >= Level.SEVERE.intValue()) {
                errors.add(entry);
            }
        }
        return errors;
    }

    public ParseLogHandler backup() {
        ParseLogHandler copy = new ParseLogHandler();
        copy.logEntries.addAll(logEntries);
        copy.error = error;
        return copy;
    }

    public void restore(ParseLogHandler copy) {
        logEntries.clear();
        logEntries.addAll(copy.logEntries);
        error = copy.error;
    }

    public void stop() {
        if (!active) {
            return;
        }
        active = false;
        SkriptLogger.stopLogHandler(this);
    }

    void log(LogEntry entry) {
        if (entry == null) {
            return;
        }
        logEntries.add(entry);
        if (entry.getLevel().intValue() >= Level.SEVERE.intValue()
                && (error == null || entry.getQuality().priority() > error.getQuality().priority())) {
            error = entry;
        }
    }

    @Override
    public void close() {
        stop();
    }
}
