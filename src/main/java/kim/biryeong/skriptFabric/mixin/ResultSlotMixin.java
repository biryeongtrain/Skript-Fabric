package kim.biryeong.skriptFabric.mixin;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ResultSlot;
import net.minecraft.world.item.ItemStack;
import org.skriptlang.skript.fabric.runtime.SkriptFabricEventBridge;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ResultSlot.class)
abstract class ResultSlotMixin {

    @Inject(method = "onTake", at = @At("HEAD"))
    private void skript$dispatchCraft(Player player, ItemStack stack, CallbackInfo callbackInfo) {
        if (player instanceof ServerPlayer serverPlayer) {
            SkriptFabricEventBridge.dispatchCraft(serverPlayer, stack);
        }
    }
}
