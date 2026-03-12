package kim.biryeong.skriptFabric.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.FireBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.skriptlang.skript.fabric.runtime.SkriptFabricEventBridge;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(FireBlock.class)
abstract class FireBlockMixin {

    private static final ThreadLocal<BlockState> SKRIPT_PREVIOUS_STATE = new ThreadLocal<>();
    private static final ThreadLocal<BlockPos> SKRIPT_PREVIOUS_POS = new ThreadLocal<>();

    @Inject(method = "checkBurnOut", at = @At("HEAD"))
    private void skript$captureBurnTarget(Level level, BlockPos pos, int chance, RandomSource random, int age, CallbackInfo ci) {
        SKRIPT_PREVIOUS_STATE.set(level.getBlockState(pos));
        SKRIPT_PREVIOUS_POS.set(pos.immutable());
    }

    @Inject(method = "checkBurnOut", at = @At("RETURN"))
    private void skript$dispatchBlockBurn(Level level, BlockPos pos, int chance, RandomSource random, int age, CallbackInfo ci) {
        BlockState previous = SKRIPT_PREVIOUS_STATE.get();
        BlockPos previousPos = SKRIPT_PREVIOUS_POS.get();
        SKRIPT_PREVIOUS_STATE.remove();
        SKRIPT_PREVIOUS_POS.remove();
        if (!(level instanceof ServerLevel serverLevel) || previous == null || previousPos == null || !previousPos.equals(pos)) {
            return;
        }
        if (previous.isAir() || previous.equals(level.getBlockState(pos))) {
            return;
        }
        SkriptFabricEventBridge.dispatchBlockBurn(serverLevel, pos.immutable(), previous);
    }
}
