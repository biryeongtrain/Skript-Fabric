package ch.njol.skript.expressions;

import java.util.Collections;
import java.util.Map;
import java.util.WeakHashMap;
import net.minecraft.world.phys.Vec3;

final class MinecartExpressionSupport {

    static final double DEFAULT_MAX_SPEED = 0.4D;
    static final Vec3 DEFAULT_DERAILED_VELOCITY = new Vec3(0.5D, 0.5D, 0.5D);
    static final Vec3 DEFAULT_FLYING_VELOCITY = new Vec3(0.95D, 0.95D, 0.95D);

    private static final Map<Object, Double> MAX_SPEEDS = Collections.synchronizedMap(new WeakHashMap<>());
    private static final Map<Object, Vec3> DERAILED_VELOCITIES = Collections.synchronizedMap(new WeakHashMap<>());
    private static final Map<Object, Vec3> FLYING_VELOCITIES = Collections.synchronizedMap(new WeakHashMap<>());

    private MinecartExpressionSupport() {
    }

    static double maxSpeed(Object minecart) {
        return MAX_SPEEDS.getOrDefault(minecart, DEFAULT_MAX_SPEED);
    }

    static void setMaxSpeed(Object minecart, double value) {
        MAX_SPEEDS.put(minecart, value);
    }

    static void resetMaxSpeed(Object minecart) {
        MAX_SPEEDS.remove(minecart);
    }

    static Vec3 velocity(Object minecart, boolean flying) {
        return (flying ? FLYING_VELOCITIES : DERAILED_VELOCITIES)
                .getOrDefault(minecart, flying ? DEFAULT_FLYING_VELOCITY : DEFAULT_DERAILED_VELOCITY);
    }

    static void setVelocity(Object minecart, boolean flying, Vec3 value) {
        (flying ? FLYING_VELOCITIES : DERAILED_VELOCITIES).put(minecart, value);
    }
}
