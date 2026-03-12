package kim.biryeong.skriptFabric.mixin;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.decoration.ItemFrame;
import org.skriptlang.skript.fabric.runtime.SkriptFabricEventBridge;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ItemFrame.class)
abstract class ItemFrameMixin {

    @Inject(
            method = "hurtServer(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/damagesource/DamageSource;F)Z",
            at = @At("RETURN")
    )
    private void skript$dispatchHangingBreak(
            ServerLevel level,
            net.minecraft.world.damagesource.DamageSource damageSource,
            float amount,
            CallbackInfoReturnable<Boolean> cir
    ) {
        ItemFrame itemFrame = (ItemFrame) (Object) this;
        if (cir.getReturnValueZ() && !itemFrame.isAlive()) {
            Entity remover = damageSource.getEntity();
            SkriptFabricEventBridge.dispatchHangingBreak(level, itemFrame, remover);
        }
    }
}
