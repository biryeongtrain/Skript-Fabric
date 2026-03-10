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
import net.minecraft.network.protocol.game.ClientboundClearTitlesPacket;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.lang.event.SkriptEvent;

@Name("Title - Reset")
@Description("Resets the title of the player to the default values.")
@Example("reset the titles of all players")
@Since("2.3")
public class EffResetTitle extends Effect {

    private static boolean registered;

    private @Nullable Expression<ServerPlayer> recipients;

    public static synchronized void register() {
        if (registered) {
            return;
        }
        Skript.registerEffect(
                EffResetTitle.class,
                "reset [the] title[s] [of %-players%]",
                "reset [the] %-players%'[s] title[s]"
        );
        registered = true;
    }

    @Override
    @SuppressWarnings("unchecked")
    public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
        recipients = (Expression<ServerPlayer>) expressions[0];
        return true;
    }

    @Override
    protected void execute(SkriptEvent event) {
        ClientboundClearTitlesPacket packet = new ClientboundClearTitlesPacket(true);
        for (ServerPlayer player : EffectRuntimeSupport.playersOrEvent(recipients == null ? null : recipients.getArray(event), event)) {
            player.connection.send(packet);
        }
    }

    @Override
    public String toString(@Nullable SkriptEvent event, boolean debug) {
        return "reset the title"
                + (recipients == null ? "" : " of " + recipients.toString(event, debug));
    }
}
