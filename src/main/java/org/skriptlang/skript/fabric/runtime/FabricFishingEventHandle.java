package org.skriptlang.skript.fabric.runtime;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.projectile.FishingHook;

public interface FabricFishingEventHandle extends FabricEntityEventHandle {

    ServerLevel level();

    ServerPlayer player();

    FishingHook hook();

    boolean lureApplied();

    void setLureApplied(boolean lureApplied);

    @Override
    default Entity entity() {
        return hook();
    }
}
