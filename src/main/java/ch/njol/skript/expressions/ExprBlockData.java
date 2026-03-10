package ch.njol.skript.expressions;

import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.SimplePropertyExpression;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.fabric.compat.FabricBlock;
import org.skriptlang.skript.lang.event.SkriptEvent;

@Name("Block Data")
@Description("Get the block state associated with a block.")
@Example("set {_data} to block data of target block")
@Since("2.5")
public class ExprBlockData extends SimplePropertyExpression<FabricBlock, BlockState> {

    static {
        register(ExprBlockData.class, BlockState.class, "block[ ]data", "blocks");
    }

    @Override
    public @Nullable BlockState convert(FabricBlock block) {
        return block.level() == null ? null : block.state();
    }

    @Override
    public Class<?> @Nullable [] acceptChange(ChangeMode mode) {
        if (mode == ChangeMode.SET) {
            return new Class[]{BlockState.class};
        }
        return null;
    }

    @Override
    public void change(SkriptEvent event, Object @Nullable [] delta, ChangeMode mode) {
        if (mode != ChangeMode.SET || delta == null || delta.length == 0) {
            return;
        }
        BlockState state = (BlockState) delta[0];
        for (FabricBlock block : getExpr().getArray(event)) {
            if (block.level() != null) {
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
