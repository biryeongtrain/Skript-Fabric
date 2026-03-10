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
import org.skriptlang.skript.fabric.compat.FabricBlock;
import org.skriptlang.skript.lang.event.SkriptEvent;

@Name("Ring Bell")
@Description({
        "Causes a bell to ring.",
        "Optionally, the entity that rang the bell and the direction the bell should ring can be specified.",
        "A bell can only ring in two directions, and the direction is determined by which way the bell is facing.",
        "By default, the bell will ring in the direction it is facing.",
})
@Example("make player ring target-block")
@Since("2.9.0")
public final class EffRing extends Effect {

    private static boolean registered;
    private @Nullable Expression<Entity> entity;
    private Expression<FabricBlock> blocks;

    public static synchronized void register() {
        if (registered) {
            return;
        }
        Skript.registerEffect(
                EffRing.class,
                "ring %blocks%",
                "(make|let) %entity% ring %blocks%"
        );
        registered = true;
    }

    @Override
    @SuppressWarnings("unchecked")
    public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
        entity = matchedPattern == 0 ? null : (Expression<Entity>) exprs[0];
        blocks = (Expression<FabricBlock>) exprs[matchedPattern];
        Skript.error("Bell ringing is not wired in the Fabric runtime yet");
        return false;
    }

    @Override
    protected void execute(SkriptEvent event) {
    }

    @Override
    public String toString(@Nullable SkriptEvent event, boolean debug) {
        return (entity != null ? "make " + entity.toString(event, debug) + " " : "")
                + "ring " + blocks.toString(event, debug);
    }
}
