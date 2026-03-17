package kim.biryeong.skriptFabric.mixin;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.fabric.runtime.SkriptFabricEventBridge;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Projectile.class)
abstract class ProjectileHitMixin {

    @Inject(method = "onHit", at = @At("HEAD"))
    private void skript$dispatchProjectileHit(HitResult hitResult, CallbackInfo ci) {
        Projectile projectile = (Projectile) (Object) this;
        if (projectile.level() instanceof ServerLevel level) {
            @Nullable Entity hitEntity = hitResult instanceof EntityHitResult ehr ? ehr.getEntity() : null;
            SkriptFabricEventBridge.dispatchProjectileHit(level, projectile, hitEntity);
        }
    }
}
