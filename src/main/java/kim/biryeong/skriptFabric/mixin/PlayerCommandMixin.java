package kim.biryeong.skriptFabric.mixin;

import net.minecraft.network.protocol.game.ServerboundPlayerCommandPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import org.skriptlang.skript.fabric.runtime.SkriptFabricEventBridge;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerGamePacketListenerImpl.class)
abstract class PlayerCommandMixin {

    @Shadow public ServerPlayer player;

    @Inject(method = "handlePlayerCommand", at = @At("HEAD"))
    private void skript$dispatchPlayerCommand(ServerboundPlayerCommandPacket packet, CallbackInfo ci) {
        if (packet.getAction() == ServerboundPlayerCommandPacket.Action.START_FALL_FLYING) {
            SkriptFabricEventBridge.dispatchGlideToggle(player);
        }
    }
}
