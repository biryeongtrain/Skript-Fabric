package ch.njol.skript.expressions;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Events;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.events.FabricEventCompatHandles;
import ch.njol.skript.lang.EventRestrictedSyntax;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.util.Kleenean;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.fabric.compat.FabricBlock;
import org.skriptlang.skript.lang.event.SkriptEvent;

@Name("Exploded Blocks")
@Description("Get all the blocks that were destroyed in an explode event.")
@Example("""
    on explode:
        loop exploded blocks:
            broadcast "%loop-block%"
    """)
@Events("explode")
@Since("2.5")
public class ExprExplodedBlocks extends SimpleExpression<FabricBlock> implements EventRestrictedSyntax {

    static {
        Skript.registerExpression(ExprExplodedBlocks.class, FabricBlock.class, "[the] exploded blocks");
    }

    @Override
    public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
        return true;
    }

    @Override
    public Class<?>[] supportedEvents() {
        return new Class<?>[]{FabricEventCompatHandles.Explosion.class};
    }

    @Override
    protected FabricBlock @Nullable [] get(SkriptEvent event) {
        if (!(event.handle() instanceof FabricEventCompatHandles.Explosion handle)
                || handle.explodedBlocks() == null) {
            return null;
        }
        return handle.explodedBlocks().toArray(FabricBlock[]::new);
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
        return "exploded blocks";
    }
}
