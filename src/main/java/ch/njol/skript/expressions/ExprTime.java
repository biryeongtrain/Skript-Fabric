package ch.njol.skript.expressions;

import ch.njol.skript.Skript;
import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.expressions.base.PropertyExpression;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.util.Time;
import ch.njol.skript.util.Timeperiod;
import ch.njol.skript.util.Timespan;
import ch.njol.util.Kleenean;
import net.minecraft.server.level.ServerLevel;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.lang.event.SkriptEvent;

public class ExprTime extends PropertyExpression<ServerLevel, Time> {

    private static final int TIME_TO_TIMESPAN_OFFSET = 18000;

    static {
        Skript.registerExpression(
                ExprTime.class,
                Time.class,
                "[the] time[s] [([with]in|of) %worlds%]",
                "%worlds%'[s] time[s]"
        );
    }

    @Override
    @SuppressWarnings("unchecked")
    public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, ParseResult parser) {
        setExpr((Expression<ServerLevel>) expressions[0]);
        return true;
    }

    @Override
    protected Time[] get(SkriptEvent event, ServerLevel[] worlds) {
        return get(worlds, world -> new Time((int) world.getDayTime()));
    }

    @Override
    public Class<?> @Nullable [] acceptChange(ChangeMode mode) {
        return switch (mode) {
            case ADD, REMOVE -> new Class[]{Time.class, Timespan.class};
            case SET -> new Class[]{Time.class, Timeperiod.class};
            default -> null;
        };
    }

    @Override
    public void change(SkriptEvent event, Object @Nullable [] delta, ChangeMode mode) {
        if (delta == null || delta.length == 0 || delta[0] == null) {
            return;
        }

        long ticks = ticksForChange(delta[0], mode);
        for (ServerLevel world : getExpr().getArray(event)) {
            switch (mode) {
                case ADD -> world.setDayTime(world.getDayTime() + ticks);
                case REMOVE -> world.setDayTime(world.getDayTime() - ticks);
                case SET -> world.setDayTime(rebaseTimeOfDay(world.getDayTime(), ticks));
                default -> {
                }
            }
        }
    }

    static long ticksForChange(Object time, ChangeMode mode) {
        if (time instanceof Time value) {
            return mode == ChangeMode.SET
                    ? value.getTicks()
                    : value.getTicks() - TIME_TO_TIMESPAN_OFFSET;
        }
        if (time instanceof Timespan value) {
            return value.getAs(Timespan.TimePeriod.TICK);
        }
        if (time instanceof Timeperiod value) {
            return value.start;
        }
        return 0L;
    }

    static long rebaseTimeOfDay(long currentDayTime, long timeOfDayTicks) {
        return Math.floorDiv(currentDayTime, 24000L) * 24000L + Math.floorMod(timeOfDayTicks, 24000L);
    }

    @Override
    public Class<Time> getReturnType() {
        return Time.class;
    }

    @Override
    public String toString(@Nullable SkriptEvent event, boolean debug) {
        return "the time in " + getExpr().toString(event, debug);
    }
}
