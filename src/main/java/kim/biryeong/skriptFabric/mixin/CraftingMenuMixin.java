package kim.biryeong.skriptFabric.mixin;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractCraftingMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.CraftingMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.fabric.runtime.SkriptFabricEventBridge;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(CraftingMenu.class)
abstract class CraftingMenuMixin extends AbstractCraftingMenu {

    @Shadow @Final private ContainerLevelAccess access;
    @Shadow @Final private Player player;

    protected CraftingMenuMixin(@Nullable MenuType<?> menuType, int containerId, int width, int height) {
        super(menuType, containerId, width, height);
    }

    @Inject(method = "slotsChanged", at = @At("TAIL"))
    private void skript$dispatchPrepareCraft(Container container, CallbackInfo callbackInfo) {
        if (!(player instanceof ServerPlayer serverPlayer)) {
            return;
        }
        ItemStack result = resultSlots.getItem(0).copy();
        access.execute((level, pos) -> {
            if (level instanceof ServerLevel serverLevel) {
                SkriptFabricEventBridge.dispatchPrepareCraft(serverLevel, pos, serverPlayer, result);
            }
        });
    }
}
