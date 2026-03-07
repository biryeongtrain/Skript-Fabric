package org.skriptlang.skript.fabric.runtime;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;

public record FabricUseItemHandle(
        ServerLevel level,
        ServerPlayer player,
        InteractionHand hand
) implements FabricItemEventHandle {

    @Override
    public ItemStack itemStack() {
        return player.getItemInHand(hand);
    }
}
