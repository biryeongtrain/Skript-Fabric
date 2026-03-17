package kim.biryeong.skriptFabric.mixin;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import org.skriptlang.skript.fabric.runtime.SkriptFabricEventBridge;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Entity.class)
abstract class EntitySwimMixin {

    @Inject(method = "setSwimming", at = @At("HEAD"))
    private void skript$dispatchSwimToggle(boolean swimming, CallbackInfo ci) {
        Entity entity = (Entity) (Object) this;
        if (entity.isSwimming() != swimming && entity.level() instanceof ServerLevel) {
            SkriptFabricEventBridge.dispatchSwimToggle(entity, swimming);
        }
    }
}
