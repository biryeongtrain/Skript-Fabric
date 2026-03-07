package kim.biryeong.skriptFabric.mixin;

import net.minecraft.world.entity.player.Player;
import org.skriptlang.skript.fabric.runtime.FabricPotionEffectCause;
import org.skriptlang.skript.fabric.runtime.FabricPotionEffectCauseContext;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Player.class)
abstract class PlayerPotionCauseMixin {

    @Inject(method = "turtleHelmetTick", at = @At("HEAD"))
    private void skript$pushTurtleHelmetPotionCause(CallbackInfo callbackInfo) {
        FabricPotionEffectCauseContext.push(FabricPotionEffectCause.TURTLE_HELMET);
    }

    @Inject(method = "turtleHelmetTick", at = @At("RETURN"))
    private void skript$popTurtleHelmetPotionCause(CallbackInfo callbackInfo) {
        FabricPotionEffectCauseContext.pop(FabricPotionEffectCause.TURTLE_HELMET);
    }
}
