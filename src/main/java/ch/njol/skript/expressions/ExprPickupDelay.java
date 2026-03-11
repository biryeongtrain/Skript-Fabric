package ch.njol.skript.expressions;

import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.SimplePropertyExpression;
import ch.njol.skript.util.Timespan;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.fabric.compat.PrivateItemEntityAccess;
import org.skriptlang.skript.lang.event.SkriptEvent;

@Name("Pickup Delay")
@Description("The amount of time before a dropped item can be picked up.")
@Example("set pickup delay of last dropped item to 5 seconds")
@Since("2.7")
public final class ExprPickupDelay extends SimplePropertyExpression<Entity, Timespan> {

    static {
        register(ExprPickupDelay.class, Timespan.class, "pick[ ]up delay", "entities");
    }

    @Override
    public @Nullable Timespan convert(Entity entity) {
        if (!(entity instanceof ItemEntity itemEntity)) {
            return null;
        }
        return new Timespan(Timespan.TimePeriod.TICK, PrivateItemEntityAccess.pickupDelay(itemEntity));
    }

    @Override
    public Class<?> @Nullable [] acceptChange(ChangeMode mode) {
        return switch (mode) {
            case SET, ADD, REMOVE, RESET, DELETE -> new Class[]{Timespan.class};
            default -> null;
        };
    }

    @Override
    public void change(SkriptEvent event, Object @Nullable [] delta, ChangeMode mode) {
        int change = delta == null || delta.length == 0 ? 0 : (int) ((Timespan) delta[0]).getAs(Timespan.TimePeriod.TICK);
        for (Entity entity : getExpr().getArray(event)) {
            if (!(entity instanceof ItemEntity itemEntity)) {
                continue;
            }
            Timespan currentDelay = convert(itemEntity);
            int current = currentDelay == null ? 0 : (int) currentDelay.getAs(Timespan.TimePeriod.TICK);
            int next = switch (mode) {
                case ADD -> current + change;
                case REMOVE -> current - change;
                case SET -> change;
                case DELETE, RESET -> 0;
                default -> current;
            };
            PrivateItemEntityAccess.setPickupDelay(itemEntity, Math.max(0, next));
        }
    }

    @Override
    public Class<? extends Timespan> getReturnType() {
        return Timespan.class;
    }

    @Override
    protected String getPropertyName() {
        return "pickup delay";
    }
}
