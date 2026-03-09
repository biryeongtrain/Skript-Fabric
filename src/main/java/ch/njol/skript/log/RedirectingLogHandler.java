package ch.njol.skript.log;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.logging.Level;
import org.jetbrains.annotations.Nullable;

public class RedirectingLogHandler extends LogHandler {

    private final Collection<Object> recipients;
    private final String prefix;
    private int numErrors;

    public RedirectingLogHandler(Object recipient, @Nullable String prefix) {
        this(Collections.singletonList(recipient), prefix);
    }

    public RedirectingLogHandler(Collection<?> recipients, @Nullable String prefix) {
        this.recipients = new ArrayList<>(recipients);
        this.prefix = prefix == null ? "" : prefix;
    }

    @Override
    public LogResult log(LogEntry entry) {
        return log(entry, null);
    }

    public LogResult log(LogEntry entry, @Nullable Object ignore) {
        String formattedMessage = prefix + entry.toFormattedString();
        for (Object recipient : recipients) {
            if (recipient == ignore) {
                continue;
            }
            SkriptLogger.sendFormatted(recipient, formattedMessage);
        }
        if (entry.getLevel().intValue() >= Level.SEVERE.intValue()) {
            numErrors++;
        }
        return LogResult.DO_NOT_LOG;
    }

    @Override
    public RedirectingLogHandler start() {
        return SkriptLogger.startLogHandler(this);
    }

    public int numErrors() {
        return numErrors;
    }
}
