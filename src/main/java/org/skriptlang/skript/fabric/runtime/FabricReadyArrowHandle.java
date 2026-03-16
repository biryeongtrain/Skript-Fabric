package org.skriptlang.skript.fabric.runtime;

import net.minecraft.world.item.ItemStack;

/**
 * Handle for ready arrow events.
 */
public record FabricReadyArrowHandle(
        ItemStack bow,
        ItemStack arrow
) implements FabricReadyArrowEventHandle {
}
