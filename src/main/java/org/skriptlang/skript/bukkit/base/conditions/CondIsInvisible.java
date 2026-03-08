package org.skriptlang.skript.bukkit.base.conditions;

import ch.njol.skript.conditions.base.PropertyCondition;
import ch.njol.skript.lang.Condition;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Kleenean;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import org.skriptlang.skript.lang.event.SkriptEvent;

public final class CondIsInvisible extends Condition {

    private Expression<?> livingEntities;

    @Override
    public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
        if (expressions.length != 1 || !expressions[0].canReturn(Entity.class)) {
            return false;
        }
        livingEntities = expressions[0];
        setNegated((matchedPattern == 1) ^ parseResult.hasTag("visible"));
        return true;
    }

    @Override
    public boolean check(SkriptEvent event) {
        return livingEntities.check(event, value -> value instanceof LivingEntity livingEntity && livingEntity.isInvisible(), isNegated());
    }

    @Override
    public String toString(SkriptEvent event, boolean debug) {
        return PropertyCondition.toString(this, PropertyCondition.PropertyType.BE, event, debug, livingEntities, "invisible");
    }
}
