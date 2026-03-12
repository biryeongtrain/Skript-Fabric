package kim.biryeong.skriptFabric.mixin;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.vehicle.AbstractMinecart;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.skriptlang.skript.fabric.runtime.SkriptFabricEventBridge;

@Mixin(AbstractMinecart.class)
abstract class AbstractMinecartCollisionMixin {

    @Inject(
            method = "push",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/entity/Entity;push(DDD)V",
                    shift = At.Shift.AFTER
            )
    )
    private void skript$dispatchEntityCollision(Entity entity, CallbackInfo ci) {
        AbstractMinecart minecart = (AbstractMinecart) (Object) this;
        if (!(minecart.level() instanceof ServerLevel serverLevel) || entity instanceof AbstractMinecart) {
            return;
        }
        SkriptFabricEventBridge.dispatchVehicleCollision(
                serverLevel,
                minecart.blockPosition(),
                minecart,
                null,
                entity
        );
    }
}
