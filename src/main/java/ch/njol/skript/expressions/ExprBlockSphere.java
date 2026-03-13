package ch.njol.skript.expressions;

import ch.njol.skript.Skript;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.util.Kleenean;
import ch.njol.util.coll.iterator.CheckedIterator;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.NoSuchElementException;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.fabric.compat.FabricBlock;
import org.skriptlang.skript.fabric.compat.FabricLocation;
import org.skriptlang.skript.lang.event.SkriptEvent;

public class ExprBlockSphere extends SimpleExpression<FabricBlock> {

    static {
        Skript.registerExpression(ExprBlockSphere.class, FabricBlock.class,
                "[(all [[of] the]|the)] blocks in radius %number% [(of|around) %location%]",
                "[(all [[of] the]|the)] blocks around %location% in radius %number%");
    }

    private Expression<Number> radius;
    private Expression<FabricLocation> center;

    @Override
    @SuppressWarnings("unchecked")
    public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parser) {
        radius = (Expression<Number>) exprs[matchedPattern];
        center = (Expression<FabricLocation>) exprs[1 - matchedPattern];
        return true;
    }

    @Override
    public @Nullable Iterator<FabricBlock> iterator(SkriptEvent event) {
        FabricLocation location = center.getSingle(event);
        Number resolvedRadius = radius.getSingle(event);
        if (location == null || location.level() == null || resolvedRadius == null) {
            return null;
        }
        return new CheckedIterator<>(cubeIterator(location, resolvedRadius.doubleValue()), block -> inSphere(block, location, resolvedRadius.doubleValue()));
    }

    @Override
    protected FabricBlock @Nullable [] get(SkriptEvent event) {
        Number resolvedRadius = radius.getSingle(event);
        if (resolvedRadius == null) {
            return new FabricBlock[0];
        }
        Iterator<FabricBlock> iterator = iterator(event);
        if (iterator == null) {
            return new FabricBlock[0];
        }
        int estimatedSize = (int) Math.max(0, 1.1D * 4D / 3D * Math.PI * Math.pow(resolvedRadius.doubleValue(), 3));
        ArrayList<FabricBlock> blocks = new ArrayList<>(estimatedSize);
        iterator.forEachRemaining(blocks::add);
        return blocks.toArray(FabricBlock[]::new);
    }

    private Iterator<FabricBlock> cubeIterator(FabricLocation center, double radius) {
        BlockPos min = BlockPos.containing(center.position().subtract(radius + 0.5001D, radius + 0.5001D, radius + 0.5001D));
        BlockPos max = BlockPos.containing(center.position().add(radius + 0.5001D, radius + 0.5001D, radius + 0.5001D));
        return new Iterator<>() {
            private int x = min.getX();
            private int y = min.getY();
            private int z = min.getZ();
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
                FabricBlock block = new FabricBlock(center.level(), new BlockPos(x, y, z));
                if (++x > max.getX()) {
                    x = min.getX();
                    if (++z > max.getZ()) {
                        z = min.getZ();
                        if (++y > max.getY()) {
                            exhausted = true;
                        }
                    }
                }
                return block;
            }
        };
    }

    private boolean inSphere(FabricBlock block, FabricLocation center, double radius) {
        Vec3 blockCenter = new Vec3(
                block.position().getX() + 0.5D,
                block.position().getY() + 0.5D,
                block.position().getZ() + 0.5D
        );
        return blockCenter.distanceToSqr(center.position()) < radius * radius * Skript.EPSILON_MULT;
    }

    @Override
    public Class<? extends FabricBlock> getReturnType() {
        return FabricBlock.class;
    }

    @Override
    public String toString(@Nullable SkriptEvent event, boolean debug) {
        return "the blocks in radius " + radius + " around " + center.toString(event, debug);
    }

    @Override
    public boolean isLoopOf(String input) {
        return input.equalsIgnoreCase("block");
    }

    @Override
    public boolean isSingle() {
        return false;
    }
}
