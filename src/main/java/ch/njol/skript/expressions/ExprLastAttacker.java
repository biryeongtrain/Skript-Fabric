package ch.njol.skript.expressions;

import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.SimplePropertyExpression;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import org.jetbrains.annotations.Nullable;

@Name("Last Attacker")
@Description("The last entity that attacked an entity.")
@Example("send \"%last attacker of event-entity%\"")
@Since("2.5.1")
public class ExprLastAttacker extends SimplePropertyExpression<Entity, Entity> {

    static {
        register(ExprLastAttacker.class, Entity.class, "last attacker", "entity");
    }

    @Override
    public @Nullable Entity convert(Entity entity) {
        if (!(entity instanceof LivingEntity livingEntity)) {
            return null;
        }
        LivingEntity attacker = livingEntity.getLastHurtByMob();
        if (attacker != null) {
            return attacker;
        }
        attacker = livingEntity.getLastAttacker();
        if (attacker != null) {
            return attacker;
        }
        return livingEntity.getLastDamageSource() == null ? null : livingEntity.getLastDamageSource().getEntity();
    }

    @Override
    public Class<? extends Entity> getReturnType() {
        return Entity.class;
    }

    @Override
    protected String getPropertyName() {
        return "last attacker";
    }
}
