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

@Name("Cancel Event")
@Description("Cancels the event (e.g. prevent blocks from being placed, or damage being taken).")
@Example("""
        on damage:
            victim is a player
            victim has the permission "skript.god"
            cancel the event
        """)
@Since("1.0")
public final class EffCancelEvent extends Effect {

    private static boolean registered;
    private boolean cancel;

    public static synchronized void register() {
        if (registered) {
            return;
        }
        Skript.registerEffect(EffCancelEvent.class, "cancel [the] event", "uncancel [the] event");
        registered = true;
    }

    @Override
    public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
        cancel = matchedPattern == 0;
        if (isDelayed == Kleenean.TRUE) {
            Skript.error("An event cannot be cancelled after it has already passed");
            return false;
        }
        Skript.error("Event cancellation is not wired in the Fabric runtime yet");
        return false;
    }

    @Override
    protected void execute(SkriptEvent event) {
    }

    @Override
    public String toString(@Nullable SkriptEvent event, boolean debug) {
        return (cancel ? "" : "un") + "cancel event";
    }
}
