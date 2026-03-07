package kim.biryeong.skriptFabric.mixin;

import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.ConduitBlockEntity;
import org.skriptlang.skript.fabric.runtime.FabricPotionEffectCause;
import org.skriptlang.skript.fabric.runtime.FabricPotionEffectCauseContext;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ConduitBlockEntity.class)
abstract class ConduitPotionCauseMixin {

    @Inject(method = "applyEffects", at = @At("HEAD"))
    private static void skript$pushConduitPotionCause(Level level, BlockPos pos, List<BlockPos> effectBlocks, CallbackInfo callbackInfo) {
        FabricPotionEffectCauseContext.push(FabricPotionEffectCause.CONDUIT);
    }

    @Inject(method = "applyEffects", at = @At("RETURN"))
    private static void skript$popConduitPotionCause(Level level, BlockPos pos, List<BlockPos> effectBlocks, CallbackInfo callbackInfo) {
        FabricPotionEffectCauseContext.pop(FabricPotionEffectCause.CONDUIT);
    }
}
