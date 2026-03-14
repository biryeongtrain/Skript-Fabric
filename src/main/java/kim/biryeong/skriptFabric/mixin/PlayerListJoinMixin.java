package kim.biryeong.skriptFabric.mixin;

import net.minecraft.network.Connection;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.CommonListenerCookie;
import net.minecraft.server.players.PlayerList;
import org.skriptlang.skript.fabric.runtime.SkriptFabricEventBridge;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerList.class)
abstract class PlayerListJoinMixin {

    @Inject(method = "placeNewPlayer", at = @At("HEAD"))
    private void skript$dispatchConnect(Connection connection, ServerPlayer player, CommonListenerCookie cookie, CallbackInfo ci) {
        SkriptFabricEventBridge.dispatchConnect(player);
    }

    @Inject(method = "placeNewPlayer", at = @At("RETURN"))
    private void skript$dispatchJoin(Connection connection, ServerPlayer player, CommonListenerCookie cookie, CallbackInfo ci) {
        SkriptFabricEventBridge.dispatchJoin(player);
    }
}
