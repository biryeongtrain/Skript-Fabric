package kim.biryeong.skriptFabric.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.skriptlang.skript.fabric.runtime.SkriptFabricEventBridge;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(CropBlock.class)
abstract class CropBlockMixin {

    @Inject(method = "randomTick", at = @At("HEAD"))
    private void skript$captureRandomTickState(
            BlockState state,
            ServerLevel level,
            BlockPos pos,
            RandomSource random,
            CallbackInfo callbackInfo
    ) {
        skript$storePreviousState(level, pos, state);
    }

    @Inject(method = "randomTick", at = @At("RETURN"))
    private void skript$dispatchRandomTickGrowth(
            BlockState state,
            ServerLevel level,
            BlockPos pos,
            RandomSource random,
            CallbackInfo callbackInfo
    ) {
        skript$dispatchIfChanged(level, pos);
    }

    @Inject(method = "performBonemeal", at = @At("HEAD"))
    private void skript$captureBonemealState(
            ServerLevel level,
            RandomSource random,
            BlockPos pos,
            BlockState state,
            CallbackInfo callbackInfo
    ) {
        skript$storePreviousState(level, pos, state);
    }

    @Inject(method = "performBonemeal", at = @At("RETURN"))
    private void skript$dispatchBonemealGrowth(
            ServerLevel level,
            RandomSource random,
            BlockPos pos,
            BlockState state,
            CallbackInfo callbackInfo
    ) {
        skript$dispatchIfChanged(level, pos);
    }

    private static final ThreadLocal<BlockState> SKRIPT_PREVIOUS_STATE = new ThreadLocal<>();
    private static final ThreadLocal<BlockPos> SKRIPT_PREVIOUS_POS = new ThreadLocal<>();

    private static void skript$storePreviousState(Level level, BlockPos pos, BlockState state) {
        SKRIPT_PREVIOUS_STATE.set(state);
        SKRIPT_PREVIOUS_POS.set(pos.immutable());
    }

    private static void skript$dispatchIfChanged(ServerLevel level, BlockPos pos) {
        BlockState previous = SKRIPT_PREVIOUS_STATE.get();
        BlockPos previousPos = SKRIPT_PREVIOUS_POS.get();
        SKRIPT_PREVIOUS_STATE.remove();
        SKRIPT_PREVIOUS_POS.remove();
        if (previous == null || previousPos == null || !previousPos.equals(pos)) {
            return;
        }
        BlockState current = level.getBlockState(pos);
        if (current.equals(previous)) {
            return;
        }
        SkriptFabricEventBridge.dispatchPlantGrowth(level, pos, previous, current);
        SkriptFabricEventBridge.dispatchGrow(level, pos, previous, current, null);
    }
}
