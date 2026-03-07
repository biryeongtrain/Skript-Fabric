package kim.biryeong.skriptFabric.mixin;

import net.minecraft.world.entity.animal.axolotl.Axolotl;
import net.minecraft.world.entity.player.Player;
import org.skriptlang.skript.fabric.runtime.FabricPotionEffectCause;
import org.skriptlang.skript.fabric.runtime.FabricPotionEffectCauseContext;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Axolotl.class)
abstract class AxolotlPotionCauseMixin {

    @Inject(method = "applySupportingEffects", at = @At("HEAD"))
    private void skript$pushAxolotlPotionCause(Player player, CallbackInfo callbackInfo) {
        FabricPotionEffectCauseContext.push(FabricPotionEffectCause.AXOLOTL);
    }

    @Inject(method = "applySupportingEffects", at = @At("RETURN"))
    private void skript$popAxolotlPotionCause(Player player, CallbackInfo callbackInfo) {
        FabricPotionEffectCauseContext.pop(FabricPotionEffectCause.AXOLOTL);
    }
}
