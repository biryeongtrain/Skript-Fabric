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
abstract class HopperTransferMixin {

    @Unique
    private static final ThreadLocal<TransferContext> SKRIPT_TRANSFER_CONTEXT = new ThreadLocal<>();

    @Inject(
            method = "addItem(Lnet/minecraft/world/Container;Lnet/minecraft/world/Container;Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/core/Direction;)Lnet/minecraft/world/item/ItemStack;",
            at = @At("HEAD"),
            cancellable = true
    )
    private static void skript$captureTransfer(
            Container source,
            Container destination,
            ItemStack stack,
            Direction direction,
            CallbackInfoReturnable<ItemStack> cir
    ) {
        if (source == null || destination == null || stack.isEmpty()) {
            SKRIPT_TRANSFER_CONTEXT.remove();
            return;
        }
        Container initiator = destination instanceof HopperBlockEntity ? destination : source;
        SKRIPT_TRANSFER_CONTEXT.set(new TransferContext(source, destination, initiator));
    }

    @Inject(
            method = "addItem(Lnet/minecraft/world/Container;Lnet/minecraft/world/Container;Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/core/Direction;)Lnet/minecraft/world/item/ItemStack;",
            at = @At("RETURN"),
            cancellable = true
    )
    private static void skript$dispatchTransfer(
            Container source,
            Container destination,
            ItemStack stack,
            Direction direction,
            CallbackInfoReturnable<ItemStack> cir
    ) {
        TransferContext context = SKRIPT_TRANSFER_CONTEXT.get();
        SKRIPT_TRANSFER_CONTEXT.remove();
        if (context == null) {
            return;
        }
        ItemStack returnValue = cir.getReturnValue();
        if (returnValue != null && !returnValue.isEmpty()) {
            return;
        }
        boolean cancelled = SkriptFabricEventBridge.dispatchInventoryMoveEvent(
                context.source(), context.destination(), context.initiator()
        );
        if (cancelled) {
            cir.setReturnValue(stack);
        }
    }

    @Unique
    private record TransferContext(Container source, Container destination, Container initiator) {
    }
}
