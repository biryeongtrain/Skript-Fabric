package ch.njol.skript.expressions;

import ch.njol.skript.Skript;
import ch.njol.skript.expressions.base.WrapperExpression;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.skript.util.Direction;
import ch.njol.util.Kleenean;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.fabric.compat.FabricLocation;
import org.skriptlang.skript.lang.event.SkriptEvent;

public class ExprLocation extends WrapperExpression<FabricLocation> {

    static {
        Skript.registerExpression(ExprLocation.class, FabricLocation.class, "[the] [event-](location|position)");
        Skript.registerExpression(ExprLocation.class, FabricLocation.class, "[the] (location|position) %directions% [%location%]");
    }

    @Override
    @SuppressWarnings("unchecked")
    public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
        if (exprs.length == 0) {
            setExpr(new SimpleExpression<>() {
                @Override
                protected FabricLocation @Nullable [] get(SkriptEvent event) {
                    FabricLocation location = FabricLocationExpressionSupport.eventLocation(event);
                    return location == null ? new FabricLocation[0] : new FabricLocation[]{location};
                }

                @Override
                public boolean isSingle() {
                    return true;
                }

                @Override
                public Class<? extends FabricLocation> getReturnType() {
                    return FabricLocation.class;
                }
            });
            return true;
        }
        Expression<? extends FabricLocation> source = exprs[1] == null
                ? new SimpleExpression<>() {
                    @Override
                    protected FabricLocation @Nullable [] get(SkriptEvent event) {
                        FabricLocation location = FabricLocationExpressionSupport.eventLocation(event);
                        return location == null ? new FabricLocation[0] : new FabricLocation[]{location};
                    }

                    @Override
                    public boolean isSingle() {
                        return true;
                    }

                    @Override
                    public Class<? extends FabricLocation> getReturnType() {
                        return FabricLocation.class;
                    }
                }
                : (Expression<? extends FabricLocation>) exprs[1];
        setExpr(Direction.combine((Expression<? extends Direction>) exprs[0], source));
        return true;
    }

    @Override
    public String toString(@Nullable SkriptEvent event, boolean debug) {
        return getExpr() instanceof SimpleExpression<?> ? "the location" : "the location " + getExpr().toString(event, debug);
    }
}
