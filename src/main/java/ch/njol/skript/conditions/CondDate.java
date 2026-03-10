package ch.njol.skript.conditions;

import ch.njol.skript.Skript;
import ch.njol.skript.lang.Condition;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.SimplifiedCondition;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.util.Date;
import ch.njol.skript.util.Timespan;
import ch.njol.util.Kleenean;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.lang.event.SkriptEvent;

public class CondDate extends Condition {

    static {
        Skript.registerCondition(CondDate.class,
                "%date% (was|were)( more|(n't| not) less) than %timespan% [ago]",
                "%date% (was|were)((n't| not) more| less) than %timespan% [ago]");
    }

    private Expression<Date> date;
    private Expression<Timespan> delta;

    @Override
    @SuppressWarnings("unchecked")
    public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
        date = (Expression<Date>) exprs[0];
        delta = (Expression<Timespan>) exprs[1];
        setNegated(matchedPattern == 1);
        return true;
    }

    @Override
    public boolean check(SkriptEvent event) {
        long now = System.currentTimeMillis();
        return date.check(event,
                value -> delta.check(event,
                        timespan -> now - value.getTime() >= timespan.getAs(Timespan.TimePeriod.MILLISECOND)
                ),
                isNegated());
    }

    public Condition simplify() {
        if (date instanceof Literal<Date> && delta instanceof Literal<Timespan>) {
            return SimplifiedCondition.fromCondition(this);
        }
        return this;
    }

    @Override
    public String toString(@Nullable SkriptEvent event, boolean debug) {
        return date.toString(event, debug) + " was " + (isNegated() ? "less" : "more") + " than " + delta.toString(event, debug) + " ago";
    }
}
