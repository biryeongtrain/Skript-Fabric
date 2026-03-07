package org.skriptlang.skript.fabric.runtime;

import net.minecraft.world.item.ItemStack;

public interface FabricItemEventHandle {

    ItemStack itemStack();

    default ItemStack itemStack(int time) {
        return time == 0 ? itemStack() : ItemStack.EMPTY;
    }
}
