package ch.njol.skript.expressions;

import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.PropertyExpression;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.world.entity.Entity;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.lang.event.SkriptEvent;

@Name("Passenger")
@Description("The passengers of an entity.")
@Example("loop passengers of {_boat}:")
@Since("2.0")
public class ExprPassenger extends PropertyExpression<Entity, Entity> {

    static {
        registerDefault(ExprPassenger.class, Entity.class, "passenger[s]", "entities");
    }

    @Override
    protected Entity[] get(SkriptEvent event, Entity[] source) {
        List<Entity> passengers = new ArrayList<>();
        for (Entity entity : source) {
            if (entity == null) {
                continue;
            }
            passengers.addAll(entity.getPassengers());
        }
        return passengers.toArray(Entity[]::new);
    }

    @Override
    public Class<?> @Nullable [] acceptChange(ChangeMode mode) {
        return switch (mode) {
            case SET, ADD -> new Class[]{Entity.class};
            case DELETE, RESET -> new Class[0];
            default -> null;
        };
    }

    @Override
    public void change(SkriptEvent event, Object @Nullable [] delta, ChangeMode mode) {
        for (Entity vehicle : getExpr().getArray(event)) {
            switch (mode) {
                case SET -> {
                    vehicle.ejectPassengers();
                    if (delta != null) {
                        addPassengers(vehicle, delta);
                    }
                }
                case ADD -> {
                    if (delta != null) {
                        addPassengers(vehicle, delta);
                    }
                }
                case DELETE, RESET -> vehicle.ejectPassengers();
                default -> {
                }
            }
        }
    }

    private static void addPassengers(Entity vehicle, Object[] delta) {
        for (Object value : delta) {
            if (!(value instanceof Entity passenger) || passenger == vehicle) {
                continue;
            }
            passenger.stopRiding();
            passenger.startRiding(vehicle);
        }
    }

    @Override
    public Class<? extends Entity> getReturnType() {
        return Entity.class;
    }

    @Override
    public boolean isSingle() {
        return false;
    }

    @Override
    public String toString(@Nullable SkriptEvent event, boolean debug) {
        return "passengers of " + getExpr().toString(event, debug);
    }
}
