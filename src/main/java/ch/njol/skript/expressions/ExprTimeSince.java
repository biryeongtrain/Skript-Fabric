package ch.njol.skript.expressions;

import ch.njol.skript.Skript;
import ch.njol.skript.expressions.base.SimplePropertyExpression;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.util.Date;
import ch.njol.skript.util.Timespan;
import ch.njol.util.Kleenean;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.lang.event.SkriptEvent;

public class ExprTimeSince extends SimplePropertyExpression<Date, Timespan> {

    static {
        Skript.registerExpression(ExprTimeSince.class, Timespan.class,
                "[the] time since %dates%",
                "[the] (time [remaining]|remaining time) until %dates%");
    }

    private boolean since;

    @Override
    public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
        since = matchedPattern == 0;
        return super.init(expressions, matchedPattern, isDelayed, parseResult);
    }

    @Override
    public @Nullable Timespan convert(Date date) {
        Date now = Date.now();
        if (since ? date.compareTo(now) <= 0 : date.compareTo(now) >= 0) {
            return date.difference(now);
        }
        return new Timespan();
    }

    @Override
    public Class<? extends Timespan> getReturnType() {
        return Timespan.class;
    }

    @Override
    protected String getPropertyName() {
        return "time " + (since ? "since" : "until");
    }

    @Override
    public String toString(@Nullable SkriptEvent event, boolean debug) {
        return "the time " + (since ? "since " : "until ") + getExpr().toString(event, debug);
    }
}
