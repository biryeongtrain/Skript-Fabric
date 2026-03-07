package org.skriptlang.skript.bukkit.misc.expressions;

import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.util.Kleenean;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import org.joml.AxisAngle4f;
import org.joml.Quaternionf;
import org.skriptlang.skript.lang.event.SkriptEvent;

public final class ExprQuaternionAxisAngle extends SimpleExpression<Object> {

    private Expression<Quaternionf> quaternions;
    private boolean axis;

    @Override
    @SuppressWarnings("unchecked")
    public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
        if (expressions.length != 1 || !expressions[0].canReturn(Quaternionf.class)) {
            return false;
        }
        quaternions = (Expression<Quaternionf>) expressions[0];
        axis = matchedPattern == 1;
        return true;
    }

    @Override
    protected Object @Nullable [] get(SkriptEvent event) {
        ListBuilder builder = new ListBuilder();
        for (Quaternionf quaternion : quaternions.getAll(event)) {
            AxisAngle4f axisAngle = new AxisAngle4f().set(quaternion);
            builder.add(axis ? new Vec3(axisAngle.x, axisAngle.y, axisAngle.z) : (float) Math.toDegrees(axisAngle.angle));
        }
        return builder.toArray();
    }

    @Override
    public boolean isSingle() {
        return quaternions.isSingle();
    }

    @Override
    public Class<?> getReturnType() {
        return axis ? Vec3.class : Float.class;
    }

    @Override
    public Class<?>[] possibleReturnTypes() {
        return new Class[]{Vec3.class, Float.class};
    }

    @Override
    public Class<?> @Nullable [] acceptChange(ChangeMode mode) {
        return switch (mode) {
            case SET, ADD, REMOVE -> axis ? new Class[]{Vec3.class, String.class} : new Class[]{Number.class, String.class};
            default -> null;
        };
    }

    @Override
    public void change(SkriptEvent event, Object @Nullable [] delta, ChangeMode mode) {
        if (delta == null || delta.length == 0 || delta[0] == null) {
            return;
        }
        Quaternionf[] updated = new Quaternionf[quaternions.getAll(event).length];
        int index = 0;
        for (Quaternionf quaternion : quaternions.getAll(event)) {
            AxisAngle4f axisAngle = new AxisAngle4f().set(quaternion);
            if (axis) {
                Vec3 nextAxis = parseVec3(delta[0]);
                if (nextAxis == null) {
                    continue;
                }
                axisAngle.x = (float) nextAxis.x;
                axisAngle.y = (float) nextAxis.y;
                axisAngle.z = (float) nextAxis.z;
            } else {
                Float nextAngle = parseAngle(delta[0]);
                if (nextAngle == null) {
                    continue;
                }
                float radians = (float) Math.toRadians(nextAngle);
                axisAngle.angle = switch (mode) {
                    case SET -> radians;
                    case ADD -> axisAngle.angle + radians;
                    case REMOVE -> axisAngle.angle - radians;
                    default -> axisAngle.angle;
                };
            }
            updated[index++] = new Quaternionf().set(axisAngle);
        }
        if (index > 0) {
            quaternions.change(event, java.util.Arrays.copyOf(updated, index), ChangeMode.SET);
        }
    }

    private @Nullable Float parseAngle(Object value) {
        if (value instanceof Number number) {
            return number.floatValue();
        }
        try {
            return Float.parseFloat(String.valueOf(value));
        } catch (NumberFormatException ignored) {
            return null;
        }
    }

    private @Nullable Vec3 parseVec3(Object value) {
        if (value instanceof Vec3 vec3) {
            return vec3;
        }
        return ch.njol.skript.registrations.Classes.parse(String.valueOf(value), Vec3.class, ch.njol.skript.lang.ParseContext.DEFAULT);
    }

    @Override
    public String toString(@Nullable SkriptEvent event, boolean debug) {
        return (axis ? "rotation axis of " : "rotation angle of ") + quaternions.toString(event, debug);
    }

    private static final class ListBuilder {
        private final java.util.List<Object> values = new java.util.ArrayList<>();
        void add(Object value) { values.add(value); }
        Object[] toArray() { return values.toArray(); }
    }
}
