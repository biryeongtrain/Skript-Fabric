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

public class ExprVectorFromYawAndPitch extends SimpleExpression<Vec3> {

    private static final double DEG_TO_RAD = Math.PI / 180.0D;

    static {
        Skript.registerExpression(
                ExprVectorFromYawAndPitch.class,
                Vec3.class,
                "[a] [new] vector (from|with) yaw %number% and pitch %number%",
                "[a] [new] vector (from|with) pitch %number% and yaw %number%"
        );
    }

    private Expression<Number> pitch;
    private Expression<Number> yaw;

    @Override
    @SuppressWarnings("unchecked")
    public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
        pitch = (Expression<Number>) exprs[matchedPattern ^ 1];
        yaw = (Expression<Number>) exprs[matchedPattern];
        return true;
    }

    @Override
    protected Vec3 @Nullable [] get(SkriptEvent event) {
        Number yawValue = yaw.getSingle(event);
        Number pitchValue = pitch.getSingle(event);
        if (yawValue == null || pitchValue == null) {
            return new Vec3[0];
        }
        return new Vec3[]{fromYawAndPitch(wrapAngleDeg(yawValue.floatValue()), wrapAngleDeg(pitchValue.floatValue()))};
    }

    static Vec3 fromYawAndPitch(float skriptYaw, float skriptPitch) {
        double internalYaw = ExprVectorCylindrical.toInternalYaw(skriptYaw) * DEG_TO_RAD;
        double internalPitch = -skriptPitch * DEG_TO_RAD;
        double y = Math.sin(internalPitch);
        double horizontal = Math.cos(internalPitch);
        return new Vec3(
                Math.cos(internalYaw) * horizontal,
                y,
                Math.sin(internalYaw) * horizontal
        );
    }

    static float wrapAngleDeg(float angle) {
        angle %= 360.0F;
        if (angle <= -180.0F) {
            return angle + 360.0F;
        }
        if (angle > 180.0F) {
            return angle - 360.0F;
        }
        return angle;
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
        if (pitch instanceof Literal<?> && yaw instanceof Literal<?>) {
            return SimplifiedLiteral.fromExpression(this);
        }
        return this;
    }

    @Override
    public String toString(@Nullable SkriptEvent event, boolean debug) {
        return "vector from yaw " + yaw.toString(event, debug) + " and pitch " + pitch.toString(event, debug);
    }
}
