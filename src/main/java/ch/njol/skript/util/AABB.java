package ch.njol.skript.util;

import java.util.Iterator;
import java.util.NoSuchElementException;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.chunk.LevelChunk;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.fabric.compat.FabricBlock;
import org.skriptlang.skript.fabric.compat.FabricLocation;

public final class AABB implements Iterable<FabricBlock> {

    private final ServerLevel level;
    private final BlockPos lower;
    private final BlockPos upper;

    public AABB(FabricLocation one, FabricLocation two) {
        if (one.level() != two.level()) {
            throw new IllegalArgumentException("Locations must be in the same world");
        }
        this.level = one.level();
        BlockPos a = BlockPos.containing(one.position());
        BlockPos b = BlockPos.containing(two.position());
        this.lower = new BlockPos(
                Math.min(a.getX(), b.getX()),
                Math.min(a.getY(), b.getY()),
                Math.min(a.getZ(), b.getZ())
        );
        this.upper = new BlockPos(
                Math.max(a.getX(), b.getX()),
                Math.max(a.getY(), b.getY()),
                Math.max(a.getZ(), b.getZ())
        );
    }

    public AABB(LevelChunk chunk) {
        this.level = (ServerLevel) chunk.getLevel();
        this.lower = new BlockPos(chunk.getPos().getMinBlockX(), level.getMinY(), chunk.getPos().getMinBlockZ());
        this.upper = new BlockPos(chunk.getPos().getMaxBlockX(), level.getMaxY(), chunk.getPos().getMaxBlockZ());
    }

    @Override
    public Iterator<FabricBlock> iterator() {
        return new Iterator<>() {
            private int x = lower.getX();
            private int y = lower.getY();
            private int z = lower.getZ();
            private boolean exhausted;

            @Override
            public boolean hasNext() {
                return !exhausted;
            }

            @Override
            public FabricBlock next() {
                if (exhausted) {
                    throw new NoSuchElementException();
                }
                FabricBlock block = new FabricBlock(level, new BlockPos(x, y, z));
                if (++x > upper.getX()) {
                    x = lower.getX();
                    if (++z > upper.getZ()) {
                        z = lower.getZ();
                        if (++y > upper.getY()) {
                            exhausted = true;
                        }
                    }
                }
                return block;
            }
        };
    }
}
