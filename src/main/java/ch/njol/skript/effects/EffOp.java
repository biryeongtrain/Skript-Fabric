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
import com.mojang.authlib.GameProfile;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.lang.event.SkriptEvent;

@Name("op/deop")
@Description("Grant or revoke a user operator state.")
@Example("op the player")
@Example("deop all players")
@Since("1.0")
public class EffOp extends Effect {

    private static boolean registered;

    private Expression<GameProfile> players;
    private boolean op;

    public static synchronized void register() {
        if (registered) {
            return;
        }
        Skript.registerEffect(EffOp.class, "[de[-]]op %offlineplayers%");
        registered = true;
    }

    @Override
    @SuppressWarnings("unchecked")
    public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
        players = (Expression<GameProfile>) expressions[0];
        op = !parseResult.expr.substring(0, 2).equalsIgnoreCase("de");
        return true;
    }

    @Override
    protected void execute(SkriptEvent event) {
        if (event.server() == null) {
            return;
        }
        for (GameProfile profile : players.getArray(event)) {
            if (op) {
                event.server().getPlayerList().op(profile);
            } else {
                event.server().getPlayerList().deop(profile);
            }
        }
    }

    @Override
    public String toString(@Nullable SkriptEvent event, boolean debug) {
        return (op ? "" : "de") + "op " + players.toString(event, debug);
    }
}
