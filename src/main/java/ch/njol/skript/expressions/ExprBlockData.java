package ch.njol.skript.expressions;

import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.expressions.base.SimplePropertyExpression;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.fabric.compat.FabricBlock;
import org.skriptlang.skript.lang.event.SkriptEvent;

public class ExprBlockData extends SimplePropertyExpression<Object, BlockState> {

    static {
        register(ExprBlockData.class, BlockState.class, "block[ ]data", "blocks/blockstates");
    }

    @Override
    public @Nullable BlockState convert(Object object) {
        if (object instanceof FabricBlock block) {
            return block.state();
        }
        if (object instanceof BlockState blockState) {
            return blockState;
        }
        return null;
    }

    @Override
    public Class<?> @Nullable [] acceptChange(ChangeMode mode) {
        return mode == ChangeMode.SET ? new Class[]{BlockState.class} : null;
    }

    @Override
    public void change(SkriptEvent event, Object @Nullable [] delta, ChangeMode mode) {
        if (mode != ChangeMode.SET || delta == null || delta.length == 0 || !(delta[0] instanceof BlockState state)) {
            return;
        }
        for (Object object : getExpr().getArray(event)) {
            if (object instanceof FabricBlock block) {
                block.level().setBlock(block.position(), state, 3);
            }
        }
    }

    @Override
    public Class<? extends BlockState> getReturnType() {
        return BlockState.class;
    }

    @Override
    protected String getPropertyName() {
        return "block data";
    }
}
