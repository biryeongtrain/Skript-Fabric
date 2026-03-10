package ch.njol.skript.expressions;

import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.expressions.base.SimplePropertyExpression;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.util.Direction;
import ch.njol.util.Kleenean;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.fabric.compat.FabricBlock;
import org.skriptlang.skript.lang.event.SkriptEvent;

public class ExprFacing extends SimplePropertyExpression<Object, Direction> {

    static {
        register(ExprFacing.class, Direction.class, "(1¦horizontal|) facing", "entities/blocks");
    }

    private boolean horizontal;

    @Override
    public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
        horizontal = parseResult.mark == 1;
        return super.init(exprs, matchedPattern, isDelayed, parseResult);
    }

    @Override
    public @Nullable Direction convert(Object value) {
        if (value instanceof FabricBlock block) {
            net.minecraft.core.Direction facing = Direction.getFacing(block);
            return facing == null ? null : new Direction(facing, 1.0);
        }
        if (value instanceof Entity entity) {
            return new Direction(Direction.getFacing(entity, horizontal), 1.0);
        }
        return null;
    }

    @Override
    protected String getPropertyName() {
        return (horizontal ? "horizontal " : "") + "facing";
    }

    @Override
    public Class<Direction> getReturnType() {
        return Direction.class;
    }

    @Override
    public Class<?> @Nullable [] acceptChange(ChangeMode mode) {
        if (!getExpr().canReturn(FabricBlock.class) || mode != ChangeMode.SET) {
            return null;
        }
        return new Class[]{Direction.class};
    }

    @Override
    public void change(SkriptEvent event, Object @Nullable [] delta, ChangeMode mode) {
        if (mode != ChangeMode.SET || delta == null || delta.length == 0 || !(delta[0] instanceof Direction direction)) {
            return;
        }
        FabricBlock block = (FabricBlock) getExpr().getSingle(event);
        if (block == null) {
            return;
        }
        net.minecraft.core.Direction face = Direction.getFacing(direction.getDirection(), false);
        var state = block.state();
        if (state.hasProperty(BlockStateProperties.FACING)) {
            block.level().setBlock(block.position(), state.setValue(BlockStateProperties.FACING, face), 3);
        } else if (state.hasProperty(BlockStateProperties.HORIZONTAL_FACING) && face.getAxis().isHorizontal()) {
            block.level().setBlock(block.position(), state.setValue(BlockStateProperties.HORIZONTAL_FACING, face), 3);
        }
    }
}
