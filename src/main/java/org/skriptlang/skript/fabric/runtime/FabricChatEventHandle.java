package org.skriptlang.skript.fabric.runtime;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

import java.util.Set;

/**
 * Marker interface for chat event handles.
 */
public interface FabricChatEventHandle {

    ServerPlayer player();

    Component message();

    String format();

    void setFormat(String format);

    Set<ServerPlayer> recipients();
}
