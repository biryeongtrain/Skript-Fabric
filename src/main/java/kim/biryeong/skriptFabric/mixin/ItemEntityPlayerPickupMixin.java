package kim.biryeong.skriptFabric.mixin;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.skriptlang.skript.fabric.runtime.SkriptFabricEventBridge;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(ItemEntity.class)
abstract class ItemEntityPlayerPickupMixin {

    @Inject(
            method = "playerTouch(Lnet/minecraft/world/entity/player/Player;)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/entity/player/Player;take(Lnet/minecraft/world/entity/Entity;I)V"
            ),
            locals = LocalCapture.CAPTURE_FAILHARD
    )
    private void skript$dispatchPlayerPickup(
            Player player,
            CallbackInfo ci,
            ItemStack itemStack,
            Item item,
            int pickedUpCount
    ) {
        if (player instanceof ServerPlayer serverPlayer) {
            SkriptFabricEventBridge.dispatchPlayerItemPickup(
                    serverPlayer,
                    (ItemEntity) (Object) this,
                    itemStack.copyWithCount(pickedUpCount)
            );
        }
    }
}
