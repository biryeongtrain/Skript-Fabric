package ch.njol.skript.expressions;

import ch.njol.skript.Skript;
import ch.njol.skript.classes.ClassInfo;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionList;
import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.skript.util.LiteralUtils;
import ch.njol.util.Kleenean;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.lang.event.SkriptEvent;

public class ExprRandom extends SimpleExpression<Object> {

    private Expression<?> expr;

    @Override
    @SuppressWarnings("unchecked")
    public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
        if (exprs.length != 2 || !(exprs[0] instanceof Literal<?>)) {
            return false;
        }
        ClassInfo<?> classInfo = ((Literal<ClassInfo<?>>) exprs[0]).getSingle(SkriptEvent.EMPTY);
        if (classInfo == null) {
            return false;
        }
        if (LiteralUtils.hasUnparsedLiteral(exprs[1])) {
            expr = LiteralUtils.defendExpression(exprs[1]);
            if (expr instanceof ExpressionList<?> expressionList) {
                Class<?> type = classInfo.getC();
                List<Expression<?>> expressions = new ArrayList<>();
                for (Expression<?> expression : expressionList.getExpressions()) {
                    Expression<?> converted = expression.getConvertedExpression(type);
                    if (converted != null) {
                        expressions.add(converted);
                    }
                }
                if (expressions.isEmpty()) {
                    Skript.error("There are no objects of type '" + exprs[0].toString() + "' in the list " + exprs[1].toString());
                    return false;
                }
                expr = expressions.get(ThreadLocalRandom.current().nextInt(expressions.size()));
            }
        } else {
            expr = exprs[1].getConvertedExpression(classInfo.getC());
        }
        return expr != null && LiteralUtils.canInitSafely(expr);
    }

    @Override
    protected Object @Nullable [] get(SkriptEvent event) {
        Object[] values = expr.getAll(event);
        if (values.length <= 1) {
            return values;
        }
        Object[] one = (Object[]) Array.newInstance(values.getClass().getComponentType(), 1);
        one[0] = values[ThreadLocalRandom.current().nextInt(values.length)];
        return one;
    }

    @Override
    public boolean isSingle() {
        return true;
    }

    @Override
    public Class<? extends Object> getReturnType() {
        return expr.getReturnType();
    }

    @Override
    public String toString(@Nullable SkriptEvent event, boolean debug) {
        return "a random element out of " + expr.toString(event, debug);
    }
}
