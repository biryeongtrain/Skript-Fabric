package ch.njol.skript.expressions;

import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.classes.Changer.ChangerUtils;
import ch.njol.skript.expressions.base.SimplePropertyExpression;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Kleenean;
import java.util.Locale;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnknownNullability;
import org.joml.Quaternionf;
import org.skriptlang.skript.lang.event.SkriptEvent;

public class ExprXYZComponent extends SimplePropertyExpression<Object, Number> {

    private enum Axis {
        W,
        X,
        Y,
        Z
    }

    static {
        register(ExprXYZComponent.class, Number.class, "[vector|quaternion] (:w|:x|:y|:z) [component[s]]", "vectors/quaternions");
    }

    private ExprXYZComponent.@UnknownNullability Axis axis;

    @Override
    public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
        axis = Axis.valueOf(parseResult.tags.get(0).toUpperCase(Locale.ENGLISH));
        return super.init(exprs, matchedPattern, isDelayed, parseResult);
    }

    @Override
    public @Nullable Number convert(Object object) {
        if (object instanceof Vec3 vector) {
            return switch (axis) {
                case W -> null;
                case X -> vector.x;
                case Y -> vector.y;
                case Z -> vector.z;
            };
        }
        if (object instanceof Quaternionf quaternion) {
            return switch (axis) {
                case W -> quaternion.w;
                case X -> quaternion.x;
                case Y -> quaternion.y;
                case Z -> quaternion.z;
            };
        }
        return null;
    }

    @Override
    public Class<?> @Nullable [] acceptChange(ChangeMode mode) {
        if (mode == ChangeMode.ADD || mode == ChangeMode.REMOVE || mode == ChangeMode.SET) {
            boolean acceptsVectors = ChangerUtils.acceptsChange(getExpr(), ChangeMode.SET, Vec3.class);
            boolean acceptsQuaternions = ChangerUtils.acceptsChange(getExpr(), ChangeMode.SET, Quaternionf.class);
            if (acceptsVectors || acceptsQuaternions) {
                return new Class[]{Number.class};
            }
        }
        return null;
    }

    @Override
    public void change(SkriptEvent event, Object @Nullable [] delta, ChangeMode mode) {
        if (delta == null || delta.length == 0 || delta[0] == null) {
            return;
        }
        double value = ((Number) delta[0]).doubleValue();
        boolean acceptsVectors = ChangerUtils.acceptsChange(getExpr(), ChangeMode.SET, Vec3.class);
        boolean acceptsQuaternions = ChangerUtils.acceptsChange(getExpr(), ChangeMode.SET, Quaternionf.class);
        getExpr().changeInPlace(event, object -> {
            if (acceptsVectors && object instanceof Vec3 vector) {
                return changeVector(vector, value, mode);
            }
            if (acceptsQuaternions && object instanceof Quaternionf quaternion) {
                return changeQuaternion(quaternion, (float) value, mode);
            }
            return object;
        });
    }

    private Object changeVector(Vec3 vector, double value, ChangeMode mode) {
        if (axis == Axis.W) {
            return vector;
        }
        double next = switch (mode) {
            case ADD -> convert(vector).doubleValue() + value;
            case REMOVE -> convert(vector).doubleValue() - value;
            case SET -> value;
            default -> convert(vector).doubleValue();
        };
        return switch (axis) {
            case X -> new Vec3(next, vector.y, vector.z);
            case Y -> new Vec3(vector.x, next, vector.z);
            case Z -> new Vec3(vector.x, vector.y, next);
            case W -> vector;
        };
    }

    private Object changeQuaternion(Quaternionf quaternion, float value, ChangeMode mode) {
        Quaternionf copy = new Quaternionf(quaternion);
        switch (axis) {
            case W -> copy.w = next(copy.w, value, mode);
            case X -> copy.x = next(copy.x, value, mode);
            case Y -> copy.y = next(copy.y, value, mode);
            case Z -> copy.z = next(copy.z, value, mode);
        }
        return copy;
    }

    private float next(float current, float value, ChangeMode mode) {
        return switch (mode) {
            case ADD -> current + value;
            case REMOVE -> current - value;
            case SET -> value;
            default -> current;
        };
    }

    @Override
    public Class<Number> getReturnType() {
        return Number.class;
    }

    @Override
    protected String getPropertyName() {
        return axis.name().toLowerCase(Locale.ENGLISH) + " component";
    }
}
