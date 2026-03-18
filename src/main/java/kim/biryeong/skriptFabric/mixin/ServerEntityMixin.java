package kim.biryeong.skriptFabric.mixin;

import kim.biryeong.skriptFabric.EntityVisibilityManager;
import net.minecraft.server.level.ServerEntity;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.server.network.ServerPlayerConnection;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerEntity.class)
abstract class ServerEntityMixin {

    @Shadow
    @Final
    private Entity entity;

    @Inject(
            method = "addPairing",
            at = @At("HEAD"),
            cancellable = true
    )
    private void skript$checkEntityVisibility(ServerPlayerConnection connection, CallbackInfo ci) {
        if (connection instanceof ServerGamePacketListenerImpl listener) {
            ServerPlayer viewer = listener.player;
            if (EntityVisibilityManager.instance().isHidden(entity.getUUID(), viewer.getUUID())) {
                ci.cancel();
            }
        }
    }
}
