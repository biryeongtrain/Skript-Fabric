package org.skriptlang.skript.bukkit.base.expressions;

import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.util.Kleenean;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.fabric.compat.FabricBlock;
import org.skriptlang.skript.fabric.runtime.FabricBlockEventHandle;
import org.skriptlang.skript.lang.event.SkriptEvent;

public final class ExprEventBlock extends SimpleExpression<FabricBlock> {

    @Override
    protected FabricBlock @Nullable [] get(SkriptEvent event) {
        if (!(event.handle() instanceof FabricBlockEventHandle handle)) {
            return null;
        }
        return new FabricBlock[]{
                new FabricBlock(handle.level(), handle.position())
        };
    }

    @Override
    public boolean isSingle() {
        return true;
    }

    @Override
    public Class<? extends FabricBlock> getReturnType() {
        return FabricBlock.class;
    }

    @Override
    public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
        return expressions.length == 0;
    }

    @Override
    public String toString(@Nullable SkriptEvent event, boolean debug) {
        return "event-block";
    }
}
