package ch.njol.skript.util;

import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.util.SimpleExpression;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.fabric.compat.FabricBlock;
import org.skriptlang.skript.fabric.compat.FabricLocation;
import org.skriptlang.skript.lang.event.SkriptEvent;

public final class Direction {

    public static final double IGNORE_PITCH = 0xF1A7;
    public static final Direction ZERO = new Direction(Vec3.ZERO);

    private final boolean relative;
    private final double pitchOrX;
    private final double yawOrY;
    private final double lengthOrZ;

    public Direction(Vec3 vector) {
        this.relative = false;
        this.pitchOrX = vector.x;
        this.yawOrY = vector.y;
        this.lengthOrZ = vector.z;
    }

    public Direction(double pitch, double yaw, double length) {
        this.relative = true;
        this.pitchOrX = pitch;
        this.yawOrY = yaw;
        this.lengthOrZ = length;
    }

    public Direction(net.minecraft.core.Direction direction, double length) {
        this(new Vec3(direction.getStepX(), direction.getStepY(), direction.getStepZ()).normalize().scale(length));
    }

    public Vec3 getDirection() {
        if (!relative) {
            return new Vec3(pitchOrX, yawOrY, lengthOrZ);
        }
        return getDirection(0.0, 0.0);
    }

    public Vec3 getDirection(FabricLocation location) {
        if (!relative) {
            return new Vec3(pitchOrX, yawOrY, lengthOrZ);
        }
        return getDirection(0.0, 0.0);
    }

    public Vec3 getDirection(Entity entity) {
        if (!relative) {
            return new Vec3(pitchOrX, yawOrY, lengthOrZ);
        }
        Vec3 look = entity.getLookAngle();
        if (look.lengthSqr() == 0.0) {
            return Vec3.ZERO;
        }
        double yaw = Math.atan2(look.z, look.x);
        double pitch = Math.atan2(look.y, Math.sqrt(look.x * look.x + look.z * look.z));
        return getDirection(pitchOrX == IGNORE_PITCH ? 0.0 : pitch, yaw);
    }

    public Vec3 getDirection(FabricBlock block) {
        if (!relative) {
            return new Vec3(pitchOrX, yawOrY, lengthOrZ);
        }
        net.minecraft.core.Direction facing = getFacing(block);
        if (facing == null) {
            return Vec3.ZERO;
        }
        double pitch = pitchOrX == IGNORE_PITCH ? 0.0 : facing.getAxis() == net.minecraft.core.Direction.Axis.Y
                ? (facing == net.minecraft.core.Direction.UP ? Math.PI / 2 : -Math.PI / 2)
                : 0.0;
        double yaw = Math.atan2(facing.getStepZ(), facing.getStepX());
        return getDirection(pitch, yaw);
    }

    public FabricLocation getRelative(FabricLocation location) {
        return new FabricLocation(location.level(), location.position().add(getDirection(location)));
    }

    public FabricLocation getRelative(Entity entity) {
        if (!(entity.level() instanceof net.minecraft.server.level.ServerLevel level)) {
            return new FabricLocation(null, entity.position());
        }
        return new FabricLocation(level, entity.position().add(getDirection(entity)));
    }

    public FabricLocation getRelative(FabricBlock block) {
        return new FabricLocation(block.level(), Vec3.atCenterOf(block.position()).add(getDirection(block)));
    }

    private Vec3 getDirection(double pitch, double yaw) {
        if (pitchOrX == IGNORE_PITCH) {
            return new Vec3(Math.cos(yaw + yawOrY) * lengthOrZ, 0.0, Math.sin(yaw + yawOrY) * lengthOrZ);
        }
        double horizontal = Math.cos(pitch + pitchOrX) * lengthOrZ;
        return new Vec3(
                Math.cos(yaw + yawOrY) * horizontal,
                Math.sin(pitch + pitchOrX) * Math.cos(yawOrY) * lengthOrZ,
                Math.sin(yaw + yawOrY) * horizontal
        );
    }

    public static @Nullable net.minecraft.core.Direction getFacing(FabricBlock block) {
        BlockState state = block.state();
        if (state.hasProperty(BlockStateProperties.FACING)) {
            return state.getValue(BlockStateProperties.FACING);
        }
        if (state.hasProperty(BlockStateProperties.HORIZONTAL_FACING)) {
            return state.getValue(BlockStateProperties.HORIZONTAL_FACING);
        }
        return null;
    }

    public static net.minecraft.core.Direction getFacing(Entity entity, boolean horizontal) {
        return getFacing(entity.getLookAngle(), horizontal);
    }

    public static net.minecraft.core.Direction getFacing(Vec3 look, boolean horizontal) {
        if (look.lengthSqr() == 0.0) {
            return net.minecraft.core.Direction.NORTH;
        }
        if (horizontal) {
            double x = look.x;
            double z = look.z;
            if (Math.abs(x) >= Math.abs(z)) {
                return x >= 0 ? net.minecraft.core.Direction.EAST : net.minecraft.core.Direction.WEST;
            }
            return z >= 0 ? net.minecraft.core.Direction.SOUTH : net.minecraft.core.Direction.NORTH;
        }
        double ax = Math.abs(look.x);
        double ay = Math.abs(look.y);
        double az = Math.abs(look.z);
        if (ay >= ax && ay >= az) {
            return look.y >= 0 ? net.minecraft.core.Direction.UP : net.minecraft.core.Direction.DOWN;
        }
        if (ax >= az) {
            return look.x >= 0 ? net.minecraft.core.Direction.EAST : net.minecraft.core.Direction.WEST;
        }
        return look.z >= 0 ? net.minecraft.core.Direction.SOUTH : net.minecraft.core.Direction.NORTH;
    }

    public static Expression<FabricLocation> combine(
            Expression<? extends Direction> directions,
            Expression<? extends FabricLocation> locations
    ) {
        return new SimpleExpression<>() {
            @Override
            protected FabricLocation @Nullable [] get(SkriptEvent event) {
                Direction direction = directions.getSingle(event);
                if (direction == null) {
                    return new FabricLocation[0];
                }
                return locations.stream(event)
                        .map(direction::getRelative)
                        .toArray(FabricLocation[]::new);
            }

            @Override
            public boolean isSingle() {
                return directions.isSingle() && locations.isSingle();
            }

            @Override
            public Class<? extends FabricLocation> getReturnType() {
                return FabricLocation.class;
            }

            @Override
            public String toString(@Nullable SkriptEvent event, boolean debug) {
                return directions.toString(event, debug) + " " + locations.toString(event, debug);
            }
        };
    }

    @Override
    public String toString() {
        if (!relative) {
            return "direction(" + pitchOrX + ", " + yawOrY + ", " + lengthOrZ + ")";
        }
        return "relative direction";
    }
}
