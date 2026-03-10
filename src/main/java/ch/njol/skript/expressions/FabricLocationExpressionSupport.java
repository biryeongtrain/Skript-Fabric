package ch.njol.skript.expressions;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.fabric.compat.FabricBlock;
import org.skriptlang.skript.fabric.compat.FabricLocation;
import org.skriptlang.skript.fabric.runtime.FabricBlockEventHandle;
import org.skriptlang.skript.fabric.runtime.FabricEntityEventHandle;
import org.skriptlang.skript.lang.event.SkriptEvent;

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

    static @Nullable FabricLocation locationOf(@Nullable Object value) {
        if (value instanceof FabricLocation location) {
            return location;
        }
        if (value instanceof FabricBlock block) {
            return new FabricLocation(block.level(), new Vec3(block.position().getX(), block.position().getY(), block.position().getZ()));
        }
        if (value instanceof Entity entity) {
            ServerLevel level = entity.level() instanceof ServerLevel serverLevel ? serverLevel : null;
            return new FabricLocation(level, entity.position());
        }
        if (value instanceof LevelChunk chunk) {
            ServerLevel level = chunk.getLevel() instanceof ServerLevel serverLevel ? serverLevel : null;
            BlockPos position = chunk.getPos().getWorldPosition();
            return new FabricLocation(level, new Vec3(position.getX(), position.getY(), position.getZ()));
        }
        return null;
    }

    static @Nullable FabricLocation eventLocation(@Nullable SkriptEvent event) {
        if (event == null) {
            return null;
        }
        Object handle = event.handle();
        if (handle instanceof FabricBlockEventHandle blockHandle) {
            BlockPos position = blockHandle.position();
            return new FabricLocation(
                    blockHandle.level(),
                    new Vec3(position.getX(), position.getY(), position.getZ())
            );
        }
        if (handle instanceof FabricEntityEventHandle entityHandle) {
            return locationOf(entityHandle.entity());
        }
        return locationOf(handle);
    }
}
