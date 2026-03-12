package kim.biryeong.skriptFabric.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.IceBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.skriptlang.skript.fabric.runtime.SkriptFabricEventBridge;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(IceBlock.class)
abstract class IceBlockMixin {

    @Inject(method = "melt", at = @At("RETURN"))
    private void skript$dispatchBlockFade(BlockState state, Level level, BlockPos pos, CallbackInfo ci) {
        if (!(level instanceof ServerLevel serverLevel) || state.equals(level.getBlockState(pos))) {
            return;
        }
        SkriptFabricEventBridge.dispatchBlockFade(serverLevel, pos.immutable(), state);
    }
}
