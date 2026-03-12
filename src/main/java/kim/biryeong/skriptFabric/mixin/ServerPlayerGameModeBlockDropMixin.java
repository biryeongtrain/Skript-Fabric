package kim.biryeong.skriptFabric.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerPlayerGameMode;
import net.minecraft.world.level.block.state.BlockState;
import org.skriptlang.skript.fabric.runtime.SkriptFabricEventBridge;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ServerPlayerGameMode.class)
abstract class ServerPlayerGameModeBlockDropMixin {

    @Shadow
    protected ServerPlayer player;

    @Unique
    private static final ThreadLocal<BlockState> SKRIPT_PREVIOUS_STATE = new ThreadLocal<>();

    @Unique
    private static final ThreadLocal<BlockPos> SKRIPT_PREVIOUS_POS = new ThreadLocal<>();

    @Inject(method = "destroyBlock", at = @At("HEAD"))
    private void skript$captureDroppedBlock(BlockPos pos, CallbackInfoReturnable<Boolean> cir) {
        ServerLevel level = (ServerLevel) player.level();
        SKRIPT_PREVIOUS_STATE.set(level.getBlockState(pos));
        SKRIPT_PREVIOUS_POS.set(pos.immutable());
    }

    @Inject(
            method = "destroyBlock",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/level/block/Block;playerDestroy(Lnet/minecraft/world/level/Level;Lnet/minecraft/world/entity/player/Player;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/world/level/block/entity/BlockEntity;Lnet/minecraft/world/item/ItemStack;)V",
                    shift = At.Shift.AFTER
            )
    )
    private void skript$dispatchBlockDrop(BlockPos pos, CallbackInfoReturnable<Boolean> cir) {
        BlockState previous = SKRIPT_PREVIOUS_STATE.get();
        BlockPos previousPos = SKRIPT_PREVIOUS_POS.get();
        if (previous == null || previousPos == null || !previousPos.equals(pos) || previous.isAir()) {
            return;
        }
        SkriptFabricEventBridge.dispatchBlockDrop((ServerLevel) player.level(), previousPos, previous, player);
    }

    @Inject(method = "destroyBlock", at = @At("RETURN"))
    private void skript$clearDroppedBlockCapture(BlockPos pos, CallbackInfoReturnable<Boolean> cir) {
        SKRIPT_PREVIOUS_STATE.remove();
        SKRIPT_PREVIOUS_POS.remove();
    }
}
