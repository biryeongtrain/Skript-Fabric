package ch.njol.skript.expressions;

import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.expressions.base.SimplePropertyExpression;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.projectile.AbstractArrow;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.lang.event.SkriptEvent;

public class ExprProjectileCriticalState extends SimplePropertyExpression<Entity, Boolean> {

    static {
        register(ExprProjectileCriticalState.class, Boolean.class, "(projectile|arrow) critical (state|ability|mode)", "projectiles");
    }

    @Override
    public @Nullable Boolean convert(Entity entity) {
        if (!(entity instanceof AbstractArrow arrow)) {
            return null;
        }
        Object state = ExpressionHandleSupport.invoke(arrow, "isCritArrow");
        if (!(state instanceof Boolean)) {
            state = ExpressionHandleSupport.invoke(arrow, "isCritical");
        }
        if (!(state instanceof Boolean)) {
            state = ExpressionHandleSupport.field(arrow, "critArrow");
        }
        return state instanceof Boolean bool ? bool : null;
    }

    @Override
    public Class<?> @Nullable [] acceptChange(ChangeMode mode) {
        return mode == ChangeMode.SET ? new Class[]{Boolean.class} : null;
    }

    @Override
    public void change(SkriptEvent event, @Nullable Object[] delta, ChangeMode mode) {
        if (delta == null) {
            return;
        }
        boolean state = (Boolean) delta[0];
        for (Entity entity : getExpr().getArray(event)) {
            if (entity instanceof AbstractArrow arrow) {
                setCritical(arrow, state);
            }
        }
    }

    private static void setCritical(AbstractArrow arrow, boolean state) {
        if (ExpressionHandleSupport.set(arrow, "setCritArrow", state)) {
            return;
        }
        if (ExpressionHandleSupport.set(arrow, "setCritical", state)) {
            return;
        }
        ExpressionHandleSupport.setField(arrow, "critArrow", state);
    }

    @Override
    public Class<? extends Boolean> getReturnType() {
        return Boolean.class;
    }

    @Override
    protected String getPropertyName() {
        return "critical arrow state";
    }
}
