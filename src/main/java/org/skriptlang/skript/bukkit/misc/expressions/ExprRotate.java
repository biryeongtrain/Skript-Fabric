package org.skriptlang.skript.bukkit.misc.expressions;

import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.util.Kleenean;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.skriptlang.skript.lang.event.SkriptEvent;

public final class ExprRotate extends SimpleExpression<Object> {

    private Expression<?> values;
    private int pattern;
    private @Nullable Expression<Number> angle;
    private @Nullable Expression<Vec3> axisVector;
    private @Nullable Expression<Number> x;
    private @Nullable Expression<Number> y;
    private @Nullable Expression<Number> z;
    private char axis;
    private boolean local;

    @Override
    @SuppressWarnings("unchecked")
    public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
        values = expressions[0];
        pattern = matchedPattern;
        switch (matchedPattern) {
            case 0, 1, 2, 3, 4, 5 -> {
                axis = switch (matchedPattern % 3) {
                    case 0 -> 'x';
                    case 1 -> 'y';
                    default -> 'z';
                };
                local = matchedPattern >= 3;
                angle = (Expression<Number>) expressions[1];
            }
            case 6 -> {
                axisVector = (Expression<Vec3>) expressions[1];
                angle = (Expression<Number>) expressions[2];
            }
            case 7 -> {
                x = (Expression<Number>) expressions[1];
                y = (Expression<Number>) expressions[2];
                z = (Expression<Number>) expressions[3];
            }
        }
        return true;
    }

    @Override
    protected Object @Nullable [] get(SkriptEvent event) {
        List<Object> results = new ArrayList<>();
        if (pattern == 7) {
            Number xAngle = x.getSingle(event);
            Number yAngle = y.getSingle(event);
            Number zAngle = z.getSingle(event);
            if (xAngle == null || yAngle == null || zAngle == null) {
                return new Object[0];
            }
            float xRad = (float) Math.toRadians(xAngle.doubleValue());
            float yRad = (float) Math.toRadians(yAngle.doubleValue());
            float zRad = (float) Math.toRadians(zAngle.doubleValue());
            for (Object value : values.getAll(event)) {
                if (value instanceof Quaternionf quaternion) {
                    results.add(new Quaternionf(quaternion).rotateZYX(zRad, yRad, xRad));
                }
            }
            return results.toArray();
        }
        Number amount = angle != null ? angle.getSingle(event) : null;
        if (amount == null) {
            return new Object[0];
        }
        float radians = (float) Math.toRadians(amount.doubleValue());
        Vec3 arbitraryAxis = axisVector != null ? axisVector.getSingle(event) : null;
        if (axisVector != null && (arbitraryAxis == null || arbitraryAxis.lengthSqr() == 0.0D)) {
            return new Object[0];
        }
        Quaternionf rotation = axisVector != null
                ? new Quaternionf().rotateAxis(radians, (float) arbitraryAxis.x, (float) arbitraryAxis.y, (float) arbitraryAxis.z)
                : switch (axis) {
                    case 'x' -> local ? new Quaternionf().rotateX(radians) : new Quaternionf().rotateLocalX(radians);
                    case 'y' -> local ? new Quaternionf().rotateY(radians) : new Quaternionf().rotateLocalY(radians);
                    default -> local ? new Quaternionf().rotateZ(radians) : new Quaternionf().rotateLocalZ(radians);
                };
        for (Object value : values.getAll(event)) {
            if (value instanceof Quaternionf quaternion) {
                results.add(new Quaternionf(quaternion).mul(rotation));
            } else if (value instanceof Vec3 vec3) {
                Vector3f rotated = new Vector3f((float) vec3.x, (float) vec3.y, (float) vec3.z).rotate(rotation);
                results.add(new Vec3(rotated.x, rotated.y, rotated.z));
            }
        }
        return results.toArray();
    }

    @Override
    public boolean isSingle() {
        return values.isSingle();
    }

    @Override
    public Class<?> getReturnType() {
        return Object.class;
    }

    @Override
    public Class<?>[] possibleReturnTypes() {
        return new Class[]{Quaternionf.class, Vec3.class};
    }

    @Override
    public String toString(@Nullable SkriptEvent event, boolean debug) {
        return "rotated " + values.toString(event, debug);
    }
}
