package kim.biryeong.skriptFabric.mixin;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LightningBolt;
import org.skriptlang.skript.fabric.runtime.SkriptFabricEventBridge;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LightningBolt.class)
abstract class LightningBoltMixin {

    @Inject(method = "tick", at = @At("HEAD"))
    private void skript$dispatchLightningStrike(CallbackInfo ci) {
        LightningBolt bolt = (LightningBolt) (Object) this;
        if (bolt.tickCount == 1 && bolt.level() instanceof ServerLevel level) {
            SkriptFabricEventBridge.dispatchLightningStrike(level, bolt);
        }
    }
}
