package ch.njol.skript.expressions;

import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.expressions.base.SimplePropertyExpression;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.entity.monster.Ghast;
import net.minecraft.world.entity.projectile.AbstractHurtingProjectile;
import net.minecraft.world.entity.item.PrimedTnt;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.lang.event.SkriptEvent;

public class ExprExplosiveYield extends SimplePropertyExpression<Entity, Number> {

    static {
        register(ExprExplosiveYield.class, Number.class, "explosive (yield|radius|size|power)", "entities");
    }

    @Override
    public @Nullable Number convert(Entity entity) {
        if (!(entity instanceof PrimedTnt || entity instanceof Creeper || entity instanceof Ghast || entity instanceof AbstractHurtingProjectile)) {
            return null;
        }
        Object value = ExpressionHandleSupport.invoke(entity, "getExplosionPower");
        if (!(value instanceof Number) && entity instanceof Creeper) {
            value = ExpressionHandleSupport.invoke(entity, "getExplosionRadius");
        }
        return value instanceof Number number ? number : null;
    }

    @Override
    public Class<?> @Nullable [] acceptChange(ChangeMode mode) {
        return switch (mode) {
            case SET, ADD, REMOVE, DELETE -> new Class[]{Number.class};
            default -> null;
        };
    }

    @Override
    public void change(SkriptEvent event, Object @Nullable [] delta, ChangeMode mode) {
        int value = delta == null ? 0 : Math.max(0, ((Number) delta[0]).intValue());
        for (Entity entity : getExpr().getArray(event)) {
            int current = convert(entity) == null ? 0 : convert(entity).intValue();
            int updated = switch (mode) {
                case SET -> value;
                case ADD -> current + value;
                case REMOVE -> Math.max(0, current - value);
                case DELETE -> 0;
                default -> current;
            };
            if (entity instanceof Ghast) {
                updated = Math.min(updated, 127);
            }
            if (!ExpressionHandleSupport.set(entity, "setExplosionPower", updated) && entity instanceof Creeper) {
                ExpressionHandleSupport.set(entity, "setExplosionRadius", updated);
            }
        }
    }

    @Override
    public Class<Number> getReturnType() {
        return Number.class;
    }

    @Override
    protected String getPropertyName() {
        return "explosive yield";
    }
}
