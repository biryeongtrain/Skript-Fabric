package org.skriptlang.skript.bukkit.misc.effects;

import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Kleenean;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.skriptlang.skript.fabric.compat.PrivateEntityAccess;
import org.skriptlang.skript.lang.event.SkriptEvent;
import com.mojang.math.Transformation;
import net.minecraft.world.entity.Display;

public final class EffRotate extends Effect {

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
            default -> {
                return false;
            }
        }
        return true;
    }

    @Override
    protected void execute(SkriptEvent event) {
        if (pattern == 3) {
            Number xAngle = x != null ? x.getSingle(event) : null;
            Number yAngle = y != null ? y.getSingle(event) : null;
            Number zAngle = z != null ? z.getSingle(event) : null;
            if (xAngle == null || yAngle == null || zAngle == null) {
                return;
            }
            Quaternionf rotation = new Quaternionf().rotateZYX(
                    (float) Math.toRadians(zAngle.doubleValue()),
                    (float) Math.toRadians(yAngle.doubleValue()),
                    (float) Math.toRadians(xAngle.doubleValue())
            );
            rotateValues(event, rotation);
            return;
        }

        Number amount = angle != null ? angle.getSingle(event) : null;
        if (amount == null) {
            return;
        }
        Quaternionf rotation;
        if (pattern == 2) {
            Vec3 axisValue = axisVector != null ? axisVector.getSingle(event) : null;
            if (axisValue == null || axisValue.lengthSqr() == 0.0D) {
                return;
            }
            rotation = new Quaternionf().rotateAxis(
                    (float) Math.toRadians(amount.doubleValue()),
                    (float) axisValue.x,
                    (float) axisValue.y,
                    (float) axisValue.z
            );
        } else {
            float radians = (float) Math.toRadians(amount.doubleValue());
            rotation = switch (axis) {
                case 'x' -> local ? new Quaternionf().rotateX(radians) : new Quaternionf().rotateLocalX(radians);
                case 'y' -> local ? new Quaternionf().rotateY(radians) : new Quaternionf().rotateLocalY(radians);
                default -> local ? new Quaternionf().rotateZ(radians) : new Quaternionf().rotateLocalZ(radians);
            };
        }
        rotateValues(event, rotation);
    }

    private void rotateValues(SkriptEvent event, Quaternionf rotation) {
        for (Object value : values.getAll(event)) {
            if (value instanceof Quaternionf quaternion) {
                quaternion.mul(rotation);
            } else if (value instanceof Display display) {
                Transformation transformation = PrivateEntityAccess.displayTransformation(display);
                Quaternionf next = new Quaternionf(transformation.getLeftRotation()).mul(rotation);
                PrivateEntityAccess.setDisplayTransformation(display, new Transformation(
                        transformation.getTranslation(),
                        next,
                        transformation.getScale(),
                        transformation.getRightRotation()
                ));
            }
        }
        if (values.acceptChange(ChangeMode.SET) != null) {
            values.changeInPlace(event, value -> {
                if (value instanceof Vec3 vec3) {
                    Vector3f rotated = new Vector3f((float) vec3.x, (float) vec3.y, (float) vec3.z).rotate(rotation);
                    return new Vec3(rotated.x, rotated.y, rotated.z);
                }
                if (value instanceof Quaternionf quaternion) {
                    return quaternion;
                }
                return value;
            }, true);
        }
    }

    @Override
    public String toString(@Nullable SkriptEvent event, boolean debug) {
        return "rotate " + values.toString(event, debug);
    }
}
