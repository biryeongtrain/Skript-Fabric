package kim.biryeong.skriptFabric.mixin;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.FireworkRocketEntity;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.skriptlang.skript.fabric.runtime.SkriptFabricEventBridge;

@Mixin(FireworkRocketEntity.class)
abstract class FireworkRocketEntityMixin {

    @Shadow
    @Nullable
    private LivingEntity attachedToEntity;

    @Unique
    private boolean skript$preventConsume;

    @Inject(
            method = "explode(Lnet/minecraft/server/level/ServerLevel;)V",
            at = @At("HEAD")
    )
    private void skript$dispatchFireworkExplode(ServerLevel serverLevel, CallbackInfo callbackInfo) {
        FireworkRocketEntity firework = (FireworkRocketEntity) (Object) this;
        SkriptFabricEventBridge.dispatchFirework(serverLevel, firework);

        skript$preventConsume = false;
        if (attachedToEntity instanceof ServerPlayer player) {
            boolean shouldConsume = SkriptFabricEventBridge.dispatchElytraBoost(serverLevel, player, firework);
            skript$preventConsume = !shouldConsume;
        }
    }

    @Redirect(
            method = "explode(Lnet/minecraft/server/level/ServerLevel;)V",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/projectile/FireworkRocketEntity;discard()V")
    )
    private void skript$conditionalDiscard(FireworkRocketEntity instance) {
        if (!skript$preventConsume) {
            instance.discard();
        }
    }
}
