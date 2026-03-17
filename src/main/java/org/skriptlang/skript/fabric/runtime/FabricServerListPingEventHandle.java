package org.skriptlang.skript.fabric.runtime;

import java.util.List;

/**
 * Marker interface for server list ping event handles.
 */
public interface FabricServerListPingEventHandle {

    List<String> playerSample();

    int protocolVersion();

    void setProtocolVersion(int version);
}
