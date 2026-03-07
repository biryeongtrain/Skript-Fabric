package org.skriptlang.skript.bukkit.potion.elements.expressions;

import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.util.Kleenean;
import java.util.ArrayList;
import java.util.List;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.bukkit.potion.util.PotionEffectSupport;
import org.skriptlang.skript.bukkit.potion.util.SkriptPotionEffect;
import org.skriptlang.skript.lang.event.SkriptEvent;

public final class ExprPotionAmplifier extends SimpleExpression<Integer> {

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
    protected Integer @Nullable [] get(SkriptEvent event) {
        List<Integer> amplifiers = new ArrayList<>();
        for (Object value : values.getAll(event)) {
            SkriptPotionEffect effect = PotionEffectSupport.parsePotionEffect(value);
            if (effect != null) {
                amplifiers.add(effect.amplifier() + 1);
            }
        }
        return amplifiers.toArray(Integer[]::new);
    }

    @Override
    public boolean isSingle() {
        return values.isSingle();
    }

    @Override
    public Class<? extends Integer> getReturnType() {
        return Integer.class;
    }

    @Override
    public String toString(@Nullable SkriptEvent event, boolean debug) {
        return "amplifier of " + values.toString(event, debug);
    }
}
