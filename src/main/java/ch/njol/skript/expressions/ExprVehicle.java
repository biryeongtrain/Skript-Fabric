package ch.njol.skript.expressions;

import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.SimplePropertyExpression;
import net.minecraft.world.entity.Entity;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.lang.event.SkriptEvent;

@Name("Vehicle")
@Description("The vehicle an entity is in, if any.")
@Example("set the vehicle of player to {_boat}")
@Since("2.0")
public class ExprVehicle extends SimplePropertyExpression<Entity, Entity> {

    static {
        registerDefault(ExprVehicle.class, Entity.class, "vehicle[s]", "entities");
    }

    @Override
    public @Nullable Entity convert(Entity entity) {
        return entity.getVehicle();
    }

    @Override
    public Class<?> @Nullable [] acceptChange(ChangeMode mode) {
        return mode == ChangeMode.SET ? new Class[]{Entity.class} : null;
    }

    @Override
    public void change(SkriptEvent event, Object @Nullable [] delta, ChangeMode mode) {
        if (mode != ChangeMode.SET || delta == null || !(delta[0] instanceof Entity vehicle)) {
            return;
        }
        for (Entity passenger : getExpr().getArray(event)) {
            if (passenger == vehicle) {
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
    protected String getPropertyName() {
        return "vehicle";
    }
}
