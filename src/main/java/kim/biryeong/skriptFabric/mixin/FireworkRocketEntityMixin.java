package kim.biryeong.skriptFabric.mixin;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.projectile.FireworkRocketEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.skriptlang.skript.fabric.runtime.SkriptFabricEventBridge;

@Mixin(FireworkRocketEntity.class)
abstract class FireworkRocketEntityMixin {

    @Inject(
            method = "explode(Lnet/minecraft/server/level/ServerLevel;)V",
            at = @At("HEAD")
    )
    private void skript$dispatchFireworkExplode(ServerLevel serverLevel, CallbackInfo callbackInfo) {
        FireworkRocketEntity firework = (FireworkRocketEntity) (Object) this;
        SkriptFabricEventBridge.dispatchFirework(serverLevel, firework);
    }
}
