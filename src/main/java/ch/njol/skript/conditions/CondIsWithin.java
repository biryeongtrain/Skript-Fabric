package ch.njol.skript.conditions;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.RequiredPlugins;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Condition;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.SyntaxStringBuilder;
import ch.njol.util.Kleenean;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.server.level.ServerLevel;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.fabric.compat.FabricBlock;
import org.skriptlang.skript.fabric.compat.FabricLocation;
import org.skriptlang.skript.lang.event.SkriptEvent;

@Name("Is Within")
@Description({
        "Whether a location is within something else. The \"something\" can be a block, an entity, a chunk, a world, "
                + "or a cuboid formed by two other locations.",
        "Note that using the <a href='#CondCompare'>is between</a> condition will refer to a straight line "
                + "between locations, while this condition will refer to the cuboid between locations."
})
@Example("""
	if player's location is within {_loc1} and {_loc2}:
		send "You are in a PvP zone!" to player
	""")
@Example("""
	if player is in world("world"):
		send "You are in the overworld!" to player
	""")
@Example("""
	if attacker's location is inside of victim:
		cancel event
		send "Back up!" to attacker and victim
	""")
@Since("2.7, 2.11 (world borders)")
@RequiredPlugins("MC 1.17+ (within block)")
public class CondIsWithin extends Condition {

    static {
        Skript.registerCondition(
                CondIsWithin.class,
                "%locations% (is|are) within %location% and %location%",
                "%locations% (isn't|is not|aren't|are not) within %location% and %location%",
                "%locations% (is|are) (within|in[side [of]]) %entities/worlds/blocks%",
                "%locations% (isn't|is not|aren't|are not) (within|in[side [of]]) %entities/worlds/blocks%"
        );
    }

    private Expression<FabricLocation> locsToCheck;
    private Expression<FabricLocation> loc1;
    private Expression<FabricLocation> loc2;
    private Expression<?> area;
    private boolean withinLocations;

    @Override
    @SuppressWarnings("unchecked")
    public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
        setNegated(matchedPattern % 2 == 1);
        locsToCheck = (Expression<FabricLocation>) exprs[0];
        if (matchedPattern <= 1) {
            withinLocations = true;
            loc1 = (Expression<FabricLocation>) exprs[1];
            loc2 = (Expression<FabricLocation>) exprs[2];
        } else {
            withinLocations = false;
            area = exprs[1];
        }
        return true;
    }

    @Override
    public boolean check(SkriptEvent event) {
        if (withinLocations) {
            FabricLocation one = loc1.getSingle(event);
            FabricLocation two = loc2.getSingle(event);
            if (one == null || two == null || one.level() != two.level()) {
                return isNegated();
            }
            AABB box = AABB.encapsulatingFullBlocks(
                    net.minecraft.core.BlockPos.containing(one.position()),
                    net.minecraft.core.BlockPos.containing(two.position())
            ).inflate(1.0E-7D);
            return locsToCheck.check(event, location -> location.level() == one.level() && box.contains(location.position()), isNegated());
        }

        Object[] areas = area.getAll(event);
        return locsToCheck.check(event, location -> area.getAnd() ? containsAll(location, areas) : containsAny(location, areas), isNegated());
    }

    private static boolean contains(FabricLocation location, Object object) {
        if (object instanceof Entity entity) {
            return entity.level() == location.level() && entity.getBoundingBox().contains(location.position());
        }
        if (object instanceof FabricBlock block) {
            if (block.level() != location.level()) {
                return false;
            }
            Vec3 local = location.position().subtract(Vec3.atLowerCornerOf(block.position()));
            for (AABB box : block.state().getCollisionShape(block.level(), block.position()).toAabbs()) {
                if (box.contains(local)) {
                    return true;
                }
            }
            return false;
        }
        if (object instanceof ServerLevel world) {
            return location.level() == world;
        }
        return false;
    }

    private static boolean containsAll(FabricLocation location, Object[] areas) {
        for (Object area : areas) {
            if (!contains(location, area)) {
                return false;
            }
        }
        return true;
    }

    private static boolean containsAny(FabricLocation location, Object[] areas) {
        for (Object area : areas) {
            if (contains(location, area)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public String toString(@Nullable SkriptEvent event, boolean debug) {
        SyntaxStringBuilder builder = new SyntaxStringBuilder(event, debug);
        builder.append(locsToCheck, "is within");
        if (withinLocations) {
            builder.append(loc1, "and", loc2);
        } else {
            builder.append(area);
        }
        return builder.toString();
    }
}
