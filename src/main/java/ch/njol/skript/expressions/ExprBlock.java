package ch.njol.skript.expressions;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.WrapperExpression;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.util.ConvertedExpression;
import ch.njol.util.Kleenean;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.bukkit.base.expressions.ExprEventBlock;
import org.skriptlang.skript.fabric.compat.FabricBlock;
import org.skriptlang.skript.fabric.compat.FabricLocation;
import org.skriptlang.skript.lang.converter.ConverterInfo;
import org.skriptlang.skript.lang.event.SkriptEvent;

@Name("Block")
@Description("The event block or the block at a location.")
@Example("block is stone")
@Example("set block at player's location to air")
@Since("1.0")
public class ExprBlock extends WrapperExpression<FabricBlock> {

    static {
        Skript.registerExpression(ExprBlock.class, FabricBlock.class, "[the] [event-]block", "[the] block at %location%");
    }

    @Override
    @SuppressWarnings("unchecked")
    public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
        if (exprs.length == 0) {
            ExprEventBlock eventBlock = new ExprEventBlock();
            eventBlock.init(exprs, matchedPattern, isDelayed, parseResult);
            setExpr(eventBlock);
            return true;
        }
        setExpr(new ConvertedExpression<>(
                (Expression<? extends FabricLocation>) exprs[0],
                FabricBlock.class,
                new ConverterInfo<>(FabricLocation.class, FabricBlock.class, FabricLocationExpressionSupport::blockAt, 0)
        ));
        return true;
    }

    @Override
    public String toString(@Nullable SkriptEvent event, boolean debug) {
        return getExpr() instanceof ExprEventBlock ? "the block" : "the block at " + getExpr().toString(event, debug);
    }
}
