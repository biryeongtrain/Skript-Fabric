package kim.biryeong.skriptFabric.mixin;

import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.skriptlang.skript.fabric.runtime.SkriptFabricEventBridge;

@Mixin(LivingEntity.class)
abstract class LivingEntityDamageMixin {

    @ModifyVariable(
            method = "actuallyHurt",
            at = @At("HEAD"),
            ordinal = 0,
            argsOnly = true
    )
    private float skript$modifyDamage(float amount) {
        Float modified = SkriptFabricEventBridge.consumeModifiedDamage();
        return modified != null ? modified : amount;
    }
}
