package ch.njol.skript.expressions;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.PropertyExpression;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.SyntaxStringBuilder;
import ch.njol.skript.lang.simplification.SimplifiedLiteral;
import ch.njol.skript.util.Patterns;
import ch.njol.util.Kleenean;
import ch.njol.util.Math2;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.lang.event.SkriptEvent;

@Name("Rounding")
@Description("Rounds numbers normally, up (ceiling) or down (floor) respectively.")
@Example("set {var} to rounded health of player")
@Example("set line 1 of the block to \"%rounded (1.5 * player's level)%\"")
@Example("add rounded down argument to the player's health")
@Since("2.0")
public class ExprRound extends PropertyExpression<Number, Long> {

    private enum RoundType {
        FLOOR, ROUND, CEIL
    }

    private static final Patterns<RoundType> PATTERNS = new Patterns<>(new Object[][]{
            {"[a|the] (round[ed] down|floored) %numbers%", RoundType.FLOOR},
            {"%numbers% (round[ed] down|floored)", RoundType.FLOOR},
            {"[a|the] round[ed] %numbers%", RoundType.ROUND},
            {"%numbers% round[ed]", RoundType.ROUND},
            {"[a|the] (round[ed] up|ceil[ing]ed) %numbers%", RoundType.CEIL},
            {"%numbers% (round[ed] up|ceil[ing]ed)", RoundType.CEIL}
    });

    static {
        Skript.registerExpression(ExprRound.class, Long.class, PATTERNS.getPatterns());
    }

    private RoundType roundType;

    @Override
    @SuppressWarnings("unchecked")
    public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
        setExpr((Expression<? extends Number>) exprs[0]);
        roundType = PATTERNS.getInfo(matchedPattern);
        return true;
    }

    @Override
    protected Long @Nullable [] get(SkriptEvent event, Number[] source) {
        return get(source, number -> {
            if (number instanceof Integer integer) {
                return integer.longValue();
            } else if (number instanceof Long longValue) {
                return longValue;
            }

            return switch (roundType) {
                case FLOOR -> Math2.floor(number.doubleValue());
                case ROUND -> Math2.round(number.doubleValue());
                case CEIL -> Math2.ceil(number.doubleValue());
            };
        });
    }

    @Override
    public Class<? extends Long> getReturnType() {
        return Long.class;
    }

    @Override
    public Expression<? extends Long> simplify() {
        if (getExpr() instanceof Literal<? extends Number>) {
            return SimplifiedLiteral.fromExpression(this);
        }
        return this;
    }

    @Override
    public String toString(@Nullable SkriptEvent event, boolean debug) {
        SyntaxStringBuilder builder = new SyntaxStringBuilder(event, debug);
        builder.append(switch (roundType) {
            case FLOOR -> "floored";
            case ROUND -> "rounded";
            case CEIL -> "ceiled";
        });
        builder.append(getExpr());
        return builder.toString();
    }
}
