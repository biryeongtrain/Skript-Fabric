package ch.njol.skript.expressions;

import ch.njol.skript.Skript;
import ch.njol.skript.lang.util.SimpleExpression;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.fabric.compat.FabricBlock;
import org.skriptlang.skript.lang.event.SkriptEvent;

public class ExprPushedBlocks extends SimpleExpression<FabricBlock> {

    static {
        Skript.registerExpression(ExprPushedBlocks.class, FabricBlock.class, "[the] pushed block[s]");
    }

    @Override
    protected FabricBlock @Nullable [] get(SkriptEvent event) {
        return new FabricBlock[0];
    }

    @Override
    public boolean isSingle() {
        return false;
    }

    @Override
    public Class<? extends FabricBlock> getReturnType() {
        return FabricBlock.class;
    }
}
