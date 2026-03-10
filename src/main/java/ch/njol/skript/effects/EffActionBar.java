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
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundSetActionBarTextPacket;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.lang.event.SkriptEvent;

@Name("Action Bar")
@Description("Sends an action bar message to the given player(s).")
@Example("send action bar \"Hello player!\" to player")
@Since("2.3")
public class EffActionBar extends Effect {

    private static boolean registered;

    private Expression<String> message;
    private @Nullable Expression<ServerPlayer> recipients;

    public static synchronized void register() {
        if (registered) {
            return;
        }
        Skript.registerEffect(EffActionBar.class, "send [the] action[ ]bar [with text] %string% [to %-players%]");
        registered = true;
    }

    @Override
    @SuppressWarnings("unchecked")
    public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
        message = (Expression<String>) expressions[0];
        recipients = (Expression<ServerPlayer>) expressions[1];
        return true;
    }

    @Override
    protected void execute(SkriptEvent event) {
        String value = message.getSingle(event);
        if (value == null) {
            return;
        }
        Component component = EffectRuntimeSupport.componentOf(value, event);
        ClientboundSetActionBarTextPacket packet = new ClientboundSetActionBarTextPacket(component);
        for (ServerPlayer player : EffectRuntimeSupport.playersOrEvent(recipients == null ? null : recipients.getArray(event), event)) {
            player.connection.send(packet);
        }
    }

    @Override
    public String toString(@Nullable SkriptEvent event, boolean debug) {
        return "send action bar " + message.toString(event, debug)
                + (recipients == null ? "" : " to " + recipients.toString(event, debug));
    }
}
