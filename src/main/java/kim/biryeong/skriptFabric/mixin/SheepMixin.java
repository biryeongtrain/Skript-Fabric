package kim.biryeong.skriptFabric.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.animal.sheep.Sheep;
import net.minecraft.world.level.block.state.BlockState;
import org.skriptlang.skript.fabric.runtime.SkriptFabricEventBridge;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Sheep.class)
abstract class SheepMixin {

    @Unique
    private BlockPos skript$previousEatPos;

    @Unique
    private BlockState skript$previousEatState;

    @Inject(method = "ate", at = @At("HEAD"))
    private void skript$capturePreEatState(CallbackInfo callbackInfo) {
        Sheep sheep = (Sheep) (Object) this;
        if (!(sheep.level() instanceof ServerLevel serverLevel)) {
            skript$previousEatPos = null;
            skript$previousEatState = null;
            return;
        }
        skript$previousEatPos = sheep.blockPosition().below().immutable();
        skript$previousEatState = serverLevel.getBlockState(skript$previousEatPos);
    }

    @Inject(method = "ate", at = @At("RETURN"))
    private void skript$dispatchSheepEatBlockChange(CallbackInfo callbackInfo) {
        Sheep sheep = (Sheep) (Object) this;
        if (!(sheep.level() instanceof ServerLevel serverLevel) || skript$previousEatPos == null || skript$previousEatState == null) {
            skript$previousEatPos = null;
            skript$previousEatState = null;
            return;
        }
        BlockState current = serverLevel.getBlockState(skript$previousEatPos);
        if (!current.equals(skript$previousEatState)) {
            SkriptFabricEventBridge.dispatchEntityBlockChange(
                    serverLevel,
                    skript$previousEatPos,
                    sheep,
                    skript$previousEatState,
                    current
            );
        }
        skript$previousEatPos = null;
        skript$previousEatState = null;
    }
}
