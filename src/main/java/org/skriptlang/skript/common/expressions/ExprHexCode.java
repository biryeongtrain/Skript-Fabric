package org.skriptlang.skript.common.expressions;

import ch.njol.skript.expressions.base.SimplePropertyExpression;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.simplification.SimplifiedLiteral;
import ch.njol.skript.util.Color;
import java.util.Locale;
import org.jetbrains.annotations.Nullable;

public class ExprHexCode extends SimplePropertyExpression<Color, String> {

    static {
        register(ExprHexCode.class, String.class, "hex[adecimal] code", "colors");
    }

    @Override
    public @Nullable String convert(Color color) {
        return String.format(Locale.ENGLISH, "%02X%02X%02X", color.red(), color.green(), color.blue());
    }

    @Override
    public Class<? extends String> getReturnType() {
        return String.class;
    }

    @Override
    protected String getPropertyName() {
        return "hexadecimal code";
    }

    @Override
    public Expression<? extends String> simplify() {
        if (getExpr() instanceof Literal<?>) {
            return SimplifiedLiteral.fromExpression(this);
        }
        return this;
    }
}
