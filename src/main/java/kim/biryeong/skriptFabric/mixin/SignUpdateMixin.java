package kim.biryeong.skriptFabric.mixin;

import net.minecraft.network.protocol.game.ServerboundSignUpdatePacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import org.skriptlang.skript.fabric.runtime.SkriptFabricEventBridge;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerGamePacketListenerImpl.class)
abstract class SignUpdateMixin {

    @Shadow public ServerPlayer player;

    @Inject(method = "handleSignUpdate", at = @At("HEAD"))
    private void skript$dispatchSignChange(ServerboundSignUpdatePacket packet, CallbackInfo ci) {
        if (player.level() instanceof ServerLevel level) {
            SkriptFabricEventBridge.dispatchSignChange(level, player, packet.getPos(), packet.getLines(), packet.isFrontText());
        }
    }
}
