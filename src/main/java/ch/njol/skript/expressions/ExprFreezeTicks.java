package ch.njol.skript.expressions;

import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.SimplePropertyExpression;
import ch.njol.skript.util.Timespan;
import net.minecraft.world.entity.Entity;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.lang.event.SkriptEvent;

@Name("Freeze Time")
@Description("How much time an entity has been in powdered snow for.")
@Example("""
    player's freeze time is less than 3 seconds:
        send "you're about to freeze!" to the player
    """)
@Since("2.7")
public class ExprFreezeTicks extends SimplePropertyExpression<Entity, Timespan> {

    static {
        register(ExprFreezeTicks.class, Timespan.class, "freeze time", "entities");
    }

    @Override
    @Nullable
    public Timespan convert(Entity entity) {
        return new Timespan(Timespan.TimePeriod.TICK, entity.getTicksFrozen());
    }

    @Override
    public @Nullable Class<?>[] acceptChange(ChangeMode mode) {
        return new Class[]{Timespan.class};
    }

    @Override
    public void change(SkriptEvent event, @Nullable Object[] delta, ChangeMode mode) {
        int time = delta == null || delta.length == 0 ? 0 : (int) ((Timespan) delta[0]).getAs(Timespan.TimePeriod.TICK);
        for (Entity entity : getExpr().getArray(event)) {
            int next = switch (mode) {
                case ADD -> entity.getTicksFrozen() + time;
                case REMOVE -> entity.getTicksFrozen() - time;
                case SET -> time;
                case DELETE, RESET -> 0;
            };
            entity.setTicksFrozen(Math.max(0, next));
        }
    }

    @Override
    public Class<? extends Timespan> getReturnType() {
        return Timespan.class;
    }

    @Override
    protected String getPropertyName() {
        return "freeze time";
    }
}
