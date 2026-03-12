package ch.njol.skript.expressions;

import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.expressions.base.SimplePropertyExpression;
import ch.njol.util.Math2;
import ch.njol.util.Kleenean;
import java.util.Set;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SeaPickleBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.fabric.compat.FabricBlock;
import org.skriptlang.skript.lang.event.SkriptEvent;

public class ExprSeaPickles extends SimplePropertyExpression<FabricBlock, Integer> {

    private static final int MIN_PICKLES = 1;
    private static final int MAX_PICKLES = 4;

    static {
        register(ExprSeaPickles.class, Integer.class, "[:(min|max)[imum]] [sea] pickle(s| (count|amount))", "blocks");
    }

    private boolean minimum;
    private boolean maximum;

    @Override
    public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
        minimum = parseResult.hasTag("min");
        maximum = parseResult.hasTag("max");
        return super.init(exprs, matchedPattern, isDelayed, parseResult);
    }

    @Override
    public @Nullable Integer convert(FabricBlock block) {
        return readPickleCount(block.state(), minimum, maximum);
    }

    static @Nullable Integer readPickleCount(BlockState state, boolean minimum, boolean maximum) {
        if (!state.hasProperty(SeaPickleBlock.PICKLES)) {
            return null;
        }
        if (maximum) {
            return MAX_PICKLES;
        }
        if (minimum) {
            return MIN_PICKLES;
        }
        return state.getValue(SeaPickleBlock.PICKLES);
    }

    @Override
    public Class<?> @Nullable [] acceptChange(ChangeMode mode) {
        if (minimum || maximum) {
            return null;
        }
        return switch (mode) {
            case SET, ADD, REMOVE, RESET, DELETE -> new Class[]{Number.class};
            default -> null;
        };
    }

    @Override
    public void change(SkriptEvent event, Object @Nullable [] delta, ChangeMode mode) {
        int amount = delta == null || delta.length == 0 ? 0 : ((Number) delta[0]).intValue();
        if (mode == ChangeMode.REMOVE) {
            amount = -amount;
        }
        for (FabricBlock block : getExpr().getArray(event)) {
            BlockState state = block.state();
            if (!state.hasProperty(SeaPickleBlock.PICKLES)) {
                continue;
            }
            int current = state.getValue(SeaPickleBlock.PICKLES);
            int next = switch (mode) {
                case SET -> amount;
                case ADD, REMOVE -> current + amount;
                case RESET -> MIN_PICKLES;
                case DELETE -> 0;
            };
            setPickleCount(block, next);
        }
    }

    static void setPickleCount(FabricBlock block, int count) {
        BlockState state = block.state();
        if (!state.hasProperty(SeaPickleBlock.PICKLES)) {
            return;
        }
        if (count <= 0) {
            boolean waterlogged = state.hasProperty(BlockStateProperties.WATERLOGGED) && state.getValue(BlockStateProperties.WATERLOGGED);
            block.level().setBlockAndUpdate(block.position(), waterlogged ? Blocks.WATER.defaultBlockState() : Blocks.AIR.defaultBlockState());
            return;
        }
        int clamped = (int) Math2.fit(MIN_PICKLES, count, MAX_PICKLES);
        block.level().setBlockAndUpdate(block.position(), state.setValue(SeaPickleBlock.PICKLES, clamped));
    }

    @Override
    public Class<? extends Integer> getReturnType() {
        return Integer.class;
    }

    @Override
    protected String getPropertyName() {
        return (maximum ? "maximum " : minimum ? "minimum " : "") + "sea pickle count";
    }
}
