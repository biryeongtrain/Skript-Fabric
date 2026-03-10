package ch.njol.skript.expressions;

import ch.njol.skript.Skript;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.util.Kleenean;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.lang.event.SkriptEvent;

public class ExprInverse extends SimpleExpression<Boolean> {

    static {
        Skript.registerExpression(ExprInverse.class, Boolean.class,
                "[the] (inverse|opposite)[s] of %booleans%");
    }

    private Expression<Boolean> booleans;

    @Override
    @SuppressWarnings("unchecked")
    public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
        booleans = (Expression<Boolean>) exprs[0];
        return true;
    }

    @Override
    protected Boolean @Nullable [] get(SkriptEvent event) {
        Boolean[] values = booleans.getArray(event);
        Boolean[] inverted = new Boolean[values.length];
        for (int index = 0; index < values.length; index++) {
            inverted[index] = !values[index];
        }
        return inverted;
    }

    @Override
    public boolean isSingle() {
        return booleans.isSingle();
    }

    @Override
    public Class<? extends Boolean> getReturnType() {
        return Boolean.class;
    }

    @Override
    public String toString(@Nullable SkriptEvent event, boolean debug) {
        return "inverse of " + booleans.toString(event, debug);
    }
}
