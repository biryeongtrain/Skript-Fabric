package ch.njol.skript.conditions;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Condition;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Kleenean;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.fabric.compat.FabricLocation;
import org.skriptlang.skript.lang.event.SkriptEvent;

@Name("Has Line of Sight")
@Description("Checks whether living entities have an unobstructed line of sight to other entities or locations.")
@Example("player has direct line of sight to location 5 blocks to the right of player")
@Example("victim has line of sight to attacker")
@Example("player has no line of sight to location 100 blocks in front of player")
@Since("2.8.0")
public class CondHasLineOfSight extends Condition {

    static {
        Skript.registerCondition(CondHasLineOfSight.class,
                "%livingentities% (has|have) [a] [direct] line of sight to %entities/locations%",
                "%livingentities% does(n't| not) have [a] [direct] line of sight to %entities/locations%",
                "%livingentities% (has|have) no [direct] line of sight to %entities/locations%");
    }

    private Expression<LivingEntity> viewers;
    private Expression<?> targets;

    @Override
    @SuppressWarnings("unchecked")
    public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
        viewers = (Expression<LivingEntity>) exprs[0];
        targets = exprs[1];
        setNegated(matchedPattern > 0);
        return true;
    }

    @Override
    public boolean check(SkriptEvent event) {
        return targets.check(event, target -> viewers.check(event, viewer -> hasLineOfSight(viewer, target)), isNegated());
    }

    private static boolean hasLineOfSight(LivingEntity viewer, Object target) {
        if (target instanceof Entity entityTarget) {
            return viewer.hasLineOfSight(entityTarget);
        }
        if (target instanceof FabricLocation locationTarget) {
            if (viewer.level() != locationTarget.level()) {
                return false;
            }
            Vec3 start = viewer.getEyePosition();
            HitResult hit = viewer.level().clip(new ClipContext(
                    start,
                    locationTarget.position(),
                    ClipContext.Block.COLLIDER,
                    ClipContext.Fluid.NONE,
                    viewer
            ));
            return hit.getType() == HitResult.Type.MISS;
        }
        return false;
    }

    @Override
    public String toString(@Nullable SkriptEvent event, boolean debug) {
        return viewers.toString(event, debug) + " has" + (isNegated() ? " no" : "") + " line of sight to " + targets.toString(event, debug);
    }
}
