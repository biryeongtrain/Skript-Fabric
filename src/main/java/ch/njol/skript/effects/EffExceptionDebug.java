package ch.njol.skript.effects;

import ch.njol.skript.doc.NoDoc;
import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Kleenean;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.lang.event.SkriptEvent;

@NoDoc
public final class EffExceptionDebug extends Effect {

    private static boolean registered;

    public static synchronized void register() {
        if (registered) {
            return;
        }
        ch.njol.skript.Skript.registerEffect(EffExceptionDebug.class, "cause exception");
        registered = true;
    }

    @Override
    public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
        return true;
    }

    @Override
    protected void execute(SkriptEvent event) {
        throw new IllegalStateException("Created by a script (debugging)...");
    }

    @Override
    public String toString(@Nullable SkriptEvent event, boolean debug) {
        return "cause exception";
    }
}
