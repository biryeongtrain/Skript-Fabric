package kim.biryeong.skriptFabric.mixin;

import org.skriptlang.skript.fabric.runtime.FabricPotionEffectCause;
import org.skriptlang.skript.fabric.runtime.FabricPotionEffectCauseContext;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(targets = "net.minecraft.world.entity.animal.Dolphin$DolphinSwimWithPlayerGoal")
abstract class DolphinPotionCauseMixin {

    @Inject(method = "start", at = @At("HEAD"))
    private void skript$pushDolphinPotionCauseOnStart(CallbackInfo callbackInfo) {
        FabricPotionEffectCauseContext.push(FabricPotionEffectCause.DOLPHIN);
    }

    @Inject(method = "start", at = @At("RETURN"))
    private void skript$popDolphinPotionCauseOnStart(CallbackInfo callbackInfo) {
        FabricPotionEffectCauseContext.pop(FabricPotionEffectCause.DOLPHIN);
    }

    @Inject(method = "tick", at = @At("HEAD"))
    private void skript$pushDolphinPotionCauseOnTick(CallbackInfo callbackInfo) {
        FabricPotionEffectCauseContext.push(FabricPotionEffectCause.DOLPHIN);
    }

    @Inject(method = "tick", at = @At("RETURN"))
    private void skript$popDolphinPotionCauseOnTick(CallbackInfo callbackInfo) {
        FabricPotionEffectCauseContext.pop(FabricPotionEffectCause.DOLPHIN);
    }
}
