package kim.biryeong.skriptFabric.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Blocks;
import org.skriptlang.skript.fabric.runtime.SkriptFabricEventBridge;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(ServerLevel.class)
abstract class ServerLevelBlockFormMixin {

    @Inject(
            method = "tickPrecipitation",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/server/level/ServerLevel;setBlockAndUpdate(Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;)Z",
                    shift = At.Shift.AFTER,
                    ordinal = 0
            ),
            locals = LocalCapture.CAPTURE_FAILHARD
    )
    private void skript$dispatchBlockForm(BlockPos pos, CallbackInfo ci, BlockPos topPos, BlockPos freezePos, Biome biome) {
        SkriptFabricEventBridge.dispatchBlockForm((ServerLevel) (Object) this, freezePos.immutable(), Blocks.ICE.defaultBlockState());
    }
}
