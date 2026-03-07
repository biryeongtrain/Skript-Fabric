package org.skriptlang.skript.bukkit.displays.generic;

import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.lang.ParseContext;
import ch.njol.skript.registrations.Classes;
import ch.njol.skript.util.Timespan;
import ch.njol.skript.util.Timespan.TimePeriod;
import net.minecraft.world.entity.Display;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.fabric.compat.PrivateEntityAccess;
import org.skriptlang.skript.lang.event.SkriptEvent;

public final class ExprDisplayTeleportDuration extends AbstractDisplayExpression<Timespan> {

    @Override
    protected @Nullable Timespan convert(Display display) {
        return new Timespan(TimePeriod.TICK, PrivateEntityAccess.displayPosRotInterpolationDuration(display));
    }

    @Override
    protected Timespan[] createArray(int length) {
        return new Timespan[length];
    }

    @Override
    public Class<? extends Timespan> getReturnType() {
        return Timespan.class;
    }

    @Override
    public Class<?> @Nullable [] acceptChange(ChangeMode mode) {
        return switch (mode) {
            case ADD, REMOVE, SET -> new Class[]{Timespan.class, Number.class, String.class};
            case RESET -> new Class[0];
            default -> null;
        };
    }

    @Override
    public void change(SkriptEvent event, Object @Nullable [] delta, ChangeMode mode) {
        long ticks = resolveTicks(delta);
        for (var entity : displays.getAll(event)) {
            if (!(entity instanceof Display display)) {
                continue;
            }
            int current = PrivateEntityAccess.displayPosRotInterpolationDuration(display);
            int next = switch (mode) {
                case ADD -> clampTicks(current + ticks);
                case REMOVE -> clampTicks(current - ticks);
                case RESET -> 0;
                case SET -> clampTicks(ticks);
                default -> current;
            };
            if (mode == ChangeMode.ADD || mode == ChangeMode.REMOVE || mode == ChangeMode.RESET || mode == ChangeMode.SET) {
                PrivateEntityAccess.setDisplayPosRotInterpolationDuration(display, next);
            }
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

    private int clampTicks(long ticks) {
        if (ticks <= 0L) {
            return 0;
        }
        if (ticks >= 59L) {
            return 59;
        }
        return (int) ticks;
    }

    @Override
    protected String propertyName() {
        return "teleport duration";
    }
}
