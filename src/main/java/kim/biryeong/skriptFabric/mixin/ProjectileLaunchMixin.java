package kim.biryeong.skriptFabric.mixin;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.projectile.Projectile;
import org.skriptlang.skript.fabric.runtime.SkriptFabricEventBridge;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ServerLevel.class)
abstract class ProjectileLaunchMixin {

    @Inject(method = "addFreshEntity", at = @At("HEAD"))
    private void skript$dispatchProjectileLaunch(Entity entity, CallbackInfoReturnable<Boolean> cir) {
        if (entity instanceof Projectile projectile) {
            ServerLevel level = (ServerLevel) (Object) this;
            SkriptFabricEventBridge.dispatchProjectileLaunch(level, projectile);
        }
    }
}
