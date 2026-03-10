package ch.njol.skript.expressions;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.util.Kleenean;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.lang.event.SkriptEvent;

@Name("IP")
@Description("The IP address of players or the player attached to the current event context.")
@Example("broadcast ip of player")
@Since("1.4, Fabric")
public class ExprIP extends SimpleExpression<String> {

    static {
        Skript.registerExpression(
                ExprIP.class,
                String.class,
                "IP[s][( |-)address[es]] of %players%",
                "%players%'[s] IP[s][( |-)address[es]]",
                "IP[( |-)address]"
        );
    }

    private @Nullable Expression<ServerPlayer> players;
    private boolean property;

    @Override
    @SuppressWarnings("unchecked")
    public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
        property = matchedPattern < 2;
        if (property) {
            players = (Expression<ServerPlayer>) exprs[0];
            return true;
        }
        if (getParser().getCurrentEventClasses() == null) {
            Skript.error("You must specify players whose IP addresses to get outside of player-backed events.");
            return false;
        }
        return true;
    }

    @Override
    protected @Nullable String[] get(SkriptEvent event) {
        if (!property) {
            return event.player() == null ? new String[0] : new String[]{event.player().getIpAddress()};
        }
        assert players != null;
        ServerPlayer[] values = players.getArray(event);
        String[] ips = new String[values.length];
        for (int i = 0; i < values.length; i++) {
            ips[i] = values[i].getIpAddress();
        }
        return ips;
    }

    @Override
    public boolean isSingle() {
        return !property || (players != null && players.isSingle());
    }

    @Override
    public Class<? extends String> getReturnType() {
        return String.class;
    }

    @Override
    public String toString(@Nullable SkriptEvent event, boolean debug) {
        if (!property || players == null) {
            return "the IP address";
        }
        return "the IP address of " + players.toString(event, debug);
    }
}
