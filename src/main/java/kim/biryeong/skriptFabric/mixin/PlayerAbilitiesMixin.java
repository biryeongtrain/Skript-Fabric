package kim.biryeong.skriptFabric.mixin;

import net.minecraft.network.protocol.game.ServerboundPlayerAbilitiesPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import org.skriptlang.skript.fabric.runtime.SkriptFabricEventBridge;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerGamePacketListenerImpl.class)
abstract class PlayerAbilitiesMixin {

    @Shadow public ServerPlayer player;

    @Inject(method = "handlePlayerAbilities", at = @At("HEAD"))
    private void skript$dispatchFlightToggle(ServerboundPlayerAbilitiesPacket packet, CallbackInfo ci) {
        boolean wasFlying = player.getAbilities().flying;
        boolean nowFlying = packet.isFlying();
        if (wasFlying != nowFlying) {
            SkriptFabricEventBridge.dispatchFlightToggle(player, nowFlying);
        }
    }
}
