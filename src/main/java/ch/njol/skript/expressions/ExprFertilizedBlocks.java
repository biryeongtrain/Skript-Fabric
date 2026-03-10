package ch.njol.skript.expressions;

import ch.njol.skript.Skript;
import ch.njol.skript.events.FabricEventCompatHandles;
import ch.njol.skript.lang.EventRestrictedSyntax;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.util.Kleenean;
import java.util.Iterator;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.fabric.compat.FabricBlock;
import org.skriptlang.skript.lang.event.SkriptEvent;

public class ExprFertilizedBlocks extends SimpleExpression<FabricBlock> implements EventRestrictedSyntax {

    static {
        Skript.registerExpression(ExprFertilizedBlocks.class, FabricBlock.class, "[all] [the] fertilized blocks");
    }

    @Override
    public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
        return true;
    }

    @Override
    public Class<?>[] supportedEvents() {
        return new Class<?>[]{FabricEventCompatHandles.BlockFertilize.class};
    }

    @Override
    protected FabricBlock @Nullable [] get(SkriptEvent event) {
        if (!(event.handle() instanceof FabricEventCompatHandles.BlockFertilize handle) || handle.blocks() == null) {
            return null;
        }
        return handle.blocks().toArray(FabricBlock[]::new);
    }

    @Override
    public @Nullable Iterator<? extends FabricBlock> iterator(SkriptEvent event) {
        if (!(event.handle() instanceof FabricEventCompatHandles.BlockFertilize handle) || handle.blocks() == null) {
            return null;
        }
        return handle.blocks().iterator();
    }

    @Override
    public boolean isSingle() {
        return false;
    }

    @Override
    public Class<? extends FabricBlock> getReturnType() {
        return FabricBlock.class;
    }

    @Override
    public String toString(@Nullable SkriptEvent event, boolean debug) {
        return "the fertilized blocks";
    }
}
