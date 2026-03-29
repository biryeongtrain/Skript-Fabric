package kim.biryeong.skriptFabric.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.SweetBerryBushBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.skriptlang.skript.fabric.runtime.SkriptFabricEventBridge;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(SweetBerryBushBlock.class)
abstract class SweetBerryBushBlockMixin {

    @Inject(
            method = "useWithoutItem",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/level/block/Block;dropFromBlockInteractLootTable(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/resources/ResourceKey;Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/world/level/block/entity/BlockEntity;Lnet/minecraft/world/item/ItemInstance;Lnet/minecraft/world/entity/Entity;Ljava/util/function/BiConsumer;)Z"
            )
    )
    private void skript$dispatchHarvestBlock(
            BlockState state,
            Level level,
            BlockPos pos,
            Player player,
            BlockHitResult hitResult,
            CallbackInfoReturnable<InteractionResult> cir
    ) {
        if (!(level instanceof ServerLevel serverLevel)) {
            return;
        }
        SkriptFabricEventBridge.dispatchHarvestBlock(
                serverLevel,
                state,
                player instanceof ServerPlayer serverPlayer ? serverPlayer : null
        );
    }
}
