package org.skriptlang.skript.fabric.compat;

import kim.biryeong.skriptFabric.mixin.BellBlockEntityAccessor;
import java.util.List;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.block.entity.BellBlockEntity;

public final class PrivateBellAccess {

    private PrivateBellAccess() {
    }

    public static boolean isRinging(BellBlockEntity bell) {
        return bell.shaking;
    }

    public static void setRinging(BellBlockEntity bell, boolean ringing) {
        bell.shaking = ringing;
    }

    public static int ringingTicks(BellBlockEntity bell) {
        return accessor(bell).skript$getTicks();
    }

    public static void setRingingTicks(BellBlockEntity bell, int ticks) {
        accessor(bell).skript$setTicks(ticks);
    }

    public static boolean isResonating(BellBlockEntity bell) {
        return accessor(bell).skript$isResonating();
    }

    public static void setResonating(BellBlockEntity bell, boolean resonating) {
        accessor(bell).skript$setResonating(resonating);
    }

    public static int resonatingTicks(BellBlockEntity bell) {
        return accessor(bell).skript$getResonationTicks();
    }

    public static void setResonatingTicks(BellBlockEntity bell, int ticks) {
        accessor(bell).skript$setResonationTicks(ticks);
    }

    public static @SuppressWarnings("unused") List<LivingEntity> nearbyEntities(BellBlockEntity bell) {
        return accessor(bell).skript$getNearbyEntities();
    }

    public static void setNearbyEntities(BellBlockEntity bell, List<LivingEntity> entities) {
        accessor(bell).skript$setNearbyEntities(entities);
    }

    private static BellBlockEntityAccessor accessor(BellBlockEntity bell) {
        return (BellBlockEntityAccessor) bell;
    }
}
