package ch.njol.skript.log;

import java.util.logging.Level;

public class CountingLogHandler extends LogHandler {

    private final int minimum;
    private int count;

    public CountingLogHandler(Level minimum) {
        this.minimum = minimum.intValue();
    }

    @Override
    public LogResult log(LogEntry entry) {
        if (entry.getLevel().intValue() >= minimum) {
            count++;
        }
        return LogResult.LOG;
    }

    @Override
    public CountingLogHandler start() {
        return SkriptLogger.startLogHandler(this);
    }

    public int getCount() {
        return count;
    }
}
