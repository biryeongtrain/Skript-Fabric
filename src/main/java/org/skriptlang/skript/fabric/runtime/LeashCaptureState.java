package org.skriptlang.skript.fabric.runtime;

import net.minecraft.world.entity.Entity;

public final class LeashCaptureState {

    private static final ThreadLocal<Entity> PREVIOUS_LEASH_HOLDER = new ThreadLocal<>();

    private LeashCaptureState() {
    }

    public static ThreadLocal<Entity> previousLeashHolder() {
        return PREVIOUS_LEASH_HOLDER;
    }
}
