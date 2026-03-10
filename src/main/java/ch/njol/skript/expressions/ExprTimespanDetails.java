package ch.njol.skript.expressions;

import ch.njol.skript.expressions.base.SimplePropertyExpression;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.simplification.SimplifiedLiteral;
import ch.njol.skript.util.Timespan;
import ch.njol.skript.util.Timespan.TimePeriod;
import ch.njol.util.Kleenean;
import java.util.Locale;

public class ExprTimespanDetails extends SimplePropertyExpression<Timespan, Long> {

    static {
        register(ExprTimespanDetails.class, Long.class,
                "(:(tick|second|minute|hour|day|week|month|year))s", "timespans");
    }

    private TimePeriod type;

    @Override
    public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
        type = TimePeriod.valueOf(parseResult.tags.get(0).toUpperCase(Locale.ENGLISH));
        return super.init(exprs, matchedPattern, isDelayed, parseResult);
    }

    @Override
    public Long convert(Timespan time) {
        return time.getAs(Timespan.TimePeriod.MILLISECOND) / type.getTime();
    }

    @Override
    public Class<? extends Long> getReturnType() {
        return Long.class;
    }

    @Override
    public Expression<? extends Long> simplify() {
        if (getExpr() instanceof Literal<? extends Timespan>) {
            return SimplifiedLiteral.fromExpression(this);
        }
        return this;
    }

    @Override
    protected String getPropertyName() {
        return type.name().toLowerCase(Locale.ENGLISH);
    }
}
