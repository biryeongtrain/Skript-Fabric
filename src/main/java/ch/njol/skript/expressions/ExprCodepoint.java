package ch.njol.skript.expressions;

import ch.njol.skript.expressions.base.SimplePropertyExpression;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.simplification.SimplifiedLiteral;
import org.jetbrains.annotations.Nullable;

public class ExprCodepoint extends SimplePropertyExpression<String, Integer> {

    static {
        register(ExprCodepoint.class, Integer.class, "[unicode|character] code([ ]point| position)", "strings");
    }

    @Override
    public @Nullable Integer convert(String string) {
        if (string.isEmpty()) {
            return null;
        }
        return string.codePointAt(0);
    }

    @Override
    public Class<? extends Integer> getReturnType() {
        return Integer.class;
    }

    @Override
    public Expression<? extends Integer> simplify() {
        if (getExpr() instanceof Literal<? extends String>) {
            return SimplifiedLiteral.fromExpression(this);
        }
        return this;
    }

    @Override
    protected String getPropertyName() {
        return "codepoint";
    }
}
