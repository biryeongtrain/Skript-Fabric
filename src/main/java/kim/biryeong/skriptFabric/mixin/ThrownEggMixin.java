package kim.biryeong.skriptFabric.mixin;

import net.minecraft.world.entity.projectile.ThrownEgg;
import net.minecraft.world.phys.HitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.skriptlang.skript.fabric.runtime.SkriptFabricEventBridge;

@Mixin(ThrownEgg.class)
abstract class ThrownEggMixin {

    @Inject(
            method = "onHit(Lnet/minecraft/world/phys/HitResult;)V",
            at = @At("HEAD"),
            cancellable = true
    )
    private void skript$dispatchPlayerEggThrow(HitResult hitResult, CallbackInfo callbackInfo) {
        if (SkriptFabricEventBridge.dispatchPlayerEggThrow((ThrownEgg) (Object) this, hitResult)) {
            callbackInfo.cancel();
        }
    }
}
