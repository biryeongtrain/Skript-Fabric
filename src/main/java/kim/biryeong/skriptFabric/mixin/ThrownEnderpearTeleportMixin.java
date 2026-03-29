package kim.biryeong.skriptFabric.mixin;

import ch.njol.skript.events.TeleportCause;
import net.minecraft.world.entity.projectile.throwableitemprojectile.ThrownEnderpearl;
import net.minecraft.world.phys.HitResult;
import org.skriptlang.skript.fabric.runtime.TeleportCauseCapture;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ThrownEnderpearl.class)
abstract class ThrownEnderpearTeleportMixin {

    @Inject(method = "onHit", at = @At("HEAD"))
    private void skript$setEnderPearlCause(HitResult hitResult, CallbackInfo ci) {
        TeleportCauseCapture.set(TeleportCause.ENDER_PEARL);
    }

    @Inject(method = "onHit", at = @At("TAIL"))
    private void skript$clearEnderPearlCause(HitResult hitResult, CallbackInfo ci) {
        // Clean up in case the teleport didn't happen (e.g. pearl was discarded)
        TeleportCauseCapture.consume();
    }
}
