package kim.biryeong.skriptFabric.mixin;

import net.minecraft.core.component.DataComponents;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.Consumable;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.fabric.runtime.FabricPotionEffectCause;
import org.skriptlang.skript.fabric.runtime.FabricPotionEffectCauseContext;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Consumable.class)
abstract class ConsumablePotionCauseMixin {

    @Unique
    private @Nullable FabricPotionEffectCause skript$pendingPotionCause;

    @Inject(method = "onConsume", at = @At("HEAD"))
    private void skript$pushConsumablePotionCause(Level level, LivingEntity user, ItemStack stack, CallbackInfoReturnable<ItemStack> callbackInfo) {
        skript$pendingPotionCause = skript$resolveCause(stack);
        if (skript$pendingPotionCause != null) {
            FabricPotionEffectCauseContext.push(skript$pendingPotionCause);
        }
    }

    @Inject(method = "onConsume", at = @At("RETURN"))
    private void skript$popConsumablePotionCause(Level level, LivingEntity user, ItemStack stack, CallbackInfoReturnable<ItemStack> callbackInfo) {
        if (skript$pendingPotionCause != null) {
            FabricPotionEffectCauseContext.pop(skript$pendingPotionCause);
            skript$pendingPotionCause = null;
        }
    }

    @Unique
    private static @Nullable FabricPotionEffectCause skript$resolveCause(ItemStack stack) {
        if (stack.is(Items.MILK_BUCKET)) {
            return FabricPotionEffectCause.MILK;
        }
        if (stack.get(DataComponents.FOOD) != null) {
            return FabricPotionEffectCause.FOOD;
        }
        return null;
    }
}
