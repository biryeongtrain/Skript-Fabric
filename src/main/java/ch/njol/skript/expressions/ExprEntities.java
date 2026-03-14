package ch.njol.skript.expressions;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.entity.EntityData;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.util.SimpleExpression;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.border.WorldBorder;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.fabric.compat.FabricLocation;
import org.skriptlang.skript.lang.event.SkriptEvent;

import java.lang.reflect.Array;
import java.util.*;

@Name("Entities")
@Description("All entities in all worlds, in a specific world, in a radius around a certain location or within two locations. " +
		"e.g. <code>all players</code>, <code>all creepers in the player's world</code>, or <code>players in radius 100 of the player</code>.")
@Example("kill all creepers in the player's world")
@Example("send \"Psst!\" to all players within 100 meters of the player")
@Example("give a diamond to all ops")
@Example("heal all tamed wolves in radius 2000 around {town center}")
@Example("size of all players within {_corner::1} and {_corner::2}")
@Since("1.2.1, 2.10 (within), Fabric")
public class ExprEntities extends SimpleExpression<Entity> {

	static {
		Skript.registerExpression(ExprEntities.class, Entity.class,
				"[(all [[of] the]|the)] %*entitydatas% [(in|of) [world[s]] %-worlds%]",
				"[(all [[of] the]|the)] entities of type[s] %entitydatas% [(in|of) [world[s]] %-worlds%]",
				"[(all [[of] the]|the)] %*entitydatas% (within|[with]in radius) %number% [(block[s]|met(er|re)[s])] (of|around) %location%",
				"[(all [[of] the]|the)] entities of type[s] %entitydatas% in radius %number% (of|around) %location%",
				"[(all [[of] the]|the)] %*entitydatas% within %location% and %location%",
				"[(all [[of] the]|the)] entities of type[s] %entitydatas% within %location% and %location%");
	}

	@SuppressWarnings("null")
	Expression<? extends EntityData<?>> types;

	private @Nullable Expression<ServerLevel> worlds;
	private @Nullable Expression<Number> radius;
	private @Nullable Expression<FabricLocation> center;
	private @Nullable Expression<FabricLocation> from;
	private @Nullable Expression<FabricLocation> to;

	private Class<? extends Entity> returnType = Entity.class;
	private boolean isUsingRadius;
	private boolean isUsingCuboid;

	@Override
	@SuppressWarnings("unchecked")
	public boolean init(Expression<?>[] exprs, int matchedPattern, ch.njol.util.Kleenean isDelayed, ParseResult parseResult) {
		types = (Expression<? extends EntityData<?>>) exprs[0];
		if (matchedPattern % 2 == 0) {
			if (types instanceof Literal) {
				// For literal patterns (not "entities of type"), require plural or "all" prefix
				String expr = parseResult.expr.toLowerCase(Locale.ROOT);
				if (!expr.startsWith("all") && !expr.startsWith("the"))
					return false;
			}
		}
		isUsingRadius = matchedPattern == 2 || matchedPattern == 3;
		isUsingCuboid = matchedPattern >= 4;
		if (isUsingRadius) {
			radius = (Expression<Number>) exprs[1];
			center = (Expression<FabricLocation>) exprs[2];
		} else if (isUsingCuboid) {
			from = (Expression<FabricLocation>) exprs[1];
			to = (Expression<FabricLocation>) exprs[2];
		} else {
			worlds = (Expression<ServerLevel>) exprs[1];
		}
		if (types instanceof Literal && ((Literal<EntityData<?>>) types).getAll(null).length == 1)
			returnType = ((Literal<EntityData<?>>) types).getSingle(null).getType();
		return true;
	}

	@Override
	@SuppressWarnings("unchecked")
	protected Entity @Nullable [] get(SkriptEvent event) {
		EntityData<?>[] entityTypes = types.getAll(event);
		if (entityTypes == null || entityTypes.length == 0)
			return null;

		List<Entity> entities = new ArrayList<>();

		if (isUsingRadius) {
			FabricLocation location = center != null ? center.getSingle(event) : null;
			if (location == null || location.level() == null)
				return null;
			Number number = radius != null ? radius.getSingle(event) : null;
			if (number == null)
				return null;
			double rad = number.doubleValue();

			ServerLevel level = location.level();
			Vec3 pos = location.position();
			AABB aabb = new AABB(
				pos.x - rad, pos.y - rad, pos.z - rad,
				pos.x + rad, pos.y + rad, pos.z + rad
			);

			double radiusSquared = rad * rad * Skript.EPSILON_MULT;
			for (EntityData<?> type : entityTypes) {
				for (Entity entity : level.getEntitiesOfClass(type.getType(), aabb, type::isInstance)) {
					if (entity.distanceToSqr(pos) <= radiusSquared) {
						entities.add(entity);
					}
				}
			}
		} else if (isUsingCuboid) {
			FabricLocation corner1 = from != null ? from.getSingle(event) : null;
			if (corner1 == null || corner1.level() == null)
				return null;
			FabricLocation corner2 = to != null ? to.getSingle(event) : null;
			if (corner2 == null)
				return null;

			ServerLevel level = corner1.level();
			Vec3 p1 = corner1.position();
			Vec3 p2 = corner2.position();
			AABB aabb = new AABB(
				Math.min(p1.x, p2.x), Math.min(p1.y, p2.y), Math.min(p1.z, p2.z),
				Math.max(p1.x, p2.x), Math.max(p1.y, p2.y), Math.max(p1.z, p2.z)
			);

			for (EntityData<?> type : entityTypes) {
				entities.addAll(level.getEntitiesOfClass(type.getType(), aabb, type::isInstance));
			}
		} else {
			// World-based or all-worlds query
			Collection<ServerLevel> levels;
			if (worlds != null) {
				ServerLevel[] worldArray = worlds.getAll(event);
				if (worldArray == null || worldArray.length == 0)
					levels = getAllLevels(event);
				else
					levels = List.of(worldArray);
			} else {
				levels = getAllLevels(event);
			}

			for (ServerLevel level : levels) {
				WorldBorder border = level.getWorldBorder();
				AABB worldBounds = new AABB(
					border.getMinX(), level.getMinY(), border.getMinZ(),
					border.getMaxX(), level.getMaxY(), border.getMaxZ()
				);
				for (EntityData<?> type : entityTypes) {
					entities.addAll(level.getEntitiesOfClass(type.getType(), worldBounds, type::isInstance));
				}
			}
		}

		if (entities.isEmpty())
			return null;
		return entities.toArray((Entity[]) Array.newInstance(returnType, entities.size()));
	}

	private static Collection<ServerLevel> getAllLevels(SkriptEvent event) {
		if (event.server() != null) {
			List<ServerLevel> levels = new ArrayList<>();
			event.server().getAllLevels().forEach(levels::add);
			return levels;
		}
		if (event.level() != null) {
			return List.of(event.level());
		}
		return List.of();
	}

	@Override
	public boolean isSingle() {
		return false;
	}

	@Override
	public Class<? extends Entity> getReturnType() {
		return returnType;
	}

	@Override
	@SuppressWarnings("null")
	public String toString(@Nullable SkriptEvent event, boolean debug) {
		String message = "all entities of type " + types.toString(event, debug);
		if (worlds != null)
			message += " in " + worlds.toString(event, debug);
		else if (radius != null && center != null)
			message += " in radius " + radius.toString(event, debug) + " around " + center.toString(event, debug);
		else if (from != null && to != null)
			message += " within " + from.toString(event, debug) + " and " + to.toString(event, debug);
		return message;
	}

}
