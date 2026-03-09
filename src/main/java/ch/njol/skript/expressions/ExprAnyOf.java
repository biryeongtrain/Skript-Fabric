package ch.njol.skript.expressions;

import ch.njol.skript.Skript;
import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.expressions.base.WrapperExpression;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.util.LiteralUtils;
import ch.njol.util.Kleenean;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.lang.event.SkriptEvent;

public class ExprAnyOf extends WrapperExpression<Object> {

    static {
        Skript.registerExpression(ExprAnyOf.class, Object.class, "(any [one]|one) of [the] %objects%");
    }

    @Override
    public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
        Expression<?> expr = LiteralUtils.defendExpression(expressions[0]);
        setExpr(expr);
        return LiteralUtils.canInitSafely(expr);
    }

    @Override
    public @Nullable Class<?>[] acceptChange(ChangeMode mode) {
        return null;
    }

    @Override
    public boolean getAnd() {
        return false;
    }

    @Override
    public boolean isSingle() {
        return true;
    }

    @Override
    public String toString(@Nullable SkriptEvent event, boolean debug) {
        return "any of " + getExpr().toString(event, debug);
    }
}
