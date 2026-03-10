package ch.njol.skript.expressions;

import ch.njol.skript.Skript;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.simplification.SimplifiedLiteral;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.util.Kleenean;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.lang.event.SkriptEvent;

public class ExprAngle extends SimpleExpression<Number> {

    static {
        Skript.registerExpression(ExprAngle.class, Number.class,
                "%number% [in] deg[ree][s]",
                "%number% [in] rad[ian][s]",
                "%numbers% in deg[ree][s]",
                "%numbers% in rad[ian][s]");
    }

    private Expression<Number> angle;
    private boolean radians;

    @Override
    @SuppressWarnings("unchecked")
    public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
        angle = (Expression<Number>) expressions[0];
        radians = (matchedPattern & 1) == 1;
        return true;
    }

    @Override
    protected Number @Nullable [] get(SkriptEvent event) {
        Number[] numbers = angle.getArray(event);
        if (!radians) {
            return numbers;
        }
        Double[] degrees = new Double[numbers.length];
        for (int i = 0; i < numbers.length; i++) {
            degrees[i] = Math.toDegrees(numbers[i].doubleValue());
        }
        return degrees;
    }

    @Override
    public boolean isSingle() {
        return angle.isSingle();
    }

    @Override
    public Class<? extends Number> getReturnType() {
        return Number.class;
    }

    @Override
    public Expression<? extends Number> simplify() {
        if (angle instanceof Literal<?>) {
            return SimplifiedLiteral.fromExpression(this);
        }
        return this;
    }

    @Override
    public String toString(@Nullable SkriptEvent event, boolean debug) {
        return angle.toString(event, debug) + " in " + (radians ? "degrees" : "radians");
    }
}
