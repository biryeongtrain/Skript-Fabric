package org.skriptlang.skript.fabric.runtime;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.projectile.FishingHook;
import org.jetbrains.annotations.Nullable;

public interface FabricFishingEventHandle extends FabricEntityEventHandle {

    ServerLevel level();

    ServerPlayer player();

    FishingHook hook();

    @Nullable Entity eventEntity();

    FabricFishingEventState state();

    boolean lureApplied();

    void setLureApplied(boolean lureApplied);

    @Override
    default Entity entity() {
        Entity eventEntity = eventEntity();
        return eventEntity != null ? eventEntity : hook();
    }
}
