package org.skriptlang.skript.bukkit.potion.elements.effects;

import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.util.Timespan;
import ch.njol.skript.util.Timespan.TimePeriod;
import ch.njol.util.Kleenean;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.bukkit.potion.util.SkriptPotionEffect;
import org.skriptlang.skript.fabric.runtime.FabricPotionEffectCause;
import org.skriptlang.skript.fabric.runtime.FabricPotionEffectCauseContext;
import org.skriptlang.skript.lang.event.SkriptEvent;

public final class EffPoison extends Effect {

    private Expression<Entity> entities;
    private @Nullable Expression<Timespan> duration;
    private boolean cure;

    @Override
    @SuppressWarnings("unchecked")
    public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
        if (expressions.length < 1 || expressions.length > 2 || !expressions[0].canReturn(Entity.class)) {
            return false;
        }
        entities = (Expression<Entity>) expressions[0];
        if (matchedPattern == 1) {
            duration = (Expression<Timespan>) expressions[1];
        }
        cure = matchedPattern == 2;
        return true;
    }

    @Override
    protected void execute(SkriptEvent event) {
        if (cure) {
            for (Entity entity : entities.getAll(event)) {
                if (entity instanceof LivingEntity livingEntity) {
                    FabricPotionEffectCauseContext.run(FabricPotionEffectCause.PLUGIN,
                            () -> livingEntity.removeEffect(MobEffects.POISON));
                }
            }
            return;
        }

        int ticks = SkriptPotionEffect.DEFAULT_DURATION_TICKS;
        if (duration != null) {
            Timespan timespan = duration.getSingle(event);
            if (timespan != null) {
                ticks = Math.max(0, (int) timespan.getAs(TimePeriod.TICK));
            }
        }
        for (Entity entity : entities.getAll(event)) {
            if (!(entity instanceof LivingEntity livingEntity)) {
                continue;
            }
            MobEffectInstance existing = livingEntity.getEffect(MobEffects.POISON);
            int totalTicks = existing != null ? Math.max(0, ticks + existing.getDuration()) : ticks;
            FabricPotionEffectCauseContext.run(FabricPotionEffectCause.PLUGIN,
                    () -> livingEntity.addEffect(new MobEffectInstance(MobEffects.POISON, totalTicks, 0)));
        }
    }

    @Override
    public String toString(@Nullable SkriptEvent event, boolean debug) {
        return (cure ? "cure " : "poison ") + entities.toString(event, debug);
    }
}
