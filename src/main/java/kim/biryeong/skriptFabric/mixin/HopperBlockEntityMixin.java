package kim.biryeong.skriptFabric.mixin;

import net.minecraft.core.Direction;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.HopperBlockEntity;
import org.skriptlang.skript.fabric.runtime.SkriptFabricEventBridge;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(HopperBlockEntity.class)
abstract class HopperBlockEntityMixin {

    @Unique
    private static final ThreadLocal<TransferCapture> SKRIPT_INVENTORY_MOVE_CAPTURE = new ThreadLocal<>();

    @Inject(
            method = "addItem(Lnet/minecraft/world/Container;Lnet/minecraft/world/Container;Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/core/Direction;)Lnet/minecraft/world/item/ItemStack;",
            at = @At("HEAD")
    )
    private static void skript$captureInventoryMove(
            Container source,
            Container destination,
            ItemStack stack,
            Direction direction,
            CallbackInfoReturnable<ItemStack> callbackInfo
    ) {
        if (source == null || destination == null || stack.isEmpty()) {
            SKRIPT_INVENTORY_MOVE_CAPTURE.remove();
            return;
        }
        SKRIPT_INVENTORY_MOVE_CAPTURE.set(new TransferCapture(source, destination, stack.copyWithCount(1)));
    }

    @Inject(
            method = "addItem(Lnet/minecraft/world/Container;Lnet/minecraft/world/Container;Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/core/Direction;)Lnet/minecraft/world/item/ItemStack;",
            at = @At("RETURN")
    )
    private static void skript$dispatchInventoryMove(
            Container source,
            Container destination,
            ItemStack stack,
            Direction direction,
            CallbackInfoReturnable<ItemStack> callbackInfo
    ) {
        TransferCapture capture = SKRIPT_INVENTORY_MOVE_CAPTURE.get();
        SKRIPT_INVENTORY_MOVE_CAPTURE.remove();
        if (capture == null || !callbackInfo.getReturnValue().isEmpty()) {
            return;
        }
        SkriptFabricEventBridge.dispatchInventoryMove(capture.source(), capture.destination(), capture.stack());
    }

    @Unique
    private record TransferCapture(Container source, Container destination, ItemStack stack) {
    }
}
