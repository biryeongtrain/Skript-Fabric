package kim.biryeong.skriptFabric.mixin;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.skriptlang.skript.fabric.runtime.SkriptFabricEventBridge;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ItemStack.class)
abstract class ItemStackConsumeMixin {

    @Inject(
            method = "finishUsingItem(Lnet/minecraft/world/level/Level;Lnet/minecraft/world/entity/LivingEntity;)Lnet/minecraft/world/item/ItemStack;",
            at = @At("HEAD"),
            cancellable = true
    )
    private void skript$dispatchPlayerConsume(Level level, LivingEntity livingEntity, CallbackInfoReturnable<ItemStack> cir) {
        if (!(level instanceof ServerLevel) || !(livingEntity instanceof ServerPlayer serverPlayer)) {
            return;
        }
        ItemStack itemStack = (ItemStack) (Object) this;
        if (!itemStack.isEmpty()) {
            boolean cancelled = SkriptFabricEventBridge.dispatchPlayerItemConsume(serverPlayer, itemStack.copy());
            if (cancelled) {
                cir.setReturnValue(itemStack);
            }
        }
    }
}
