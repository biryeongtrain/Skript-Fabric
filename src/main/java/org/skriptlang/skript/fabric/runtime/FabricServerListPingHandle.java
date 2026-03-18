package org.skriptlang.skript.fabric.runtime;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import org.jetbrains.annotations.Nullable;

/**
 * Mutable handle for server list ping events, storing the player sample list,
 * protocol version, MOTD, favicon, and player visibility settings.
 */
public class FabricServerListPingHandle implements FabricServerListPingEventHandle {

    private final List<String> playerSample;
    private int protocolVersion;
    private @Nullable String motd;
    private byte @Nullable [] faviconBytes;
    private boolean hidePlayerInfo;
    private final Set<UUID> hiddenPlayers = new HashSet<>();

    public FabricServerListPingHandle(List<String> playerSample, int protocolVersion) {
        this.playerSample = playerSample;
        this.protocolVersion = protocolVersion;
    }

    @Override
    public List<String> playerSample() {
        return playerSample;
    }

    @Override
    public int protocolVersion() {
        return protocolVersion;
    }

    @Override
    public void setProtocolVersion(int version) {
        this.protocolVersion = version;
    }

    @Override
    public @Nullable String motd() {
        return motd;
    }

    @Override
    public void setMotd(@Nullable String motd) {
        this.motd = motd;
    }

    @Override
    public byte @Nullable [] faviconBytes() {
        return faviconBytes;
    }

    @Override
    public void setFaviconBytes(byte @Nullable [] bytes) {
        this.faviconBytes = bytes;
    }

    @Override
    public boolean hidePlayerInfo() {
        return hidePlayerInfo;
    }

    @Override
    public void setHidePlayerInfo(boolean hide) {
        this.hidePlayerInfo = hide;
    }

    @Override
    public Set<UUID> hiddenPlayers() {
        return hiddenPlayers;
    }

    @Override
    public void hidePlayer(UUID playerId) {
        hiddenPlayers.add(playerId);
    }
}
