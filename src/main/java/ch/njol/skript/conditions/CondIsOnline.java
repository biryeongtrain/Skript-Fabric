package ch.njol.skript.conditions;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Condition;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Kleenean;
import com.mojang.authlib.GameProfile;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.lang.event.SkriptEvent;

@Name("Is Online")
@Description(
        "Checks whether a player is online. The 'connected' pattern will return false once this player leaves the server, "
                + "even if they rejoin. Be aware that using the 'connected' pattern with a variable will not have this special behavior. "
                + "Use the direct event-player or other non-variable expression for best results."
)
@Example("player is online")
@Example("player-argument is offline")
@Example("""
    while player is connected:
        wait 60 seconds
        send "hello!" to player
    """)
@Example("""
    # The following will act like `{_player} is online`.
    # Using variables with `is connected` will not behave the same as with non-variables.
    while {_player} is connected:
        broadcast "online!"
        wait 1 tick
    """)
@Since("1.4")
public class CondIsOnline extends Condition {

    static {
        Skript.registerCondition(CondIsOnline.class,
                "%offlineplayers% (is|are) (online|:offline|:connected)",
                "%offlineplayers% (isn't|is not|aren't|are not) (online|:offline|:connected)");
    }

    private Expression<GameProfile> players;
    private boolean connected;

    @Override
    @SuppressWarnings("unchecked")
    public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
        players = (Expression<GameProfile>) exprs[0];
        setNegated(matchedPattern == 1 ^ parseResult.hasTag("offline"));
        connected = parseResult.hasTag("connected");
        return true;
    }

    @Override
    public boolean check(SkriptEvent event) {
        if (event.server() == null) {
            return isNegated();
        }
        return players.check(event, profile -> isOnline(event, profile), isNegated());
    }

    private boolean isOnline(SkriptEvent event, GameProfile profile) {
        ServerPlayer player = event.server().getPlayerList().getPlayer(profile.id());
        if (player == null && profile.name() != null) {
            player = event.server().getPlayerList().getPlayerByName(profile.name());
        }
        if (player == null) {
            return false;
        }
        return !connected || !player.hasDisconnected();
    }

    @Override
    public String toString(@Nullable SkriptEvent event, boolean debug) {
        String property = connected ? "connected" : "online";
        return players.toString(event, debug)
                + (players.isSingle() ? " is " : " are ")
                + (isNegated() ? "not " : "")
                + property;
    }
}
