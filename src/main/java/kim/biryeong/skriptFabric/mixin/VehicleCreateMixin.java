package kim.biryeong.skriptFabric.mixin;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.vehicle.boat.AbstractBoat;
import net.minecraft.world.entity.vehicle.minecart.AbstractMinecart;
import org.skriptlang.skript.fabric.runtime.SkriptFabricEventBridge;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ServerLevel.class)
abstract class VehicleCreateMixin {

    @Inject(method = "addFreshEntity", at = @At("RETURN"))
    private void skript$dispatchVehicleCreate(Entity entity, CallbackInfoReturnable<Boolean> cir) {
        if (!cir.getReturnValue()) return;
        if (entity instanceof AbstractBoat || entity instanceof AbstractMinecart) {
            ServerLevel level = (ServerLevel) (Object) this;
            SkriptFabricEventBridge.dispatchVehicleCreate(level, entity);
        }
    }
}
