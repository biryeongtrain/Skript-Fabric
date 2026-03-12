package kim.biryeong.skriptFabric.mixin;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.fabric.runtime.SkriptFabricEventBridge;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Entity.class)
abstract class EntitySpawnAtLocationMixin {

    @Inject(
            method = "spawnAtLocation(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/item/ItemStack;)Lnet/minecraft/world/entity/item/ItemEntity;",
            at = @At("HEAD"),
            cancellable = true
    )
    private void skript$captureDeathDrop(ServerLevel level, ItemStack stack, CallbackInfoReturnable<@Nullable ItemEntity> callbackInfo) {
        if (SkriptFabricEventBridge.captureDeathDrop((Entity) (Object) this, stack)) {
            callbackInfo.setReturnValue(null);
        }
    }

    @Inject(
            method = "spawnAtLocation(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/phys/Vec3;)Lnet/minecraft/world/entity/item/ItemEntity;",
            at = @At("HEAD"),
            cancellable = true
    )
    private void skript$captureDeathOffsetDrop(
            ServerLevel level,
            ItemStack stack,
            Vec3 offset,
            CallbackInfoReturnable<@Nullable ItemEntity> callbackInfo
    ) {
        if (SkriptFabricEventBridge.captureDeathDrop((Entity) (Object) this, stack)) {
            callbackInfo.setReturnValue(null);
        }
    }

    @Inject(
            method = "spawnAtLocation(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/item/ItemStack;F)Lnet/minecraft/world/entity/item/ItemEntity;",
            at = @At("HEAD"),
            cancellable = true
    )
    private void skript$captureDeathYOffsetDrop(
            ServerLevel level,
            ItemStack stack,
            float yOffset,
            CallbackInfoReturnable<@Nullable ItemEntity> callbackInfo
    ) {
        if (SkriptFabricEventBridge.captureDeathDrop((Entity) (Object) this, stack)) {
            callbackInfo.setReturnValue(null);
        }
    }
}
