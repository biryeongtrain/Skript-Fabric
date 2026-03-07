package org.skriptlang.skript.fabric.compat;

import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.world.entity.AgeableMob;

public final class FabricBreedingState {

    private static final Set<UUID> AGE_LOCKED = ConcurrentHashMap.newKeySet();
    private static final Set<UUID> UNBREEDABLE = ConcurrentHashMap.newKeySet();

    private FabricBreedingState() {
    }

    public static boolean canAge(AgeableMob entity) {
        return !isAgeLocked(entity);
    }

    public static boolean isAgeLocked(AgeableMob entity) {
        return AGE_LOCKED.contains(entity.getUUID());
    }

    public static void setAgeLocked(AgeableMob entity, boolean ageLocked) {
        if (ageLocked) {
            AGE_LOCKED.add(entity.getUUID());
        } else {
            AGE_LOCKED.remove(entity.getUUID());
        }
    }

    public static boolean canBreed(AgeableMob entity) {
        return !UNBREEDABLE.contains(entity.getUUID());
    }

    public static void setBreedable(AgeableMob entity, boolean breedable) {
        if (breedable) {
            UNBREEDABLE.remove(entity.getUUID());
        } else {
            UNBREEDABLE.add(entity.getUUID());
        }
    }
}
