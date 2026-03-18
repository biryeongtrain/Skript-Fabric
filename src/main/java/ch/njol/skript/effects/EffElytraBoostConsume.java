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

    private static final @Nullable Class<?> ELYTRA_BOOST_EVENT = resolveEventClass(
            "ch.njol.skript.effects.FabricEffectEventHandles$PlayerElytraBoost"
    );

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
        if (ELYTRA_BOOST_EVENT == null || !getParser().isCurrentEvent(ELYTRA_BOOST_EVENT)) {
            Skript.error("The elytra boost consume effect can only be used in an 'elytra boost' event.");
            return false;
        }
        consume = matchedPattern == 1;
        return true;
    }

    @Override
    protected void execute(SkriptEvent event) {
        Object handle = event.handle();
        if (handle instanceof FabricEffectEventHandles.PlayerElytraBoost boost) {
            boost.setShouldConsume(consume);
        }
    }

    @Override
    public String toString(@Nullable SkriptEvent event, boolean debug) {
        return consume ? "allow the boosting firework to be consumed"
                : "prevent the boosting firework from being consumed";
    }

    private static @Nullable Class<?> resolveEventClass(String name) {
        try {
            return Class.forName(name);
        } catch (ClassNotFoundException ignored) {
            return null;
        }
    }
}
