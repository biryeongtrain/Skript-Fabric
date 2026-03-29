package kim.biryeong.skriptFabric.mixin;

import net.minecraft.network.protocol.game.ServerboundContainerClickPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.item.ItemStack;
import org.skriptlang.skript.fabric.runtime.SkriptFabricEventBridge;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerGamePacketListenerImpl.class)
abstract class ServerGamePacketListenerImplInventoryClickMixin {

    @Shadow public ServerPlayer player;

    @Inject(
            method = "handleContainerClick",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/inventory/AbstractContainerMenu;clicked(IILnet/minecraft/world/inventory/ContainerInput;Lnet/minecraft/world/entity/player/Player;)V"
            )
    )
    private void skript$dispatchInventoryClick(ServerboundContainerClickPacket packet, CallbackInfo callbackInfo) {
        if (packet.containerInput() != ContainerInput.PICKUP) {
            return;
        }
        int slotNum = packet.slotNum();
        if (slotNum < 0) {
            return;
        }
        ItemStack clicked = player.containerMenu.getSlot(slotNum).getItem().copy();
        if (clicked.isEmpty()) {
            return;
        }
        SkriptFabricEventBridge.dispatchInventoryClick(player, clicked);
    }
}
