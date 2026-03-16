package org.skriptlang.skript.fabric.runtime;

import net.minecraft.world.Container;

/**
 * Handle for inventory item move events.
 */
public record FabricInventoryMoveHandle(
        Container source,
        Container destination,
        Container initiator
) implements FabricInventoryMoveEventHandle {
}
