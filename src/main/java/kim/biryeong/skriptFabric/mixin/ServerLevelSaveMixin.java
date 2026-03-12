package kim.biryeong.skriptFabric.mixin;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.ProgressListener;
import org.skriptlang.skript.fabric.runtime.SkriptFabricEventBridge;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerLevel.class)
abstract class ServerLevelSaveMixin {

    @Inject(method = "save", at = @At("TAIL"))
    private void skript$dispatchWorldSave(ProgressListener progressListener, boolean flush, boolean skipSave, CallbackInfo callbackInfo) {
        SkriptFabricEventBridge.dispatchWorldSave((ServerLevel) (Object) this);
    }
}
