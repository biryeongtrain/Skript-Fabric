package ch.njol.skript.expressions;

import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.SimplePropertyExpression;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import org.jetbrains.annotations.Nullable;

@Name("Leash Holder")
@Description("The leash holder of a living entity.")
@Example("set {_holder} to the leash holder of the target mob")
@Since("2.3")
public class ExprLeashHolder extends SimplePropertyExpression<LivingEntity, Entity> {

    static {
        register(ExprLeashHolder.class, Entity.class, "leash holder[s]", "livingentities");
    }

    @Override
    public @Nullable Entity convert(LivingEntity entity) {
        return entity instanceof Mob mob && mob.isLeashed() ? mob.getLeashHolder() : null;
    }

    @Override
    public Class<Entity> getReturnType() {
        return Entity.class;
    }

    @Override
    protected String getPropertyName() {
        return "leash holder";
    }
}
