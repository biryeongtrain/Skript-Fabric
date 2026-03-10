package ch.njol.skript.expressions;

import ch.njol.skript.Skript;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.util.Kleenean;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.fabric.compat.FabricLocation;
import org.skriptlang.skript.lang.event.SkriptEvent;

public class ExprDistance extends SimpleExpression<Number> {

    static {
        Skript.registerExpression(ExprDistance.class, Number.class, "[the] distance between %location% and %location%");
    }

    private Expression<FabricLocation> loc1;
    private Expression<FabricLocation> loc2;

    @Override
    @SuppressWarnings("unchecked")
    public boolean init(Expression<?>[] vars, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
        loc1 = (Expression<FabricLocation>) vars[0];
        loc2 = (Expression<FabricLocation>) vars[1];
        return true;
    }

    @Override
    protected Number @Nullable [] get(SkriptEvent event) {
        FabricLocation one = loc1.getSingle(event);
        FabricLocation two = loc2.getSingle(event);
        if (one == null || two == null) {
            return new Number[0];
        }
        if (one.level() != two.level()) {
            return new Number[0];
        }
        return new Number[]{one.position().distanceTo(two.position())};
    }

    @Override
    public boolean isSingle() {
        return true;
    }

    @Override
    public Class<? extends Number> getReturnType() {
        return Number.class;
    }
}
