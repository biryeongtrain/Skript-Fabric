package kim.biryeong.skriptFabric.mixin;

import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.ServerExplosion;
import net.minecraft.world.level.block.Block;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.fabric.compat.FabricBlock;
import org.skriptlang.skript.fabric.runtime.SkriptFabricEventBridge;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(ServerExplosion.class)
abstract class ServerExplosionMixin {

    @Shadow @Final private ServerLevel level;
    @Shadow @Final private @Nullable Entity source;

    @Unique
    private float skript$yield = 1.0F;

    @Inject(
            method = "explode",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/level/ServerExplosion;interactWithBlocks(Ljava/util/List;)V"
            ),
            locals = LocalCapture.CAPTURE_FAILHARD
    )
    private void skript$dispatchExplode(CallbackInfo callbackInfo, List<BlockPos> explodedPositions, ProfilerFiller profiler) {
        if (source == null) {
            return;
        }
        var handle = SkriptFabricEventBridge.dispatchExplosion(level, source, explodedPositions);
        skript$yield = Math.max(0.0F, handle.yield());

        explodedPositions.clear();
        List<FabricBlock> explodedBlocks = handle.explodedBlocks();
        if (explodedBlocks == null) {
            return;
        }
        for (FabricBlock block : explodedBlocks) {
            if (block != null && block.level() == level) {
                explodedPositions.add(block.position().immutable());
            }
        }
    }

    @Inject(
            method = "interactWithBlocks",
            at = @At(
                    value = "INVOKE",
                    target = "Ljava/util/List;iterator()Ljava/util/Iterator;",
                    ordinal = 1
            ),
            locals = LocalCapture.CAPTURE_FAILHARD
    )
    private void skript$applyExplosionYield(List<BlockPos> explodedPositions, CallbackInfo callbackInfo, List<?> drops) {
        if (skript$yield >= 1.0F) {
            return;
        }
        if (skript$yield <= 0.0F) {
            drops.clear();
            return;
        }
        drops.removeIf(drop -> level.getRandom().nextFloat() > skript$yield);
    }
}
