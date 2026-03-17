package kim.biryeong.skriptFabric.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.SpongeBlock;
import org.skriptlang.skript.fabric.runtime.SkriptFabricEventBridge;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(SpongeBlock.class)
abstract class SpongeAbsorbMixin {

    @Inject(method = "tryAbsorbWater", at = @At("HEAD"))
    private void skript$dispatchSpongeAbsorb(Level level, BlockPos pos, CallbackInfo ci) {
        if (level instanceof ServerLevel serverLevel) {
            SkriptFabricEventBridge.dispatchSpongeAbsorb(serverLevel, pos);
        }
    }
}
