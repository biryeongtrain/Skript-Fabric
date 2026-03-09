package ch.njol.skript.log;

import ch.njol.skript.config.Node;
import ch.njol.skript.lang.parser.ParserInstance;
import ch.njol.skript.log.LogHandler.LogResult;
import java.util.Collection;
import java.util.logging.Level;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class SkriptLogger {

    static final Logger LOGGER = LoggerFactory.getLogger("skript-fabric");
    static final Level SEVERE = Level.SEVERE;
    static final Level DEBUG = Level.INFO;
    private static Verbosity verbosity = Verbosity.NORMAL;
    private static boolean debug;

    private SkriptLogger() {
    }

    private static HandlerList getHandlers() {
        return ParserInstance.get().getHandlers();
    }

    public static RetainingLogHandler startRetainingLog() {
        return new RetainingLogHandler().start();
    }

    public static ParseLogHandler startParseLogHandler() {
        return new ParseLogHandler().start();
    }

    public static <T extends LogHandler> T startLogHandler(T handler) {
        getHandlers().add(handler);
        return handler;
    }

    static boolean removeHandler(LogHandler handler) {
        HandlerList handlers = getHandlers();
        if (!handlers.contains(handler)) {
            return false;
        }
        if (handler.equals(handlers.remove())) {
            return true;
        }
        while (!handler.equals(handlers.remove())) {
            // Restore upstream stack removal semantics for out-of-order stops.
        }
        return true;
    }

    static boolean isStopped(LogHandler handler) {
        return !getHandlers().contains(handler);
    }

    public static void logAll(Collection<LogEntry> entries) {
        entries.forEach(SkriptLogger::log);
    }

    public static void log(LogEntry entry) {
        if (entry == null) {
            return;
        }
        for (LogHandler handler : getHandlers()) {
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

    public static void log(Level level, String message) {
        log(new LogEntry(level, message));
    }

    public static void setVerbosity(Verbosity value) {
        verbosity = value == null ? Verbosity.NORMAL : value;
        debug = verbosity.compareTo(Verbosity.DEBUG) >= 0;
    }

    public static boolean log(Verbosity minimum) {
        return minimum.compareTo(verbosity) <= 0;
    }

    public static boolean debug() {
        return debug;
    }

    public static void setNode(Node node) {
        ParserInstance.get().setNode(node);
    }

    public static Node getNode() {
        return ParserInstance.get().getNode();
    }

    public static void logTracked(Level level, String message, ErrorQuality quality) {
        log(new LogEntry(level, quality, message));
    }
}
