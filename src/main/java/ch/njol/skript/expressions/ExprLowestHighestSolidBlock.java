package ch.njol.skript.expressions;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.SimplePropertyExpression;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Kleenean;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.fabric.compat.FabricBlock;
import org.skriptlang.skript.fabric.compat.FabricLocation;

@Name("Lowest/Highest Solid Block")
@Description({
        "Obtains the lowest or highest solid block at a location.",
        "The y-coordinate of the input location is ignored."
})
@Example("teleport the player to the block above the highest solid block at the player's location")
@Since("2.2-dev34, 2.9.0, Fabric")
public class ExprLowestHighestSolidBlock extends SimplePropertyExpression<FabricLocation, FabricBlock> {

    static {
        Skript.registerExpression(
                ExprLowestHighestSolidBlock.class,
                FabricBlock.class,
                "[the] (highest|:lowest) [solid] block (at|of) %locations%",
                "%locations%'[s] (highest|:lowest) [solid] block"
        );
    }

    private boolean lowest;

    @Override
    public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
        lowest = parseResult.hasTag("lowest");
        return super.init(exprs, matchedPattern, isDelayed, parseResult);
    }

    @Override
    public @Nullable FabricBlock convert(FabricLocation location) {
        ServerLevel level = location.level();
        if (level == null) {
            return null;
        }
        int x = (int) Math.floor(location.position().x);
        int z = (int) Math.floor(location.position().z);
        return lowest ? lowestSolidBlock(level, x, z) : highestSolidBlock(level, x, z);
    }

    static @Nullable FabricBlock highestSolidBlock(ServerLevel level, int x, int z) {
        int y = level.getHeight(Heightmap.Types.MOTION_BLOCKING, x, z) - 1;
        if (y < level.getMinY()) {
            return null;
        }
        BlockPos pos = new BlockPos(x, y, z);
        return isSolid(level.getBlockState(pos)) ? new FabricBlock(level, pos) : null;
    }

    static @Nullable FabricBlock lowestSolidBlock(ServerLevel level, int x, int z) {
        FabricBlock highest = highestSolidBlock(level, x, z);
        for (int y = level.getMinY(); y <= level.getMaxY(); y++) {
            BlockPos pos = new BlockPos(x, y, z);
            if (isSolid(level.getBlockState(pos))) {
                return new FabricBlock(level, pos);
            }
        }
        return highest;
    }

    static boolean isSolid(BlockState state) {
        return !state.isAir() && state.blocksMotion();
    }

    @Override
    public Class<? extends FabricBlock> getReturnType() {
        return FabricBlock.class;
    }

    @Override
    protected String getPropertyName() {
        return (lowest ? "lowest" : "highest") + " solid block";
    }
}
