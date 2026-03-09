package ch.njol.skript.expressions;

import ch.njol.skript.expressions.base.SimplePropertyExpression;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.simplification.SimplifiedLiteral;

public class ExprLength extends SimplePropertyExpression<String, Long> {

    static {
        register(ExprLength.class, Long.class, "length", "strings");
    }

    @Override
    public Long convert(String value) {
        return (long) value.length();
    }

    @Override
    public Class<? extends Long> getReturnType() {
        return Long.class;
    }

    @Override
    public Expression<? extends Long> simplify() {
        if (getExpr() instanceof Literal<? extends String>) {
            return SimplifiedLiteral.fromExpression(this);
        }
        return this;
    }

    @Override
    protected String getPropertyName() {
        return "length";
    }
}
