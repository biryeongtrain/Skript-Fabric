package ch.njol.skript.expressions;

import ch.njol.skript.Skript;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.simplification.SimplifiedLiteral;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.skript.util.Date;
import ch.njol.skript.util.Timespan;
import ch.njol.util.Kleenean;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.lang.event.SkriptEvent;

public class ExprDateAgoLater extends SimpleExpression<Date> {

    static {
        Skript.registerExpression(ExprDateAgoLater.class, Date.class,
                "%timespan% (ago|in the past|before [the] [date] %-date%)",
                "%timespan% (later|(from|after) [the] [date] %-date%)");
    }

    private Expression<Timespan> timespan;
    @Nullable
    private Expression<Date> date;
    private boolean ago;

    @Override
    @SuppressWarnings("unchecked")
    public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
        timespan = (Expression<Timespan>) exprs[0];
        date = (Expression<Date>) exprs[1];
        ago = matchedPattern == 0;
        return true;
    }

    @Override
    protected Date @Nullable [] get(SkriptEvent event) {
        Timespan delta = timespan.getSingle(event);
        Date origin = date != null ? date.getSingle(event) : new Date();
        if (delta == null || origin == null) {
            return null;
        }
        return new Date[]{ago ? origin.minus(delta) : origin.plus(delta)};
    }

    @Override
    public boolean isSingle() {
        return true;
    }

    @Override
    public Class<? extends Date> getReturnType() {
        return Date.class;
    }

    @Override
    public Expression<? extends Date> simplify() {
        if (date instanceof Literal<Date> && timespan instanceof Literal<Timespan>) {
            return SimplifiedLiteral.fromExpression(this);
        }
        return this;
    }

    @Override
    public String toString(@Nullable SkriptEvent event, boolean debug) {
        return timespan.toString(event, debug) + " " + (ago
                ? (date != null ? "before " + date.toString(event, debug) : "ago")
                : (date != null ? "after " + date.toString(event, debug) : "later"));
    }
}
