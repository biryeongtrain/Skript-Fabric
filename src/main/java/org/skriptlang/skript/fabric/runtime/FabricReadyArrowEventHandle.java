package org.skriptlang.skript.fabric.runtime;

import net.minecraft.world.item.ItemStack;

/**
 * Marker interface for ready arrow event handles.
 */
public interface FabricReadyArrowEventHandle {

    ItemStack bow();

    ItemStack arrow();
}
