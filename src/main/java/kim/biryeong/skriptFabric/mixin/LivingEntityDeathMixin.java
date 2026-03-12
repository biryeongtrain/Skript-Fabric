package kim.biryeong.skriptFabric.mixin;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;
import org.skriptlang.skript.fabric.runtime.SkriptFabricEventBridge;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntity.class)
abstract class LivingEntityDeathMixin {

    @Inject(
            method = "dropAllDeathLoot(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/damagesource/DamageSource;)V",
            at = @At("HEAD")
    )
    private void skript$beginDeathCapture(ServerLevel level, DamageSource damageSource, CallbackInfo callbackInfo) {
        SkriptFabricEventBridge.beginDeathCapture((LivingEntity) (Object) this, level, damageSource);
    }

    @Inject(
            method = "dropAllDeathLoot(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/damagesource/DamageSource;)V",
            at = @At("TAIL")
    )
    private void skript$finishDeathCapture(ServerLevel level, DamageSource damageSource, CallbackInfo callbackInfo) {
        SkriptFabricEventBridge.finishDeathCapture((LivingEntity) (Object) this);
    }

    @Redirect(
            method = "dropExperience(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/entity/Entity;)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/entity/ExperienceOrb;award(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/phys/Vec3;I)V"
            )
    )
    private void skript$captureDeathExperience(ServerLevel level, Vec3 pos, int amount, ServerLevel originalLevel, Entity attacker) {
        LivingEntity entity = (LivingEntity) (Object) this;
        if (!SkriptFabricEventBridge.captureDeathExperience(entity, amount)) {
            ExperienceOrb.award(level, pos, amount);
        }
    }
}
