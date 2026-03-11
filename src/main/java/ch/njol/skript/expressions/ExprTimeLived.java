package ch.njol.skript.expressions;

import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.SimplePropertyExpression;
import ch.njol.skript.util.Timespan;
import ch.njol.util.Math2;
import org.jetbrains.annotations.Nullable;
import net.minecraft.world.entity.Entity;
import org.skriptlang.skript.lang.event.SkriptEvent;

@Name("Time Lived of Entity")
@Description("Returns the total amount of time an entity has existed.")
@Example("send \"%time lived of entity%\"")
@Since("2.13")
public class ExprTimeLived extends SimplePropertyExpression<Entity, Timespan> {

    static {
        register(ExprTimeLived.class, Timespan.class, "time (alive|lived)", "entities");
    }

    @Override
    public @Nullable Timespan convert(Entity entity) {
        return new Timespan(Timespan.TimePeriod.TICK, entity.tickCount);
    }

    @Override
    public Class<?> @Nullable [] acceptChange(ChangeMode mode) {
        return switch (mode) {
            case ADD, REMOVE, SET, RESET -> new Class[]{Timespan.class};
            default -> null;
        };
    }

    @Override
    public void change(SkriptEvent event, Object @Nullable [] delta, ChangeMode mode) {
        long provided = 1L;
        if (delta != null && delta[0] instanceof Timespan timespan) {
            provided = timespan.getAs(Timespan.TimePeriod.TICK);
        }
        for (Entity entity : getExpr().getArray(event)) {
            long current = entity.tickCount;
            long next = switch (mode) {
                case ADD -> current + provided;
                case REMOVE -> current - provided;
                case SET, RESET -> provided;
                default -> current;
            };
            entity.tickCount = (int) Math2.fit(1, next, Integer.MAX_VALUE);
        }
    }

    @Override
    public Class<? extends Timespan> getReturnType() {
        return Timespan.class;
    }

    @Override
    protected String getPropertyName() {
        return "time lived";
    }
}
