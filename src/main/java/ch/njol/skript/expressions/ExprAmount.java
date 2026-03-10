package ch.njol.skript.expressions;

import ch.njol.skript.Skript;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionList;
import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.simplification.SimplifiedLiteral;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.skript.lang.util.common.AnyAmount;
import ch.njol.skript.util.LiteralUtils;
import ch.njol.util.Kleenean;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.lang.event.SkriptEvent;

public class ExprAmount extends SimpleExpression<Number> {

    static {
        Skript.registerExpression(ExprAmount.class, Number.class,
                "[the] (amount|number|size) of %objects%");
    }

    private ExpressionList<?> expressions;
    private @Nullable Expression<AnyAmount> anyAmount;

    @Override
    @SuppressWarnings({"unchecked", "rawtypes"})
    public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
        Expression<?> expression = LiteralUtils.defendExpression(exprs[0]);
        if (!LiteralUtils.canInitSafely(expression)) {
            return false;
        }

        if (expression.isSingle()) {
            anyAmount = (Expression<AnyAmount>) expression.getConvertedExpression(AnyAmount.class);
        }

        expressions = expression instanceof ExpressionList<?> expressionList
                ? expressionList
                : new ExpressionList(new Expression[]{expression}, Object.class, false);

        if (anyAmount == null && expressions.isSingle()) {
            Skript.error("'" + expressions.toString(null, Skript.debug())
                    + "' can only ever have one value at most, thus the 'amount of ...' expression is useless. "
                    + "Use '... exists' instead to find out whether the expression has a value.");
            return false;
        }
        return true;
    }

    @Override
    protected Number @Nullable [] get(SkriptEvent event) {
        if (anyAmount != null) {
            AnyAmount value = anyAmount.getSingle(event);
            return new Number[]{value == null ? 0 : value.amount()};
        }
        return new Long[]{(long) expressions.getArray(event).length};
    }

    @Override
    public boolean isSingle() {
        return true;
    }

    @Override
    public Class<? extends Number> getReturnType() {
        return anyAmount != null ? Number.class : Long.class;
    }

    @Override
    public Expression<? extends Number> simplify() {
        if ((anyAmount != null && anyAmount instanceof Literal<AnyAmount>)
                || (anyAmount == null && expressions instanceof Literal<?>)) {
            return SimplifiedLiteral.fromExpression(this);
        }
        return this;
    }

    @Override
    public String toString(@Nullable SkriptEvent event, boolean debug) {
        return "amount of " + (anyAmount != null ? anyAmount.toString(event, debug) : expressions.toString(event, debug));
    }
}
