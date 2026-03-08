package ch.njol.skript.log;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import org.jetbrains.annotations.Nullable;

public class ParseLogHandler extends LogHandler {

    private final List<LogEntry> logEntries = new ArrayList<>();
    private @Nullable LogEntry error;

    public ParseLogHandler start() {
        if (isStopped()) {
            SkriptLogger.startLogHandler(this);
        }
        return this;
    }

    public void error(String message, ErrorQuality quality) {
        log(new LogEntry(Level.SEVERE, quality, message));
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
        printLog(true);
    }

    public void printLog(boolean includeErrors) {
        stop();
        for (LogEntry entry : logEntries) {
            if (includeErrors || entry.getLevel().intValue() < Level.SEVERE.intValue()) {
                SkriptLogger.log(entry);
            }
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

    public int getNumErrors() {
        return error == null ? 0 : 1;
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

    @Override
    public LogResult log(LogEntry entry) {
        if (entry == null) {
            return LogResult.CACHED;
        }
        logEntries.add(entry);
        if (entry.getLevel().intValue() >= Level.SEVERE.intValue()
                && (error == null || entry.getQuality().priority() > error.getQuality().priority())) {
            error = entry;
        }
        return LogResult.CACHED;
    }
}
