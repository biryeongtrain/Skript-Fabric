package org.skriptlang.skript.fabric.compat;

import kim.biryeong.skriptFabric.mixin.BeaconBlockEntityAccessor;
import net.minecraft.core.Holder;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.level.block.entity.BeaconBlockEntity;
import org.jetbrains.annotations.Nullable;

public final class PrivateBeaconAccess {

    private PrivateBeaconAccess() {
    }

    public static int levels(BeaconBlockEntity beacon) {
        return accessor(beacon).skript$getLevels();
    }

    public static void setLevels(BeaconBlockEntity beacon, int levels) {
        accessor(beacon).skript$setLevels(levels);
    }

    public static @Nullable Holder<MobEffect> primaryPower(BeaconBlockEntity beacon) {
        return accessor(beacon).skript$getPrimaryPower();
    }

    public static void setPrimaryPower(BeaconBlockEntity beacon, @Nullable Holder<MobEffect> effect) {
        accessor(beacon).skript$setPrimaryPower(effect);
    }

    public static @Nullable Holder<MobEffect> secondaryPower(BeaconBlockEntity beacon) {
        return accessor(beacon).skript$getSecondaryPower();
    }

    public static void setSecondaryPower(BeaconBlockEntity beacon, @Nullable Holder<MobEffect> effect) {
        accessor(beacon).skript$setSecondaryPower(effect);
    }

    private static BeaconBlockEntityAccessor accessor(BeaconBlockEntity beacon) {
        return (BeaconBlockEntityAccessor) beacon;
    }
}
