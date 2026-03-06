package ch.njol.skript.lang;

import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.registration.SyntaxInfo;
import org.skriptlang.skript.util.Priority;

/**
 * Used to define parse ordering for expressions.
 *
 * @deprecated Replaced by {@link Priority}.
 */
@Deprecated(since = "2.14", forRemoval = true)
public enum ExpressionType {

    SIMPLE(SyntaxInfo.SIMPLE),
    EVENT(SyntaxInfo.COMBINED),
    COMBINED(SyntaxInfo.COMBINED),
    PROPERTY(SyntaxInfo.COMBINED),
    PATTERN_MATCHES_EVERYTHING(SyntaxInfo.PATTERN_MATCHES_EVERYTHING);

    private final Priority priority;

    ExpressionType(Priority priority) {
        this.priority = priority;
    }

    public Priority priority() {
        return priority;
    }

    public static @Nullable ExpressionType fromModern(Priority priority) {
        if (SyntaxInfo.SIMPLE.equals(priority)) {
            return SIMPLE;
        }
        if (SyntaxInfo.PATTERN_MATCHES_EVERYTHING.equals(priority)) {
            return PATTERN_MATCHES_EVERYTHING;
        }
        if (SyntaxInfo.COMBINED.equals(priority)) {
            return COMBINED;
        }
        return null;
    }
}
