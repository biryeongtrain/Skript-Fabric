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
import org.skriptlang.skript.fabric.compat.PrivateFishingHookAccess;
import org.skriptlang.skript.fabric.runtime.FabricFishingEventHandle;
import org.skriptlang.skript.lang.event.SkriptEvent;

public final class ExprFishingBiteTime extends SimpleExpression<Timespan> {

    @Override
    public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
        if (expressions.length != 0) {
            return false;
        }
        if (!getParser().isCurrentEvent(FabricFishingEventHandle.class)) {
            Skript.error("The 'fishing bite time' expression can only be used in a fishing event.");
            return false;
        }
        return true;
    }

    @Override
    protected Timespan @Nullable [] get(SkriptEvent event) {
        if (!(event.handle() instanceof FabricFishingEventHandle handle)) {
            return null;
        }
        return new Timespan[]{new Timespan(TimePeriod.TICK, PrivateFishingHookAccess.timeUntilHooked(handle.hook()))};
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
            case SET, ADD, REMOVE -> new Class[]{Timespan.class, Number.class, String.class};
            default -> null;
        };
    }

    @Override
    public void change(SkriptEvent event, Object @Nullable [] delta, ChangeMode mode) {
        if (!(event.handle() instanceof FabricFishingEventHandle handle)) {
            return;
        }
        int amount = Math.max(1, clampTicks(resolveTicks(delta)));
        var hook = handle.hook();
        int current = PrivateFishingHookAccess.timeUntilHooked(hook);
        int next = switch (mode) {
            case SET -> amount;
            case ADD -> Math.max(1, current + amount);
            case REMOVE -> Math.max(1, current - amount);
            default -> current;
        };
        PrivateFishingHookAccess.setTimeUntilHooked(hook, next);
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
        return "fishing bite time";
    }
}
