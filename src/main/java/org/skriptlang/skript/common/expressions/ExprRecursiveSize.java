package org.skriptlang.skript.common.expressions;

import ch.njol.skript.Skript;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionList;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.Variable;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.skript.util.LiteralUtils;
import ch.njol.skript.variables.Variables;
import ch.njol.util.Kleenean;
import java.util.Map;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.lang.event.SkriptEvent;

public class ExprRecursiveSize extends SimpleExpression<Long> {

    static {
        Skript.registerExpression(ExprRecursiveSize.class, Long.class,
                "[the] recursive (amount|number|size) of %objects%");
    }

    private ExpressionList<?> exprs;

    @Override
    public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
        exprs = expressions[0] instanceof ExpressionList<?> exprList
                ? exprList
                : new ExpressionList<>(new Expression<?>[]{expressions[0]}, Object.class, false);

        exprs = (ExpressionList<?>) LiteralUtils.defendExpression(exprs);
        if (!LiteralUtils.canInitSafely(exprs)) {
            return false;
        }
        if (exprs.isSingle()) {
            Skript.error("'" + exprs.toString(null, Skript.debug()) + "' can only ever have one value at most, thus the 'recursive size of ...' expression is useless. Use '... exists' instead to find out whether the expression has a value.");
            return false;
        }
        for (Expression<?> expr : exprs.getExpressions()) {
            if (!(expr instanceof Variable<?>)) {
                Skript.error("Getting the recursive size of a list only applies to variables, thus the '" + expr.toString(null, Skript.debug()) + "' expression is useless.");
                return false;
            }
        }
        return true;
    }

    @Override
    protected Long @Nullable [] get(SkriptEvent event) {
        long total = 0;
        for (Expression<?> expr : exprs.getExpressions()) {
            Variable<?> variable = (Variable<?>) expr;
            Object value = Variables.getVariable(variable.getName().toString(event), event, variable.isLocal());
            if (value instanceof Map<?, ?> map) {
                total += getRecursiveSize(map, true);
            } else if (value != null) {
                total++;
            }
        }
        return new Long[]{total};
    }

    private static long getRecursiveSize(Map<?, ?> map, boolean skipNullKey) {
        long count = 0;
        for (Map.Entry<?, ?> entry : map.entrySet()) {
            if (skipNullKey && entry.getKey() == null) {
                continue;
            }
            Object value = entry.getValue();
            if (value instanceof Map<?, ?> nestedMap) {
                count += getRecursiveSize(nestedMap, false);
            } else {
                count++;
            }
        }
        return count;
    }

    @Override
    public boolean isSingle() {
        return true;
    }

    @Override
    public Class<? extends Long> getReturnType() {
        return Long.class;
    }

    @Override
    public String toString(@Nullable SkriptEvent event, boolean debug) {
        return "recursive size of " + exprs.toString(event, debug);
    }
}
