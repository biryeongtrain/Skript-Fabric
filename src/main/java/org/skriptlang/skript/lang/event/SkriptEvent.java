package org.skriptlang.skript.lang.event;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.Nullable;

/**
 * Minimal event context wrapper used by the language layer.
 * This avoids direct Bukkit API coupling while keeping call-sites event-aware.
 */
public record SkriptEvent(
        @Nullable Object handle,
        @Nullable MinecraftServer server,
        @Nullable ServerLevel level,
        @Nullable ServerPlayer player
) {

    public static final SkriptEvent EMPTY = new SkriptEvent(null, null, null, null);
}
