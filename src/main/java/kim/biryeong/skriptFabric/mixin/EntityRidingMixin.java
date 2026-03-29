package kim.biryeong.skriptFabric.mixin;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import org.skriptlang.skript.fabric.runtime.SkriptFabricEventBridge;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Entity.class)
abstract class EntityRidingMixin {

    @Inject(method = "startRiding(Lnet/minecraft/world/entity/Entity;ZZ)Z", at = @At("RETURN"))
    private void skript$dispatchMount(Entity vehicle, boolean force, boolean suppressEvents, CallbackInfoReturnable<Boolean> cir) {
        if (!cir.getReturnValue()) return;
        Entity rider = (Entity) (Object) this;
        if (rider.level() instanceof ServerLevel level) {
            SkriptFabricEventBridge.dispatchEntityMount(level, rider, vehicle);
            SkriptFabricEventBridge.dispatchVehicleEnter(level, vehicle, rider);
        }
    }

    @Inject(method = "removeVehicle", at = @At("HEAD"))
    private void skript$dispatchDismount(CallbackInfo ci) {
        Entity rider = (Entity) (Object) this;
        Entity vehicle = rider.getVehicle();
        if (vehicle != null && rider.level() instanceof ServerLevel level) {
            SkriptFabricEventBridge.dispatchEntityDismount(level, rider, vehicle);
            SkriptFabricEventBridge.dispatchVehicleExit(level, vehicle, rider);
        }
    }
}
