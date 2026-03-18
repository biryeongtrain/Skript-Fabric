package ch.njol.skript.effects;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.util.Direction;
import ch.njol.util.Kleenean;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.fabric.compat.FabricLocation;
import org.skriptlang.skript.lang.event.SkriptEvent;

import java.util.Set;

@Name("Teleport")
@Description({
        "Teleport an entity to a specific location.",
        "Teleport flags are settings to retain during a teleport."
})
@Example("teleport the player to {home::%uuid of player%}")
@Example("teleport the attacker to the victim")
@Since("1.0, 2.10 (flags)")
public class EffTeleport extends Effect {

    private static boolean registered;

    public static synchronized void register() {
        if (registered) {
            return;
        }
        Skript.registerEffect(EffTeleport.class, "[:force] teleport %entities% (to|%direction%) %location% [[while] retaining %-teleportflags%]");
        registered = true;
    }

    private Expression<Entity> entities;
    private Expression<FabricLocation> locations;
    private boolean force;

    @SuppressWarnings("unchecked")
    @Override
    public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
        entities = (Expression<Entity>) exprs[0];
        Expression<?> directionExpr = exprs[1];
        Expression<FabricLocation> locExpr = (Expression<FabricLocation>) exprs[2];
        // exprs[3] is teleportflags, currently not implemented — ignore

        if (directionExpr != null) {
            locations = Direction.combine(
                    (Expression<? extends Direction>) directionExpr,
                    (Expression<? extends FabricLocation>) locExpr
            );
        } else {
            locations = locExpr;
        }

        force = parseResult.hasTag("force");
        return true;
    }

    @Override
    protected void execute(SkriptEvent event) {
        FabricLocation loc = locations.getSingle(event);
        if (loc == null)
            return;
        if (!(loc.level() instanceof ServerLevel serverLevel))
            return;

        for (Entity entity : entities.getArray(event)) {
            if (force) {
                entity.stopRiding();
                entity.ejectPassengers();
            }
            entity.teleportTo(
                    serverLevel,
                    loc.position().x, loc.position().y, loc.position().z,
                    Set.of(),
                    entity.getYRot(), entity.getXRot(),
                    true
            );
        }
    }

    @Override
    public String toString(@Nullable SkriptEvent event, boolean debug) {
        return (force ? "force " : "") + "teleport " + entities.toString(event, debug) + " to " + locations.toString(event, debug);
    }
}
