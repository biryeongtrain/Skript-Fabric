package ch.njol.skript.expressions;

import ch.njol.skript.Skript;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.simplification.SimplifiedLiteral;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.util.Kleenean;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.lang.event.SkriptEvent;

public class ExprNumberOfCharacters extends SimpleExpression<Long> {

    static {
        Skript.registerExpression(ExprNumberOfCharacters.class, Long.class,
                "number of upper[ ]case char(acters|s) in %string%",
                "number of lower[ ]case char(acters|s) in %string%",
                "number of digit char(acters|s) in %string%");
    }

    private int pattern;
    private Expression<String> expr;

    @Override
    @SuppressWarnings("unchecked")
    public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
        pattern = matchedPattern;
        expr = (Expression<String>) exprs[0];
        return true;
    }

    @Override
    protected Long @Nullable [] get(SkriptEvent event) {
        String value = expr.getSingle(event);
        if (value == null) {
            return null;
        }

        long count = 0;
        for (int codePoint : (Iterable<Integer>) value.codePoints()::iterator) {
            if ((pattern == 0 && Character.isUpperCase(codePoint))
                    || (pattern == 1 && Character.isLowerCase(codePoint))
                    || (pattern == 2 && Character.isDigit(codePoint))) {
                count++;
            }
        }
        return new Long[]{count};
    }

    @Override
    public boolean isSingle() {
        return true;
    }

    @Override
    public Class<? extends Long> getReturnType() {
        return Long.class;
    }

    @Override
    public Expression<? extends Long> simplify() {
        if (expr instanceof Literal<String>) {
            return SimplifiedLiteral.fromExpression(this);
        }
        return this;
    }

    @Override
    public String toString(@Nullable SkriptEvent event, boolean debug) {
        return switch (pattern) {
            case 0 -> "number of uppercase characters in " + expr.toString(event, debug);
            case 1 -> "number of lowercase characters in " + expr.toString(event, debug);
            default -> "number of digits in " + expr.toString(event, debug);
        };
    }
}
