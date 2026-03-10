package ch.njol.skript.effects;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.RequiredPlugins;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Kleenean;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.lang.event.SkriptEvent;

@Name("Message")
@Description({
        "Sends a message to the given player.",
        "Adding an optional sender keeps the imported syntax shape even though the current Fabric bridge sends a system message."
})
@Example("message \"A wild %player% appeared!\"")
@Example("send \"Your kill streak is high\" to player")
@RequiredPlugins("Minecraft 1.16.4+ for optional sender")
@Since("1.0, 2.5.2 (optional sender), 2.6 (sending objects)")
public class EffMessage extends Effect {

    private static boolean registered;

    private Expression<?> messages;
    private @Nullable Expression<ServerPlayer> recipients;
    private @Nullable Expression<ServerPlayer> sender;

    public static synchronized void register() {
        if (registered) {
            return;
        }
        Skript.registerEffect(EffMessage.class, "(message|send [message[s]]) %objects% [to %-players%] [from %-player%]");
        registered = true;
    }

    @Override
    @SuppressWarnings("unchecked")
    public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
        messages = expressions[0];
        recipients = (Expression<ServerPlayer>) expressions[1];
        sender = (Expression<ServerPlayer>) expressions[2];
        return true;
    }

    @Override
    protected void execute(SkriptEvent event) {
        ServerPlayer[] resolvedRecipients = EffectRuntimeSupport.playersOrEvent(
                recipients == null ? null : recipients.getArray(event),
                event
        );
        for (Object message : messages.getArray(event)) {
            for (ServerPlayer player : resolvedRecipients) {
                player.sendSystemMessage(EffectRuntimeSupport.componentOf(message, event));
            }
        }
    }

    @Override
    public String toString(@Nullable SkriptEvent event, boolean debug) {
        return "send " + messages.toString(event, debug)
                + (recipients == null ? "" : " to " + recipients.toString(event, debug))
                + (sender == null ? "" : " from " + sender.toString(event, debug));
    }
}
