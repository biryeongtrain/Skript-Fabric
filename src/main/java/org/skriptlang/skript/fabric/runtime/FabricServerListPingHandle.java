package org.skriptlang.skript.fabric.runtime;

import java.util.List;

/**
 * Mutable handle for server list ping events, storing the player sample list
 * and protocol version.
 */
public class FabricServerListPingHandle implements FabricServerListPingEventHandle {

    private final List<String> playerSample;
    private int protocolVersion;

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
}
