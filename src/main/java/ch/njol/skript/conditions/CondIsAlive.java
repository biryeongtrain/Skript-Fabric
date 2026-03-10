package ch.njol.skript.conditions;

import ch.njol.skript.conditions.base.PropertyCondition;
import ch.njol.skript.lang.Condition;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Kleenean;
import net.minecraft.world.entity.Entity;
import org.skriptlang.skript.lang.event.SkriptEvent;

public class CondIsAlive extends Condition {

    private Expression<?> entities;
    private boolean dead;

    @Override
    public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
        if (expressions.length != 1 || !expressions[0].canReturn(Entity.class)) {
            return false;
        }
        entities = expressions[0];
        dead = parseResult.mark == 1;
        setNegated(matchedPattern == 1);
        return true;
    }

    @Override
    public boolean check(SkriptEvent event) {
        return entities.check(event, value -> {
            if (!(value instanceof Entity entity)) {
                return false;
            }
            boolean alive = entity.isAlive();
            return dead ? !alive : alive;
        }, isNegated());
    }

    @Override
    public String toString(SkriptEvent event, boolean debug) {
        return PropertyCondition.toString(this, PropertyCondition.PropertyType.BE, event, debug, entities, dead ? "dead" : "alive");
    }
}
