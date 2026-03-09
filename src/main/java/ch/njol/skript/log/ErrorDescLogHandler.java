package ch.njol.skript.log;

import ch.njol.skript.Skript;
import java.util.logging.Level;
import org.jetbrains.annotations.Nullable;

/**
 * Logs a prefix when the first error is seen and an optional trailing message on stop.
 */
public class ErrorDescLogHandler extends LogHandler {

    private final @Nullable String before;
    private final @Nullable String after;
    private final @Nullable String success;
    private boolean hadError;

    public ErrorDescLogHandler() {
        this(null, null, null);
    }

    public ErrorDescLogHandler(@Nullable String before, @Nullable String after, @Nullable String success) {
        this.before = before;
        this.after = after;
        this.success = success;
    }

    @Override
    public LogResult log(LogEntry entry) {
        if (!hadError && entry.getLevel() == Level.SEVERE) {
            hadError = true;
            beforeErrors();
        }
        return LogResult.LOG;
    }

    @Override
    public ErrorDescLogHandler start() {
        return SkriptLogger.startLogHandler(this);
    }

    protected void beforeErrors() {
        if (before != null) {
            Skript.error(before);
        }
    }

    protected void afterErrors() {
        if (after != null) {
            Skript.error(after);
        }
    }

    protected void onSuccess() {
        if (success != null) {
            SkriptLogger.log(Level.INFO, success);
        }
    }

    @Override
    protected void onStop() {
        if (hadError) {
            afterErrors();
        } else {
            onSuccess();
        }
    }
}
