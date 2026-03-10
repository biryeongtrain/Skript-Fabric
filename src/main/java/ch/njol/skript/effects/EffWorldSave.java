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
import net.minecraft.server.level.ServerLevel;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.lang.event.SkriptEvent;

@Name("Save World")
@Description({
        "Save all worlds or a given world manually.",
        "Note: saving many worlds at once may possibly cause the server to freeze."
})
@Example("save \"world_nether\"")
@Example("save all worlds")
@Since("2.8.0")
public final class EffWorldSave extends Effect {

    private static boolean registered;

    private Expression<ServerLevel> worlds;

    public static synchronized void register() {
        if (registered) {
            return;
        }
        Skript.registerEffect(EffWorldSave.class, "save [[the] world[s]] %worlds%");
        registered = true;
    }

    @Override
    @SuppressWarnings("unchecked")
    public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
        worlds = (Expression<ServerLevel>) exprs[0];
        return true;
    }

    @Override
    protected void execute(SkriptEvent event) {
        for (ServerLevel world : worlds.getArray(event)) {
            world.save(null, false, world.noSave());
        }
    }

    @Override
    public String toString(@Nullable SkriptEvent event, boolean debug) {
        return "save the world(s) " + worlds.toString(event, debug);
    }
}
