package ch.njol.skript.effects;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Kleenean;
import net.minecraft.world.entity.Entity;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.fabric.compat.FabricLocation;
import org.skriptlang.skript.lang.event.SkriptEvent;

@Name("Launch firework")
@Description("Launch firework effects at the given location(s).")
@Example("launch ball large colored red, purple and white fading to light green and black at player's location with duration 1")
@Since("2.4")
public class EffFireworkLaunch extends Effect {

    private static boolean registered;

    @Nullable
    public static Entity lastSpawned = null;

    private Expression<?> effects;
    private Expression<FabricLocation> locations;
    private @Nullable Expression<Number> lifetime;

    public static synchronized void register() {
        if (registered) {
            return;
        }
        Skript.registerEffect(
                EffFireworkLaunch.class,
                "(launch|deploy) [[a] firework [with effect[s]]] %fireworkeffects% at %locations% [([with] (duration|power)|timed) %number%]"
        );
        registered = true;
    }

    @Override
    @SuppressWarnings("unchecked")
    public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
        effects = exprs[0];
        locations = (Expression<FabricLocation>) exprs[1];
        lifetime = (Expression<Number>) exprs[2];
        return true;
    }

    @Override
    protected void execute(SkriptEvent event) {
    }

    @Override
    public String toString(@Nullable SkriptEvent event, boolean debug) {
        return "launch firework " + effects.toString(event, debug) + " at " + locations.toString(event, debug)
                + (lifetime == null ? "" : " timed " + lifetime.toString(event, debug));
    }
}
