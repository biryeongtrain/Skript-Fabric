package kim.biryeong.skriptFabric.mixin;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.item.trading.MerchantOffer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.skriptlang.skript.fabric.runtime.FabricPotionEffectCause;
import org.skriptlang.skript.fabric.runtime.FabricPotionEffectCauseContext;

@Mixin(Villager.class)
abstract class VillagerPotionCauseMixin {

    @Unique
    private boolean skript$pendingVillagerTradePotion;

    @Inject(method = "rewardTradeXp", at = @At("TAIL"))
    private void skript$markVillagerTradePotion(MerchantOffer offer, CallbackInfo callbackInfo) {
        skript$pendingVillagerTradePotion = true;
    }

    @Inject(
            method = "customServerAiStep",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/entity/npc/Villager;addEffect(Lnet/minecraft/world/effect/MobEffectInstance;)Z",
                    shift = At.Shift.BEFORE
            )
    )
    private void skript$pushVillagerTradePotionCause(ServerLevel level, CallbackInfo callbackInfo) {
        if (skript$pendingVillagerTradePotion) {
            FabricPotionEffectCauseContext.push(FabricPotionEffectCause.VILLAGER_TRADE);
        }
    }

    @Inject(
            method = "customServerAiStep",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/entity/npc/Villager;addEffect(Lnet/minecraft/world/effect/MobEffectInstance;)Z",
                    shift = At.Shift.AFTER
            )
    )
    private void skript$popVillagerTradePotionCause(ServerLevel level, CallbackInfo callbackInfo) {
        if (skript$pendingVillagerTradePotion) {
            FabricPotionEffectCauseContext.pop(FabricPotionEffectCause.VILLAGER_TRADE);
            skript$pendingVillagerTradePotion = false;
        }
    }
}
