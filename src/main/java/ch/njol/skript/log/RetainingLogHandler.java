package ch.njol.skript.log;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Deque;
import java.util.List;
import java.util.logging.Level;
import org.jetbrains.annotations.Nullable;

public class RetainingLogHandler extends LogHandler {

    private final Deque<LogEntry> logEntries = new ArrayDeque<>();
    private int numErrors;
    private boolean printedErrorOrLog;

    public RetainingLogHandler backup() {
        RetainingLogHandler copy = new RetainingLogHandler();
        copy.numErrors = numErrors;
        copy.printedErrorOrLog = printedErrorOrLog;
        copy.logEntries.addAll(logEntries);
        return copy;
    }

    public void restore(RetainingLogHandler copy) {
        numErrors = copy.numErrors;
        printedErrorOrLog = copy.printedErrorOrLog;
        logEntries.clear();
        logEntries.addAll(copy.logEntries);
    }

    @Override
    public LogResult log(LogEntry entry) {
        logEntries.addLast(entry);
        if (entry.getLevel().intValue() >= Level.SEVERE.intValue()) {
            numErrors++;
        }
        printedErrorOrLog = false;
        return LogResult.CACHED;
    }

    @Override
    public RetainingLogHandler start() {
        return SkriptLogger.startLogHandler(this);
    }

    public final boolean printErrors() {
        return printErrors(null);
    }

    public final boolean printErrors(@Nullable String defaultError) {
        return printErrors(defaultError, ErrorQuality.GENERIC);
    }

    public final boolean printErrors(@Nullable String defaultError, ErrorQuality quality) {
        printedErrorOrLog = true;
        stop();

        boolean hasError = false;
        for (LogEntry entry : logEntries) {
            if (entry.getLevel().intValue() >= Level.SEVERE.intValue()) {
                SkriptLogger.log(entry);
                hasError = true;
            }
        }

        if (!hasError && defaultError != null && !defaultError.isBlank()) {
            SkriptLogger.log(new LogEntry(Level.SEVERE, quality, defaultError));
        }

        return hasError;
    }

    public final void printLog() {
        printedErrorOrLog = true;
        stop();
        SkriptLogger.logAll(logEntries);
    }

    public boolean hasErrors() {
        return numErrors != 0;
    }

    public @Nullable LogEntry getFirstError() {
        for (LogEntry entry : logEntries) {
            if (entry.getLevel().intValue() >= Level.SEVERE.intValue()) {
                return entry;
            }
        }
        return null;
    }

    public LogEntry getFirstError(String defaultError) {
        LogEntry firstError = getFirstError();
        if (firstError != null) {
            return firstError;
        }
        return new LogEntry(Level.SEVERE, defaultError);
    }

    public void clear() {
        logEntries.clear();
        numErrors = 0;
    }

    public int size() {
        return logEntries.size();
    }

    public Collection<LogEntry> getLog() {
        printedErrorOrLog = true;
        return Collections.unmodifiableCollection(logEntries);
    }

    public Collection<LogEntry> getErrors() {
        List<LogEntry> errors = new ArrayList<>();
        for (LogEntry entry : logEntries) {
            if (entry.getLevel().intValue() >= Level.SEVERE.intValue()) {
                errors.add(entry);
            }
        }
        return errors;
    }

    public int getNumErrors() {
        return numErrors;
    }
}
