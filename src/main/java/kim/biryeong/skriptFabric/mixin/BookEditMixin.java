package kim.biryeong.skriptFabric.mixin;

import java.util.List;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.FilteredText;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.item.ItemStack;
import org.skriptlang.skript.fabric.runtime.SkriptFabricEventBridge;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerGamePacketListenerImpl.class)
abstract class BookEditMixin {

    @Shadow public ServerPlayer player;

    @Unique
    private ItemStack skript$bookBefore = ItemStack.EMPTY;

    @Unique
    private int skript$bookSlot = -1;

    @Inject(method = "updateBookContents", at = @At("HEAD"))
    private void skript$captureBookBeforeEdit(List<String> pages, int slot, CallbackInfo callbackInfo) {
        skript$captureBook(slot);
    }

    @Inject(method = "updateBookContents", at = @At("RETURN"))
    private void skript$dispatchBookEdit(List<String> pages, int slot, CallbackInfo callbackInfo) {
        skript$dispatch(false);
    }

    @Inject(method = "signBook", at = @At("HEAD"))
    private void skript$captureBookBeforeSign(FilteredText title, List<String> pages, int slot, CallbackInfo callbackInfo) {
        skript$captureBook(slot);
    }

    @Inject(method = "signBook", at = @At("RETURN"))
    private void skript$dispatchBookSign(FilteredText title, List<String> pages, int slot, CallbackInfo callbackInfo) {
        skript$dispatch(true);
    }

    @Unique
    private void skript$captureBook(int slot) {
        skript$bookSlot = slot;
        skript$bookBefore = player.getInventory().getItem(slot).copy();
    }

    @Unique
    private void skript$dispatch(boolean signing) {
        if (skript$bookSlot < 0) {
            return;
        }
        ItemStack current = player.getInventory().getItem(skript$bookSlot).copy();
        SkriptFabricEventBridge.dispatchBookEdit(player, skript$bookBefore, current, signing);
        skript$bookSlot = -1;
        skript$bookBefore = ItemStack.EMPTY;
    }
}
