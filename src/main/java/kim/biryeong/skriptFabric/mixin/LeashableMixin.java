package kim.biryeong.skriptFabric.mixin;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Leashable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.skriptlang.skript.fabric.runtime.LeashCaptureState;
import org.skriptlang.skript.fabric.runtime.SkriptFabricEventBridge;

@Mixin(net.minecraft.world.entity.Leashable.class)
public interface LeashableMixin {

    @Inject(method = "setLeashedTo(Lnet/minecraft/world/entity/Entity;Lnet/minecraft/world/entity/Entity;Z)V", at = @At("HEAD"))
    private static void skript$capturePreviousLeashHolder(Entity entity, Entity leashHolder, boolean sendPacket, CallbackInfo ci) {
        Entity previous = entity instanceof Leashable leashable ? leashable.getLeashHolder() : null;
        LeashCaptureState.previousLeashHolder().set(previous);
    }

    @Inject(method = "setLeashedTo(Lnet/minecraft/world/entity/Entity;Lnet/minecraft/world/entity/Entity;Z)V", at = @At("TAIL"))
    private static void skript$dispatchLeash(Entity entity, Entity leashHolder, boolean sendPacket, CallbackInfo ci) {
        try {
            if (!(entity.level() instanceof ServerLevel serverLevel)) {
                return;
            }
            if (!(entity instanceof Leashable leashable)) {
                return;
            }
            Entity previous = LeashCaptureState.previousLeashHolder().get();
            Entity current = leashable.getLeashHolder();
            if (current != null && current != previous) {
                SkriptFabricEventBridge.dispatchEntityLeash(serverLevel, entity, current);
            }
        } finally {
            LeashCaptureState.previousLeashHolder().remove();
        }
    }

    @Inject(method = "dropLeash(Lnet/minecraft/world/entity/Entity;ZZ)V", at = @At("HEAD"))
    private static void skript$dispatchUnleash(Entity entity, boolean sendPacket, boolean dropLeash, CallbackInfo ci) {
        if (entity.level() instanceof ServerLevel serverLevel) {
            Entity leashHolder = entity instanceof Leashable leashable ? leashable.getLeashHolder() : null;
            SkriptFabricEventBridge.dispatchEntityUnleash(serverLevel, entity, leashHolder, dropLeash);
        }
    }
}
