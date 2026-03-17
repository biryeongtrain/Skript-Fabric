package kim.biryeong.skriptFabric.mixin;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ClickType;
import org.skriptlang.skript.fabric.runtime.SkriptFabricEventBridge;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AbstractContainerMenu.class)
abstract class InventoryDragMixin {

    @Inject(method = "doClick", at = @At("HEAD"))
    private void skript$dispatchInventoryDrag(int slotId, int button, ClickType clickType, Player player, CallbackInfo ci) {
        if (clickType == ClickType.QUICK_CRAFT && player instanceof ServerPlayer serverPlayer && player.level() instanceof ServerLevel level) {
            SkriptFabricEventBridge.dispatchInventoryDrag(level, serverPlayer);
        }
    }
}
