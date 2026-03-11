package ch.njol.skript.expressions;

import ch.njol.skript.Skript;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.util.Kleenean;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.lang.event.SkriptEvent;

public class ExprVectorFromXYZ extends SimpleExpression<Vec3> {

    static {
        Skript.registerExpression(
                ExprVectorFromXYZ.class,
                Vec3.class,
                "[a] [new] vector [(from|at|to)] %number%,[ ]%number%(,[ ]| and )%number%"
        );
    }

    private Expression<Number> x;
    private Expression<Number> y;
    private Expression<Number> z;

    @Override
    @SuppressWarnings("unchecked")
    public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
        x = (Expression<Number>) exprs[0];
        y = (Expression<Number>) exprs[1];
        z = (Expression<Number>) exprs[2];
        return true;
    }

    @Override
    protected Vec3 @Nullable [] get(SkriptEvent event) {
        Number xValue = x.getSingle(event);
        Number yValue = y.getSingle(event);
        Number zValue = z.getSingle(event);
        if (xValue == null || yValue == null || zValue == null) {
            return new Vec3[0];
        }
        return new Vec3[]{new Vec3(xValue.doubleValue(), yValue.doubleValue(), zValue.doubleValue())};
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
