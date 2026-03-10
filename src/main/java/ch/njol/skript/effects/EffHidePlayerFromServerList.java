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
        players = (Expression<ServerPlayer>) exprs[0];
        Skript.error("Server list ping effects are not wired in the Fabric runtime yet");
        return false;
    }

    @Override
    protected void execute(SkriptEvent event) {
    }

    @Override
    public String toString(@Nullable SkriptEvent event, boolean debug) {
        return "hide " + players.toString(event, debug) + " from the server list";
    }
}
