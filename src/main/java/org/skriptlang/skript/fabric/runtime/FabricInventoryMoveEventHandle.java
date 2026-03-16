package org.skriptlang.skript.fabric.runtime;

import net.minecraft.world.Container;

/**
 * Marker interface for inventory item move event handles.
 */
public interface FabricInventoryMoveEventHandle {

    Container source();

    Container destination();

    Container initiator();
}
