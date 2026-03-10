package ch.njol.skript.expressions;

import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.SimplePropertyExpression;
import ch.njol.skript.util.Timespan;
import ch.njol.skript.util.Timespan.TimePeriod;
import net.minecraft.world.entity.LivingEntity;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.lang.event.SkriptEvent;

@Name("Remaining Air")
@Description("How much time a player has left underwater before starting to drown.")
@Example("""
    if the player's remaining air is less than 3 seconds:
        send "hurry, get to the surface!" to the player
    """)
@Since("2.0")
public class ExprRemainingAir extends SimplePropertyExpression<LivingEntity, Timespan> {

    static {
        register(ExprRemainingAir.class, Timespan.class, "remaining air", "livingentities");
    }

    @Override
    public Timespan convert(LivingEntity entity) {
        return new Timespan(TimePeriod.TICK, Math.max(0, entity.getAirSupply()));
    }

    @Override
    public Class<?> @Nullable [] acceptChange(ChangeMode mode) {
        return switch (mode) {
            case ADD, SET, REMOVE, DELETE, RESET -> new Class[]{Timespan.class};
        };
    }

    @Override
    public void change(SkriptEvent event, Object @Nullable [] delta, ChangeMode mode) {
        long change = delta == null ? 20L * 15L : ((Timespan) delta[0]).getAs(TimePeriod.TICK);
        if (mode == ChangeMode.REMOVE) {
            change *= -1L;
        }
        for (LivingEntity entity : getExpr().getArray(event)) {
            long next = switch (mode) {
                case ADD, REMOVE -> entity.getAirSupply() + change;
                case SET -> change;
                case DELETE, RESET -> 20L * 15L;
            };
            entity.setAirSupply((int) Math.max(0L, Math.min(next, Integer.MAX_VALUE)));
        }
    }

    @Override
    public Class<Timespan> getReturnType() {
        return Timespan.class;
    }

    @Override
    protected String getPropertyName() {
        return "remaining air";
    }
}
