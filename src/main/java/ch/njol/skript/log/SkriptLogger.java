package ch.njol.skript.log;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.logging.Level;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class SkriptLogger {

    private static final Logger LOGGER = LoggerFactory.getLogger("skript-fabric");
    private static final ThreadLocal<Deque<ParseLogHandler>> PARSE_LOG_HANDLERS =
            ThreadLocal.withInitial(ArrayDeque::new);

    private SkriptLogger() {
    }

    public static ParseLogHandler startParseLogHandler() {
        return new ParseLogHandler().start();
    }

    static void startLogHandler(ParseLogHandler handler) {
        PARSE_LOG_HANDLERS.get().addLast(handler);
    }

    static void stopLogHandler(ParseLogHandler handler) {
        Deque<ParseLogHandler> handlers = PARSE_LOG_HANDLERS.get();
        handlers.removeLastOccurrence(handler);
        if (handlers.isEmpty()) {
            PARSE_LOG_HANDLERS.remove();
        }
    }

    public static void log(LogEntry entry) {
        if (entry == null) {
            return;
        }
        ParseLogHandler handler = PARSE_LOG_HANDLERS.get().peekLast();
        if (handler != null) {
            handler.log(entry);
            return;
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
