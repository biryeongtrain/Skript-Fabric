package ch.njol.skript.lang.function;

import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.parser.ParserInstance;
import ch.njol.util.Kleenean;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.lang.event.SkriptEvent;

/**
 * Effect wrapper for function calls where return values are ignored.
 */
public class EffFunctionCall extends Effect {

    private final FunctionReference<?> reference;

    public EffFunctionCall(FunctionReference<?> reference) {
        this.reference = reference;
    }

    public static @Nullable EffFunctionCall parse(String line) {
        String script = null;
        var current = ParserInstance.get().getCurrentScript();
        if (current != null && current.getConfig() != null) {
            script = current.getConfig().getFileName();
        }
        FunctionReference<?> function = FunctionReference.parse(line, script, null);
        if (function != null && function.validateFunction(true)) {
            return new EffFunctionCall(function);
        }
        return null;
    }

    @Override
    protected void execute(SkriptEvent event) {
        reference.execute(event);
        if (reference.function() != null) {
            reference.function().resetReturnValue();
        }
    }

    @Override
    public String toString(@Nullable SkriptEvent event, boolean debug) {
        return reference.toString(event, debug);
    }

    @Override
    public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
        return false;
    }
}
