package kim.biryeong.skriptFabric.mixin;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import org.skriptlang.skript.fabric.runtime.SkriptFabricEventBridge;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Entity.class)
abstract class EntityIgniteMixin {

    @Inject(method = "igniteForSeconds", at = @At("HEAD"))
    private void skript$dispatchCombust(float seconds, CallbackInfo ci) {
        Entity entity = (Entity) (Object) this;
        if (seconds > 0 && entity.getRemainingFireTicks() <= 0 && entity.level() instanceof ServerLevel level) {
            SkriptFabricEventBridge.dispatchCombust(level, entity, (int) (seconds * 20));
        }
    }
}
