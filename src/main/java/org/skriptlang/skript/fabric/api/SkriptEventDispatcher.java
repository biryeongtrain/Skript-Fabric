package org.skriptlang.skript.fabric.api;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.fabric.runtime.SkriptRuntime;
import org.skriptlang.skript.lang.event.SkriptEvent;

/**
 * Utility for dispatching custom events to the Skript runtime.
 *
 * <p>Reduces the boilerplate of creating a {@link SkriptEvent}, dispatching it,
 * and checking its cancellation state.</p>
 *
 * <h3>Example:</h3>
 * <pre>{@code
 * // In a Fabric event callback or Mixin:
 * boolean allowed = SkriptEventDispatcher.dispatch(
 *     new MyCustomHandle(data),
 *     server, level, player
 * );
 * if (!allowed) {
 *     // Event was cancelled by a script
 *     return;
 * }
 * }</pre>
 */
public final class SkriptEventDispatcher {

    private SkriptEventDispatcher() {}

    /**
     * Dispatches a custom event handle to all loaded scripts and returns
     * whether the event was allowed (not cancelled).
     *
     * @param handle the event handle object (matched via {@code instanceof} in event handlers)
     * @param server the current server instance
     * @param level  the server level where the event occurred, or null
     * @param player the player involved, or null
     * @return {@code true} if the event was NOT cancelled, {@code false} if cancelled
     */
    public static boolean dispatch(
            Object handle,
            MinecraftServer server,
            @Nullable ServerLevel level,
            @Nullable ServerPlayer player
    ) {
        SkriptEvent event = new SkriptEvent(handle, server, level, player);
        SkriptRuntime.instance().dispatch(event);
        return !event.isCancelled();
    }

    /**
     * Dispatches a custom event that cannot be cancelled (fire-and-forget).
     *
     * @param handle the event handle object
     * @param server the current server instance
     * @param level  the server level where the event occurred, or null
     * @param player the player involved, or null
     */
    public static void dispatchUncancellable(
            Object handle,
            MinecraftServer server,
            @Nullable ServerLevel level,
            @Nullable ServerPlayer player
    ) {
        SkriptRuntime.instance().dispatch(new SkriptEvent(handle, server, level, player));
    }
}
