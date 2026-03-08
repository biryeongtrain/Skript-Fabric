package ch.njol.skript.log;

import java.util.ArrayDeque;
import java.util.Collection;
import java.util.Deque;
import java.util.logging.Level;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ch.njol.skript.log.LogHandler.LogResult;

public final class SkriptLogger {

    private static final Logger LOGGER = LoggerFactory.getLogger("skript-fabric");
    private static final ThreadLocal<Deque<LogHandler>> LOG_HANDLERS =
            ThreadLocal.withInitial(ArrayDeque::new);

    private SkriptLogger() {
    }

    public static RetainingLogHandler startRetainingLog() {
        return new RetainingLogHandler().start();
    }

    public static ParseLogHandler startParseLogHandler() {
        return new ParseLogHandler().start();
    }

    public static <T extends LogHandler> T startLogHandler(T handler) {
        LOG_HANDLERS.get().addFirst(handler);
        return handler;
    }

    static boolean removeHandler(LogHandler handler) {
        Deque<LogHandler> handlers = LOG_HANDLERS.get();
        boolean removed = handlers.removeFirstOccurrence(handler);
        if (handlers.isEmpty()) {
            LOG_HANDLERS.remove();
        }
        return removed;
    }

    static boolean isStopped(LogHandler handler) {
        return !LOG_HANDLERS.get().contains(handler);
    }

    public static void logAll(Collection<LogEntry> entries) {
        entries.forEach(SkriptLogger::log);
    }

    public static void log(LogEntry entry) {
        if (entry == null) {
            return;
        }
        for (LogHandler handler : LOG_HANDLERS.get()) {
            LogResult result = handler.log(entry);
            if (result == LogResult.CACHED || result == LogResult.DO_NOT_LOG) {
                return;
            }
        }
        Level level = entry.getLevel();
        String message = entry.getMessage();
        if (level.intValue() >= Level.SEVERE.intValue()) {
            LOGGER.error(message);
        } else if (level.intValue() >= Level.WARNING.intValue()) {
            LOGGER.warn(message);
        } else {
            LOGGER.debug(message);
        }
    }
}
