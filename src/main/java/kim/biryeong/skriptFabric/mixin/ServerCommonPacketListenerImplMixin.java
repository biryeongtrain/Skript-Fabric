package kim.biryeong.skriptFabric.mixin;

import net.minecraft.network.protocol.common.ServerboundResourcePackPacket;
import net.minecraft.server.network.ServerCommonPacketListenerImpl;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import org.skriptlang.skript.fabric.runtime.FabricPlayerClientState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerCommonPacketListenerImpl.class)
abstract class ServerCommonPacketListenerImplMixin {

    @Inject(method = "handleResourcePackResponse", at = @At("HEAD"))
    private void skript$trackResourcePackResponse(ServerboundResourcePackPacket packet, CallbackInfo callbackInfo) {
        if ((Object) this instanceof ServerGamePacketListenerImpl listener) {
            FabricPlayerClientState.setResourcePackStatus(listener.player, packet.action());
        }
    }
}
