package ch.njol.skript.log;

/**
 * A log handler that records the time since its creation.
 */
public class TimingLogHandler extends LogHandler {

    private final long start = System.currentTimeMillis();

    @Override
    public LogResult log(LogEntry entry) {
        return LogResult.LOG;
    }

    @Override
    public TimingLogHandler start() {
        return SkriptLogger.startLogHandler(this);
    }

    public long getStart() {
        return start;
    }

    public long getTimeTaken() {
        return System.currentTimeMillis() - start;
    }
}
