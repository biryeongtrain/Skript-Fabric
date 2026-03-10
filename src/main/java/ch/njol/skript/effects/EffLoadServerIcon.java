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

@Name("Load Server Icon")
@Description({"Loads server icons from the given files. You can get the loaded icon using the",
        "<a href='#ExprLastLoadedServerIcon'>last loaded server icon</a> expression.",
        "Please note that the image must be 64x64 and the file path starts from the server folder.",})
@Example("""
        on load:
            clear {server-icons::*}
            loop 5 times:
                load server icon from file "icons/%loop-number%.png"
                add the last loaded server icon to {server-icons::*}
        """)
@Since("2.3")
public final class EffLoadServerIcon extends Effect {

    private static boolean registered;
    private Expression<String> path;

    public static synchronized void register() {
        if (registered) {
            return;
        }
        Skript.registerEffect(EffLoadServerIcon.class, "load [the] server icon (from|of) [the] [image] [file] %string%");
        registered = true;
    }

    @Override
    @SuppressWarnings("unchecked")
    public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
        path = (Expression<String>) exprs[0];
        Skript.error("Server icon loading is not wired in the Fabric runtime yet");
        return false;
    }

    @Override
    protected void execute(SkriptEvent event) {
    }

    @Override
    public String toString(@Nullable SkriptEvent event, boolean debug) {
        return "load server icon from file " + path.toString(event, debug);
    }
}
