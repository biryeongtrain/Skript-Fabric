package ch.njol.skript.expressions;

import ch.njol.skript.Skript;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.util.Kleenean;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.fabric.compat.FabricLocation;
import org.skriptlang.skript.lang.event.SkriptEvent;

public class ExprMidpoint extends SimpleExpression<Object> {

    static {
        Skript.registerExpression(
                ExprMidpoint.class,
                Object.class,
                "[the] mid[-]point (of|between) %locations% and %locations%",
                "[the] mid[-]point (of|between) %vectors% and %vectors%"
        );
    }

    private Expression<?> first;
    private Expression<?> second;
    private Class<?>[] possibleTypes = new Class[]{Object.class};
    private Class<?> returnType = Object.class;

    @Override
    public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
        first = exprs[0];
        second = exprs[1];
        if (matchedPattern == 0) {
            possibleTypes = new Class[]{FabricLocation.class};
            returnType = FabricLocation.class;
            return true;
        }
        if (matchedPattern == 1) {
            possibleTypes = new Class[]{Vec3.class};
            returnType = Vec3.class;
            return true;
        }
        return false;
    }

    @Override
    protected Object @Nullable [] get(SkriptEvent event) {
        Object firstValue = first.getSingle(event);
        Object secondValue = second.getSingle(event);
        if (firstValue == null || secondValue == null) {
            return new Object[0];
        }
        if (firstValue instanceof FabricLocation firstLocation && secondValue instanceof FabricLocation secondLocation) {
            if (firstLocation.level() != secondLocation.level()) {
                return new Object[0];
            }
            return new Object[]{
                    new FabricLocation(
                            firstLocation.level(),
                            Vec3ExpressionSupport.midpoint(firstLocation.position(), secondLocation.position())
                    )
            };
        }
        if (firstValue instanceof Vec3 firstVector && secondValue instanceof Vec3 secondVector) {
            return new Object[]{Vec3ExpressionSupport.midpoint(firstVector, secondVector)};
        }
        return new Object[0];
    }

    @Override
    public boolean isSingle() {
        return true;
    }

    @Override
    public Class<?> getReturnType() {
        return returnType;
    }

    @Override
    public Class<?>[] possibleReturnTypes() {
        return possibleTypes;
    }
}
