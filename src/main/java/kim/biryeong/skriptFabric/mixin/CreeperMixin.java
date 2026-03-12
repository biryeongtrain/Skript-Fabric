package kim.biryeong.skriptFabric.mixin;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.monster.Creeper;
import org.skriptlang.skript.fabric.runtime.SkriptFabricEventBridge;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Creeper.class)
abstract class CreeperMixin {

    @Shadow
    private int explosionRadius;

    @Shadow
    public abstract boolean isPowered();

    @Inject(
            method = "explodeCreeper",
            at = @At("HEAD")
    )
    private void skript$dispatchExplosionPrime(CallbackInfo callbackInfo) {
        Creeper creeper = (Creeper) (Object) this;
        if (!(creeper.level() instanceof ServerLevel serverLevel)) {
            return;
        }
        float multiplier = isPowered() ? 2.0F : 1.0F;
        float updatedRadius = SkriptFabricEventBridge.dispatchExplosionPrime(serverLevel, explosionRadius * multiplier, false);
        explosionRadius = Math.max(0, Math.round(updatedRadius / multiplier));
    }
}
