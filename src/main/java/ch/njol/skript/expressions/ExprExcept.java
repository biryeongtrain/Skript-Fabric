package ch.njol.skript.expressions;

import ch.njol.skript.Skript;
import ch.njol.skript.expressions.base.WrapperExpression;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionList;
import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.SyntaxStringBuilder;
import ch.njol.skript.lang.simplification.SimplifiedLiteral;
import ch.njol.skript.util.LiteralUtils;
import ch.njol.util.Kleenean;
import java.lang.reflect.Array;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.lang.event.SkriptEvent;
import org.skriptlang.skript.lang.comparator.Comparators;
import org.skriptlang.skript.lang.comparator.Relation;

public class ExprExcept extends WrapperExpression<Object> {

    static {
        Skript.registerExpression(ExprExcept.class, Object.class, "%objects% (except|excluding|not including) %objects%");
    }

    private Expression<?> exclude;

    @Override
    public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
        Expression<?> source = LiteralUtils.defendExpression(expressions[0]);
        setExpr(source);
        if (source.isSingle() && !(source instanceof ExpressionList<?>)) {
            Skript.error("Must provide a list containing more than one object to exclude objects from.");
            return false;
        }
        exclude = LiteralUtils.defendExpression(expressions[1]);
        return LiteralUtils.canInitSafely(source) && LiteralUtils.canInitSafely(exclude);
    }

    @Override
    protected Object @Nullable [] get(SkriptEvent event) {
        Object[] excluded = exclude.getArray(event);
        if (excluded.length == 0) {
            return getExpr().getArray(event);
        }
        return getExpr().streamAll(event)
            .filter(sourceObject -> {
                for (Object excludedObject : excluded) {
                    if (sourceObject.equals(excludedObject) || Comparators.compare(sourceObject, excludedObject) == Relation.EQUAL) {
                        return false;
                    }
                }
                return true;
            })
            .toArray(length -> (Object[]) Array.newInstance(getReturnType(), length));
    }

    @Override
    public Expression<?> simplify() {
        setExpr(getExpr().simplify());
        if (getExpr() instanceof Literal<?> && exclude instanceof Literal<?>) {
            return SimplifiedLiteral.fromExpression(this);
        }
        return this;
    }

    @Override
    public String toString(@Nullable SkriptEvent event, boolean debug) {
        return new SyntaxStringBuilder(event, debug)
            .append(getExpr(), "except", exclude)
            .toString();
    }
}
