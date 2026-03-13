package org.skriptlang.skript.common.expressions;

import ch.njol.skript.Skript;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.simplification.SimplifiedLiteral;
import ch.njol.skript.util.Color;
import ch.njol.skript.util.ColorRGB;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.lang.event.SkriptEvent;

public class ExprColorFromHexCode extends ch.njol.skript.expressions.base.SimplePropertyExpression<String, Color> {

    static {
        Skript.registerExpression(ExprColorFromHexCode.class, Color.class,
                "[the] colo[u]r[s] (from|of) hex[adecimal] code[s] %strings%");
    }

    @Override
    public @Nullable Color convert(String from) {
        String normalized = from.trim();
        if (normalized.startsWith("#")) {
            normalized = normalized.substring(1);
        }
        if (normalized.length() == 8) {
            normalized = normalized.substring(2);
        }
        if (normalized.length() != 6) {
            Skript.error("Could not parse '" + from + "' as a hex code!");
            return null;
        }
        try {
            return ColorRGB.fromRgb(Integer.parseInt(normalized, 16));
        } catch (NumberFormatException ignored) {
            Skript.error("Could not parse '" + from + "' as a hex code!");
            return null;
        }
    }

    @Override
    public Class<? extends Color> getReturnType() {
        return Color.class;
    }

    @Override
    protected String getPropertyName() {
        return "color of hex code";
    }

    @Override
    public String toString(@Nullable SkriptEvent event, boolean debug) {
        return "the color of hex code " + getExpr().toString(event, debug);
    }

    @Override
    public Expression<? extends Color> simplify() {
        if (getExpr() instanceof Literal<?>) {
            return SimplifiedLiteral.fromExpression(this);
        }
        return this;
    }
}
