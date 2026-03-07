package kim.biryeong.skriptFabric.mixin;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.FurnaceResultSlot;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.AbstractFurnaceBlockEntity;
import org.skriptlang.skript.fabric.runtime.SkriptFabricEventBridge;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(FurnaceResultSlot.class)
abstract class FurnaceResultSlotMixin extends Slot {

    protected FurnaceResultSlotMixin(Container container, int slot, int x, int y) {
        super(container, slot, x, y);
    }

    @Shadow @Final private Player player;
    @Shadow private int removeCount;

    @Inject(method = "checkTakeAchievements", at = @At("HEAD"))
    private void skript$dispatchFurnaceExtract(ItemStack stack, CallbackInfo callbackInfo) {
        if (!(player instanceof ServerPlayer serverPlayer)) {
            return;
        }
        if (!(container instanceof AbstractFurnaceBlockEntity furnace)) {
            return;
        }
        int itemAmount = Math.max(1, removeCount);
        ItemStack extracted = stack.copyWithCount(itemAmount);
        SkriptFabricEventBridge.dispatchFurnaceExtract(serverPlayer, furnace, extracted, itemAmount);
    }
}
