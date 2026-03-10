package ch.njol.skript.effects;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Kleenean;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.lang.event.SkriptEvent;

@Name("Knockback")
@Description("Apply the same velocity as a knockback to living entities in a direction. Mechanics such as knockback resistance will be factored in.")
@Example("knockback player north")
@Example("knock victim (vector from attacker to victim) with strength 10")
@Since("2.7")
public class EffKnockback extends Effect {

    private static boolean registered;

    private Expression<LivingEntity> entities;
    private Expression<Vec3> direction;
    private @Nullable Expression<Number> strength;

    public static synchronized void register() {
        if (registered) {
            return;
        }
        Skript.registerEffect(EffKnockback.class, "(apply knockback to|knock[back]) %livingentities% [%vector%] [with (strength|force) %-number%]");
        registered = true;
    }

    @Override
    @SuppressWarnings("unchecked")
    public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
        entities = (Expression<LivingEntity>) exprs[0];
        direction = (Expression<Vec3>) exprs[1];
        strength = (Expression<Number>) exprs[2];
        return true;
    }

    @Override
    protected void execute(SkriptEvent event) {
        Vec3 knockbackDirection = direction.getSingle(event);
        if (knockbackDirection == null) {
            return;
        }
        double appliedStrength = strength == null ? 1.0D : strength.getOptionalSingle(event).orElse(1).doubleValue();
        for (LivingEntity livingEntity : entities.getArray(event)) {
            livingEntity.knockback(appliedStrength, -knockbackDirection.x(), -knockbackDirection.z());
            livingEntity.hasImpulse = true;
        }
    }

    @Override
    public String toString(@Nullable SkriptEvent event, boolean debug) {
        return "knockback " + entities.toString(event, debug) + " " + direction.toString(event, debug)
                + " with strength " + (strength != null ? strength.toString(event, debug) : "1");
    }
}
