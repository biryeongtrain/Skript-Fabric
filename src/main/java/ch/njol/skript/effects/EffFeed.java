package ch.njol.skript.effects;

import ch.njol.skript.Skript;
import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Kleenean;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.lang.event.SkriptEvent;

public class EffFeed extends Effect {

    private static boolean registered;

    private Expression<ServerPlayer> players;
    private @Nullable Expression<Number> beefs;

    public static synchronized void register() {
        if (registered) {
            return;
        }
        Skript.registerEffect(EffFeed.class, "feed [the] %players% [by %-number% [beef[s]]]");
        registered = true;
    }

    @Override
    @SuppressWarnings("unchecked")
    public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
        if (expressions.length != 2 || !expressions[0].canReturn(ServerPlayer.class)) {
            return false;
        }
        if (expressions[1] != null && !expressions[1].canReturn(Number.class)) {
            return false;
        }
        players = (Expression<ServerPlayer>) expressions[0];
        beefs = (Expression<Number>) expressions[1];
        return true;
    }

    @Override
    protected void execute(SkriptEvent event) {
        int level = 20;
        if (beefs != null) {
            Number amount = beefs.getSingle(event);
            if (amount == null) {
                return;
            }
            level = amount.intValue();
        }
        for (ServerPlayer player : players.getAll(event)) {
            player.getFoodData().setFoodLevel(player.getFoodData().getFoodLevel() + level);
        }
    }

    @Override
    public String toString(@Nullable SkriptEvent event, boolean debug) {
        return "feed " + players.toString(event, debug) + (beefs != null ? " by " + beefs.toString(event, debug) : "");
    }
}
