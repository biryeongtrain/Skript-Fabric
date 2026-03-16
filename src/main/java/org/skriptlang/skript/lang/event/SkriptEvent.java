package org.skriptlang.skript.lang.event;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.Nullable;

/**
 * Minimal event context wrapper used by the language layer.
 * This avoids direct Bukkit API coupling while keeping call-sites event-aware.
 */
public final class SkriptEvent {

    public static final SkriptEvent EMPTY = new SkriptEvent(null, null, null, null);

    private final @Nullable Object handle;
    private final @Nullable MinecraftServer server;
    private final @Nullable ServerLevel level;
    private final @Nullable ServerPlayer player;
    private boolean cancelled;

    public SkriptEvent(
            @Nullable Object handle,
            @Nullable MinecraftServer server,
            @Nullable ServerLevel level,
            @Nullable ServerPlayer player
    ) {
        this.handle = handle;
        this.server = server;
        this.level = level;
        this.player = player;
    }

    public @Nullable Object handle() {
        return handle;
    }

    public @Nullable MinecraftServer server() {
        return server;
    }

    public @Nullable ServerLevel level() {
        return level;
    }

    public @Nullable ServerPlayer player() {
        return player;
    }

    public boolean isCancelled() {
        return cancelled;
    }

    public void setCancelled(boolean cancelled) {
        this.cancelled = cancelled;
    }
}
