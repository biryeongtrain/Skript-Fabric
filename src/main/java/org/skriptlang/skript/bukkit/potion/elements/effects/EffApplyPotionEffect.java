package org.skriptlang.skript.bukkit.potion.elements.effects;

import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.util.Timespan;
import ch.njol.skript.util.Timespan.TimePeriod;
import ch.njol.util.Kleenean;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.bukkit.potion.util.PotionEffectSupport;
import org.skriptlang.skript.bukkit.potion.util.SkriptPotionEffect;
import org.skriptlang.skript.lang.event.SkriptEvent;

public final class EffApplyPotionEffect extends Effect {

    private Expression<?> potions;
    private Expression<Entity> entities;
    private @Nullable Expression<Timespan> duration;

    @Override
    @SuppressWarnings("unchecked")
    public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
        if (expressions.length != 2 && expressions.length != 3) {
            return false;
        }
        boolean first = matchedPattern <= 1;
        potions = expressions[first ? 0 : 1];
        if (!expressions[first ? 1 : 0].canReturn(Entity.class)) {
            return false;
        }
        entities = (Expression<Entity>) expressions[first ? 1 : 0];
        duration = expressions.length == 3 ? (Expression<Timespan>) expressions[2] : null;
        return true;
    }

    @Override
    protected void execute(SkriptEvent event) {
        int customDuration = -1;
        if (duration != null) {
            Timespan timespan = duration.getSingle(event);
            if (timespan != null) {
                customDuration = (int) timespan.getAs(TimePeriod.TICK);
            }
        }
        for (Object rawPotion : potions.getAll(event)) {
            SkriptPotionEffect potionEffect = PotionEffectSupport.parsePotionEffect(rawPotion);
            if (potionEffect == null) {
                continue;
            }
            SkriptPotionEffect applied = potionEffect.copy();
            if (customDuration >= 0) {
                applied.duration(customDuration);
            }
            for (Entity entity : entities.getAll(event)) {
                if (entity instanceof LivingEntity livingEntity) {
                    livingEntity.addEffect(applied.asMobEffectInstance());
                }
            }
        }
    }

    @Override
    public String toString(@Nullable SkriptEvent event, boolean debug) {
        return "apply " + potions.toString(event, debug) + " to " + entities.toString(event, debug);
    }
}
