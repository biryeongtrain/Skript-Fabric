package kim.biryeong.skriptFabric.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.block.state.BlockState;
import org.skriptlang.skript.fabric.compat.FabricBlock;
import org.skriptlang.skript.fabric.runtime.SkriptFabricEventBridge;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(net.minecraft.world.item.BoneMealItem.class)
abstract class BoneMealItemMixin {

    private static final ThreadLocal<BlockState> SKRIPT_PREVIOUS_STATE = new ThreadLocal<>();
    private static final ThreadLocal<BlockPos> SKRIPT_PREVIOUS_POS = new ThreadLocal<>();
    private static final ThreadLocal<ServerLevel> SKRIPT_PREVIOUS_LEVEL = new ThreadLocal<>();

    @Inject(method = "useOn", at = @At("HEAD"))
    private void skript$captureBonemealTarget(UseOnContext context, CallbackInfoReturnable<InteractionResult> cir) {
        if (!(context.getLevel() instanceof ServerLevel level)) {
            return;
        }
        BlockPos pos = context.getClickedPos().immutable();
        SKRIPT_PREVIOUS_LEVEL.set(level);
        SKRIPT_PREVIOUS_POS.set(pos);
        SKRIPT_PREVIOUS_STATE.set(level.getBlockState(pos));
    }

    @Inject(method = "useOn", at = @At("RETURN"))
    private void skript$dispatchBlockFertilize(UseOnContext context, CallbackInfoReturnable<InteractionResult> cir) {
        ServerLevel level = SKRIPT_PREVIOUS_LEVEL.get();
        BlockPos pos = SKRIPT_PREVIOUS_POS.get();
        BlockState previous = SKRIPT_PREVIOUS_STATE.get();
        SKRIPT_PREVIOUS_LEVEL.remove();
        SKRIPT_PREVIOUS_POS.remove();
        SKRIPT_PREVIOUS_STATE.remove();
        if (level == null || pos == null || previous == null || !cir.getReturnValue().consumesAction()) {
            return;
        }
        BlockState current = level.getBlockState(pos);
        if (current.equals(previous)) {
            return;
        }
        SkriptFabricEventBridge.dispatchBlockFertilize(level, java.util.List.of(new FabricBlock(level, pos)));
    }
}
