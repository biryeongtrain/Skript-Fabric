package ch.njol.skript.expressions;

import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.expressions.base.SimplePropertyExpression;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.Projectile;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.lang.event.SkriptEvent;

public class ExprArrowKnockbackStrength extends SimplePropertyExpression<Projectile, Long> {

    static {
        register(ExprArrowKnockbackStrength.class, Long.class, "arrow knockback strength", "projectiles");
    }

    @Override
    public @Nullable Long convert(Projectile projectile) {
        Object knockback = projectile instanceof AbstractArrow arrow ? ExpressionHandleSupport.invoke(arrow, "getKnockback") : null;
        return knockback instanceof Number number ? number.longValue() : null;
    }

    @Override
    public Class<?> @Nullable [] acceptChange(ChangeMode mode) {
        return switch (mode) {
            case SET, ADD, REMOVE, RESET -> new Class[]{Number.class};
            default -> null;
        };
    }

    @Override
    public void change(SkriptEvent event, Object @Nullable [] delta, ChangeMode mode) {
        int change = delta == null ? 0 : Math.max(0, ((Number) delta[0]).intValue());
        for (Projectile projectile : getExpr().getArray(event)) {
            if (!(projectile instanceof AbstractArrow arrow)) {
                continue;
            }
            Number knockback = (Number) ExpressionHandleSupport.invoke(arrow, "getKnockback");
            if (knockback == null) {
                continue;
            }
            int current = knockback.intValue();
            int updated = switch (mode) {
                case SET -> change;
                case ADD -> current + change;
                case REMOVE -> Math.max(0, current - change);
                case RESET -> 0;
                default -> current;
            };
            ExpressionHandleSupport.set(arrow, "setKnockback", Math.max(0, updated));
        }
    }

    @Override
    public Class<? extends Long> getReturnType() {
        return Long.class;
    }

    @Override
    protected String getPropertyName() {
        return "arrow knockback strength";
    }
}
