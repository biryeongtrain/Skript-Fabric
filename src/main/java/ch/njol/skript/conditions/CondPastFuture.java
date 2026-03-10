package ch.njol.skript.conditions;

import ch.njol.skript.Skript;
import ch.njol.skript.lang.Condition;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.SimplifiedCondition;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.util.Date;
import ch.njol.util.Kleenean;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.lang.event.SkriptEvent;

public class CondPastFuture extends Condition {

    static {
        Skript.registerCondition(CondPastFuture.class,
                "%dates% (is|are)[negated:(n't| not)] in the (past|:future)",
                "%dates% ha(s|ve)[negated:(n't| not)] passed");
    }

    private Expression<Date> dates;
    private boolean future;

    @Override
    @SuppressWarnings("unchecked")
    public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
        setNegated(parseResult.hasTag("negated"));
        future = parseResult.hasTag("future");
        dates = (Expression<Date>) expressions[0];
        return true;
    }

    @Override
    public boolean check(SkriptEvent event) {
        if (future) {
            return dates.check(event, date -> date.compareTo(new Date()) > 0, isNegated());
        }
        return dates.check(event, date -> date.compareTo(new Date()) < 0, isNegated());
    }

    public Condition simplify() {
        if (dates instanceof Literal<Date>) {
            return SimplifiedCondition.fromCondition(this);
        }
        return this;
    }

    @Override
    public String toString(@Nullable SkriptEvent event, boolean debug) {
        return dates.toString(event, debug) + (dates.isSingle() ? " is" : " are") + " in the " + (future ? "future" : "past");
    }
}
