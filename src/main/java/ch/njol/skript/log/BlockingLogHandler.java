package ch.njol.skript.log;

public class BlockingLogHandler extends LogHandler {

    @Override
    public LogResult log(LogEntry entry) {
        return LogResult.DO_NOT_LOG;
    }

    @Override
    public BlockingLogHandler start() {
        return SkriptLogger.startLogHandler(this);
    }
}
