package ch.njol.skript.expressions;

import ch.njol.skript.Skript;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.util.Kleenean;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.lang.event.SkriptEvent;

public class ExprVectorCrossProduct extends SimpleExpression<Vec3> {

    static {
        Skript.registerExpression(
                ExprVectorCrossProduct.class,
                Vec3.class,
                "%vector% cross %vector%"
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
    protected Vec3 @Nullable [] get(SkriptEvent event) {
        Vec3 firstValue = first.getSingle(event);
        Vec3 secondValue = second.getSingle(event);
        if (firstValue == null || secondValue == null) {
            return new Vec3[0];
        }
        return new Vec3[]{Vec3ExpressionSupport.cross(firstValue, secondValue)};
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
