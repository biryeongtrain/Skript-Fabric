package kim.biryeong.skriptFabric.mixin;

import ch.njol.skript.events.FabricPlayerEventHandles;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import org.skriptlang.skript.fabric.runtime.SkriptFabricEventBridge;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerPlayer.class)
abstract class ServerPlayerSpectateMixin {

    @Unique
    private Entity skript$previousCamera;

    @Inject(method = "setCameraEntity", at = @At("HEAD"))
    private void skript$captureCamera(Entity camera, CallbackInfo callbackInfo) {
        ServerPlayer self = (ServerPlayer) (Object) this;
        skript$previousCamera = self.getCamera();
    }

    @Inject(method = "setCameraEntity", at = @At("TAIL"))
    private void skript$dispatchSpectate(Entity camera, CallbackInfo callbackInfo) {
        ServerPlayer self = (ServerPlayer) (Object) this;
        Entity previousCamera = skript$previousCamera;
        Entity currentCamera = self.getCamera();
        skript$previousCamera = null;
        if (previousCamera == null || currentCamera == null || previousCamera == currentCamera) {
            return;
        }

        FabricPlayerEventHandles.SpectateAction action;
        Entity currentTarget = previousCamera == self ? null : previousCamera;
        Entity newTarget = currentCamera == self ? null : currentCamera;

        if (currentTarget == null && newTarget != null) {
            action = FabricPlayerEventHandles.SpectateAction.START;
        } else if (currentTarget != null && newTarget == null) {
            action = FabricPlayerEventHandles.SpectateAction.STOP;
        } else if (currentTarget != null) {
            action = FabricPlayerEventHandles.SpectateAction.SWAP;
        } else {
            return;
        }

        SkriptFabricEventBridge.dispatchSpectate(self, action, currentTarget, newTarget);
    }
}
