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
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.lang.event.SkriptEvent;

@Name("Make Say")
@Description("Forces a player to send a message to the chat. If the message starts with a slash it will force the player to use command.")
@Example("make the player say \"Hello.\"")
@Example("force all players to send the message \"I love this server\"")
@Since("2.3")
public class EffMakeSay extends Effect {

    private static boolean registered;

    private Expression<ServerPlayer> players;
    private Expression<String> messages;

    public static synchronized void register() {
        if (registered) {
            return;
        }
        Skript.registerEffect(
                EffMakeSay.class,
                "make %players% (say|send [the] message[s]) %strings%",
                "force %players% to (say|send [the] message[s]) %strings%"
        );
        registered = true;
    }

    @Override
    @SuppressWarnings("unchecked")
    public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
        players = (Expression<ServerPlayer>) exprs[0];
        messages = (Expression<String>) exprs[1];
        Skript.error("Player chat injection is not wired in the Fabric runtime yet");
        return false;
    }

    @Override
    protected void execute(SkriptEvent event) {
    }

    @Override
    public String toString(@Nullable SkriptEvent event, boolean debug) {
        return "make " + players.toString(event, debug) + " say " + messages.toString(event, debug);
    }
}
