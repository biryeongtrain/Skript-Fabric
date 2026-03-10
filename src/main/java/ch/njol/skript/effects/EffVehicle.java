package ch.njol.skript.effects;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.entity.EntityData;
import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Kleenean;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.lang.event.SkriptEvent;

@Name("Vehicle")
@Description("Makes an entity ride another entity, e.g. a minecart, a saddled pig, an arrow, etc.")
@Example("make the player ride a saddled pig")
@Example("make the attacker ride the victim")
@Since("2.0")
public class EffVehicle extends Effect {

    private static boolean registered;

    private @Nullable Expression<Entity> passengers;
    private @Nullable Expression<?> vehicles;

    public static synchronized void register() {
        if (registered) {
            return;
        }
        Skript.registerEffect(
                EffVehicle.class,
                "(make|let|force) %entities% [to] (ride|mount) [(in|on)] %entity/entitydata%",
                "(make|let|force) %entities% [to] (dismount|(dismount|leave) (from|of|) (any|the[ir]|his|her|) vehicle[s])",
                "(eject|dismount) (any|the|) passenger[s] (of|from) %entities%",
                "eject passenger[s] (of|from) %entities%"
        );
        registered = true;
    }

    @Override
    @SuppressWarnings("unchecked")
    public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
        passengers = matchedPattern >= 2 ? null : (Expression<Entity>) exprs[0];
        vehicles = matchedPattern == 1 ? null : exprs[exprs.length - 1];
        return true;
    }

    @Override
    protected void execute(SkriptEvent event) {
        if (vehicles == null) {
            assert passengers != null;
            for (Entity passenger : passengers.getArray(event)) {
                passenger.stopRiding();
            }
            return;
        }
        if (passengers == null) {
            for (Object vehicle : vehicles.getArray(event)) {
                if (vehicle instanceof Entity entity) {
                    entity.ejectPassengers();
                }
            }
            return;
        }

        Entity[] passengerArray = passengers.getArray(event);
        if (passengerArray.length == 0) {
            return;
        }
        Object vehicleObject = vehicles.getSingle(event);
        if (vehicleObject instanceof Entity vehicleEntity) {
            for (Entity passenger : passengerArray) {
                if (passenger == vehicleEntity) {
                    continue;
                }
                passenger.stopRiding();
                passenger.startRiding(vehicleEntity, true);
            }
            return;
        }
        if (vehicleObject instanceof EntityData<?> vehicleData) {
            for (Entity passenger : passengerArray) {
                Entity spawned = spawnVehicle(passenger, vehicleData);
                if (spawned == null) {
                    continue;
                }
                passenger.stopRiding();
                passenger.startRiding(spawned, true);
            }
        }
    }

    private @Nullable Entity spawnVehicle(Entity passenger, EntityData<?> vehicleData) {
        Object minecraftType = EffectRuntimeSupport.invokeCompatible(vehicleData, "getMinecraftType");
        if (!(minecraftType instanceof EntityType<?> entityType)) {
            return null;
        }
        Entity entity;
        Object created = EffectRuntimeSupport.invokeCompatible(entityType, "create", passenger.level(), null);
        if (!(created instanceof Entity createdEntity)) {
            created = EffectRuntimeSupport.invokeCompatible(entityType, "create", passenger.level());
            if (!(created instanceof Entity fallback)) {
                return null;
            }
            entity = fallback;
        } else {
            entity = createdEntity;
        }
        entity.setPos(passenger.getX(), passenger.getY(), passenger.getZ());
        EffectRuntimeSupport.invokeCompatible(entity, "setYRot", passenger.getYRot());
        EffectRuntimeSupport.invokeCompatible(entity, "setXRot", passenger.getXRot());
        if (passenger.level().addFreshEntity(entity)) {
            return entity;
        }
        return null;
    }

    @Override
    public String toString(@Nullable SkriptEvent event, boolean debug) {
        if (vehicles == null) {
            assert passengers != null;
            return "make " + passengers.toString(event, debug) + " dismount";
        }
        if (passengers == null) {
            return "eject passenger" + (vehicles.isSingle() ? "" : "s") + " of " + vehicles.toString(event, debug);
        }
        return "make " + passengers.toString(event, debug) + " ride " + vehicles.toString(event, debug);
    }
}
