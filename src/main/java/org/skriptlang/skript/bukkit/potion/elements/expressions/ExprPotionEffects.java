package org.skriptlang.skript.bukkit.potion.elements.expressions;

import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.util.Kleenean;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.bukkit.potion.util.SkriptPotionEffect;
import org.skriptlang.skript.lang.event.SkriptEvent;

public final class ExprPotionEffects extends SimpleExpression<SkriptPotionEffect> {

    private Expression<?> entities;

    @Override
    @SuppressWarnings("unchecked")
    public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
        if (expressions.length != 1 || !expressions[0].canReturn(Entity.class)) {
            return false;
        }
        entities = expressions[0];
        return true;
    }

    @Override
    protected SkriptPotionEffect @Nullable [] get(SkriptEvent event) {
        List<SkriptPotionEffect> values = new ArrayList<>();
        for (Object rawEntity : entities.getAll(event)) {
            if (!(rawEntity instanceof Entity entity)) {
                continue;
            }
            if (!(entity instanceof LivingEntity livingEntity)) {
                continue;
            }
            for (MobEffectInstance instance : livingEntity.getActiveEffects()) {
                values.add(SkriptPotionEffect.fromInstance(instance, livingEntity));
            }
        }
        return values.toArray(SkriptPotionEffect[]::new);
    }

    @Override
    public boolean isSingle() {
        return false;
    }

    @Override
    public Class<? extends SkriptPotionEffect> getReturnType() {
        return SkriptPotionEffect.class;
    }

    @Override
    public String toString(@Nullable SkriptEvent event, boolean debug) {
        return "active potion effects of " + entities.toString(event, debug);
    }
}
