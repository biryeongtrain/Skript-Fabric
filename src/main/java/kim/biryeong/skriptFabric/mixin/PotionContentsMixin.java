package kim.biryeong.skriptFabric.mixin;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.component.Consumable;
import net.minecraft.world.level.Level;
import org.skriptlang.skript.fabric.runtime.FabricPotionEffectCause;
import org.skriptlang.skript.fabric.runtime.FabricPotionEffectCauseContext;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PotionContents.class)
abstract class PotionContentsMixin {

    @Unique
    private boolean skript$potionDrinkPending;

    @Inject(method = "onConsume", at = @At("HEAD"))
    private void skript$pushPotionDrinkCause(Level level, LivingEntity user, ItemStack stack, Consumable consumable, CallbackInfo callbackInfo) {
        if (stack.is(Items.POTION)) {
            skript$potionDrinkPending = true;
            FabricPotionEffectCauseContext.push(FabricPotionEffectCause.POTION_DRINK);
        }
    }

    @Inject(method = "onConsume", at = @At("RETURN"))
    private void skript$popPotionDrinkCause(Level level, LivingEntity user, ItemStack stack, Consumable consumable, CallbackInfo callbackInfo) {
        if (skript$potionDrinkPending) {
            FabricPotionEffectCauseContext.pop(FabricPotionEffectCause.POTION_DRINK);
            skript$potionDrinkPending = false;
        }
    }
}
