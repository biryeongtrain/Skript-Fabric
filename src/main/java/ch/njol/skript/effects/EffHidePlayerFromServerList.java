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
import org.skriptlang.skript.fabric.runtime.FabricServerListPingEventHandle;
import org.skriptlang.skript.lang.event.SkriptEvent;

@Name("Hide Player from Server List")
@Description({"Hides a player from the <a href='#ExprHoverList'>hover list</a> "
        + "and decreases the <a href='#ExprOnlinePlayersCount'>online players count</a> (only if the player count wasn't changed before)."})
@Example("""
        on server list ping:
            hide {vanished::*} from the server list
        """)
@Since("2.3")
public final class EffHidePlayerFromServerList extends Effect {

    private static boolean registered;
    private Expression<ServerPlayer> players;

    public static synchronized void register() {
        if (registered) {
            return;
        }
        Skript.registerEffect(
                EffHidePlayerFromServerList.class,
                "hide %players% (in|on|from) [the] server list",
                "hide %players%'[s] info[rmation] (in|on|from) [the] server list"
        );
        registered = true;
    }

    @Override
    @SuppressWarnings("unchecked")
    public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
        if (!getParser().isCurrentEvent(FabricServerListPingEventHandle.class)) {
            Skript.error("The 'hide player from server list' effect can only be used in a server list ping event");
            return false;
        }
        players = (Expression<ServerPlayer>) exprs[0];
        return true;
    }

    @Override
    protected void execute(SkriptEvent event) {
        if (!(event.handle() instanceof FabricServerListPingEventHandle handle)) {
            return;
        }
        for (ServerPlayer player : players.getArray(event)) {
            handle.hidePlayer(player.getUUID());
            handle.playerSample().remove(player.getGameProfile().name());
        }
    }

    @Override
    public String toString(@Nullable SkriptEvent event, boolean debug) {
        return "hide " + players.toString(event, debug) + " from the server list";
    }
}
