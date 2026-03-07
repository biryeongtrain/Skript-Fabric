package kim.biryeong.skriptFabric.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BeaconBlockEntity;
import org.skriptlang.skript.fabric.runtime.FabricPotionEffectCause;
import org.skriptlang.skript.fabric.runtime.FabricPotionEffectCauseContext;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(BeaconBlockEntity.class)
abstract class BeaconPotionCauseMixin {

    @Inject(method = "applyEffects", at = @At("HEAD"))
    private static void skript$pushBeaconPotionCause(
            Level level,
            BlockPos pos,
            int levels,
            Holder<MobEffect> primaryPower,
            Holder<MobEffect> secondaryPower,
            CallbackInfo callbackInfo
    ) {
        FabricPotionEffectCauseContext.push(FabricPotionEffectCause.BEACON);
    }

    @Inject(method = "applyEffects", at = @At("RETURN"))
    private static void skript$popBeaconPotionCause(
            Level level,
            BlockPos pos,
            int levels,
            Holder<MobEffect> primaryPower,
            Holder<MobEffect> secondaryPower,
            CallbackInfo callbackInfo
    ) {
        FabricPotionEffectCauseContext.pop(FabricPotionEffectCause.BEACON);
    }
}
