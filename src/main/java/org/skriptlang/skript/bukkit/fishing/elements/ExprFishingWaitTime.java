package org.skriptlang.skript.bukkit.fishing.elements;

import ch.njol.skript.Skript;
import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ParseContext;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.skript.registrations.Classes;
import ch.njol.skript.util.Timespan;
import ch.njol.skript.util.Timespan.TimePeriod;
import ch.njol.util.Kleenean;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.fabric.compat.FabricFishingState;
import org.skriptlang.skript.fabric.runtime.FabricFishingEventHandle;
import org.skriptlang.skript.lang.event.SkriptEvent;

public final class ExprFishingWaitTime extends SimpleExpression<Timespan> {

    private static final int DEFAULT_MINIMUM_TICKS = 5 * 20;
    private static final int DEFAULT_MAXIMUM_TICKS = 30 * 20;

    private boolean minimum;

    @Override
    public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
        if (expressions.length != 0) {
            return false;
        }
        if (!getParser().isCurrentEvent(FabricFishingEventHandle.class)) {
            Skript.error("The 'fishing wait time' expression can only be used in a fishing event.");
            return false;
        }
        minimum = matchedPattern == 0;
        return true;
    }

    @Override
    protected Timespan @Nullable [] get(SkriptEvent event) {
        if (!(event.handle() instanceof FabricFishingEventHandle handle)) {
            return null;
        }
        return new Timespan[]{new Timespan(TimePeriod.TICK, minimum ? FabricFishingState.minWaitTime(handle.hook()) : FabricFishingState.maxWaitTime(handle.hook()))};
    }

    @Override
    public boolean isSingle() {
        return true;
    }

    @Override
    public Class<? extends Timespan> getReturnType() {
        return Timespan.class;
    }

    @Override
    public Class<?> @Nullable [] acceptChange(ChangeMode mode) {
        return switch (mode) {
            case SET, ADD, REMOVE, RESET -> new Class[]{Timespan.class, Number.class, String.class};
            default -> null;
        };
    }

    @Override
    public void change(SkriptEvent event, Object @Nullable [] delta, ChangeMode mode) {
        if (!(event.handle() instanceof FabricFishingEventHandle handle)) {
            return;
        }
        int amount = mode == ChangeMode.RESET
                ? (minimum ? DEFAULT_MINIMUM_TICKS : DEFAULT_MAXIMUM_TICKS)
                : Math.max(0, clampTicks(resolveTicks(delta)));
        var hook = handle.hook();
        int current = minimum ? FabricFishingState.minWaitTime(hook) : FabricFishingState.maxWaitTime(hook);
        int next = switch (mode) {
            case SET, RESET -> amount;
            case ADD -> Math.max(0, current + amount);
            case REMOVE -> Math.max(0, current - amount);
            default -> current;
        };
        if (minimum) {
            FabricFishingState.minWaitTime(hook, next);
        } else {
            FabricFishingState.maxWaitTime(hook, next);
        }
    }

    private long resolveTicks(Object @Nullable [] delta) {
        if (delta == null || delta.length == 0 || delta[0] == null) {
            return 0L;
        }
        Object value = delta[0];
        if (value instanceof Timespan timespan) {
            return timespan.getAs(TimePeriod.TICK);
        }
        if (value instanceof Number number) {
            return number.longValue();
        }
        if (value instanceof String string) {
            Timespan parsed = Classes.parse(string, Timespan.class, ParseContext.DEFAULT);
            if (parsed != null) {
                return parsed.getAs(TimePeriod.TICK);
            }
        }
        return 0L;
    }

    private int clampTicks(long value) {
        if (value <= 0L) {
            return 0;
        }
        return value >= Integer.MAX_VALUE ? Integer.MAX_VALUE : (int) value;
    }

    @Override
    public String toString(@Nullable SkriptEvent event, boolean debug) {
        return (minimum ? "minimum" : "maximum") + " fishing wait time";
    }
}
