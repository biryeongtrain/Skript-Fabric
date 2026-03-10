package ch.njol.skript.expressions;

import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.expressions.base.SimplePropertyExpression;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.projectile.AbstractArrow;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.lang.event.SkriptEvent;

public class ExprArrowPierceLevel extends SimplePropertyExpression<Entity, Long> {

    static {
        register(ExprArrowPierceLevel.class, Long.class, "arrow pierce level", "projectiles");
    }

    @Override
    public @Nullable Long convert(Entity entity) {
        return entity instanceof AbstractArrow arrow ? (long) arrow.getPierceLevel() : null;
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
        for (Entity entity : getExpr().getArray(event)) {
            if (!(entity instanceof AbstractArrow arrow)) {
                continue;
            }
            int current = arrow.getPierceLevel();
            int updated = switch (mode) {
                case SET -> change;
                case ADD -> current + change;
                case REMOVE -> Math.max(0, current - change);
                case RESET -> 0;
                default -> current;
            };
            ExpressionHandleSupport.set(arrow, "setPierceLevel", (byte) Math.max(0, updated));
        }
    }

    @Override
    public Class<? extends Long> getReturnType() {
        return Long.class;
    }

    @Override
    protected String getPropertyName() {
        return "arrow pierce level";
    }
}
