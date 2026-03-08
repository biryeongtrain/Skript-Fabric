package ch.njol.skript.log;

import java.io.Closeable;

public abstract class LogHandler implements Closeable {

    public enum LogResult {
        LOG,
        CACHED,
        DO_NOT_LOG
    }

    public abstract LogResult log(LogEntry entry);

    protected void onStop() {
    }

    public final void stop() {
        if (!SkriptLogger.removeHandler(this)) {
            return;
        }
        onStop();
    }

    public final boolean isStopped() {
        return SkriptLogger.isStopped(this);
    }

    public LogHandler start() {
        return SkriptLogger.startLogHandler(this);
    }

    @Override
    public void close() {
        stop();
    }
}
