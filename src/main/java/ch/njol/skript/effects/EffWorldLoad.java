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
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.lang.event.SkriptEvent;

@Name("Load World")
@Description({
        "Load your worlds or unload your worlds",
        "The load effect will create a new world if world doesn't already exist.",
        "When attempting to load a normal vanilla world you must define it's environment i.e \"world_nether\" must be loaded with nether environment"
})
@Example("load world \"world_nether\" with environment nether")
@Example("load the world \"myCustomWorld\"")
@Example("unload \"world_nether\"")
@Example("unload \"world_the_end\" without saving")
@Example("unload all worlds")
@Since("2.8.0")
public final class EffWorldLoad extends Effect {

    private static boolean registered;

    private boolean load;
    private Expression<?> worlds;

    public static synchronized void register() {
        if (registered) {
            return;
        }
        Skript.registerEffect(
                EffWorldLoad.class,
                "load [the] world[s] %strings%",
                "unload [[the] world[s]] %worlds% [:without saving]"
        );
        registered = true;
    }

    @Override
    public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
        worlds = exprs[0];
        load = matchedPattern == 0;
        Skript.error("World load and unload effects are not wired in the Fabric runtime yet");
        return false;
    }

    @Override
    protected void execute(SkriptEvent event) {
    }

    @Override
    public String toString(@Nullable SkriptEvent event, boolean debug) {
        return (load ? "load" : "unload") + " the world(s) " + worlds.toString(event, debug);
    }
}
