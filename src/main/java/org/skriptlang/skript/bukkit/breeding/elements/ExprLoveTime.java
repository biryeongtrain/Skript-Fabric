package org.skriptlang.skript.bukkit.breeding.elements;

import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ParseContext;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.skript.registrations.Classes;
import ch.njol.skript.util.Timespan;
import ch.njol.skript.util.Timespan.TimePeriod;
import ch.njol.util.Kleenean;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.animal.Animal;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.lang.event.SkriptEvent;

public final class ExprLoveTime extends SimpleExpression<Timespan> {

    private Expression<Entity> entities;

    @Override
    @SuppressWarnings("unchecked")
    public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
        if (expressions.length != 1 || !expressions[0].canReturn(Entity.class)) {
            return false;
        }
        entities = (Expression<Entity>) expressions[0];
        return true;
    }

    @Override
    protected Timespan @Nullable [] get(SkriptEvent event) {
        List<Timespan> values = new ArrayList<>();
        for (Entity entity : entities.getAll(event)) {
            if (entity instanceof Animal animal) {
                values.add(new Timespan(TimePeriod.TICK, animal.getInLoveTime()));
            } else {
                values.add(new Timespan(TimePeriod.TICK, 0));
            }
        }
        return values.toArray(Timespan[]::new);
    }

    @Override
    public boolean isSingle() {
        return entities.isSingle();
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
        int amount = mode == ChangeMode.RESET ? 0 : resolveTicks(delta);
        for (Entity entity : entities.getAll(event)) {
            if (!(entity instanceof Animal animal)) {
                continue;
            }
            int current = animal.getInLoveTime();
            int next = switch (mode) {
                case SET, RESET -> amount;
                case ADD -> Math.max(0, current + amount);
                case REMOVE -> Math.max(0, current - amount);
                default -> current;
            };
            animal.setInLoveTime(next);
        }
    }

    private int resolveTicks(Object @Nullable [] delta) {
        if (delta == null || delta.length == 0 || delta[0] == null) {
            return 0;
        }
        Object value = delta[0];
        if (value instanceof Timespan timespan) {
            return clampTicks(timespan.getAs(TimePeriod.TICK));
        }
        if (value instanceof Number number) {
            return clampTicks(number.longValue());
        }
        if (value instanceof String string) {
            Timespan parsed = Classes.parse(string, Timespan.class, ParseContext.DEFAULT);
            if (parsed != null) {
                return clampTicks(parsed.getAs(TimePeriod.TICK));
            }
        }
        return 0;
    }

    private int clampTicks(long ticks) {
        if (ticks <= 0L) {
            return 0;
        }
        return ticks >= Integer.MAX_VALUE ? Integer.MAX_VALUE : (int) ticks;
    }

    @Override
    public String toString(@Nullable SkriptEvent event, boolean debug) {
        return "love time of " + entities.toString(event, debug);
    }
}
