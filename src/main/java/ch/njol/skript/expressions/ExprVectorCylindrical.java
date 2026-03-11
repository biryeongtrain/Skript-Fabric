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

public class ExprVectorCylindrical extends SimpleExpression<Vec3> {

    private static final double DEG_TO_RAD = Math.PI / 180.0D;

    static {
        Skript.registerExpression(
                ExprVectorCylindrical.class,
                Vec3.class,
                "[a] [new] cylindrical vector [from|with] [radius] %number%, [yaw] %number%(,[ and]| and) [height] %number%"
        );
    }

    private Expression<Number> radius;
    private Expression<Number> yaw;
    private Expression<Number> height;

    @Override
    @SuppressWarnings("unchecked")
    public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
        radius = (Expression<Number>) exprs[0];
        yaw = (Expression<Number>) exprs[1];
        height = (Expression<Number>) exprs[2];
        return true;
    }

    @Override
    protected Vec3 @Nullable [] get(SkriptEvent event) {
        Number radiusValue = radius.getSingle(event);
        Number yawValue = yaw.getSingle(event);
        Number heightValue = height.getSingle(event);
        if (radiusValue == null || yawValue == null || heightValue == null) {
            return new Vec3[0];
        }
        return new Vec3[]{fromCylindrical(radiusValue.doubleValue(), yawValue.floatValue(), heightValue.doubleValue())};
    }

    static Vec3 fromCylindrical(double radius, float skriptYaw, double height) {
        double magnitude = Math.abs(radius);
        double internalYaw = toInternalYaw(skriptYaw) * DEG_TO_RAD;
        return new Vec3(
                Math.cos(internalYaw) * magnitude,
                height,
                Math.sin(internalYaw) * magnitude
        );
    }

    static float toInternalYaw(float skriptYaw) {
        return skriptYaw > 270.0F ? skriptYaw - 270.0F : skriptYaw + 90.0F;
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
        if (radius instanceof Literal<?> && yaw instanceof Literal<?> && height instanceof Literal<?>) {
            return SimplifiedLiteral.fromExpression(this);
        }
        return this;
    }

    @Override
    public String toString(@Nullable SkriptEvent event, boolean debug) {
        return "cylindrical vector with radius " + radius.toString(event, debug)
                + ", yaw " + yaw.toString(event, debug)
                + " and height " + height.toString(event, debug);
    }
}
