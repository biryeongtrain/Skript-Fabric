package ch.njol.skript.conditions;

import ch.njol.skript.conditions.base.PropertyCondition;
import ch.njol.skript.lang.Condition;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Kleenean;
import net.minecraft.server.level.ServerPlayer;
import org.skriptlang.skript.lang.event.SkriptEvent;

public class CondIsSprinting extends Condition {

    private Expression<?> players;

    @Override
    public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
        if (expressions.length != 1 || !expressions[0].canReturn(ServerPlayer.class)) {
            return false;
        }
        players = expressions[0];
        setNegated(matchedPattern == 1);
        return true;
    }

    @Override
    public boolean check(SkriptEvent event) {
        return players.check(event, value -> value instanceof ServerPlayer player && player.isSprinting(), isNegated());
    }

    @Override
    public String toString(SkriptEvent event, boolean debug) {
        return PropertyCondition.toString(this, PropertyCondition.PropertyType.BE, event, debug, players, "sprinting");
    }
}
