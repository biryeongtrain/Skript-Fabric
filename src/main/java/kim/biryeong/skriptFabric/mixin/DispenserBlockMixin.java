package kim.biryeong.skriptFabric.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.core.dispenser.BlockSource;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.DispenserBlock;
import net.minecraft.world.level.block.entity.DispenserBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.skriptlang.skript.fabric.runtime.SkriptFabricEventBridge;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(DispenserBlock.class)
abstract class DispenserBlockMixin {

    @Inject(
            method = "dispenseFrom(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/core/BlockPos;)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/level/block/DispenserBlock;getDispenseMethod(Lnet/minecraft/world/level/Level;Lnet/minecraft/world/item/ItemStack;)Lnet/minecraft/core/dispenser/DispenseItemBehavior;"
            ),
            locals = LocalCapture.CAPTURE_FAILHARD
    )
    private void skript$dispatchItemDispense(
            ServerLevel level,
            BlockState state,
            BlockPos pos,
            CallbackInfo ci,
            DispenserBlockEntity dispenser,
            BlockSource source,
            int slot,
            ItemStack itemStack
    ) {
        if (!itemStack.isEmpty()) {
            SkriptFabricEventBridge.dispatchItemDispense(level, pos, itemStack);
        }
    }
}
