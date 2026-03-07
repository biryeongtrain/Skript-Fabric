package org.skriptlang.skript.fabric.compat;

import java.util.Collections;
import java.util.Map;
import java.util.WeakHashMap;
import net.minecraft.world.entity.projectile.FishingHook;

public final class FabricFishingState {

    private static final int DEFAULT_MIN_WAIT_TIME = 5 * 20;
    private static final int DEFAULT_MAX_WAIT_TIME = 30 * 20;
    private static final float DEFAULT_MIN_LURE_ANGLE = 0.0F;
    private static final float DEFAULT_MAX_LURE_ANGLE = 360.0F;
    private static final Map<FishingHook, State> STATES = Collections.synchronizedMap(new WeakHashMap<>());

    private FabricFishingState() {
    }

    public static int minWaitTime(FishingHook hook) {
        return state(hook).minWaitTime;
    }

    public static void minWaitTime(FishingHook hook, int value) {
        state(hook).minWaitTime = Math.max(0, value);
    }

    public static int maxWaitTime(FishingHook hook) {
        return state(hook).maxWaitTime;
    }

    public static void maxWaitTime(FishingHook hook, int value) {
        state(hook).maxWaitTime = Math.max(0, value);
    }

    public static float minLureAngle(FishingHook hook) {
        return state(hook).minLureAngle;
    }

    public static void minLureAngle(FishingHook hook, float value) {
        state(hook).minLureAngle = clampAngle(value);
    }

    public static float maxLureAngle(FishingHook hook) {
        return state(hook).maxLureAngle;
    }

    public static void maxLureAngle(FishingHook hook, float value) {
        state(hook).maxLureAngle = clampAngle(value);
    }

    private static float clampAngle(float value) {
        return Math.max(0.0F, Math.min(360.0F, value));
    }

    private static State state(FishingHook hook) {
        return STATES.computeIfAbsent(hook, ignored -> new State());
    }

    private static final class State {
        private int minWaitTime = DEFAULT_MIN_WAIT_TIME;
        private int maxWaitTime = DEFAULT_MAX_WAIT_TIME;
        private float minLureAngle = DEFAULT_MIN_LURE_ANGLE;
        private float maxLureAngle = DEFAULT_MAX_LURE_ANGLE;
    }
}
