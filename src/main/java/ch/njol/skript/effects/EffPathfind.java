package ch.njol.skript.effects;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.RequiredPlugins;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Kleenean;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.fabric.compat.FabricLocation;
import org.skriptlang.skript.lang.event.SkriptEvent;

@Name("Pathfind")
@Description({"Make an entity pathfind towards a location or another entity. Not all entities can pathfind. "
        + "If the pathfinding target is another entity, the entities may or may not continuously follow the target."})
@Example("make all creepers pathfind towards player")
@Example("make all cows stop pathfinding")
@Example("make event-entity pathfind towards player at speed 1")
@Since("2.7")
@RequiredPlugins("Minecraft 1.14+")
public class EffPathfind extends Effect {

    private static boolean registered;

    private Expression<LivingEntity> entities;
    private @Nullable Expression<Number> speed;
    private @Nullable Expression<?> target;

    public static synchronized void register() {
        if (registered) {
            return;
        }
        Skript.registerEffect(
                EffPathfind.class,
                "make %livingentities% (pathfind|move) to[wards] %livingentity/location% [at speed %-number%]",
                "make %livingentities% stop (pathfinding|moving)"
        );
        registered = true;
    }

    @Override
    @SuppressWarnings("unchecked")
    public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
        entities = (Expression<LivingEntity>) exprs[0];
        target = matchedPattern == 0 ? exprs[1] : null;
        speed = matchedPattern == 0 ? (Expression<Number>) exprs[2] : null;
        return true;
    }

    @Override
    protected void execute(SkriptEvent event) {
        Object resolvedTarget = target == null ? null : target.getSingle(event);
        double resolvedSpeed = speed == null ? 1.0D : speed.getOptionalSingle(event).orElse(1).doubleValue();
        for (LivingEntity entity : entities.getArray(event)) {
            if (!(entity instanceof Mob mob)) {
                continue;
            }
            if (resolvedTarget instanceof LivingEntity livingTarget) {
                mob.getNavigation().moveTo(livingTarget, resolvedSpeed);
            } else if (resolvedTarget instanceof FabricLocation location) {
                mob.getNavigation().moveTo(location.position().x(), location.position().y(), location.position().z(), resolvedSpeed);
            } else if (target == null) {
                mob.getNavigation().stop();
            }
        }
    }

    @Override
    public String toString(@Nullable SkriptEvent event, boolean debug) {
        if (target == null) {
            return "make " + entities.toString(event, debug) + " stop pathfinding";
        }
        String repr = "make " + entities.toString(event, debug) + " pathfind towards " + target.toString(event, debug);
        if (speed != null) {
            repr += " at speed " + speed.toString(event, debug);
        }
        return repr;
    }
}
