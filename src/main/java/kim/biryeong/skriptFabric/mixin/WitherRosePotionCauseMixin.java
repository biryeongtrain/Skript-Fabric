package kim.biryeong.skriptFabric.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.InsideBlockEffectApplier;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.WitherRoseBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.skriptlang.skript.fabric.runtime.FabricPotionEffectCause;
import org.skriptlang.skript.fabric.runtime.FabricPotionEffectCauseContext;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(WitherRoseBlock.class)
abstract class WitherRosePotionCauseMixin {

    @Inject(method = "entityInside", at = @At("HEAD"))
    private void skript$pushWitherRosePotionCause(
            BlockState state,
            Level level,
            BlockPos pos,
            Entity entity,
            InsideBlockEffectApplier effectApplier,
            CallbackInfo callbackInfo
    ) {
        FabricPotionEffectCauseContext.push(FabricPotionEffectCause.WITHER_ROSE);
    }

    @Inject(method = "entityInside", at = @At("RETURN"))
    private void skript$popWitherRosePotionCause(
            BlockState state,
            Level level,
            BlockPos pos,
            Entity entity,
            InsideBlockEffectApplier effectApplier,
            CallbackInfo callbackInfo
    ) {
        FabricPotionEffectCauseContext.pop(FabricPotionEffectCause.WITHER_ROSE);
    }
}
