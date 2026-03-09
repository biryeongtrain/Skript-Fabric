package ch.njol.skript.expressions;

import ch.njol.skript.Skript;
import ch.njol.skript.expressions.base.SimplePropertyExpression;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.simplification.SimplifiedLiteral;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.lang.event.SkriptEvent;

public class ExprCharacterFromCodepoint extends SimplePropertyExpression<Integer, String> {

    static {
        Skript.registerExpression(ExprCharacterFromCodepoint.class, String.class,
                "character (from|at|with) code([ ]point| position) %integer%");
    }

    @Override
    public @Nullable String convert(Integer integer) {
        return String.valueOf((char) integer.intValue());
    }

    @Override
    public Class<? extends String> getReturnType() {
        return String.class;
    }

    @Override
    public Expression<? extends String> simplify() {
        if (getExpr() instanceof Literal<? extends Integer>) {
            return SimplifiedLiteral.fromExpression(this);
        }
        return this;
    }

    @Override
    public String toString(@Nullable SkriptEvent event, boolean debug) {
        return "character at codepoint " + getExpr().toString(event, debug);
    }

    @Override
    protected String getPropertyName() {
        assert false;
        return null;
    }
}
