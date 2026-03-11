package ch.njol.skript.expressions;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.WrapperExpression;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.registrations.EventValues;
import ch.njol.util.Kleenean;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.lang.event.SkriptEvent;

@Name("Former/Future State")
@Description({
        "Represents the value of an expression before an event happened or the value it will have directly after the event.",
        "If you do not specify whether to use the past or future state, the wrapped expression keeps its default state."
})
@Example("on teleport:\n\tformer world was \"world_nether\"")
@Example("on weather change:\n\tset {weather::%world%::old} to past weather")
@Since("1.1")
public class ExprTimeState extends WrapperExpression<Object> {

    static {
        Skript.registerExpression(
                ExprTimeState.class,
                Object.class,
                "[the] (former|past|old) [state] [of] %~objects%",
                "%~objects% before [the event]",
                "[the] (future|to-be|new) [state] [of] %~objects%",
                "%~objects%(-to-be| after[(wards| the event)])"
        );
    }

    @Override
    public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
        Expression<?> expression = expressions[0];
        if (isDelayed == Kleenean.TRUE) {
            Skript.error("Cannot use time states after the event has already passed");
            return false;
        }
        int time = matchedPattern >= 2 ? EventValues.TIME_FUTURE : EventValues.TIME_PAST;
        if (!expression.setTime(time)) {
            Skript.error(expression + " does not have a " + (time == EventValues.TIME_FUTURE ? "future" : "past") + " state");
            return false;
        }
        setExpr(expression);
        return true;
    }

    @Override
    public String toString(@Nullable SkriptEvent event, boolean debug) {
        return "the " + (getTime() == EventValues.TIME_PAST ? "past" : "future") + " state of " + getExpr().toString(event, debug);
    }

    @Override
    public boolean setTime(int time) {
        return time == getTime();
    }
}
