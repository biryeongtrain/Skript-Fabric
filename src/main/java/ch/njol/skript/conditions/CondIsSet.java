package ch.njol.skript.conditions;

import ch.njol.skript.Skript;
import ch.njol.skript.lang.Condition;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionList;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.VerboseAssert;
import ch.njol.util.Kleenean;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.lang.event.SkriptEvent;

public class CondIsSet extends Condition implements VerboseAssert {

    static {
        Skript.registerCondition(CondIsSet.class,
                "%~objects% (exist[s]|(is|are) set)",
                "%~objects% (do[es](n't| not) exist|(is|are)(n't| not) set)");
    }

    private Expression<?> expr;

    @Override
    public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
        expr = exprs[0];
        setNegated(matchedPattern == 1);
        return true;
    }

    @Override
    public boolean check(SkriptEvent event) {
        return check(expr, event);
    }

    private boolean check(Expression<?> expression, SkriptEvent event) {
        if (expression instanceof ExpressionList<?> list) {
            for (Expression<?> child : list.getExpressions()) {
                boolean value = check(child, event);
                if (expression.getAnd() ^ value) {
                    return !expression.getAnd();
                }
            }
            return expression.getAnd();
        }
        return isNegated() ^ (expression.getAll(event).length != 0);
    }

    @Override
    public String getExpectedMessage(SkriptEvent event) {
        return isNegated() ? "none" : "a value";
    }

    @Override
    public String getReceivedMessage(SkriptEvent event) {
        return VerboseAssert.getExpressionValue(expr, event);
    }

    @Override
    public String toString(@Nullable SkriptEvent event, boolean debug) {
        return expr.toString(event, debug) + (isNegated() ? " isn't" : " is") + " set";
    }
}
