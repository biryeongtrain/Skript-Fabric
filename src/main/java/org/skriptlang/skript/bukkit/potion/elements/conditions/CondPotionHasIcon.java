package org.skriptlang.skript.bukkit.potion.elements.conditions;

import ch.njol.skript.conditions.base.PropertyCondition;
import ch.njol.skript.lang.Condition;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Kleenean;
import org.skriptlang.skript.bukkit.potion.util.PotionEffectSupport;
import org.skriptlang.skript.bukkit.potion.util.SkriptPotionEffect;
import org.skriptlang.skript.lang.event.SkriptEvent;

public final class CondPotionHasIcon extends Condition {

    private Expression<?> values;

    @Override
    public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
        if (expressions.length != 1) {
            return false;
        }
        values = expressions[0];
        setNegated((matchedPattern % 2) == 1);
        return true;
    }

    @Override
    public boolean check(SkriptEvent event) {
        return values.check(event, value -> {
            SkriptPotionEffect potionEffect = PotionEffectSupport.parsePotionEffect(value);
            return potionEffect != null && potionEffect.icon();
        }, isNegated());
    }

    @Override
    public String toString(SkriptEvent event, boolean debug) {
        return PropertyCondition.toString(this, PropertyCondition.PropertyType.HAVE, event, debug, values, "an icon");
    }
}
