package kim.biryeong.skriptFabric.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.InsideBlockEffectApplier;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.TripWireBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import org.skriptlang.skript.fabric.runtime.SkriptFabricEventBridge;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(TripWireBlock.class)
abstract class TripWireBlockMixin {

    @Inject(method = "entityInside", at = @At("TAIL"))
    private void skript$dispatchTripwire(
            BlockState state,
            Level level,
            BlockPos pos,
            Entity entity,
            InsideBlockEffectApplier effectApplier,
            CallbackInfo callbackInfo
    ) {
        if (!(level instanceof ServerLevel serverLevel) || !state.hasProperty(BlockStateProperties.POWERED)) {
            return;
        }
        BlockState currentState = serverLevel.getBlockState(pos);
        if (!currentState.is(state.getBlock()) || !currentState.hasProperty(BlockStateProperties.POWERED)) {
            return;
        }
        if (!state.getValue(BlockStateProperties.POWERED) && currentState.getValue(BlockStateProperties.POWERED)) {
            SkriptFabricEventBridge.dispatchPressurePlate(serverLevel, pos, true);
        }
    }
}
