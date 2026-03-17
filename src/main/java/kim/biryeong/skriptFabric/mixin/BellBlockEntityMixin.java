package kim.biryeong.skriptFabric.mixin;

import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.entity.BellBlockEntity;
import org.skriptlang.skript.fabric.runtime.SkriptFabricEventBridge;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(BellBlockEntity.class)
abstract class BellBlockEntityMixin {

    @Inject(method = "onHit", at = @At("HEAD"))
    private void skript$dispatchBellRing(Direction direction, CallbackInfo ci) {
        BellBlockEntity bell = (BellBlockEntity) (Object) this;
        if (bell.getLevel() instanceof ServerLevel level) {
            SkriptFabricEventBridge.dispatchBellRing(level, bell.getBlockPos());
        }
    }
}
