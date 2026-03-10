package ch.njol.skript.expressions;

import ch.njol.skript.expressions.base.SimplePropertyExpression;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.simplification.SimplifiedLiteral;
import ch.njol.skript.util.Color;
import ch.njol.util.Kleenean;
import java.util.Locale;

public class ExprARGB extends SimplePropertyExpression<Color, Integer> {

    static {
        register(ExprARGB.class, Integer.class, "(:alpha|:red|:green|:blue) (value|component)", "colors");
    }

    private Channel channel;

    @Override
    public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
        channel = Channel.valueOf(parseResult.tags.get(0).toUpperCase(Locale.ENGLISH));
        return super.init(expressions, matchedPattern, isDelayed, parseResult);
    }

    @Override
    public Integer convert(Color from) {
        return channel.value(from);
    }

    @Override
    public Class<? extends Integer> getReturnType() {
        return Integer.class;
    }

    @Override
    protected String getPropertyName() {
        return channel.name().toLowerCase(Locale.ENGLISH);
    }

    @Override
    public Expression<? extends Integer> simplify() {
        if (getExpr() instanceof Literal<? extends Color>) {
            return SimplifiedLiteral.fromExpression(this);
        }
        return this;
    }

    private enum Channel {
        ALPHA {
            @Override
            int value(Color color) {
                return 255;
            }
        },
        RED {
            @Override
            int value(Color color) {
                return color.red();
            }
        },
        GREEN {
            @Override
            int value(Color color) {
                return color.green();
            }
        },
        BLUE {
            @Override
            int value(Color color) {
                return color.blue();
            }
        };

        abstract int value(Color color);
    }
}
