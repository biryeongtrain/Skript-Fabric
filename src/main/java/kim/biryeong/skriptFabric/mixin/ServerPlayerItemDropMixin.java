package kim.biryeong.skriptFabric.mixin;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import org.skriptlang.skript.fabric.runtime.SkriptFabricEventBridge;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ServerPlayer.class)
abstract class ServerPlayerItemDropMixin {

    @Inject(method = "drop(Lnet/minecraft/world/item/ItemStack;ZZ)Lnet/minecraft/world/entity/item/ItemEntity;", at = @At("RETURN"))
    private void skript$dispatchPlayerDrop(
            ItemStack stack,
            boolean dropAround,
            boolean includeThrowerName,
            CallbackInfoReturnable<ItemEntity> cir
    ) {
        ItemEntity droppedItem = cir.getReturnValue();
        if (droppedItem == null) {
            return;
        }
        SkriptFabricEventBridge.dispatchPlayerItemDrop((ServerPlayer) (Object) this, droppedItem);
    }
}
