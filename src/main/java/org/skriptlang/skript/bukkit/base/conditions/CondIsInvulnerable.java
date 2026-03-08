package org.skriptlang.skript.bukkit.base.conditions;

import ch.njol.skript.conditions.base.PropertyCondition;
import ch.njol.skript.lang.Condition;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Kleenean;
import net.minecraft.world.entity.Entity;
import org.skriptlang.skript.lang.event.SkriptEvent;

public final class CondIsInvulnerable extends Condition {

    private Expression<?> entities;

    @Override
    public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
        if (expressions.length != 1 || !expressions[0].canReturn(Entity.class)) {
            return false;
        }
        entities = expressions[0];
        setNegated(matchedPattern == 1);
        return true;
    }

    @Override
    public boolean check(SkriptEvent event) {
        return entities.check(event, value -> value instanceof Entity entity && entity.isInvulnerable(), isNegated());
    }

    @Override
    public String toString(SkriptEvent event, boolean debug) {
        return PropertyCondition.toString(this, PropertyCondition.PropertyType.BE, event, debug, entities, "invulnerable");
    }
}
