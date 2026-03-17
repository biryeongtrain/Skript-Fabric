package kim.biryeong.skriptFabric.mixin;

import ch.njol.skript.events.TeleportCause;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.portal.TeleportTransition;
import net.minecraft.world.phys.Vec3;
import org.skriptlang.skript.fabric.runtime.SkriptFabricEventBridge;
import org.skriptlang.skript.fabric.runtime.TeleportCauseCapture;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ServerPlayer.class)
abstract class ServerPlayerTeleportTransitionMixin {

    @Unique
    private Vec3 skript$transitionFromPosition;

    @Unique
    private ServerLevel skript$transitionFromLevel;

    @Inject(
            method = "teleport(Lnet/minecraft/world/level/portal/TeleportTransition;)Lnet/minecraft/server/level/ServerPlayer;",
            at = @At("HEAD")
    )
    private void skript$captureTransitionTeleport(TeleportTransition transition, CallbackInfoReturnable<ServerPlayer> cir) {
        ServerPlayer self = (ServerPlayer) (Object) this;
        skript$transitionFromLevel = self.level();
        skript$transitionFromPosition = self.position();
    }

    @Inject(
            method = "teleport(Lnet/minecraft/world/level/portal/TeleportTransition;)Lnet/minecraft/server/level/ServerPlayer;",
            at = @At("TAIL")
    )
    private void skript$dispatchTransitionTeleport(TeleportTransition transition, CallbackInfoReturnable<ServerPlayer> cir) {
        ServerPlayer self = (ServerPlayer) (Object) this;
        ServerLevel fromLevel = skript$transitionFromLevel;
        Vec3 fromPosition = skript$transitionFromPosition;
        skript$transitionFromLevel = null;
        skript$transitionFromPosition = null;
        if (fromLevel == null || fromPosition == null) {
            return;
        }
        Vec3 toPosition = self.position();
        if (self.level() != fromLevel) {
            SkriptFabricEventBridge.dispatchPlayerWorldChange(self, fromLevel);
        }
        if (fromPosition.equals(toPosition) && self.level() == fromLevel) {
            return;
        }
        TeleportCause cause = TeleportCauseCapture.consume();
        if (cause == null) {
            // No cause was set - this teleport was triggered by something we don't track
            // (e.g. handlePortal which dispatches its own portal event)
            return;
        }
        ServerLevel dispatchLevel = self.level();
        SkriptFabricEventBridge.dispatchTeleport(self, dispatchLevel, fromPosition, toPosition, cause);
    }
}
