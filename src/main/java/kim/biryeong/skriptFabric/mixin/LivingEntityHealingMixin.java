package kim.biryeong.skriptFabric.mixin;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import org.skriptlang.skript.fabric.runtime.SkriptFabricEventBridge;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntity.class)
abstract class LivingEntityHealingMixin {

    @Inject(method = "heal", at = @At("RETURN"))
    private void skript$dispatchHealing(float amount, CallbackInfo callbackInfo) {
        if (amount <= 0.0F) {
            return;
        }
        LivingEntity entity = (LivingEntity) (Object) this;
        if (entity.level() instanceof ServerLevel serverLevel) {
            SkriptFabricEventBridge.dispatchHealing(serverLevel, entity, null);
        }
    }
}
