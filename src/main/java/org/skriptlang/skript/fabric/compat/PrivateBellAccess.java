package org.skriptlang.skript.fabric.compat;

import kim.biryeong.skriptFabric.mixin.BellBlockEntityAccessor;
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

    public static boolean isResonating(BellBlockEntity bell) {
        return accessor(bell).skript$isResonating();
    }

    public static void setResonating(BellBlockEntity bell, boolean resonating) {
        accessor(bell).skript$setResonating(resonating);
    }

    private static BellBlockEntityAccessor accessor(BellBlockEntity bell) {
        return (BellBlockEntityAccessor) bell;
    }
}
