package ch.njol.skript.expressions;

import ch.njol.skript.expressions.base.SimplePropertyExpression;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.simplification.SimplifiedLiteral;
import ch.njol.skript.util.Date;
import org.jetbrains.annotations.Nullable;

public class ExprUnixTicks extends SimplePropertyExpression<Date, Number> {

    static {
        register(ExprUnixTicks.class, Number.class, "unix timestamp", "dates");
    }

    @Override
    public @Nullable Number convert(Date date) {
        return date.getTime() / 1000.0;
    }

    @Override
    public Class<? extends Number> getReturnType() {
        return Number.class;
    }

    @Override
    public Expression<? extends Number> simplify() {
        if (getExpr() instanceof Literal<? extends Date>) {
            return SimplifiedLiteral.fromExpression(this);
        }
        return this;
    }

    @Override
    protected String getPropertyName() {
        return "unix timestamp";
    }
}
