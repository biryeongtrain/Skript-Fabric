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
import net.minecraft.commands.arguments.EntityAnchorArgument;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.fabric.compat.FabricLocation;
import org.skriptlang.skript.lang.event.SkriptEvent;

@Name("Look At")
@Description("Forces the mob(s) or player(s) to look at an entity, vector or location. Vanilla max head pitches range from 10 to 50.")
@Example("force the player to look towards event-entity's feet")
@Example("""
        on entity explosion:
            set {_player} to the nearest player
            {_player} is set
            distance between {_player} and the event-location is less than 15
            make {_player} look towards vector from the {_player} to location of the event-entity
        """)
@Example("force {_enderman} to face the block 3 meters above {_location} at head rotation speed 100.5 and max head pitch -40")
@Since("2.7")
public final class EffLook extends Effect {

    private static boolean registered;

    private EntityAnchorArgument.Anchor anchor = EntityAnchorArgument.Anchor.EYES;
    private Expression<LivingEntity> entities;
    private @Nullable Expression<Number> speed;
    private @Nullable Expression<Number> maxPitch;
    private Expression<?> target;

    public static synchronized void register() {
        if (registered) {
            return;
        }
        Skript.registerEffect(
                EffLook.class,
                "(force|make) %livingentities% [to] (face [towards]|look [(at|towards)]) %entity%'s (feet:feet|eyes) [(at|with) [head] [rotation] speed %-number%] [[and] max[imum] [head] pitch %-number%]",
                "(force|make) %livingentities% [to] (face [towards]|look [(at|towards)]) [the] (feet:feet|eyes) of %entity% [(at|with) [head] [rotation] speed %-number%] [[and] max[imum] [head] pitch %-number%]",
                "(force|make) %livingentities% [to] (face [towards]|look [(at|towards)]) %vector/location/entity% [(at|with) [head] [rotation] speed %-number%] [[and] max[imum] [head] pitch %-number%]"
        );
        registered = true;
    }

    @Override
    @SuppressWarnings("unchecked")
    public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
        entities = (Expression<LivingEntity>) exprs[0];
        target = exprs[1];
        speed = (Expression<Number>) exprs[2];
        maxPitch = (Expression<Number>) exprs[3];
        if (parseResult.hasTag("feet")) {
            anchor = EntityAnchorArgument.Anchor.FEET;
        }
        return true;
    }

    @Override
    protected void execute(SkriptEvent event) {
        Object value = target.getSingle(event);
        if (value == null) {
            return;
        }
        EntityAnchorArgument.Anchor targetAnchor = anchor;
        for (LivingEntity entity : entities.getArray(event)) {
            if (value instanceof Entity targetEntity) {
                Vec3 position = targetAnchor == EntityAnchorArgument.Anchor.FEET
                        ? targetEntity.position()
                        : targetEntity.getEyePosition();
                entity.lookAt(EntityAnchorArgument.Anchor.EYES, position);
                continue;
            }
            Vec3 targetPosition = null;
            if (value instanceof Vec3 vec3) {
                targetPosition = vec3;
            } else if (value instanceof FabricLocation location) {
                targetPosition = location.position();
            }
            if (targetPosition != null) {
                entity.lookAt(EntityAnchorArgument.Anchor.EYES, targetPosition);
            }
        }
    }

    @Override
    public String toString(@Nullable SkriptEvent event, boolean debug) {
        return "force " + entities.toString(event, debug) + " to look at " + target.toString(event, debug);
    }
}
