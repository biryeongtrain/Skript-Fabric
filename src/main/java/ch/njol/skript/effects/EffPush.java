package ch.njol.skript.effects;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.SyntaxStringBuilder;
import ch.njol.util.Kleenean;
import java.util.function.Function;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.fabric.compat.FabricLocation;
import org.skriptlang.skript.lang.event.SkriptEvent;

@Name("Push")
@Description("Push entities in a given direction or towards a specific location.")
@Example("push the player upwards")
@Example("push the victim downwards at speed 0.5")
@Example("push player towards player's target at speed 2")
@Example("pull player along vector(1,1,1) at speed 1.5")
@Since({"1.4.6", "2.12 (push towards)"})
public class EffPush extends Effect {

    private static boolean registered;

    private Expression<Entity> entities;
    private @Nullable Expression<Vec3> direction;
    private @Nullable Expression<FabricLocation> target;
    private boolean awayFrom;
    private @Nullable Expression<Number> speed;

    public static synchronized void register() {
        if (registered) {
            return;
        }
        Skript.registerEffect(
                EffPush.class,
                "(push|thrust|pull) %entities% [along] %vector% [(at|with) [a] (speed|velocity|force) [of] %-number%]",
                "(push|thrust|pull) %entities% (towards|away:away from) %location% [(at|with) [a] (speed|velocity|force) [of] %-number%]"
        );
        registered = true;
    }

    @Override
    @SuppressWarnings("unchecked")
    public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
        entities = (Expression<Entity>) exprs[0];
        if (matchedPattern == 0) {
            direction = (Expression<Vec3>) exprs[1];
        } else {
            target = (Expression<FabricLocation>) exprs[1];
            awayFrom = parseResult.hasTag("away");
        }
        speed = (Expression<Number>) exprs[2];
        return true;
    }

    @Override
    protected void execute(SkriptEvent event) {
        Number speedValue = speed == null ? null : speed.getSingle(event);
        if (speed != null && speedValue == null) {
            return;
        }
        Function<Entity, Vec3> getDirection;
        if (direction != null) {
            Vec3 resolved = direction.getSingle(event);
            if (resolved == null) {
                return;
            }
            getDirection = entity -> resolved;
        } else {
            FabricLocation resolvedTarget = target == null ? null : target.getSingle(event);
            if (resolvedTarget == null) {
                return;
            }
            Vec3 targetVector = resolvedTarget.position();
            getDirection = entity -> {
                Vec3 push = targetVector.subtract(entity.position());
                return awayFrom ? push.scale(-1.0D) : push;
            };
        }
        for (Entity entity : entities.getArray(event)) {
            Vec3 pushDirection = getDirection.apply(entity);
            if (speedValue != null) {
                pushDirection = pushDirection.normalize().scale(speedValue.doubleValue());
            }
            if (!(Double.isFinite(pushDirection.x()) && Double.isFinite(pushDirection.y()) && Double.isFinite(pushDirection.z()))) {
                return;
            }
            entity.setDeltaMovement(entity.getDeltaMovement().add(pushDirection));
            entity.hasImpulse = true;
        }
    }

    @Override
    public String toString(@Nullable SkriptEvent event, boolean debug) {
        SyntaxStringBuilder builder = new SyntaxStringBuilder(event, debug).append("push", entities);
        if (direction != null) {
            builder.append(direction);
        } else if (target != null) {
            builder.append(awayFrom ? "away from" : "towards", target);
        }
        if (speed != null) {
            builder.append("at a speed of", speed);
        }
        return builder.toString();
    }
}
