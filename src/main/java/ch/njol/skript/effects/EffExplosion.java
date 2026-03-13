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
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.fabric.compat.FabricLocation;
import org.skriptlang.skript.lang.event.SkriptEvent;

@Name("Explosion")
@Description({
        "Creates an explosion of a given force.",
        "Starting with Bukkit 1.4.5 and Skript 2.0 you can use safe explosions which will damage entities but won't destroy any blocks."
})
@Example("create an explosion of force 10 at the player")
@Example("create an explosion of force 0 at the victim")
@Since("1.0")
public class EffExplosion extends Effect {

    private static boolean registered;

    public static synchronized void register() {
        if (registered) {
            return;
        }
        Skript.registerEffect(
                EffExplosion.class,
                "[(create|make)] [an] explosion (of|with) (force|strength|power) %number% [%directions% %locations%] [(1¦with fire)]",
                "[(create|make)] [a] safe explosion (of|with) (force|strength|power) %number% [%directions% %locations%]",
                "[(create|make)] [a] fake explosion [%directions% %locations%]",
                "[(create|make)] [an] explosion[ ]effect [%directions% %locations%]"
        );
        registered = true;
    }

    @Nullable
    private Expression<Number> force;
    private Expression<FabricLocation> locations;
    private boolean blockDamage;
    private boolean setFire;

    @Override
    @SuppressWarnings("unchecked")
    public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parser) {
        force = matchedPattern <= 1 ? (Expression<Number>) exprs[0] : null;
        blockDamage = matchedPattern != 1;
        setFire = parser.mark == 1;
        locations = Direction.combine(
                (Expression<? extends Direction>) exprs[exprs.length - 2],
                (Expression<? extends FabricLocation>) exprs[exprs.length - 1]
        );
        return true;
    }

    @Override
    protected void execute(SkriptEvent event) {
        Number power = force == null ? 0 : force.getSingle(event);
        if (power == null) {
            return;
        }
        float strength = Math.max(0.0F, power.floatValue());
        Level.ExplosionInteraction interaction = blockDamage
                ? Level.ExplosionInteraction.BLOCK
                : Level.ExplosionInteraction.NONE;
        for (FabricLocation location : locations.getArray(event)) {
            if (location.level() == null) {
                continue;
            }
            location.level().explode(
                    null,
                    location.position().x,
                    location.position().y,
                    location.position().z,
                    strength,
                    setFire,
                    interaction
            );
        }
    }

    @Override
    public String toString(@Nullable SkriptEvent event, boolean debug) {
        if (force != null) {
            return "create explosion of force " + force.toString(event, debug) + " " + locations.toString(event, debug);
        }
        return "create explosion effect " + locations.toString(event, debug);
    }
}
