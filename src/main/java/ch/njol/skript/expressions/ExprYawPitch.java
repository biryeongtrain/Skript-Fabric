package ch.njol.skript.expressions;

import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.expressions.base.SimplePropertyExpression;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Kleenean;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.lang.event.SkriptEvent;

public class ExprYawPitch extends SimplePropertyExpression<Object, Float> {

    static {
        register(ExprYawPitch.class, Float.class, "(:yaw|pitch)", "entities/locations/vectors");
    }

    private boolean yaw;

    @Override
    public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
        yaw = parseResult.hasTag("yaw");
        return super.init(exprs, matchedPattern, isDelayed, parseResult);
    }

    @Override
    public @Nullable Float convert(Object object) {
        if (object instanceof Entity entity) {
            return yaw
                    ? Vec3ExpressionSupport.normalizeYaw(entity.getYRot())
                    : entity.getXRot();
        }
        if (object instanceof Vec3 vector) {
            return yaw ? Vec3ExpressionSupport.skriptYaw(vector) : Vec3ExpressionSupport.skriptPitch(vector);
        }
        return null;
    }

    @Override
    public Class<?> @Nullable [] acceptChange(ChangeMode mode) {
        return switch (mode) {
            case SET, ADD, REMOVE, RESET -> new Class[]{Number.class};
            default -> null;
        };
    }

    @Override
    @SuppressWarnings("unchecked")
    public void change(SkriptEvent event, Object @Nullable [] delta, ChangeMode mode) {
        float amount = delta == null || delta.length == 0 || delta[0] == null ? 0.0F : ((Number) delta[0]).floatValue();
        for (Object object : getExpr().getArray(event)) {
            if (object instanceof Entity entity) {
                changeEntity(entity, amount, mode);
            }
        }
        Expression<Object> expression = (Expression<Object>) getExpr();
        expression.changeInPlace(event, value -> value instanceof Vec3 vector ? changeVector(vector, amount, mode) : value);
    }

    private void changeEntity(Entity entity, float amount, ChangeMode mode) {
        if (yaw) {
            entity.setYRot(next(entity.getYRot(), amount, mode, false));
        } else {
            entity.setXRot(next(entity.getXRot(), amount, mode, true));
        }
    }

    private Object changeVector(Vec3 vector, float amount, ChangeMode mode) {
        float nextYaw = Vec3ExpressionSupport.skriptYaw(vector);
        float nextPitch = Vec3ExpressionSupport.skriptPitch(vector);
        if (yaw) {
            nextYaw = next(nextYaw, amount, mode, false);
        } else {
            nextPitch = next(nextPitch, amount, mode, true);
        }
        return Vec3ExpressionSupport.withSkriptYawPitch(vector, nextYaw, nextPitch);
    }

    private float next(float current, float amount, ChangeMode mode, boolean invertAdditive) {
        return switch (mode) {
            case SET -> amount;
            case ADD -> invertAdditive ? current - amount : current + amount;
            case REMOVE -> invertAdditive ? current + amount : current - amount;
            case RESET -> 0.0F;
            default -> current;
        };
    }

    @Override
    public Class<? extends Float> getReturnType() {
        return Float.class;
    }

    @Override
    protected String getPropertyName() {
        return yaw ? "yaw" : "pitch";
    }
}
