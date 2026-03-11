package ch.njol.skript.expressions;

import ch.njol.skript.Skript;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.util.Kleenean;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.lang.event.SkriptEvent;

public class ExprVectorDotProduct extends SimpleExpression<Number> {

    static {
        Skript.registerExpression(
                ExprVectorDotProduct.class,
                Number.class,
                "%vector% dot %vector%"
        );
    }

    private Expression<Vec3> first;
    private Expression<Vec3> second;

    @Override
    @SuppressWarnings("unchecked")
    public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
        first = (Expression<Vec3>) exprs[0];
        second = (Expression<Vec3>) exprs[1];
        return true;
    }

    @Override
    protected Number @Nullable [] get(SkriptEvent event) {
        Vec3 firstValue = first.getSingle(event);
        Vec3 secondValue = second.getSingle(event);
        if (firstValue == null || secondValue == null) {
            return new Number[0];
        }
        return new Number[]{Vec3ExpressionSupport.dot(firstValue, secondValue)};
    }

    @Override
    public boolean isSingle() {
        return true;
    }

    @Override
    public Class<? extends Number> getReturnType() {
        return Number.class;
    }
}
