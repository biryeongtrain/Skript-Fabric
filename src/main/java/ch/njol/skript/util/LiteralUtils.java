package ch.njol.skript.util;

import ch.njol.skript.lang.Expression;
import org.jetbrains.annotations.Nullable;

/**
 * Minimal literal utility shim used by compatibility parsing paths.
 */
public final class LiteralUtils {

    private LiteralUtils() {
    }

    public static boolean hasUnparsedLiteral(@Nullable Expression<?> expression) {
        return false;
    }

    public static @Nullable Expression<?> defendExpression(@Nullable Expression<?> expression) {
        return expression;
    }

    public static boolean canInitSafely(@Nullable Expression<?> expression) {
        return expression != null;
    }
}
