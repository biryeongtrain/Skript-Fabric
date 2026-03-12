package kim.biryeong.skriptFabric.mixin;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.inventory.StonecutterMenu;
import net.minecraft.world.item.ItemStack;
import org.skriptlang.skript.fabric.runtime.SkriptFabricEventBridge;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(StonecutterMenu.class)
abstract class StonecutterMenuMixin {

    @Shadow @Final private ContainerLevelAccess access;
    @Shadow @Final private Slot resultSlot;

    @Inject(method = "quickMoveStack", at = @At("HEAD"))
    private void skript$dispatchStonecutting(Player player, int slot, CallbackInfoReturnable<ItemStack> callbackInfo) {
        if (!(player instanceof ServerPlayer serverPlayer) || slot != 1) {
            return;
        }
        ItemStack result = resultSlot.getItem().copy();
        if (result.isEmpty()) {
            return;
        }
        access.execute((level, pos) -> {
            if (level instanceof ServerLevel serverLevel) {
                SkriptFabricEventBridge.dispatchStonecutting(serverLevel, pos, serverPlayer, result);
            }
        });
    }
}
