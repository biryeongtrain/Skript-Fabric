package org.skriptlang.skript.fabric.runtime;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

import java.util.Set;

/**
 * Mutable handle for chat events, storing format, player, message, and recipients.
 */
public class FabricChatHandle implements FabricChatEventHandle {

    private final ServerPlayer player;
    private final Component message;
    private String format;
    private final Set<ServerPlayer> recipients;
    private boolean cancelled;

    public FabricChatHandle(ServerPlayer player, Component message, String format, Set<ServerPlayer> recipients) {
        this.player = player;
        this.message = message;
        this.format = format;
        this.recipients = recipients;
    }

    public boolean isCancelled() {
        return cancelled;
    }

    public void setCancelled(boolean cancelled) {
        this.cancelled = cancelled;
    }

    @Override
    public ServerPlayer player() {
        return player;
    }

    @Override
    public Component message() {
        return message;
    }

    @Override
    public String format() {
        return format;
    }

    @Override
    public void setFormat(String format) {
        this.format = format;
    }

    @Override
    public Set<ServerPlayer> recipients() {
        return recipients;
    }
}
