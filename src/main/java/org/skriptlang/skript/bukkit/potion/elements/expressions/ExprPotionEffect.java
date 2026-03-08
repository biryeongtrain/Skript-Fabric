package org.skriptlang.skript.bukkit.potion.elements.expressions;

import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.util.Kleenean;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.Holder;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.bukkit.potion.util.PotionEffectSupport;
import org.skriptlang.skript.bukkit.potion.util.SkriptPotionEffect;
import org.skriptlang.skript.lang.event.SkriptEvent;

public final class ExprPotionEffect extends SimpleExpression<SkriptPotionEffect> {

    private Expression<?> types;
    private Expression<?> entities;

    @Override
    @SuppressWarnings("unchecked")
    public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
        if (expressions.length != 2) {
            return false;
        }
        if (matchedPattern == 0 || matchedPattern == 1 || matchedPattern == 4 || matchedPattern == 5) {
            if (!expressions[1].canReturn(Entity.class)) {
                return false;
            }
            types = expressions[0];
            entities = expressions[1];
            return true;
        }
        if (matchedPattern == 2 || matchedPattern == 3 || matchedPattern == 6 || matchedPattern == 7) {
            if (!expressions[0].canReturn(Entity.class)) {
                return false;
            }
            entities = expressions[0];
            types = expressions[1];
            return true;
        }
        return false;
    }

    @Override
    protected SkriptPotionEffect @Nullable [] get(SkriptEvent event) {
        List<SkriptPotionEffect> values = new ArrayList<>();
        Object[] requested = types.getAll(event);
        for (Object rawEntity : entities.getAll(event)) {
            if (!(rawEntity instanceof Entity entity)) {
                continue;
            }
            if (!(entity instanceof LivingEntity livingEntity)) {
                continue;
            }
            for (Object requestedType : requested) {
                Holder<MobEffect> parsedType = PotionEffectSupport.parsePotionType(requestedType);
                if (parsedType == null) {
                    continue;
                }
                for (MobEffectInstance instance : livingEntity.getActiveEffects()) {
                    if (instance.is(parsedType)) {
                        values.add(SkriptPotionEffect.fromInstance(instance, livingEntity));
                    }
                }
            }
        }
        return values.toArray(SkriptPotionEffect[]::new);
    }

    @Override
    public boolean isSingle() {
        return types.isSingle() && entities.isSingle();
    }

    @Override
    public Class<? extends SkriptPotionEffect> getReturnType() {
        return SkriptPotionEffect.class;
    }

    @Override
    public String toString(@Nullable SkriptEvent event, boolean debug) {
        return types.toString(event, debug) + " effect of " + entities.toString(event, debug);
    }
}
