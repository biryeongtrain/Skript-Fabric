package org.skriptlang.skript.fabric.runtime;

import net.minecraft.world.entity.EntitySpawnReason;
import org.jetbrains.annotations.Nullable;

public final class SpawnReasonCapture {

    private static final ThreadLocal<@Nullable EntitySpawnReason> CAPTURE = new ThreadLocal<>();

    private SpawnReasonCapture() {
    }

    public static void set(EntitySpawnReason reason) {
        CAPTURE.set(reason);
    }

    public static @Nullable EntitySpawnReason consume() {
        EntitySpawnReason reason = CAPTURE.get();
        CAPTURE.remove();
        return reason;
    }
}
