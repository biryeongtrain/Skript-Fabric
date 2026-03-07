package kim.biryeong.skriptFabric.mixin;

import java.util.Optional;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.Bucketable;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.skriptlang.skript.fabric.runtime.SkriptFabricEventBridge;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(Bucketable.class)
interface BucketableMixin {

    @Inject(
            method = "bucketMobPickup",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/entity/player/Player;setItemInHand(Lnet/minecraft/world/InteractionHand;Lnet/minecraft/world/item/ItemStack;)V"
            ),
            locals = LocalCapture.CAPTURE_FAILHARD
    )
    private static void skript$dispatchBucketCatch(
            Player player,
            InteractionHand hand,
            LivingEntity entity,
            CallbackInfoReturnable<Optional<InteractionResult>> callbackInfo,
            ItemStack originalBucket,
            ItemStack entityBucket,
            ItemStack filledResult
    ) {
        if (player instanceof ServerPlayer serverPlayer && entity.level() instanceof ServerLevel serverLevel) {
            SkriptFabricEventBridge.dispatchBucketCatch(
                    serverLevel,
                    serverPlayer,
                    entity,
                    originalBucket.copy(),
                    entityBucket.copy()
            );
        }
    }
}
