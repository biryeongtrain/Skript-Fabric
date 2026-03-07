package org.skriptlang.skript.bukkit.interactions.elements.conditions;

import ch.njol.skript.lang.Condition;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Kleenean;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Interaction;
import org.skriptlang.skript.fabric.compat.PrivateEntityAccess;
import org.skriptlang.skript.lang.event.SkriptEvent;

public final class CondIsResponsive extends Condition {

    private Expression<?> entities;
    private boolean responsive;

    @Override
    public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
        if (expressions.length != 1 || !expressions[0].canReturn(Entity.class)) {
            return false;
        }
        entities = expressions[0];
        responsive = matchedPattern < 2;
        setNegated((matchedPattern % 2) == 1);
        return true;
    }

    @Override
    public boolean check(SkriptEvent event) {
        return entities.check(event, value -> value instanceof Interaction interaction
                && PrivateEntityAccess.interactionResponse(interaction) == responsive, isNegated());
    }

    @Override
    public String toString(SkriptEvent event, boolean debug) {
        String property = responsive ? "responsive" : "unresponsive";
        if (isNegated()) {
            return entities.toString(event, debug) + " is not " + property;
        }
        return entities.toString(event, debug) + " is " + property;
    }
}
