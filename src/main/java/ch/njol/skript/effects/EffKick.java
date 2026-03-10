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
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.lang.event.SkriptEvent;

@Name("Kick")
@Description("Kicks a player from the server.")
@Example("kick the player due to \"You may not do that!\"")
@Since("1.0")
public class EffKick extends Effect {

    private static boolean registered;

    private Expression<ServerPlayer> players;
    private @Nullable Expression<String> reason;

    public static synchronized void register() {
        if (registered) {
            return;
        }
        Skript.registerEffect(EffKick.class, "kick %players% [(by reason of|because [of]|on account of|due to) %-string%]");
        registered = true;
    }

    @Override
    @SuppressWarnings("unchecked")
    public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
        players = (Expression<ServerPlayer>) expressions[0];
        reason = (Expression<String>) expressions[1];
        return true;
    }

    @Override
    protected void execute(SkriptEvent event) {
        Component component = Component.empty();
        if (reason != null) {
            String value = reason.getSingle(event);
            if (value == null) {
                return;
            }
            component = EffectRuntimeSupport.componentOf(value, event);
        }
        for (ServerPlayer player : players.getArray(event)) {
            player.connection.disconnect(component);
        }
    }

    @Override
    public String toString(@Nullable SkriptEvent event, boolean debug) {
        return "kick " + players.toString(event, debug)
                + (reason == null ? "" : " on account of " + reason.toString(event, debug));
    }
}
