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

@Name("Tree")
@Description({
        "Creates a tree.",
        "This may require that there is enough space above the given location and that the block below is dirt/grass."
})
@Example("grow a tall redwood tree above the clicked block")
@Since("1.0")
public class EffTree extends Effect {

    private static boolean registered;

    public static synchronized void register() {
        if (registered) {
            return;
        }
        Skript.registerEffect(
                EffTree.class,
                "(grow|create|generate) tree [of type %structuretype%] %directions% %locations%",
                "(grow|create|generate) %structuretype% %directions% %locations%"
        );
        registered = true;
    }

    @Override
    public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parser) {
        Skript.error("EffTree is blocked in the Fabric port until StructureType and Direction compatibility are imported.");
        return false;
    }

    @Override
    protected void execute(SkriptEvent event) {
    }

    @Override
    public String toString(@Nullable SkriptEvent event, boolean debug) {
        return "grow tree";
    }
}
