package kim.biryeong.skriptFabric.mixin;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.skriptlang.skript.fabric.runtime.SkriptFabricEventBridge;

@Mixin(net.minecraft.world.entity.Leashable.class)
public interface LeashableMixin {

    @Inject(method = "dropLeash(Lnet/minecraft/world/entity/Entity;ZZ)V", at = @At("HEAD"))
    private static void skript$dispatchUnleash(Entity entity, boolean sendPacket, boolean dropLeash, CallbackInfo ci) {
        if (entity.level() instanceof ServerLevel serverLevel) {
            SkriptFabricEventBridge.dispatchEntityUnleash(serverLevel, null, dropLeash);
        }
    }
}
