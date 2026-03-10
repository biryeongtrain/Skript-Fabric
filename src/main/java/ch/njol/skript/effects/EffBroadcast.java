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
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.lang.event.SkriptEvent;

@Name("Broadcast")
@Description("Broadcasts a message to the server.")
@Example("broadcast \"Welcome %player% to the server!\"")
@Since("1.0, 2.6 (broadcasting objects)")
public class EffBroadcast extends Effect {

    private static boolean registered;

    private Expression<?> messageExpr;
    private @Nullable Expression<ServerLevel> worlds;

    public static synchronized void register() {
        if (registered) {
            return;
        }
        Skript.registerEffect(EffBroadcast.class, "broadcast %objects% [(to|in) %-worlds%]");
        registered = true;
    }

    @Override
    @SuppressWarnings("unchecked")
    public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
        messageExpr = expressions[0];
        worlds = (Expression<ServerLevel>) expressions[1];
        return true;
    }

    @Override
    protected void execute(SkriptEvent event) {
        for (Object message : messageExpr.getArray(event)) {
            Component component = EffectRuntimeSupport.componentOf(message, event);
            if (worlds == null) {
                if (event.server() == null) {
                    continue;
                }
                event.server().getPlayerList().broadcastSystemMessage(component, false);
                event.server().sendSystemMessage(component);
                continue;
            }
            for (ServerLevel world : worlds.getArray(event)) {
                for (ServerPlayer player : EffectRuntimeSupport.worldPlayers(world)) {
                    player.sendSystemMessage(component);
                }
            }
        }
    }

    @Override
    public String toString(@Nullable SkriptEvent event, boolean debug) {
        return "broadcast " + messageExpr.toString(event, debug)
                + (worlds == null ? "" : " to " + worlds.toString(event, debug));
    }
}
