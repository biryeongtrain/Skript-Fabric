package kim.biryeong.skriptFabric.mixin;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.projectile.FishingHook;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.fabric.runtime.FabricFishingEventState;
import org.skriptlang.skript.fabric.runtime.SkriptFabricEventBridge;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(FishingHook.class)
abstract class FishingHookMixin {

    @Shadow private boolean biting;
    @Shadow private int nibble;
    @Shadow private int timeUntilLured;
    @Shadow @Final private int lureSpeed;
    @Shadow @Nullable public abstract Player getPlayerOwner();
    @Shadow @Nullable private Entity hookedIn;

    @Unique
    private int skript$previousTimeUntilLured;

    @Unique
    private int skript$previousNibble;

    @Unique
    private boolean skript$previousBiting;

    @Inject(method = "<init>(Lnet/minecraft/world/entity/player/Player;Lnet/minecraft/world/level/Level;II)V", at = @At("TAIL"))
    private void skript$dispatchFishing(Player player, Level level, int luck, int lureSpeed, CallbackInfo callbackInfo) {
        if (level instanceof ServerLevel serverLevel && player instanceof ServerPlayer serverPlayer) {
            SkriptFabricEventBridge.dispatchFishing(
                    serverLevel,
                    serverPlayer,
                    (FishingHook) (Object) this,
                    null,
                    lureSpeed > 0,
                    FabricFishingEventState.FISHING
            );
        }
    }

    @Inject(method = "onHitEntity", at = @At("TAIL"))
    private void skript$dispatchCaughtEntity(EntityHitResult hitResult, CallbackInfo callbackInfo) {
        skript$dispatch(FabricFishingEventState.CAUGHT_ENTITY, hitResult.getEntity());
    }

    @Inject(method = "onHitBlock", at = @At("TAIL"))
    private void skript$dispatchInGround(BlockHitResult hitResult, CallbackInfo callbackInfo) {
        skript$dispatch(FabricFishingEventState.IN_GROUND, null);
    }

    @Inject(method = "catchingFish", at = @At("HEAD"))
    private void skript$captureFishingState(BlockPos pos, CallbackInfo callbackInfo) {
        skript$previousTimeUntilLured = timeUntilLured;
        skript$previousNibble = nibble;
        skript$previousBiting = biting;
    }

    @Inject(method = "catchingFish", at = @At("RETURN"))
    private void skript$dispatchCatchingFishState(BlockPos pos, CallbackInfo callbackInfo) {
        if (skript$previousTimeUntilLured <= 0 && timeUntilLured > 0) {
            skript$dispatch(FabricFishingEventState.LURED, null);
        }
        if (!skript$previousBiting && biting) {
            skript$dispatch(FabricFishingEventState.BITE, null);
        }
        if (skript$previousNibble > 0 && nibble == 0 && !biting) {
            skript$dispatch(FabricFishingEventState.FISH_ESCAPE, null);
        }
    }

    @Inject(method = "retrieve", at = @At("RETURN"))
    private void skript$dispatchRetrieve(ItemStack stack, CallbackInfoReturnable<Integer> callbackInfo) {
        FishingHook hook = (FishingHook) (Object) this;
        if (!(hook.level() instanceof ServerLevel serverLevel)) {
            return;
        }
        int result = callbackInfo.getReturnValueI();
        if (result == 1) {
            skript$dispatch(
                    FabricFishingEventState.CAUGHT_FISH,
                    SkriptFabricEventBridge.findNearestCaughtFishEntity(serverLevel, hook)
            );
            return;
        }
        if (result == 0 && hookedIn == null && nibble <= 0 && !hook.onGround()) {
            skript$dispatch(FabricFishingEventState.REEL_IN, null);
        }
    }

    @Unique
    private void skript$dispatch(FabricFishingEventState state, @Nullable Entity eventEntity) {
        FishingHook hook = (FishingHook) (Object) this;
        if (!(hook.level() instanceof ServerLevel serverLevel)) {
            return;
        }
        Player owner = getPlayerOwner();
        if (!(owner instanceof ServerPlayer serverPlayer)) {
            return;
        }
        SkriptFabricEventBridge.dispatchFishing(
                serverLevel,
                serverPlayer,
                hook,
                eventEntity,
                lureSpeed > 0,
                state
        );
    }
}
