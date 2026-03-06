package ch.njol.skript.log;

public class ParseLogHandler implements AutoCloseable {

    private boolean hasError;

    public ParseLogHandler start() {
        return this;
    }

    public void printError() {
        hasError = true;
    }

    public void printError(String message, ErrorQuality quality) {
        hasError = true;
    }

    public void printLog() {
    }

    public void clear() {
        hasError = false;
    }

    public boolean hasError() {
        return hasError;
    }

    public void stop() {
    }

    @Override
    public void close() {
    }
}
