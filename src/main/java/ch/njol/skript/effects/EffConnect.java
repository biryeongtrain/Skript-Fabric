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

@Name("Connect")
@Description({
        "Connect a player to a server running on your proxy, or any server supporting transfers. Read below for more information.",
        "If the server is running Minecraft 1.20.5 or above, you may specify an IP and Port to transfer a player over to that server.",
        "When transferring players using an IP, the transfer will not complete if the `accepts-transfers` option isn't enabled in `server.properties` for the server specified.",
        "If the port is not provided, it will default to `25565`."
})
@Example("connect all players to proxy server \"hub\"")
@Example("transfer player to server \"my.server.com\"")
@Example("transfer player to server \"localhost\" on port 25566")
@Since("2.3, 2.10 (transfer)")
public class EffConnect extends Effect {

    private static boolean registered;

    private Expression<ServerPlayer> players;
    private Expression<String> server;
    private @Nullable Expression<Number> port;
    private boolean transfer;

    public static synchronized void register() {
        if (registered) {
            return;
        }
        Skript.registerEffect(
                EffConnect.class,
                "connect %players% to [proxy|bungeecord] [server] %string%",
                "send %players% to [proxy|bungeecord] server %string%",
                "transfer %players% to server %string% [on port %-number%]"
        );
        registered = true;
    }

    @Override
    @SuppressWarnings("unchecked")
    public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
        players = (Expression<ServerPlayer>) exprs[0];
        server = (Expression<String>) exprs[1];
        transfer = matchedPattern == 2;
        if (transfer) {
            port = (Expression<Number>) exprs[2];
        }
        Skript.error("Cross-server player transfer is not wired in the Fabric runtime yet");
        return false;
    }

    @Override
    protected void execute(SkriptEvent event) {
    }

    @Override
    public String toString(@Nullable SkriptEvent event, boolean debug) {
        if (transfer) {
            return "transfer " + players.toString(event, debug) + " to server " + server.toString(event, debug)
                    + (port != null ? " on port " + port.toString(event, debug) : "");
        }
        return "connect " + players.toString(event, debug) + " to proxy server " + server.toString(event, debug);
    }
}
