package ch.njol.skript.lang;

import java.util.Objects;
import java.util.StringJoiner;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.lang.event.SkriptEvent;

/**
 * Utility class to build syntax strings, primarily for Debuggable#toString implementations.
 */
public class SyntaxStringBuilder {

    private final boolean debug;
    private final @Nullable SkriptEvent event;
    private final StringJoiner joiner = new StringJoiner(" ");

    public SyntaxStringBuilder(@Nullable SkriptEvent event, boolean debug) {
        this.event = event;
        this.debug = debug;
    }

    public SyntaxStringBuilder append(@NotNull Object object) {
        Objects.requireNonNull(object, "object");
        if (object instanceof Debuggable debuggable) {
            joiner.add(debuggable.toString(event, debug));
        } else {
            joiner.add(object.toString());
        }
        return this;
    }

    public SyntaxStringBuilder append(@NotNull Object... objects) {
        for (Object object : objects) {
            append(object);
        }
        return this;
    }

    public SyntaxStringBuilder appendIf(boolean condition, Object object) {
        if (condition) {
            append(object);
        }
        return this;
    }

    public SyntaxStringBuilder appendIf(boolean condition, Object... objects) {
        if (condition) {
            append(objects);
        }
        return this;
    }

    @Override
    public String toString() {
        return joiner.toString();
    }
}
