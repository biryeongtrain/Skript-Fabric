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

@Name("Stop Server")
@Description("Stops or restarts the server. If restart is used when the restart-script spigot.yml option isn't defined, the server will stop instead.")
@Example("stop the server")
@Example("restart server")
@Since("2.5")
public final class EffStopServer extends Effect {

    private static boolean registered;

    private boolean restart;

    public static synchronized void register() {
        if (registered) {
            return;
        }
        Skript.registerEffect(EffStopServer.class, "(stop|shut[ ]down) [the] server", "restart [the] server");
        registered = true;
    }

    @Override
    public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
        restart = matchedPattern == 1;
        return true;
    }

    @Override
    protected void execute(SkriptEvent event) {
        if (event.server() == null) {
            return;
        }
        event.server().halt(restart);
    }

    @Override
    public String toString(@Nullable SkriptEvent event, boolean debug) {
        return (restart ? "restart" : "stop") + " the server";
    }
}
