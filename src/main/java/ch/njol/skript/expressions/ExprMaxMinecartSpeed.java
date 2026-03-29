package ch.njol.skript.expressions;

import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.SimplePropertyExpression;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.vehicle.minecart.AbstractMinecart;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.lang.event.SkriptEvent;

@Name("Max Minecart Speed")
@Description("The configured maximum speed of a minecart on the Fabric compatibility surface.")
@Example("""
    on right click on minecart:
        set max minecart speed of event-entity to 1
    """)
@Since("2.5.1, 2.13 (Fabric)")
public class ExprMaxMinecartSpeed extends SimplePropertyExpression<Entity, Number> {

    static {
        register(ExprMaxMinecartSpeed.class, Number.class, "max[imum] minecart (speed|velocity)", "entities");
    }

    @Override
    public @Nullable Number convert(Entity entity) {
        return entity instanceof AbstractMinecart minecart ? MinecartExpressionSupport.maxSpeed(minecart) : null;
    }

    @Override
    public Class<?> @Nullable [] acceptChange(ChangeMode mode) {
        return switch (mode) {
            case SET, ADD, REMOVE, RESET -> new Class[]{Number.class};
            default -> null;
        };
    }

    @Override
    public void change(SkriptEvent event, @Nullable Object[] delta, ChangeMode mode) {
        double input = delta == null ? 0.0D : ((Number) delta[0]).doubleValue();
        for (Entity entity : getExpr().getArray(event)) {
            if (!(entity instanceof AbstractMinecart minecart)) {
                continue;
            }
            switch (mode) {
                case SET -> MinecartExpressionSupport.setMaxSpeed(minecart, input);
                case ADD -> MinecartExpressionSupport.setMaxSpeed(minecart, MinecartExpressionSupport.maxSpeed(minecart) + input);
                case REMOVE -> MinecartExpressionSupport.setMaxSpeed(minecart, MinecartExpressionSupport.maxSpeed(minecart) - input);
                case RESET -> MinecartExpressionSupport.resetMaxSpeed(minecart);
            }
        }
    }

    @Override
    public Class<? extends Number> getReturnType() {
        return Number.class;
    }

    @Override
    protected String getPropertyName() {
        return "max minecart speed";
    }
}
