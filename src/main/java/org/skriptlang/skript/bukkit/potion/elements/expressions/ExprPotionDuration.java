package org.skriptlang.skript.bukkit.potion.elements.expressions;

import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.skript.util.Timespan;
import ch.njol.skript.util.Timespan.TimePeriod;
import ch.njol.util.Kleenean;
import java.util.ArrayList;
import java.util.List;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.bukkit.potion.util.PotionEffectSupport;
import org.skriptlang.skript.bukkit.potion.util.SkriptPotionEffect;
import org.skriptlang.skript.lang.event.SkriptEvent;

public final class ExprPotionDuration extends SimpleExpression<Timespan> {

    private Expression<?> values;

    @Override
    public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
        if (expressions.length != 1) {
            return false;
        }
        values = expressions[0];
        return true;
    }

    @Override
    protected Timespan @Nullable [] get(SkriptEvent event) {
        List<Timespan> durations = new ArrayList<>();
        for (Object value : values.getAll(event)) {
            SkriptPotionEffect effect = PotionEffectSupport.parsePotionEffect(value);
            if (effect != null) {
                durations.add(new Timespan(TimePeriod.TICK, effect.duration()));
            }
        }
        return durations.toArray(Timespan[]::new);
    }

    @Override
    public boolean isSingle() {
        return values.isSingle();
    }

    @Override
    public Class<? extends Timespan> getReturnType() {
        return Timespan.class;
    }

    @Override
    public String toString(@Nullable SkriptEvent event, boolean debug) {
        return "duration of " + values.toString(event, debug);
    }
}
