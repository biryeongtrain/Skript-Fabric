package kim.biryeong.skriptFabric.mixin;

import org.skriptlang.skript.fabric.runtime.FabricPotionEffectCause;
import org.skriptlang.skript.fabric.runtime.FabricPotionEffectCauseContext;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(targets = "net.minecraft.world.entity.monster.illager.Illusioner$IllusionerMirrorSpellGoal")
abstract class IllusionerPotionCauseMixin {

    @Inject(method = "performSpellCasting", at = @At("HEAD"))
    private void skript$pushIllusionPotionCause(CallbackInfo callbackInfo) {
        FabricPotionEffectCauseContext.push(FabricPotionEffectCause.ILLUSION);
    }

    @Inject(method = "performSpellCasting", at = @At("RETURN"))
    private void skript$popIllusionPotionCause(CallbackInfo callbackInfo) {
        FabricPotionEffectCauseContext.pop(FabricPotionEffectCause.ILLUSION);
    }
}
