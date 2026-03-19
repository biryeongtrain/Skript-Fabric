package ch.njol.skript.expressions;

import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.SimplePropertyExpression;
import kim.biryeong.skriptFabric.mixin.LivingEntityLastHurtAccessor;
import net.minecraft.world.entity.LivingEntity;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.lang.event.SkriptEvent;

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
        return ((LivingEntityLastHurtAccessor) livingEntity).skript$getLastHurt();
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
            LivingEntityLastHurtAccessor accessor = (LivingEntityLastHurtAccessor) entity;
            switch (mode) {
                case SET -> accessor.skript$setLastHurt(damage);
                case REMOVE -> accessor.skript$setLastHurt(accessor.skript$getLastHurt() - damage);
                case ADD -> accessor.skript$setLastHurt(accessor.skript$getLastHurt() + damage);
                default -> {}
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
}
