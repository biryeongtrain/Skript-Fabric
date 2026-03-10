package kim.biryeong.skriptFabric.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.state.BlockState;
import org.skriptlang.skript.fabric.runtime.SkriptFabricEventBridge;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BlockItem.class)
abstract class BlockItemMixin {

    @Inject(method = "place", at = @At("RETURN"))
    private void skript$dispatchBlockPlace(BlockPlaceContext context, CallbackInfoReturnable<InteractionResult> cir) {
        if (!cir.getReturnValue().consumesAction()) {
            return;
        }
        if (!(context.getLevel() instanceof ServerLevel level)) {
            return;
        }
        BlockPos pos = context.getClickedPos();
        BlockState state = level.getBlockState(pos);
        ItemStack stack = context.getItemInHand();
        SkriptFabricEventBridge.dispatchBlockPlace(
                level,
                pos,
                state,
                stack.isEmpty() ? null : stack.copyWithCount(1),
                context.getPlayer() instanceof ServerPlayer player ? player : null
        );
    }
}
