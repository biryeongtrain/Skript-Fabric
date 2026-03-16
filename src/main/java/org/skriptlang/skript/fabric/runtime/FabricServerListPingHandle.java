package org.skriptlang.skript.fabric.runtime;

import java.util.List;

/**
 * Mutable handle for server list ping events, storing the player sample list.
 */
public class FabricServerListPingHandle implements FabricServerListPingEventHandle {

    private final List<String> playerSample;

    public FabricServerListPingHandle(List<String> playerSample) {
        this.playerSample = playerSample;
    }

    @Override
    public List<String> playerSample() {
        return playerSample;
    }
}
