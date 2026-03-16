package kim.biryeong.skriptFabric.mixin;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.skriptlang.skript.fabric.runtime.SkriptFabricEventBridge;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BowItem.class)
abstract class BowReleaseMixin {

    @Inject(
            method = "releaseUsing",
            at = @At("HEAD"),
            cancellable = true
    )
    private void skript$dispatchReadyArrow(
            ItemStack bowStack,
            Level level,
            LivingEntity entity,
            int timeCharged,
            CallbackInfoReturnable<Boolean> cir
    ) {
        if (level.isClientSide() || !(entity instanceof ServerPlayer player)) {
            return;
        }
        ItemStack arrowStack = player.getProjectile(bowStack);
        if (arrowStack.isEmpty()) {
            return;
        }
        boolean cancelled = SkriptFabricEventBridge.dispatchReadyArrow(player, bowStack.copy(), arrowStack.copy());
        if (cancelled) {
            cir.setReturnValue(false);
        }
    }
}
