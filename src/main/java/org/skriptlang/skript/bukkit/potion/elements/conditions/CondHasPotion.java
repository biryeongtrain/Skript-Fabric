package org.skriptlang.skript.bukkit.potion.elements.conditions;

import ch.njol.skript.conditions.base.PropertyCondition;
import ch.njol.skript.conditions.base.PropertyCondition.PropertyType;
import ch.njol.skript.lang.Condition;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Kleenean;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.bukkit.potion.util.PotionEffectSupport;
import org.skriptlang.skript.bukkit.potion.util.SkriptPotionEffect;
import org.skriptlang.skript.lang.event.SkriptEvent;

public final class CondHasPotion extends Condition {

    private Expression<?> entities;
    private @Nullable Expression<?> effects;

    @Override
    public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
        if (expressions.length < 1 || expressions.length > 2) {
            return false;
        }
        entities = expressions[0];
        effects = expressions.length == 2 ? expressions[1] : null;
        setNegated((matchedPattern % 2) == 1);
        return true;
    }

    @Override
    public boolean check(SkriptEvent event) {
        if (effects == null) {
            return entities.check(event, value -> {
                LivingEntity entity = PotionEffectSupport.asLivingEntity(value);
                return entity != null && !entity.getActiveEffects().isEmpty();
            }, isNegated());
        }

        Object[] requested = effects.getAll(event);
        boolean and = effects.getAnd();
        return entities.check(event, value -> {
            LivingEntity entity = PotionEffectSupport.asLivingEntity(value);
            if (entity == null) {
                return false;
            }
            for (Object requestedValue : requested) {
                SkriptPotionEffect potionEffect = PotionEffectSupport.parsePotionEffect(requestedValue);
                boolean matched = false;
                if (potionEffect != null) {
                    for (MobEffectInstance activeEffect : entity.getActiveEffects()) {
                        if (potionEffect.matchesQualities(activeEffect)) {
                            matched = true;
                            break;
                        }
                    }
                }
                if (and && !matched) {
                    return false;
                }
                if (!and && matched) {
                    return true;
                }
            }
            return and && requested.length > 0;
        }, isNegated());
    }

    @Override
    public String toString(SkriptEvent event, boolean debug) {
        String property = effects != null ? effects.toString(event, debug) : "active potion effects";
        return PropertyCondition.toString(this, PropertyType.HAVE, event, debug, entities, property);
    }
}
