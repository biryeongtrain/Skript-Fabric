package ch.njol.skript.conditions;

import ch.njol.skript.Skript;
import ch.njol.skript.conditions.base.PropertyCondition;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Kleenean;
import net.minecraft.world.entity.Entity;

public final class CondFromMobSpawner extends PropertyCondition<Entity> {

    static {
        Skript.registerCondition(
                CondFromMobSpawner.class,
                "%entities% (is|are) from a [mob] spawner",
                "%entities% (isn't|aren't|is not|are not) from a [mob] spawner",
                "%entities% (was|were) spawned (from|by) a [mob] spawner",
                "%entities% (wasn't|weren't|was not|were not) spawned (from|by) a [mob] spawner"
        );
    }

    @Override
    @SuppressWarnings("unchecked")
    public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
        setNegated(matchedPattern == 1 || matchedPattern == 3);
        setExpr((Expression<Entity>) exprs[0]);
        return true;
    }

    @Override
    public boolean check(Entity entity) {
        return ConditionRuntimeSupport.booleanMethod(entity, false, "fromMobSpawner", "isFromMobSpawner", "wasSpawnedFromSpawner");
    }

    @Override
    protected String getPropertyName() {
        return "from a mob spawner";
    }
}
