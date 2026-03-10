package ch.njol.skript.expressions;

import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.SimplePropertyExpression;
import ch.njol.skript.util.Timespan;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.lang.event.SkriptEvent;

@Name("Experience Pickup Cooldown")
@Description({
        "The experience cooldown of a player.",
        "Experience cooldown is how long until a player can pick up another orb of experience.",
        "The cooldown of a player must be 0 to pick up another orb of experience."
})
@Example("send experience cooldown of player")
@Example("set the xp pickup cooldown of player to 1 hour")
@Example("""
    if exp collection cooldown of player >= 10 minutes:
        clear the experience pickup cooldown of player
    """)
@Since("2.10")
public class ExprExperienceCooldown extends SimplePropertyExpression<ServerPlayer, Timespan> {

    static {
        register(ExprExperienceCooldown.class, Timespan.class, "(experience|[e]xp) [pickup|collection] cooldown", "players");
    }

    @Override
    public Timespan convert(ServerPlayer player) {
        return new Timespan(Timespan.TimePeriod.TICK, player.takeXpDelay);
    }

    @Override
    public Class<?> @Nullable [] acceptChange(ChangeMode mode) {
        return switch (mode) {
            case ADD, REMOVE, SET, RESET, DELETE -> new Class[]{Timespan.class};
        };
    }

    @Override
    public void change(SkriptEvent event, Object @Nullable [] delta, ChangeMode mode) {
        int provided = delta == null ? 0 : (int) ((Timespan) delta[0]).getAs(Timespan.TimePeriod.TICK);
        for (ServerPlayer player : getExpr().getArray(event)) {
            switch (mode) {
                case ADD -> player.takeXpDelay = Math.max(-1, player.takeXpDelay + provided);
                case REMOVE -> player.takeXpDelay = Math.max(-1, player.takeXpDelay - provided);
                case SET -> player.takeXpDelay = Math.max(-1, provided);
                case RESET, DELETE -> player.takeXpDelay = 0;
            }
        }
    }

    @Override
    protected String getPropertyName() {
        return "experience cooldown";
    }

    @Override
    public Class<? extends Timespan> getReturnType() {
        return Timespan.class;
    }
}
