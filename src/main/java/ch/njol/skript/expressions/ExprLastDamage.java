package ch.njol.skript.expressions;

import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.SimplePropertyExpression;
import net.minecraft.world.entity.LivingEntity;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.lang.event.SkriptEvent;

import java.lang.reflect.Field;

@Name("Last Damage")
@Description("The last damage that was done to an entity. Note that changing it doesn't deal more/less damage.")
@Example("set last damage of event-entity to 2")
@Since("2.5.1")
public class ExprLastDamage extends SimplePropertyExpression<LivingEntity, Number> {

    static {
        register(ExprLastDamage.class, Number.class, "last damage", "livingentities");
    }

    @Override
    @Nullable
    public Number convert(LivingEntity livingEntity) {
        return getLastHurt(livingEntity);
    }

    @Override
    public @Nullable Class<?>[] acceptChange(ChangeMode mode) {
        return switch (mode) {
            case ADD, SET, REMOVE -> new Class[]{Number.class};
            default -> null;
        };
    }

    @Override
    public void change(SkriptEvent event, @Nullable Object[] delta, ChangeMode mode) {
        if (delta == null || delta.length == 0 || !(delta[0] instanceof Number number)) {
            return;
        }
        float damage = number.floatValue();
        for (LivingEntity entity : getExpr().getArray(event)) {
            switch (mode) {
                case SET -> setLastHurt(entity, damage);
                case REMOVE -> setLastHurt(entity, getLastHurt(entity) - damage);
                case ADD -> setLastHurt(entity, getLastHurt(entity) + damage);
                default -> {
                }
            }
        }
    }

    @Override
    public Class<? extends Number> getReturnType() {
        return Number.class;
    }

    @Override
    protected String getPropertyName() {
        return "last damage";
    }

    private float getLastHurt(LivingEntity entity) {
        try {
            Field field = LivingEntity.class.getDeclaredField("lastHurt");
            field.setAccessible(true);
            return field.getFloat(entity);
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException("Failed to read last damage", e);
        }
    }

    private void setLastHurt(LivingEntity entity, float value) {
        try {
            Field field = LivingEntity.class.getDeclaredField("lastHurt");
            field.setAccessible(true);
            field.setFloat(entity, value);
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException("Failed to set last damage", e);
        }
    }
}
