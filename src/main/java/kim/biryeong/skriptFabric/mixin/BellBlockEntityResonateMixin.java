package kim.biryeong.skriptFabric.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BellBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Coerce;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.skriptlang.skript.fabric.runtime.SkriptFabricEventBridge;

@Mixin(BellBlockEntity.class)
abstract class BellBlockEntityResonateMixin {

    @Shadow
    private boolean resonating;

    @Unique
    private boolean skript$wasResonating;

    @Inject(method = "tick", at = @At("HEAD"))
    private static void skript$captureResonating(Level level, BlockPos pos, BlockState state, BellBlockEntity bell, @Coerce Object action, CallbackInfo ci) {
        ((BellBlockEntityResonateMixin) (Object) bell).skript$wasResonating = ((BellBlockEntityResonateMixin) (Object) bell).resonating;
    }

    @Inject(method = "tick", at = @At("RETURN"))
    private static void skript$dispatchBellResonate(Level level, BlockPos pos, BlockState state, BellBlockEntity bell, @Coerce Object action, CallbackInfo ci) {
        BellBlockEntityResonateMixin self = (BellBlockEntityResonateMixin) (Object) bell;
        if (!self.skript$wasResonating && self.resonating && level instanceof ServerLevel serverLevel) {
            SkriptFabricEventBridge.dispatchBellResonate(serverLevel, pos);
        }
    }
}
