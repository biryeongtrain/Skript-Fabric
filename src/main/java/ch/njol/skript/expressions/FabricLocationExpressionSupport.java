package ch.njol.skript.expressions;

import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.fabric.compat.FabricBlock;
import org.skriptlang.skript.fabric.compat.FabricLocation;

final class FabricLocationExpressionSupport {

    private FabricLocationExpressionSupport() {
    }

    static @Nullable FabricBlock blockAt(@Nullable FabricLocation location) {
        if (location == null || location.level() == null) {
            return null;
        }
        return new FabricBlock(location.level(), BlockPos.containing(location.position()));
    }

    static FabricLocation centered(@Nullable FabricLocation location) {
        if (location == null) {
            throw new IllegalArgumentException("location must not be null");
        }
        BlockPos position = BlockPos.containing(location.position());
        return new FabricLocation(location.level(), new Vec3(position.getX() + 0.5D, position.getY(), position.getZ() + 0.5D));
    }

    static FabricLocation withAxis(@Nullable FabricLocation location, int axis, double value) {
        if (location == null) {
            throw new IllegalArgumentException("location must not be null");
        }
        Vec3 position = location.position();
        return switch (axis) {
            case 0 -> new FabricLocation(location.level(), new Vec3(value, position.y, position.z));
            case 1 -> new FabricLocation(location.level(), new Vec3(position.x, value, position.z));
            case 2 -> new FabricLocation(location.level(), new Vec3(position.x, position.y, value));
            default -> throw new IllegalArgumentException("Unknown axis " + axis);
        };
    }
}
