package ch.njol.skript.util;

import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.NoSuchElementException;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.skriptlang.skript.fabric.compat.FabricBlock;
import org.skriptlang.skript.fabric.compat.FabricLocation;

public final class BlockLineIterator implements Iterator<FabricBlock> {

    private final Iterator<BlockPos> positions;
    private final ServerLevel level;

    public BlockLineIterator(@NotNull FabricLocation start, @NotNull FabricLocation end) {
        this(start.level(), start.position(), end.position());
    }

    public BlockLineIterator(@NotNull FabricBlock start, @NotNull FabricBlock end) {
        this(start.level(), Vec3.atCenterOf(start.position()), Vec3.atCenterOf(end.position()));
    }

    public BlockLineIterator(@NotNull FabricLocation start, @NotNull Vec3 direction, double distance) {
        this(start.level(), start.position(), start.position().add(direction.normalize().scale(distance)));
    }

    public BlockLineIterator(@NotNull FabricBlock start, @NotNull Vec3 direction, double distance) {
        this(start.level(), Vec3.atCenterOf(start.position()), Vec3.atCenterOf(start.position()).add(direction.normalize().scale(distance)));
    }

    private BlockLineIterator(ServerLevel level, Vec3 start, Vec3 end) {
        this.level = level;
        this.positions = trace(level, start, end).iterator();
    }

    private static Set<BlockPos> trace(ServerLevel level, Vec3 start, Vec3 end) {
        Set<BlockPos> blocks = new LinkedHashSet<>();
        Vec3 delta = end.subtract(start);
        double length = delta.length();
        if (length == 0.0) {
            blocks.add(BlockPos.containing(start));
            return blocks;
        }
        int steps = Math.max(1, (int) Math.ceil(length * 4));
        for (int i = 0; i <= steps; i++) {
            double t = i / (double) steps;
            blocks.add(BlockPos.containing(start.add(delta.scale(t))));
        }
        return blocks;
    }

    @Override
    public boolean hasNext() {
        return positions.hasNext();
    }

    @Override
    public FabricBlock next() {
        if (!hasNext()) {
            throw new NoSuchElementException();
        }
        return new FabricBlock(level, positions.next());
    }
}
