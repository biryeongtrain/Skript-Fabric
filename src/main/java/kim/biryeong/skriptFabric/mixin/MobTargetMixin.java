package kim.biryeong.skriptFabric.mixin;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.fabric.runtime.SkriptFabricEventBridge;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Mob.class)
abstract class MobTargetMixin {

    @Unique
    private @Nullable LivingEntity skript$previousTarget;

    @Inject(method = "setTarget(Lnet/minecraft/world/entity/LivingEntity;)V", at = @At("HEAD"))
    private void skript$capturePreviousTarget(@Nullable LivingEntity target, CallbackInfo callbackInfo) {
        Mob self = (Mob) (Object) this;
        skript$previousTarget = self.level() instanceof ServerLevel ? self.getTarget() : null;
    }

    @Inject(method = "setTarget(Lnet/minecraft/world/entity/LivingEntity;)V", at = @At("TAIL"))
    private void skript$dispatchEntityTarget(@Nullable LivingEntity target, CallbackInfo callbackInfo) {
        Mob self = (Mob) (Object) this;
        LivingEntity previousTarget = skript$previousTarget;
        skript$previousTarget = null;
        if (!(self.level() instanceof ServerLevel serverLevel)) {
            return;
        }
        LivingEntity currentTarget = self.getTarget();
        if (previousTarget == currentTarget) {
            return;
        }
        SkriptFabricEventBridge.dispatchEntityTarget(serverLevel, self, currentTarget);
    }
}
