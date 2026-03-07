package org.skriptlang.skript.fabric.runtime;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.projectile.FishingHook;

public final class FabricFishingHandle implements FabricFishingEventHandle {

    private final ServerLevel level;
    private final ServerPlayer player;
    private final FishingHook hook;
    private boolean lureApplied;

    public FabricFishingHandle(
            ServerLevel level,
            ServerPlayer player,
            FishingHook hook,
            boolean lureApplied
    ) {
        this.level = level;
        this.player = player;
        this.hook = hook;
        this.lureApplied = lureApplied;
    }

    @Override
    public ServerLevel level() {
        return level;
    }

    @Override
    public ServerPlayer player() {
        return player;
    }

    @Override
    public FishingHook hook() {
        return hook;
    }

    @Override
    public boolean lureApplied() {
        return lureApplied;
    }

    @Override
    public void setLureApplied(boolean lureApplied) {
        this.lureApplied = lureApplied;
    }
}
