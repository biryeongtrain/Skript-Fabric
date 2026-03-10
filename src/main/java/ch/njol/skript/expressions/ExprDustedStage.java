package ch.njol.skript.expressions;

import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.expressions.base.PropertyExpression;
import net.minecraft.world.level.block.BrushableBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.fabric.compat.FabricBlock;
import org.skriptlang.skript.lang.event.SkriptEvent;

public class ExprDustedStage extends PropertyExpression<Object, Integer> {

    static {
        register(ExprDustedStage.class, Integer.class, "[:max[imum]] dust[ed|ing] (value|stage|progress[ion])", "blocks/blockstates");
    }

    private boolean max;

    @Override
    public boolean init(ch.njol.skript.lang.Expression<?>[] exprs, int matchedPattern, ch.njol.util.Kleenean isDelayed,
                        ch.njol.skript.lang.SkriptParser.ParseResult parseResult) {
        setExpr((ch.njol.skript.lang.Expression<Object>) exprs[0]);
        max = parseResult.hasTag("max");
        return true;
    }

    @Override
    protected Integer @Nullable [] get(SkriptEvent event, Object[] source) {
        return get(source, value -> {
            BlockState state = state(value);
            if (state == null || !(state.getBlock() instanceof BrushableBlock) || !state.hasProperty(BlockStateProperties.DUSTED)) {
                return null;
            }
            return max ? 3 : state.getValue(BlockStateProperties.DUSTED);
        });
    }

    @Override
    public Class<?> @Nullable [] acceptChange(ChangeMode mode) {
        if (max) {
            return null;
        }
        return switch (mode) {
            case SET, ADD, REMOVE, RESET -> new Class[]{Integer.class};
            default -> null;
        };
    }

    @Override
    public void change(SkriptEvent event, Object @Nullable [] delta, ChangeMode mode) {
        if (max) {
            return;
        }
        int offset = delta != null && delta.length > 0 && delta[0] instanceof Number number ? number.intValue() : 0;
        for (Object value : getExpr().getArray(event)) {
            if (!(value instanceof FabricBlock block)) {
                continue;
            }
            BlockState state = block.state();
            if (!(state.getBlock() instanceof BrushableBlock) || !state.hasProperty(BlockStateProperties.DUSTED)) {
                continue;
            }
            int current = state.getValue(BlockStateProperties.DUSTED);
            int updated = switch (mode) {
                case SET -> offset;
                case ADD -> current + offset;
                case REMOVE -> current - offset;
                case RESET -> 0;
                default -> current;
            };
            updated = Math.max(0, Math.min(3, updated));
            block.level().setBlock(block.position(), state.setValue(BlockStateProperties.DUSTED, updated), 3);
        }
    }

    private static @Nullable BlockState state(Object value) {
        if (value instanceof FabricBlock block) {
            return block.state();
        }
        if (value instanceof BlockState blockState) {
            return blockState;
        }
        return null;
    }

    @Override
    public Class<? extends Integer> getReturnType() {
        return Integer.class;
    }
}
