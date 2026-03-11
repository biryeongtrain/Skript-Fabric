package ch.njol.skript.expressions;

import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.expressions.base.SimplePropertyExpression;
import ch.njol.skript.lang.Expression;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.lang.event.SkriptEvent;

public class ExprVectorLength extends SimplePropertyExpression<Vec3, Number> {

    static {
        register(ExprVectorLength.class, Number.class, "(vector|standard|normal) length[s]", "vectors");
    }

    @Override
    public Number convert(Vec3 vector) {
        return Vec3ExpressionSupport.length(vector);
    }

    @Override
    public Class<?> @Nullable [] acceptChange(ChangeMode mode) {
        return switch (mode) {
            case ADD, REMOVE, SET -> new Class[]{Number.class};
            default -> null;
        };
    }

    @Override
    @SuppressWarnings("unchecked")
    public void change(SkriptEvent event, Object @Nullable [] delta, ChangeMode mode) {
        if (delta == null || delta.length == 0 || delta[0] == null) {
            return;
        }
        double amount = ((Number) delta[0]).doubleValue();
        Expression<Vec3> expression = (Expression<Vec3>) getExpr();
        expression.changeInPlace(event, vector -> {
            double currentLength = vector.length();
            double nextLength = switch (mode) {
                case ADD -> currentLength + amount;
                case REMOVE -> currentLength - amount;
                case SET -> amount;
                default -> currentLength;
            };
            if (!Double.isFinite(nextLength) || nextLength <= 0.0D || vector.lengthSqr() == 0.0D) {
                return Vec3.ZERO;
            }
            return vector.normalize().scale(nextLength);
        });
    }

    @Override
    public Class<? extends Number> getReturnType() {
        return Number.class;
    }

    @Override
    protected String getPropertyName() {
        return "vector length";
    }
}
