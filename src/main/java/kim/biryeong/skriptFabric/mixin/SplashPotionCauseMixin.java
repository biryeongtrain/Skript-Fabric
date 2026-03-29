package kim.biryeong.skriptFabric.mixin;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.projectile.throwableitemprojectile.ThrownSplashPotion;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.HitResult;
import org.skriptlang.skript.fabric.runtime.FabricPotionEffectCause;
import org.skriptlang.skript.fabric.runtime.FabricPotionEffectCauseContext;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ThrownSplashPotion.class)
abstract class SplashPotionCauseMixin {

    @Inject(method = "onHitAsPotion", at = @At("HEAD"))
    private void skript$pushSplashPotionCause(ServerLevel level, ItemStack stack, HitResult hitResult, CallbackInfo callbackInfo) {
        FabricPotionEffectCauseContext.push(FabricPotionEffectCause.POTION_SPLASH);
    }

    @Inject(method = "onHitAsPotion", at = @At("RETURN"))
    private void skript$popSplashPotionCause(ServerLevel level, ItemStack stack, HitResult hitResult, CallbackInfo callbackInfo) {
        FabricPotionEffectCauseContext.pop(FabricPotionEffectCause.POTION_SPLASH);
    }
}
