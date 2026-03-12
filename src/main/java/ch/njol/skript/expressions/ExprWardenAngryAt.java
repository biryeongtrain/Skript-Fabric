package ch.njol.skript.expressions;

import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.expressions.base.SimplePropertyExpression;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.warden.Warden;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.lang.event.SkriptEvent;

public class ExprWardenAngryAt extends SimplePropertyExpression<LivingEntity, LivingEntity> {

    static {
        register(ExprWardenAngryAt.class, LivingEntity.class, "most angered entity", "livingentities");
    }

    @Override
    public @Nullable LivingEntity convert(LivingEntity livingEntity) {
        return livingEntity instanceof Warden warden ? warden.getEntityAngryAt().orElse(null) : null;
    }

    @Override
    public Class<?> @Nullable [] acceptChange(ChangeMode mode) {
        return mode == ChangeMode.SET ? new Class[]{LivingEntity.class} : null;
    }

    @Override
    public void change(SkriptEvent event, Object @Nullable [] delta, ChangeMode mode) {
        if (!(delta != null && delta.length > 0 && delta[0] instanceof LivingEntity target)) {
            return;
        }
        for (LivingEntity livingEntity : getExpr().getArray(event)) {
            if (livingEntity instanceof Warden warden) {
                warden.clearAnger(target);
                warden.increaseAngerAt(target, 150, false);
                warden.setAttackTarget(target);
            }
        }
    }

    @Override
    public Class<LivingEntity> getReturnType() {
        return LivingEntity.class;
    }

    @Override
    protected String getPropertyName() {
        return "most angered entity";
    }
}
