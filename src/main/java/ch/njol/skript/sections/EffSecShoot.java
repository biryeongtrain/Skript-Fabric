package ch.njol.skript.sections;

import ch.njol.skript.Skript;
import ch.njol.skript.config.SectionNode;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.entity.EntityData;
import ch.njol.skript.lang.EffectSection;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.Trigger;
import ch.njol.skript.lang.TriggerItem;
import ch.njol.skript.lang.util.SectionUtils;
import ch.njol.skript.util.Direction;
import ch.njol.skript.variables.Variables;
import ch.njol.util.Kleenean;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.fabric.compat.FabricLocation;
import org.skriptlang.skript.lang.event.SkriptEvent;

import java.util.List;

@Name("Shoot")
@Description("Shoots a projectile (or any other entity) from a given entity or location.")
@Example("shoot arrow from all players at speed 2")
@Example("""
	shoot a pig from all players:
		add event-entity to {_projectiles::*}
	""")
@Since("2.10")
public class EffSecShoot extends EffectSection {

	public static void register() {
		Skript.registerSection(EffSecShoot.class,
			"shoot %entitydatas% [from %livingentities/locations%] [(at|with) (speed|velocity) %-number%] [%-direction%]",
			"(make|let) %livingentities/locations% shoot %entitydatas% [(at|with) (speed|velocity) %-number%] [%-direction%]"
		);
	}

	private static final double DEFAULT_SPEED = 5.0;
	@SuppressWarnings("unchecked")
	private Expression<EntityData<?>> types;
	private Expression<?> shooters;
	private @Nullable Expression<Number> velocity;
	private @Nullable Expression<Direction> direction;
	public static @Nullable Entity lastSpawned;
	private @Nullable Trigger trigger;

	@Override
	@SuppressWarnings("unchecked")
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult,
			@Nullable SectionNode sectionNode, @Nullable List<TriggerItem> triggerItems) {
		types = (Expression<EntityData<?>>) exprs[matchedPattern];
		shooters = exprs[1 - matchedPattern];
		velocity = (Expression<Number>) exprs[2];
		direction = (Expression<Direction>) exprs[3];

		if (sectionNode != null) {
			trigger = SectionUtils.loadLinkedCode("shoot", (beforeLoading, afterLoading)
					-> loadCode(sectionNode, "shoot", beforeLoading, afterLoading));
			return trigger != null;
		}

		return true;
	}

	@Override
	protected @Nullable TriggerItem walk(SkriptEvent event) {
		lastSpawned = null;
		Number finalVelocity = velocity != null ? velocity.getSingle(event) : DEFAULT_SPEED;
		Direction finalDirection = direction != null ? direction.getSingle(event) : Direction.ZERO;
		if (finalVelocity == null || finalDirection == null)
			return null;
		EntityData<?>[] data = types.getArray(event);

		for (Object shooter : shooters.getArray(event)) {
			for (EntityData<?> entityData : data) {
				Vec3 vector;
				if (shooter instanceof LivingEntity livingShooter) {
					vector = finalDirection.getDirection(livingShooter).scale(finalVelocity.doubleValue());
					ServerLevel level = (ServerLevel) livingShooter.level();
					net.minecraft.world.entity.EntityType<?> mcType = entityData.getMinecraftEntityType();
					if (mcType == null) continue;

					Entity entity = mcType.create(level, EntitySpawnReason.TRIGGERED);
					if (entity == null) continue;

					Vec3 spawnPos = livingShooter.position().add(0, livingShooter.getEyeHeight() / 2, 0);
					entity.setPos(spawnPos);

					if (entity instanceof Projectile projectile) {
						projectile.setOwner(livingShooter);
					}

					entity.setDeltaMovement(vector);

					if (trigger != null) {
						SkriptEvent sectionEvent = new SkriptEvent(entity, event.server(), event.level(), event.player());
						Variables.withLocalVariables(event, sectionEvent, () -> TriggerItem.walk(trigger, sectionEvent));
					}

					level.addFreshEntity(entity);
					lastSpawned = entity;
				} else if (shooter instanceof FabricLocation location) {
					vector = finalDirection.getDirection(location).scale(finalVelocity.doubleValue());
					net.minecraft.world.entity.EntityType<?> mcType = entityData.getMinecraftEntityType();
					if (mcType == null) continue;

					ServerLevel level = location.level();
					Entity entity = mcType.create(level, EntitySpawnReason.TRIGGERED);
					if (entity == null) continue;

					entity.setPos(location.position());
					entity.setDeltaMovement(vector);

					if (trigger != null) {
						SkriptEvent sectionEvent = new SkriptEvent(entity, event.server(), event.level(), event.player());
						Variables.withLocalVariables(event, sectionEvent, () -> TriggerItem.walk(trigger, sectionEvent));
					}

					level.addFreshEntity(entity);
					lastSpawned = entity;
				}
			}
		}

		return super.walk(event, false);
	}

	@Override
	public String toString(@Nullable SkriptEvent event, boolean debug) {
		return "shoot " + types.toString(event, debug) + " from " + shooters.toString(event, debug)
			+ (velocity != null ? " at speed " + velocity.toString(event, debug) : "")
			+ (direction != null ? " " + direction.toString(event, debug) : "");
	}

}
