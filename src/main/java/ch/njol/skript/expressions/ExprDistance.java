package ch.njol.skript.expressions;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.simplification.SimplifiedLiteral;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.util.Kleenean;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.fabric.compat.FabricLocation;
import org.skriptlang.skript.lang.event.SkriptEvent;

@Name("Distance")
@Description("The distance between two points.")
@Example("distance between player and {_spawn}")
@Since("1.0")
public class ExprDistance extends SimpleExpression<Number> {

    static {
        Skript.registerExpression(ExprDistance.class, Number.class,
                "[the] distance between %location% and %location%");
    }

    private Expression<FabricLocation> first;
    private Expression<FabricLocation> second;

    @Override
    @SuppressWarnings("unchecked")
    public boolean init(Expression<?>[] vars, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
        first = (Expression<FabricLocation>) vars[0];
        second = (Expression<FabricLocation>) vars[1];
        return true;
    }

    @Override
    protected Number @Nullable [] get(SkriptEvent event) {
        FabricLocation left = first.getSingle(event);
        FabricLocation right = second.getSingle(event);
        if (left == null || right == null) {
            return new Number[0];
        }
        if (left.level() != right.level()) {
            Skript.error("Cannot calculate the distance between locations from two different worlds");
            return new Number[0];
        }
        return new Number[]{left.position().distanceTo(right.position())};
    }

    @Override
    public boolean isSingle() {
        return true;
    }

    @Override
    public Class<? extends Number> getReturnType() {
        return Number.class;
    }

    @Override
    public Expression<? extends Number> simplify() {
        if (first instanceof Literal<?> && second instanceof Literal<?>) {
            return SimplifiedLiteral.fromExpression(this);
        }
        return this;
    }

    @Override
    public String toString(@Nullable SkriptEvent event, boolean debug) {
        return "distance between " + first.toString(event, debug) + " and " + second.toString(event, debug);
    }
}
