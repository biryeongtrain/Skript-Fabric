package ch.njol.skript.expressions;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Events;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.util.Kleenean;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.fabric.compat.FabricBlock;
import org.skriptlang.skript.lang.event.SkriptEvent;

@Name("Source Block")
@Description("The source block exposed by a spread-style event handle.")
@Events("Spread")
@Example("""
    on spread:
        set event-block to source block
    """)
@Since("2.7")
public final class ExprSourceBlock extends SimpleExpression<FabricBlock> {

    static {
        Skript.registerExpression(ExprSourceBlock.class, FabricBlock.class, "[the] source block");
    }

    @Override
    public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
        if (!ReflectiveHandleAccess.currentEventSupports("source", "getSource", "sourceBlock", "getSourceBlock")) {
            Skript.error("The 'source block' is only usable in a spread event.");
            return false;
        }
        return expressions.length == 0;
    }

    @Override
    protected FabricBlock @Nullable [] get(SkriptEvent event) {
        Object value = ReflectiveHandleAccess.invokeNoArg(event.handle(), "source", "getSource", "sourceBlock", "getSourceBlock");
        return value instanceof FabricBlock block ? new FabricBlock[]{block} : null;
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
    public String toString(@Nullable SkriptEvent event, boolean debug) {
        return "source block";
    }
}
