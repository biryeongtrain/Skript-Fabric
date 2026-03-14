package ch.njol.skript.events;

import net.minecraft.world.entity.EntitySpawnReason;
import org.jetbrains.annotations.Nullable;

public enum SpawnReason {
    NATURAL,
    CHUNK_GENERATION,
    SPAWNER,
    STRUCTURE,
    BREEDING,
    MOB_SUMMONED,
    JOCKEY,
    EVENT,
    CONVERSION,
    REINFORCEMENT,
    TRIGGERED,
    BUCKET,
    SPAWN_ITEM_USE,
    COMMAND,
    DISPENSER,
    PATROL,
    TRIAL_SPAWNER,
    LOAD,
    DIMENSION_TRAVEL,
    UNKNOWN;

    public static SpawnReason fromMinecraft(@Nullable EntitySpawnReason mc) {
        if (mc == null) {
            return UNKNOWN;
        }
        try {
            return valueOf(mc.name());
        } catch (IllegalArgumentException e) {
            return UNKNOWN;
        }
    }
}
