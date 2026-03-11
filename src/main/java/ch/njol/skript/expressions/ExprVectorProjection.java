package ch.njol.skript.expressions;

import ch.njol.skript.Skript;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.util.Kleenean;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.lang.event.SkriptEvent;

public class ExprVectorProjection extends SimpleExpression<Vec3> {

    static {
        Skript.registerExpression(
                ExprVectorProjection.class,
                Vec3.class,
                "[vector] projection [of] %vector% on[to] %vector%"
        );
    }

    private Expression<Vec3> left;
    private Expression<Vec3> right;

    @Override
    @SuppressWarnings("unchecked")
    public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
        left = (Expression<Vec3>) exprs[0];
        right = (Expression<Vec3>) exprs[1];
        return true;
    }

    @Override
    protected Vec3 @Nullable [] get(SkriptEvent event) {
        Vec3 leftValue = left.getOptionalSingle(event).orElse(Vec3.ZERO);
        Vec3 rightValue = right.getOptionalSingle(event).orElse(Vec3.ZERO);
        double scalar = Vec3ExpressionSupport.dot(leftValue, rightValue) / rightValue.lengthSqr();
        return new Vec3[]{rightValue.scale(scalar)};
    }

    @Override
    public boolean isSingle() {
        return true;
    }

    @Override
    public Class<? extends Vec3> getReturnType() {
        return Vec3.class;
    }
}
