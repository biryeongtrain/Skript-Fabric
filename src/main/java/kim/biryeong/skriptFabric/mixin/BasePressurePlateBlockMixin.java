package kim.biryeong.skriptFabric.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.InsideBlockEffectApplier;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BasePressurePlateBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.skriptlang.skript.fabric.runtime.SkriptFabricEventBridge;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(BasePressurePlateBlock.class)
abstract class BasePressurePlateBlockMixin {

    @Inject(method = "entityInside", at = @At("TAIL"))
    private void skript$dispatchPressurePlate(
            BlockState state,
            Level level,
            BlockPos pos,
            Entity entity,
            InsideBlockEffectApplier effectApplier,
            boolean moving,
            CallbackInfo callbackInfo
    ) {
        if (!(level instanceof ServerLevel serverLevel)) {
            return;
        }
        BlockState currentState = serverLevel.getBlockState(pos);
        if (!currentState.is(state.getBlock())) {
            return;
        }
        int previousSignal = state.getSignal(level, pos, Direction.UP);
        int currentSignal = currentState.getSignal(level, pos, Direction.UP);
        if (previousSignal == 0 && currentSignal > 0) {
            SkriptFabricEventBridge.dispatchPressurePlate(serverLevel, pos, false);
        }
    }
}
