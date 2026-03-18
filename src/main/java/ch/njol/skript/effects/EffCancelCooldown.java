package ch.njol.skript.effects;

import ch.njol.skript.Skript;
import ch.njol.skript.command.ScriptCommand;
import ch.njol.skript.command.ScriptCommandContext;
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

@Name("Cancel Command Cooldown")
@Description({"Only usable in commands. Makes it so the current command usage isn't counted towards the cooldown."})
@Example("""
        command /nick <text>:
            executable by: players
            cooldown: 10 seconds
            trigger:
                if length of arg-1 is more than 16:
                    cancel the cooldown
                    send "Your nickname may be at most 16 characters."
                    stop
        """)
@Since("2.2-dev34")
public final class EffCancelCooldown extends Effect {

    private static boolean registered;
    private boolean cancel;

    public static synchronized void register() {
        if (registered) {
            return;
        }
        Skript.registerEffect(
                EffCancelCooldown.class,
                "(cancel|ignore) [the] [current] [command] cooldown",
                "un(cancel|ignore) [the] [current] [command] cooldown"
        );
        registered = true;
    }

    @Override
    public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
        if (!getParser().isCurrentEvent(ScriptCommandContext.class)) {
            Skript.error("The cancel cooldown effect can only be used in a command trigger");
            return false;
        }
        cancel = matchedPattern == 0;
        return true;
    }

    @Override
    protected void execute(SkriptEvent event) {
        ScriptCommand.CANCEL_COOLDOWN.set(cancel);
    }

    @Override
    public String toString(@Nullable SkriptEvent event, boolean debug) {
        return (cancel ? "" : "un") + "cancel the command cooldown";
    }
}
