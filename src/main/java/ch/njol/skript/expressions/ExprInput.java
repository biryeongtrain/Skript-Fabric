package ch.njol.skript.expressions;

import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.InputSource;
import ch.njol.skript.lang.parser.ParserInstance;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.lang.event.SkriptEvent;

/**
 * Minimal compatibility stub for input-based expressions.
 */
public abstract class ExprInput<T> implements Expression<T> {

    protected final @Nullable InputSource getInputSource() {
        return ParserInstance.get().getData(InputSource.InputData.class).getSource();
    }

    @Override
    public String toString(@Nullable SkriptEvent event, boolean debug) {
        return "input";
    }
}
