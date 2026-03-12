package kim.biryeong.skriptFabric.mixin;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.portal.TeleportTransition;
import net.minecraft.world.phys.Vec3;
import org.skriptlang.skript.fabric.runtime.SkriptFabricEventBridge;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Entity.class)
abstract class EntityTeleportMixin {

    @Unique
    private ServerLevel skript$teleportFromLevel;

    @Unique
    private Vec3 skript$teleportFromPosition;

    @Inject(method = "teleportTo(DDD)V", at = @At("HEAD"))
    private void skript$captureTeleport(double x, double y, double z, CallbackInfo callbackInfo) {
        Entity self = (Entity) (Object) this;
        if (self.level() instanceof ServerLevel serverLevel) {
            skript$teleportFromLevel = serverLevel;
            skript$teleportFromPosition = self.position();
        }
    }

    @Inject(method = "teleportTo(DDD)V", at = @At("TAIL"))
    private void skript$dispatchTeleport(double x, double y, double z, CallbackInfo callbackInfo) {
        Entity self = (Entity) (Object) this;
        ServerLevel fromLevel = skript$teleportFromLevel;
        Vec3 fromPosition = skript$teleportFromPosition;
        skript$teleportFromLevel = null;
        skript$teleportFromPosition = null;
        if (fromLevel == null || fromPosition == null || self.level() != fromLevel) {
            return;
        }
        Vec3 toPosition = self.position();
        if (fromPosition.equals(toPosition)) {
            return;
        }
        SkriptFabricEventBridge.dispatchTeleport(self, fromLevel, fromPosition, toPosition);
    }

    @Redirect(
            method = "handlePortal",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/entity/Entity;teleport(Lnet/minecraft/world/level/portal/TeleportTransition;)Lnet/minecraft/world/entity/Entity;"
            )
    )
    private Entity skript$dispatchPortal(Entity entity, TeleportTransition transition) {
        SkriptFabricEventBridge.dispatchPortal(entity);
        return entity.teleport(transition);
    }
}
