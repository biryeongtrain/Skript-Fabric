package org.skriptlang.skript.bukkit.breeding.elements;

import ch.njol.skript.conditions.base.PropertyCondition;
import ch.njol.skript.conditions.base.PropertyCondition.PropertyType;
import ch.njol.skript.lang.Condition;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Kleenean;
import net.minecraft.world.entity.Entity;
import org.skriptlang.skript.lang.event.SkriptEvent;

abstract class AbstractBreedingEntityCondition extends Condition {

    private final PropertyType propertyType;
    private final String propertyName;
    protected Expression<?> entities;

    protected AbstractBreedingEntityCondition(PropertyType propertyType, String propertyName) {
        this.propertyType = propertyType;
        this.propertyName = propertyName;
    }

    @Override
    public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
        if (expressions.length != 1 || !expressions[0].canReturn(Entity.class)) {
            return false;
        }
        entities = expressions[0];
        setNegated((matchedPattern % 2) == 1);
        return true;
    }

    @Override
    public boolean check(SkriptEvent event) {
        return entities.check(event, value -> value instanceof Entity entity && checkEntity(entity), isNegated());
    }

    protected abstract boolean checkEntity(Entity entity);

    @Override
    public String toString(SkriptEvent event, boolean debug) {
        return PropertyCondition.toString(this, propertyType, event, debug, entities, propertyName);
    }
}
