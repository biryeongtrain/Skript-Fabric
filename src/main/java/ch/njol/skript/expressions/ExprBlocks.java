package ch.njol.skript.expressions;

import ch.njol.skript.Skript;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.skript.util.AABB;
import ch.njol.skript.util.BlockLineIterator;
import ch.njol.skript.util.Direction;
import ch.njol.util.Kleenean;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import net.minecraft.world.level.chunk.LevelChunk;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.fabric.compat.FabricBlock;
import org.skriptlang.skript.fabric.compat.FabricLocation;
import org.skriptlang.skript.lang.event.SkriptEvent;

public class ExprBlocks extends SimpleExpression<FabricBlock> {

    static {
        Skript.registerExpression(ExprBlocks.class, FabricBlock.class,
                "[(all [[of] the]|the)] blocks %direction% [%locations%]",
                "[(all [[of] the]|the)] blocks from %location% [on] %direction%",
                "[(all [[of] the]|the)] blocks from %location% to %location%",
                "[(all [[of] the]|the)] blocks between %location% and %location%",
                "[(all [[of] the]|the)] blocks within %location% and %location%",
                "[(all [[of] the]|the)] blocks (in|within) %chunk%");
    }

    private @Nullable Expression<Direction> direction;
    private @Nullable Expression<FabricLocation> end;
    private @Nullable Expression<LevelChunk> chunk;
    private Expression<?> from;
    private int pattern;

    @Override
    @SuppressWarnings("unchecked")
    public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parser) {
        pattern = matchedPattern;
        switch (matchedPattern) {
            case 0 -> {
                direction = (Expression<Direction>) exprs[0];
                from = exprs[1];
            }
            case 1 -> {
                from = exprs[0];
                direction = (Expression<Direction>) exprs[1];
            }
            case 2, 3, 4 -> {
                from = exprs[0];
                end = (Expression<FabricLocation>) exprs[1];
            }
            case 5 -> chunk = (Expression<LevelChunk>) exprs[0];
            default -> {
                return false;
            }
        }
        return true;
    }

    @Override
    protected FabricBlock @Nullable [] get(SkriptEvent event) {
        Iterator<FabricBlock> iterator = iterator(event);
        if (iterator == null) {
            return new FabricBlock[0];
        }
        List<FabricBlock> blocks = new ArrayList<>();
        iterator.forEachRemaining(blocks::add);
        return blocks.toArray(FabricBlock[]::new);
    }

    @Override
    public @Nullable Iterator<FabricBlock> iterator(SkriptEvent event) {
        if (chunk != null) {
            LevelChunk resolved = chunk.getSingle(event);
            return resolved == null ? null : new AABB(resolved).iterator();
        }
        if (direction != null) {
            Direction resolved = direction.getSingle(event);
            if (resolved == null) {
                return null;
            }
            if (!from.isSingle()) {
                List<FabricBlock> blocks = from.stream(event)
                        .filter(FabricLocation.class::isInstance)
                        .map(FabricLocation.class::cast)
                        .map(resolved::getRelative)
                        .filter(location -> location.level() != null)
                        .map(location -> new FabricBlock(location.level(), net.minecraft.core.BlockPos.containing(location.position())))
                        .toList();
                return blocks.iterator();
            }
            Object origin = from.getSingle(event);
            if (origin == null) {
                return null;
            }
            if (origin instanceof FabricBlock block) {
                return new BlockLineIterator(block, resolved.getDirection(block), distanceFromDirection(event));
            }
            FabricLocation location = (FabricLocation) origin;
            if (location.level() == null) {
                return null;
            }
            return new BlockLineIterator(location, resolved.getDirection(location), distanceFromDirection(event));
        }
        FabricLocation start = (FabricLocation) from.getSingle(event);
        if (start == null || start.level() == null || end == null) {
            return null;
        }
        FabricLocation finish = end.getSingle(event);
        if (finish == null || finish.level() != start.level()) {
            return null;
        }
        if (pattern == 4) {
            return new AABB(start, finish).iterator();
        }
        return new BlockLineIterator(start, finish);
    }

    private double distanceFromDirection(SkriptEvent event) {
        if (direction instanceof ExprDirection exprDirection && exprDirection.amount != null) {
            Number number = exprDirection.amount.getSingle(event);
            if (number != null) {
                return number.doubleValue();
            }
        }
        return 15.0;
    }

    @Override
    public Class<? extends FabricBlock> getReturnType() {
        return FabricBlock.class;
    }

    @Override
    public boolean isSingle() {
        return false;
    }
}
