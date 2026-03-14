package kim.biryeong.skriptFabric.mixin;

import ch.njol.skript.events.FabricEventCompatHandles;
import net.minecraft.network.protocol.common.ServerboundResourcePackPacket;
import net.minecraft.server.network.ServerCommonPacketListenerImpl;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import org.skriptlang.skript.fabric.runtime.FabricPlayerClientState;
import org.skriptlang.skript.fabric.runtime.SkriptFabricEventBridge;
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
            FabricEventCompatHandles.ResourcePackState state = switch (packet.action()) {
                case ACCEPTED -> FabricEventCompatHandles.ResourcePackState.ACCEPTED;
                case DECLINED -> FabricEventCompatHandles.ResourcePackState.DECLINED;
                case FAILED_DOWNLOAD -> FabricEventCompatHandles.ResourcePackState.FAILED_DOWNLOAD;
                case SUCCESSFULLY_LOADED -> FabricEventCompatHandles.ResourcePackState.SUCCESSFULLY_LOADED;
                case DOWNLOADED -> FabricEventCompatHandles.ResourcePackState.DOWNLOADED;
                case INVALID_URL -> FabricEventCompatHandles.ResourcePackState.INVALID_URL;
                case FAILED_RELOAD -> FabricEventCompatHandles.ResourcePackState.FAILED_RELOAD;
                case DISCARDED -> FabricEventCompatHandles.ResourcePackState.DISCARDED;
            };
            SkriptFabricEventBridge.dispatchResourcePackResponse(listener.player, state);
        }
    }
}
