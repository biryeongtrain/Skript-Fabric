package ch.njol.skript.expressions;

import ch.njol.skript.expressions.base.SimplePropertyExpression;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.fabric.compat.FabricBlock;

public class ExprRedstoneBlockPower extends SimplePropertyExpression<FabricBlock, Long> {

    static {
        register(ExprRedstoneBlockPower.class, Long.class, "redstone power", "blocks");
    }

    @Override
    public @Nullable Long convert(FabricBlock block) {
        return block.level() == null ? null : (long) block.level().getBestNeighborSignal(block.position());
    }

    @Override
    public Class<? extends Long> getReturnType() {
        return Long.class;
    }

    @Override
    protected String getPropertyName() {
        return "redstone power";
    }
}
