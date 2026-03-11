package ch.njol.skript.expressions;

import ch.njol.skript.Skript;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.simplification.SimplifiedLiteral;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.util.Kleenean;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.lang.event.SkriptEvent;

public class ExprVectorSpherical extends SimpleExpression<Vec3> {

    static {
        Skript.registerExpression(
                ExprVectorSpherical.class,
                Vec3.class,
                "[a] [new] spherical vector [(from|with)] [radius] %number%, [yaw] %number%(,[ and]| and) [pitch] %number%"
        );
    }

    private Expression<Number> radius;
    private Expression<Number> yaw;
    private Expression<Number> pitch;

    @Override
    @SuppressWarnings("unchecked")
    public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
        radius = (Expression<Number>) exprs[0];
        yaw = (Expression<Number>) exprs[1];
        pitch = (Expression<Number>) exprs[2];
        return true;
    }

    @Override
    protected Vec3 @Nullable [] get(SkriptEvent event) {
        Number radiusValue = radius.getSingle(event);
        Number yawValue = yaw.getSingle(event);
        Number pitchValue = pitch.getSingle(event);
        if (radiusValue == null || yawValue == null || pitchValue == null) {
            return new Vec3[0];
        }
        return new Vec3[]{ExprVectorFromYawAndPitch.fromYawAndPitch(
                yawValue.floatValue(),
                pitchValue.floatValue()
        ).scale(Math.abs(radiusValue.doubleValue()))};
    }

    @Override
    public boolean isSingle() {
        return true;
    }

    @Override
    public Class<? extends Vec3> getReturnType() {
        return Vec3.class;
    }

    @Override
    public Expression<? extends Vec3> simplify() {
        if (radius instanceof Literal<?> && yaw instanceof Literal<?> && pitch instanceof Literal<?>) {
            return SimplifiedLiteral.fromExpression(this);
        }
        return this;
    }

    @Override
    public String toString(@Nullable SkriptEvent event, boolean debug) {
        return "spherical vector with radius " + radius.toString(event, debug)
                + ", yaw " + yaw.toString(event, debug)
                + " and pitch " + pitch.toString(event, debug);
    }
}
