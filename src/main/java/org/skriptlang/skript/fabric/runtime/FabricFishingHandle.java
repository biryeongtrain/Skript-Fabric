package org.skriptlang.skript.fabric.runtime;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.projectile.FishingHook;
import org.jetbrains.annotations.Nullable;

public final class FabricFishingHandle implements FabricFishingEventHandle {

    private final ServerLevel level;
    private final ServerPlayer player;
    private final FishingHook hook;
    private final @Nullable Entity eventEntity;
    private final FabricFishingEventState state;
    private boolean lureApplied;

    public FabricFishingHandle(
            ServerLevel level,
            ServerPlayer player,
            FishingHook hook,
            boolean lureApplied
    ) {
        this(level, player, hook, null, lureApplied, FabricFishingEventState.FISHING);
    }

    public FabricFishingHandle(
            ServerLevel level,
            ServerPlayer player,
            FishingHook hook,
            @Nullable Entity eventEntity,
            boolean lureApplied,
            FabricFishingEventState state
    ) {
        this.level = level;
        this.player = player;
        this.hook = hook;
        this.eventEntity = eventEntity;
        this.lureApplied = lureApplied;
        this.state = state;
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
    public @Nullable Entity eventEntity() {
        return eventEntity;
    }

    @Override
    public FabricFishingEventState state() {
        return state;
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
