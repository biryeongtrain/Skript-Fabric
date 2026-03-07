package kim.biryeong.skriptFabric.mixin;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.Arrow;
import org.skriptlang.skript.fabric.runtime.FabricPotionEffectCause;
import org.skriptlang.skript.fabric.runtime.FabricPotionEffectCauseContext;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Arrow.class)
abstract class ArrowPotionCauseMixin {

    @Inject(method = "doPostHurtEffects", at = @At("HEAD"))
    private void skript$pushArrowPotionCause(LivingEntity target, CallbackInfo callbackInfo) {
        FabricPotionEffectCauseContext.push(FabricPotionEffectCause.ARROW);
    }

    @Inject(method = "doPostHurtEffects", at = @At("RETURN"))
    private void skript$popArrowPotionCause(LivingEntity target, CallbackInfo callbackInfo) {
        FabricPotionEffectCauseContext.pop(FabricPotionEffectCause.ARROW);
    }
}
