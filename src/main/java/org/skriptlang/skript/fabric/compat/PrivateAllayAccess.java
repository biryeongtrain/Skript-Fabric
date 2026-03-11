package org.skriptlang.skript.fabric.compat;

import kim.biryeong.skriptFabric.mixin.AllayAccessor;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.animal.allay.Allay;
import org.jetbrains.annotations.Nullable;

public final class PrivateAllayAccess {

    private PrivateAllayAccess() {
    }

    public static boolean canDuplicate(Allay allay) {
        return accessor(allay).skript$invokeCanDuplicate();
    }

    public static void setCanDuplicate(Allay allay, boolean canDuplicate) {
        allay.getEntityData().set(AllayAccessor.skript$getCanDuplicateTrackedData(), canDuplicate);
    }

    public static long duplicationCooldown(Allay allay) {
        return accessor(allay).skript$getDuplicationCooldown();
    }

    public static void setDuplicationCooldown(Allay allay, long duplicationCooldown) {
        accessor(allay).skript$setDuplicationCooldown(duplicationCooldown);
    }

    public static @Nullable BlockPos jukeboxPos(Allay allay) {
        return accessor(allay).skript$getJukeboxPos();
    }

    public static void setJukeboxPos(Allay allay, @Nullable BlockPos jukeboxPos) {
        accessor(allay).skript$setJukeboxPos(jukeboxPos);
    }

    private static AllayAccessor accessor(Allay allay) {
        return (AllayAccessor) allay;
    }
}
