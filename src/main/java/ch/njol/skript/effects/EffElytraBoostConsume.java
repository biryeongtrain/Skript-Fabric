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

@Name("Consume Boosting Firework")
@Description("Prevent the firework used in an 'elytra boost' event to be consumed.")
@Example("""
        on elytra boost:
            if the used firework will be consumed:
                prevent the used firework from being consume
        """)
@Since("2.10")
public class EffElytraBoostConsume extends Effect {

    private static boolean registered;
    private boolean consume;

    public static synchronized void register() {
        if (registered) {
            return;
        }
        Skript.registerEffect(
                EffElytraBoostConsume.class,
                "(prevent|disallow) [the] (boosting|used) firework from being consumed",
                "allow [the] (boosting|used) firework to be consumed"
        );
        registered = true;
    }

    @Override
    public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
        consume = matchedPattern == 1;
        Skript.error("Elytra boost firework consumption is not wired in the Fabric runtime yet.");
        return false;
    }

    @Override
    protected void execute(SkriptEvent event) {
    }

    @Override
    public String toString(@Nullable SkriptEvent event, boolean debug) {
        return consume ? "allow the boosting firework to be consumed"
                : "prevent the boosting firework from being consumed";
    }
}
