package ch.njol.skript.log;

import ch.njol.skript.Skript;
import java.util.logging.Level;

public final class SkriptLogger {

    private SkriptLogger() {
    }

    public static ParseLogHandler startParseLogHandler() {
        return new ParseLogHandler().start();
    }

    public static void log(LogEntry entry) {
        if (entry == null) {
            return;
        }
        Level level = entry.getLevel();
        String message = entry.getMessage();
        if (level.intValue() >= Level.SEVERE.intValue()) {
            Skript.error(message);
        } else if (level.intValue() >= Level.WARNING.intValue()) {
            Skript.warning(message);
        } else {
            Skript.debug(message);
        }
    }
}
