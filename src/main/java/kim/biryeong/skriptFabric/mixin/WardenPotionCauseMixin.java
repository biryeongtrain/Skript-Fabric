package kim.biryeong.skriptFabric.mixin;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.monster.warden.Warden;
import net.minecraft.world.phys.Vec3;
import org.skriptlang.skript.fabric.runtime.FabricPotionEffectCause;
import org.skriptlang.skript.fabric.runtime.FabricPotionEffectCauseContext;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Warden.class)
abstract class WardenPotionCauseMixin {

    @Inject(method = "applyDarknessAround", at = @At("HEAD"))
    private static void skript$pushWardenPotionCause(ServerLevel level, Vec3 pos, Entity source, int duration, CallbackInfo callbackInfo) {
        FabricPotionEffectCauseContext.push(FabricPotionEffectCause.WARDEN);
    }

    @Inject(method = "applyDarknessAround", at = @At("RETURN"))
    private static void skript$popWardenPotionCause(ServerLevel level, Vec3 pos, Entity source, int duration, CallbackInfo callbackInfo) {
        FabricPotionEffectCauseContext.pop(FabricPotionEffectCause.WARDEN);
    }
}
