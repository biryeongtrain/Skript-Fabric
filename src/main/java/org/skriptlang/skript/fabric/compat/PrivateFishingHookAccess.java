package org.skriptlang.skript.fabric.compat;

import kim.biryeong.skriptFabric.mixin.FishingHookAccessor;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.projectile.FishingHook;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import org.jetbrains.annotations.Nullable;

public final class PrivateFishingHookAccess {

    private PrivateFishingHookAccess() {
    }

    public static int timeUntilHooked(FishingHook hook) {
        return accessor(hook).skript$getTimeUntilHooked();
    }

    public static void setTimeUntilHooked(FishingHook hook, int value) {
        accessor(hook).skript$setTimeUntilHooked(value);
    }

    public static int timeUntilLured(FishingHook hook) {
        return accessor(hook).skript$getTimeUntilLured();
    }

    public static void setTimeUntilLured(FishingHook hook, int value) {
        accessor(hook).skript$setTimeUntilLured(value);
    }

    public static int nibble(FishingHook hook) {
        return accessor(hook).skript$getNibble();
    }

    public static void setNibble(FishingHook hook, int value) {
        accessor(hook).skript$setNibble(value);
    }

    public static boolean biting(FishingHook hook) {
        return accessor(hook).skript$isBiting();
    }

    public static void setBiting(FishingHook hook, boolean value) {
        accessor(hook).skript$setBiting(value);
    }

    public static void setCurrentState(FishingHook hook, String stateName) {
        accessor(hook).skript$setCurrentState(FishingHook.FishHookState.valueOf(stateName));
    }

    public static boolean lureApplied(FishingHook hook) {
        return accessor(hook).skript$getLureSpeed() > 0;
    }

    public static void pullEntity(FishingHook hook, Entity entity) {
        accessor(hook).skript$invokePullEntity(entity);
    }

    public static void onHitEntity(FishingHook hook, Entity entity) {
        accessor(hook).skript$invokeOnHitEntity(new EntityHitResult(entity));
    }

    public static void onHitBlock(FishingHook hook, BlockHitResult hitResult) {
        accessor(hook).skript$invokeOnHitBlock(hitResult);
    }

    public static void catchingFish(FishingHook hook, BlockPos position) {
        accessor(hook).skript$invokeCatchingFish(position);
    }

    public static @Nullable Entity hookedIn(FishingHook hook) {
        return accessor(hook).skript$getHookedIn();
    }

    public static void setHookedIn(FishingHook hook, @Nullable Entity entity) {
        accessor(hook).skript$setHookedIn(entity);
    }

    private static FishingHookAccessor accessor(FishingHook hook) {
        return (FishingHookAccessor) hook;
    }
}
