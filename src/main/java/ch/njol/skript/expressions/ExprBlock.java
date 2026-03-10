package ch.njol.skript.expressions;

import ch.njol.skript.Skript;
import ch.njol.skript.expressions.base.WrapperExpression;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.util.ConvertedExpression;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.skript.util.Direction;
import ch.njol.util.Kleenean;
import net.minecraft.core.BlockPos;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.fabric.compat.FabricBlock;
import org.skriptlang.skript.fabric.compat.FabricLocation;
import org.skriptlang.skript.fabric.runtime.FabricBlockEventHandle;
import org.skriptlang.skript.lang.converter.ConverterInfo;
import org.skriptlang.skript.lang.event.SkriptEvent;

public class ExprBlock extends WrapperExpression<FabricBlock> {

    static {
        Skript.registerExpression(ExprBlock.class, FabricBlock.class, "[the] [event-]block");
        Skript.registerExpression(ExprBlock.class, FabricBlock.class, "[the] block %direction% [%location%]");
    }

    @Override
    @SuppressWarnings("unchecked")
    public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parser) {
        if (exprs.length > 0) {
            setExpr(new ConvertedExpression<>(
                    Direction.combine((Expression<? extends Direction>) exprs[0], (Expression<? extends FabricLocation>) exprs[1]),
                    FabricBlock.class,
                    new ConverterInfo<>(FabricLocation.class, FabricBlock.class,
                            location -> location.level() == null ? null : new FabricBlock(location.level(), BlockPos.containing(location.position())),
                            0)
            ));
            return true;
        }
        setExpr(new SimpleExpression<>() {
            @Override
            protected FabricBlock @Nullable [] get(SkriptEvent event) {
                if (!(event.handle() instanceof FabricBlockEventHandle handle)) {
                    return new FabricBlock[0];
                }
                return new FabricBlock[]{new FabricBlock(handle.level(), handle.position())};
            }

            @Override
            public boolean isSingle() {
                return true;
            }

            @Override
            public Class<? extends FabricBlock> getReturnType() {
                return FabricBlock.class;
            }
        });
        return true;
    }

    @Override
    public String toString(@Nullable SkriptEvent event, boolean debug) {
        return exprString(event, debug);
    }

    private String exprString(@Nullable SkriptEvent event, boolean debug) {
        return getExpr() instanceof SimpleExpression<?> ? "the block" : "the block " + getExpr().toString(event, debug);
    }
}
