package ch.njol.skript.variables;

import ch.njol.skript.lang.Variable;
import ch.njol.skript.lang.parser.ParserInstance;
import org.jetbrains.annotations.Nullable;

/**
 * Legacy compatibility bridge for parse-time local variable type hints.
 *
 * @deprecated Use {@link HintManager}.
 */
@Deprecated(since = "2.12", forRemoval = true)
public final class TypeHints {

    private TypeHints() {
    }

    public static void add(String variable, Class<?> hint) {
        if (hint == Object.class) {
            return;
        }
        hintManagerWithScope().setSingleHint(normalizeVariableName(variable), hint);
    }

    public static @Nullable Class<?> get(String variable) {
        return hintManagerWithScope().getSingleHint(normalizeVariableName(variable));
    }

    public static void enterScope() {
        hintManagerWithScope().pushScope(false, true);
    }

    public static void exitScope() {
        hintManagerWithScope().popScope(false);
    }

    public static void clear() {
        HintManager hintManager = ParserInstance.get().getHintManager();
        hintManager.resetScopes();
        hintManager.pushScope(false, false);
    }

    private static HintManager hintManagerWithScope() {
        HintManager hintManager = ParserInstance.get().getHintManager();
        if (!hintManager.hasScopes()) {
            hintManager.pushScope(false, false);
        }
        return hintManager;
    }

    private static String normalizeVariableName(String variable) {
        String normalized = variable.trim();
        if (normalized.startsWith("{") && normalized.endsWith("}") && normalized.length() > 1) {
            normalized = normalized.substring(1, normalized.length() - 1).trim();
        }
        if (normalized.startsWith(Variable.LOCAL_VARIABLE_TOKEN)
                || normalized.startsWith(Variable.EPHEMERAL_VARIABLE_TOKEN)) {
            normalized = normalized.substring(1);
        }
        return normalized;
    }
}
