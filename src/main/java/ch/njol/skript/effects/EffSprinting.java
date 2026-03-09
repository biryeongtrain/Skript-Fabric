package ch.njol.skript.effects;

import ch.njol.skript.Skript;
import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Kleenean;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.lang.event.SkriptEvent;

public class EffSprinting extends Effect {

    private static boolean registered;

    private Expression<ServerPlayer> players;
    private boolean sprint;

    public static synchronized void register() {
        if (registered) {
            return;
        }
        Skript.registerEffect(
                EffSprinting.class,
                "make %players% (start sprinting|sprint)",
                "force %players% to (start sprinting|sprint)",
                "make %players% (stop sprinting|not sprint)",
                "force %players% to (stop sprinting|not sprint)"
        );
        registered = true;
    }

    @Override
    @SuppressWarnings("unchecked")
    public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
        if (expressions.length != 1 || !expressions[0].canReturn(ServerPlayer.class)) {
            return false;
        }
        players = (Expression<ServerPlayer>) expressions[0];
        sprint = matchedPattern <= 1;
        return true;
    }

    @Override
    protected void execute(SkriptEvent event) {
        for (ServerPlayer player : players.getAll(event)) {
            player.setSprinting(sprint);
        }
    }

    @Override
    public String toString(@Nullable SkriptEvent event, boolean debug) {
        return "make " + players.toString(event, debug) + (sprint ? " start" : " stop") + " sprinting";
    }
}
