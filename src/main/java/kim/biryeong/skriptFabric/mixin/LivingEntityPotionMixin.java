package kim.biryeong.skriptFabric.mixin;

import java.util.ArrayList;
import java.util.Collection;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.fabric.runtime.FabricPotionEffectAction;
import org.skriptlang.skript.fabric.runtime.FabricPotionEffectCause;
import org.skriptlang.skript.fabric.runtime.FabricPotionEffectCauseContext;
import org.skriptlang.skript.fabric.runtime.SkriptFabricEventBridge;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntity.class)
abstract class LivingEntityPotionMixin {

    @Unique
    private boolean skript$pendingPotionApply;

    @Unique
    private @Nullable MobEffectInstance skript$pendingPreviousPotionEffect;

    @Unique
    private @Nullable FabricPotionEffectAction skript$pendingPotionRemovalAction;

    @Inject(method = "tickEffects", at = @At("HEAD"))
    private void skript$pushExpirationPotionCause(CallbackInfo callbackInfo) {
        FabricPotionEffectCauseContext.push(FabricPotionEffectCause.EXPIRATION);
    }

    @Inject(method = "tickEffects", at = @At("RETURN"))
    private void skript$popExpirationPotionCause(CallbackInfo callbackInfo) {
        FabricPotionEffectCauseContext.pop(FabricPotionEffectCause.EXPIRATION);
    }

    @Inject(method = "checkTotemDeathProtection", at = @At("HEAD"))
    private void skript$pushTotemPotionCause(net.minecraft.world.damagesource.DamageSource damageSource, CallbackInfoReturnable<Boolean> callbackInfo) {
        FabricPotionEffectCauseContext.push(FabricPotionEffectCause.TOTEM);
    }

    @Inject(method = "checkTotemDeathProtection", at = @At("RETURN"))
    private void skript$popTotemPotionCause(net.minecraft.world.damagesource.DamageSource damageSource, CallbackInfoReturnable<Boolean> callbackInfo) {
        FabricPotionEffectCauseContext.pop(FabricPotionEffectCause.TOTEM);
    }

    @Inject(method = "addEffect(Lnet/minecraft/world/effect/MobEffectInstance;Lnet/minecraft/world/entity/Entity;)Z", at = @At("HEAD"))
    private void skript$capturePotionAdd(MobEffectInstance effect, @Nullable Entity source, CallbackInfoReturnable<Boolean> callbackInfo) {
        LivingEntity self = (LivingEntity) (Object) this;
        skript$pendingPotionApply = true;
        skript$pendingPreviousPotionEffect = skript$copy(self.getEffect(effect.getEffect()));
    }

    @Inject(method = "addEffect(Lnet/minecraft/world/effect/MobEffectInstance;Lnet/minecraft/world/entity/Entity;)Z", at = @At("RETURN"))
    private void skript$clearPotionAdd(MobEffectInstance effect, @Nullable Entity source, CallbackInfoReturnable<Boolean> callbackInfo) {
        skript$pendingPotionApply = false;
        skript$pendingPreviousPotionEffect = null;
    }

    @Inject(method = "forceAddEffect", at = @At("HEAD"))
    private void skript$captureForcedPotionAdd(MobEffectInstance effect, @Nullable Entity source, CallbackInfo callbackInfo) {
        LivingEntity self = (LivingEntity) (Object) this;
        skript$pendingPotionApply = true;
        skript$pendingPreviousPotionEffect = skript$copy(self.getEffect(effect.getEffect()));
    }

    @Inject(method = "forceAddEffect", at = @At("RETURN"))
    private void skript$clearForcedPotionAdd(MobEffectInstance effect, @Nullable Entity source, CallbackInfo callbackInfo) {
        skript$pendingPotionApply = false;
        skript$pendingPreviousPotionEffect = null;
    }

    @Inject(method = "onEffectAdded", at = @At("TAIL"))
    private void skript$dispatchPotionAdded(MobEffectInstance effect, @Nullable Entity source, CallbackInfo callbackInfo) {
        if (!skript$pendingPotionApply) {
            return;
        }
        LivingEntity self = (LivingEntity) (Object) this;
        if (self.level() instanceof ServerLevel serverLevel) {
            FabricPotionEffectCause cause = FabricPotionEffectCause.resolve(source, self, effect);
            SkriptFabricEventBridge.dispatchPotionEffect(
                    serverLevel,
                    self,
                    effect,
                    skript$pendingPreviousPotionEffect,
                    FabricPotionEffectAction.ADDED,
                    cause
            );
        }
    }

    @Inject(method = "onEffectUpdated", at = @At("TAIL"))
    private void skript$dispatchPotionChanged(MobEffectInstance effect, boolean reapplyAttributes, @Nullable Entity source, CallbackInfo callbackInfo) {
        if (!skript$pendingPotionApply) {
            return;
        }
        LivingEntity self = (LivingEntity) (Object) this;
        if (self.level() instanceof ServerLevel serverLevel) {
            FabricPotionEffectCause cause = FabricPotionEffectCause.resolve(source, self, effect);
            SkriptFabricEventBridge.dispatchPotionEffect(
                    serverLevel,
                    self,
                    effect,
                    skript$pendingPreviousPotionEffect,
                    FabricPotionEffectAction.CHANGED,
                    cause
            );
        }
    }

    @Inject(method = "removeEffect", at = @At("HEAD"))
    private void skript$markPotionRemoval(net.minecraft.core.Holder<net.minecraft.world.effect.MobEffect> effect, CallbackInfoReturnable<Boolean> callbackInfo) {
        skript$pendingPotionRemovalAction = FabricPotionEffectAction.REMOVED;
    }

    @Inject(method = "removeEffect", at = @At("RETURN"))
    private void skript$clearPotionRemoval(net.minecraft.core.Holder<net.minecraft.world.effect.MobEffect> effect, CallbackInfoReturnable<Boolean> callbackInfo) {
        skript$pendingPotionRemovalAction = null;
    }

    @Inject(method = "removeAllEffects", at = @At("HEAD"))
    private void skript$markPotionClear(CallbackInfoReturnable<Boolean> callbackInfo) {
        skript$pendingPotionRemovalAction = FabricPotionEffectAction.CLEARED;
    }

    @Inject(method = "removeAllEffects", at = @At("RETURN"))
    private void skript$clearPotionClear(CallbackInfoReturnable<Boolean> callbackInfo) {
        skript$pendingPotionRemovalAction = null;
    }

    @Inject(method = "onEffectsRemoved", at = @At("TAIL"))
    private void skript$dispatchPotionRemoved(Collection<MobEffectInstance> effects, CallbackInfo callbackInfo) {
        LivingEntity self = (LivingEntity) (Object) this;
        if (!(self.level() instanceof ServerLevel serverLevel)) {
            return;
        }
        FabricPotionEffectAction action = skript$pendingPotionRemovalAction != null
                ? skript$pendingPotionRemovalAction
                : FabricPotionEffectAction.REMOVED;
        FabricPotionEffectCause cause = FabricPotionEffectCause.resolve(null, self, null);
        for (MobEffectInstance effect : effects) {
            SkriptFabricEventBridge.dispatchPotionEffect(serverLevel, self, null, effect, action, cause);
        }
    }

    @Inject(method = "remove", at = @At("HEAD"))
    private void skript$dispatchPotionDeathRemoval(Entity.RemovalReason reason, CallbackInfo callbackInfo) {
        if (reason != Entity.RemovalReason.KILLED) {
            return;
        }
        LivingEntity self = (LivingEntity) (Object) this;
        if (!(self.level() instanceof ServerLevel serverLevel)) {
            return;
        }
        Collection<MobEffectInstance> activeEffects = new ArrayList<>(self.getActiveEffects());
        if (activeEffects.isEmpty()) {
            return;
        }
        for (MobEffectInstance effect : activeEffects) {
            SkriptFabricEventBridge.dispatchPotionEffect(
                    serverLevel,
                    self,
                    null,
                    new MobEffectInstance(effect),
                    FabricPotionEffectAction.CLEARED,
                    FabricPotionEffectCause.DEATH
            );
        }
    }

    @Unique
    private static @Nullable MobEffectInstance skript$copy(@Nullable MobEffectInstance effect) {
        return effect == null ? null : new MobEffectInstance(effect);
    }
}
