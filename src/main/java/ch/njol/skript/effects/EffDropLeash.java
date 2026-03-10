package ch.njol.skript.effects;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Events;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Keywords;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Kleenean;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.fabric.runtime.FabricEntityUnleashEventHandle;
import org.skriptlang.skript.lang.event.SkriptEvent;

@Name("Allow / Prevent Leash Drop")
@Description("Allows or prevents the leash from being dropped in an unleash event.")
@Example("""
        on unleash:
            if player is not set:
                prevent the leash from dropping
            else if player is op:
                allow the leash to drop
        """)
@Keywords("lead")
@Events("Leash / Unleash")
@Since("2.10")
public class EffDropLeash extends Effect {

    private static boolean registered;

    private boolean allowLeashDrop;

    public static synchronized void register() {
        if (registered) {
            return;
        }
        Skript.registerEffect(
                EffDropLeash.class,
                "(force|allow) [the] (lead|leash) [item] to drop",
                "(block|disallow|prevent) [the] (lead|leash) [item] from dropping"
        );
        registered = true;
    }

    @Override
    public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
        if (!getParser().isCurrentEvent(FabricEffectEventHandles.EntityUnleash.class)) {
            Skript.error("The 'drop leash' effect can only be used in an 'unleash' event");
            return false;
        }
        allowLeashDrop = matchedPattern == 0;
        return true;
    }

    @Override
    protected void execute(SkriptEvent event) {
        if (event.handle() instanceof FabricEntityUnleashEventHandle unleash) {
            unleash.setDropLeash(allowLeashDrop);
        }
    }

    @Override
    public String toString(@Nullable SkriptEvent event, boolean debug) {
        return allowLeashDrop ? "allow the leash to drop" : "prevent the leash from dropping";
    }
}
