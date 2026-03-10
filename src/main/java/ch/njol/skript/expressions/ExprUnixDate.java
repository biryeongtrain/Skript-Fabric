package ch.njol.skript.expressions;

import ch.njol.skript.expressions.base.SimplePropertyExpression;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.simplification.SimplifiedLiteral;
import ch.njol.skript.util.Date;
import org.jetbrains.annotations.Nullable;

public class ExprUnixDate extends SimplePropertyExpression<Number, Date> {

    static {
        register(ExprUnixDate.class, Date.class, "unix date", "numbers");
    }

    @Override
    public @Nullable Date convert(Number value) {
        return new Date((long) (value.doubleValue() * 1000));
    }

    @Override
    public Class<? extends Date> getReturnType() {
        return Date.class;
    }

    @Override
    public Expression<? extends Date> simplify() {
        if (getExpr() instanceof Literal<? extends Number>) {
            return SimplifiedLiteral.fromExpression(this);
        }
        return this;
    }

    @Override
    protected String getPropertyName() {
        return "unix date";
    }
}
