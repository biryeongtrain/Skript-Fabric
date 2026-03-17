package kim.biryeong.skriptFabric.mixin;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.ambient.Bat;
import org.skriptlang.skript.fabric.runtime.SkriptFabricEventBridge;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Bat.class)
abstract class BatMixin {

    @Inject(method = "setResting", at = @At("HEAD"))
    private void skript$dispatchBatSleep(boolean resting, CallbackInfo ci) {
        Bat bat = (Bat) (Object) this;
        if (bat.isResting() != resting && bat.level() instanceof ServerLevel level) {
            SkriptFabricEventBridge.dispatchBatToggleSleep(level, bat, resting);
        }
    }
}
