package ch.njol.skript.expressions;

import net.minecraft.world.phys.Vec3;

final class Vec3ExpressionSupport {

    private static final double DEG_TO_RAD = Math.PI / 180.0D;
    private static final double RAD_TO_DEG = 180.0D / Math.PI;

    private Vec3ExpressionSupport() {
    }

    static Vec3 withAxis(Vec3 vector, int axis, double value) {
        return switch (axis) {
            case 0 -> new Vec3(value, vector.y, vector.z);
            case 1 -> new Vec3(vector.x, value, vector.z);
            case 2 -> new Vec3(vector.x, vector.y, value);
            default -> throw new IllegalArgumentException("Unknown axis " + axis);
        };
    }

    static Vec3 add(Vec3 first, Vec3 second) {
        return new Vec3(first.x + second.x, first.y + second.y, first.z + second.z);
    }

    static Vec3 subtract(Vec3 first, Vec3 second) {
        return new Vec3(first.x - second.x, first.y - second.y, first.z - second.z);
    }

    static Vec3 midpoint(Vec3 first, Vec3 second) {
        return new Vec3(
                (first.x + second.x) / 2.0D,
                (first.y + second.y) / 2.0D,
                (first.z + second.z) / 2.0D
        );
    }

    static double dot(Vec3 first, Vec3 second) {
        return first.x * second.x + first.y * second.y + first.z * second.z;
    }

    static Vec3 cross(Vec3 first, Vec3 second) {
        return first.cross(second);
    }

    static Vec3 normalize(Vec3 vector) {
        return vector.lengthSqr() == 0.0D ? Vec3.ZERO : vector.normalize();
    }

    static double length(Vec3 vector) {
        return vector.length();
    }

    static float skriptYaw(Vec3 vector) {
        return normalizeYaw(rawSkriptYaw(vector));
    }

    static float skriptPitch(Vec3 vector) {
        double horizontal = Math.sqrt(vector.x * vector.x + vector.z * vector.z);
        if (horizontal == 0.0D) {
            if (vector.y > 0.0D) {
                return -90.0F;
            }
            if (vector.y < 0.0D) {
                return 90.0F;
            }
            return 0.0F;
        }
        return (float) -Math.toDegrees(Math.atan2(vector.y, horizontal));
    }

    static Vec3 withSkriptYawPitch(Vec3 vector, float skriptYaw, float skriptPitch) {
        double length = vector.length();
        if (length == 0.0D) {
            return Vec3.ZERO;
        }
        float internalYaw = fromSkriptYaw(skriptYaw);
        float internalPitch = fromSkriptPitch(skriptPitch);
        return fromYawAndPitch(internalYaw, internalPitch).scale(length);
    }

    static float normalizeYaw(float yaw) {
        float normalized = yaw % 360.0F;
        return normalized < 0.0F ? normalized + 360.0F : normalized;
    }

    private static float rawSkriptYaw(Vec3 vector) {
        if (vector.x == 0.0D && vector.z == 0.0D) {
            return 0.0F;
        }
        float internalYaw = (float) (Math.atan2(vector.z, vector.x) * RAD_TO_DEG);
        return internalYaw < 90.0F ? internalYaw + 270.0F : internalYaw - 90.0F;
    }

    private static float fromSkriptYaw(float yaw) {
        return yaw > 270.0F ? yaw - 270.0F : yaw + 90.0F;
    }

    private static float fromSkriptPitch(float pitch) {
        return -pitch;
    }

    private static Vec3 fromYawAndPitch(float yaw, float pitch) {
        double y = Math.sin(pitch * DEG_TO_RAD);
        double horizontal = Math.cos(pitch * DEG_TO_RAD);
        double x = Math.cos(yaw * DEG_TO_RAD) * horizontal;
        double z = Math.sin(yaw * DEG_TO_RAD) * horizontal;
        return new Vec3(x, y, z);
    }
}
