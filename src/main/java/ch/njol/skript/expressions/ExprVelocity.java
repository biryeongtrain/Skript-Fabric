package ch.njol.skript.expressions;

import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.SimplePropertyExpression;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.bukkit.particles.particleeffects.DirectionalEffect;
import org.skriptlang.skript.lang.event.SkriptEvent;

@Name("Velocity")
@Description({
        "Gets or changes the velocity of an entity or directional particle.",
        "Setting the velocity of a directional particle forces it to use that exact motion."
})
@Example("set player's velocity to vector(0, 1, 0)")
@Since("2.2-dev31")
public class ExprVelocity extends SimplePropertyExpression<Object, Vec3> {

    static {
        register(ExprVelocity.class, Vec3.class, "velocit(y|ies)", "entities/directionalparticles");
    }

    @Override
    public @Nullable Vec3 convert(Object object) {
        if (object instanceof Entity entity) {
            return entity.getDeltaMovement();
        }
        if (object instanceof DirectionalEffect particleEffect) {
            return particleEffect.velocity();
        }
        return null;
    }

    @Override
    public Class<?> @Nullable [] acceptChange(ChangeMode mode) {
        return switch (mode) {
            case ADD, REMOVE, SET, RESET, DELETE -> new Class[]{Vec3.class};
            default -> null;
        };
    }

    @Override
    public void change(SkriptEvent event, Object @Nullable [] delta, ChangeMode mode) {
        Vec3 value = delta == null ? Vec3.ZERO : (Vec3) delta[0];
        for (Object object : getExpr().getArray(event)) {
            if (object instanceof Entity entity) {
                Vec3 current = entity.getDeltaMovement();
                Vec3 next = switch (mode) {
                    case ADD -> current.add(value);
                    case REMOVE -> current.subtract(value);
                    case SET -> value;
                    case RESET, DELETE -> Vec3.ZERO;
                    default -> current;
                };
                entity.setDeltaMovement(next);
                entity.hasImpulse = true;
            } else if (object instanceof DirectionalEffect particleEffect) {
                Vec3 current = particleEffect.velocity();
                Vec3 next = switch (mode) {
                    case ADD -> current.add(value);
                    case REMOVE -> current.subtract(value);
                    case SET -> value;
                    case RESET, DELETE -> Vec3.ZERO;
                    default -> current;
                };
                particleEffect.velocity(next);
            }
        }
    }

    @Override
    public Class<Vec3> getReturnType() {
        return Vec3.class;
    }

    @Override
    protected String getPropertyName() {
        return "velocity";
    }
}
