package org.skriptlang.skript.fabric.runtime;

import java.util.List;
import java.util.Set;
import java.util.UUID;
import org.jetbrains.annotations.Nullable;

/**
 * Marker interface for server list ping event handles.
 */
public interface FabricServerListPingEventHandle {

    List<String> playerSample();

    int protocolVersion();

    void setProtocolVersion(int version);

    @Nullable String motd();

    void setMotd(@Nullable String motd);

    byte @Nullable [] faviconBytes();

    void setFaviconBytes(byte @Nullable [] bytes);

    boolean hidePlayerInfo();

    void setHidePlayerInfo(boolean hide);

    Set<UUID> hiddenPlayers();

    void hidePlayer(UUID playerId);
}
