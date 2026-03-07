package kim.biryeong.skriptFabric.mixin;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.fabric.compat.FabricBreedingItemSource;
import org.skriptlang.skript.fabric.runtime.SkriptFabricEventBridge;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.Unique;

@Mixin(Animal.class)
abstract class AnimalMixin implements FabricBreedingItemSource {

    @Unique
    private boolean skript$settingLoveCause;

    @Unique
    private boolean skript$wasInLoveBeforeCauseSet;

    @Unique
    private boolean skript$wasInLoveBeforeTimeSet;

    @Unique
    private ItemStack skript$lastLoveItem = ItemStack.EMPTY;

    @Unique
    private ItemStack skript$pendingLoveItem = ItemStack.EMPTY;

    @Inject(method = "mobInteract", at = @At("HEAD"))
    private void skript$capturePendingLoveItem(Player player, InteractionHand hand, CallbackInfoReturnable<InteractionResult> callbackInfo) {
        ItemStack held = player.getItemInHand(hand);
        skript$pendingLoveItem = held.isEmpty() ? ItemStack.EMPTY : held.copyWithCount(1);
    }

    @Inject(method = "mobInteract", at = @At("TAIL"))
    private void skript$clearPendingLoveItem(Player player, InteractionHand hand, CallbackInfoReturnable<InteractionResult> callbackInfo) {
        skript$pendingLoveItem = ItemStack.EMPTY;
    }

    @Inject(method = "finalizeSpawnChildFromBreeding", at = @At("HEAD"))
    private void skript$dispatchBreeding(ServerLevel level, Animal partner, AgeableMob offspring, CallbackInfo callbackInfo) {
        if (offspring == null) {
            return;
        }
        Animal mother = (Animal) (Object) this;
        Player breeder = mother.getLoveCause();
        if (breeder == null) {
            breeder = partner.getLoveCause();
        }
        SkriptFabricEventBridge.dispatchBreeding(level, mother, partner, offspring, breeder);
    }

    @Inject(method = "setInLove", at = @At("HEAD"))
    private void skript$captureLoveModeCause(@Nullable Player player, CallbackInfo callbackInfo) {
        Animal animal = (Animal) (Object) this;
        skript$settingLoveCause = true;
        skript$wasInLoveBeforeCauseSet = animal.isInLove();
        skript$lastLoveItem = skript$resolveLoveItem(player);
    }

    @Inject(method = "setInLove", at = @At("TAIL"))
    private void skript$dispatchLoveModeEnter(@Nullable Player player, CallbackInfo callbackInfo) {
        try {
            Animal animal = (Animal) (Object) this;
            if (skript$wasInLoveBeforeCauseSet || !animal.isInLove() || !(animal.level() instanceof ServerLevel serverLevel)) {
                return;
            }
            SkriptFabricEventBridge.dispatchLoveModeEnter(
                    serverLevel,
                    animal,
                    player instanceof ServerPlayer serverPlayer ? serverPlayer : null
            );
        } finally {
            skript$settingLoveCause = false;
        }
    }

    @Inject(method = "setInLoveTime", at = @At("HEAD"))
    private void skript$captureLoveModeTime(int loveTime, CallbackInfo callbackInfo) {
        Animal animal = (Animal) (Object) this;
        skript$wasInLoveBeforeTimeSet = animal.isInLove();
    }

    @Inject(method = "setInLoveTime", at = @At("TAIL"))
    private void skript$dispatchLoveModeTimeEnter(int loveTime, CallbackInfo callbackInfo) {
        Animal animal = (Animal) (Object) this;
        if (skript$settingLoveCause || skript$wasInLoveBeforeTimeSet || !animal.isInLove() || !(animal.level() instanceof ServerLevel serverLevel)) {
            return;
        }
        SkriptFabricEventBridge.dispatchLoveModeEnter(serverLevel, animal, null);
    }

    @Unique
    private ItemStack skript$resolveLoveItem(@Nullable Player player) {
        if (!skript$pendingLoveItem.isEmpty()) {
            return skript$pendingLoveItem.copy();
        }
        if (player == null) {
            return ItemStack.EMPTY;
        }
        ItemStack mainHand = player.getMainHandItem();
        if (!mainHand.isEmpty()) {
            return mainHand.copyWithCount(1);
        }
        ItemStack offHand = player.getOffhandItem();
        return offHand.isEmpty() ? ItemStack.EMPTY : offHand.copyWithCount(1);
    }

    @Override
    public ItemStack skript$getLastLoveItem() {
        return skript$lastLoveItem.copy();
    }
}
