package kim.biryeong.skriptFabric.mixin;

import net.minecraft.server.level.ServerLevel;
import org.skriptlang.skript.fabric.runtime.SkriptFabricEventBridge;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerLevel.class)
abstract class ServerLevelWeatherMixin {

    @Unique
    private boolean skript$previousRain;

    @Unique
    private boolean skript$previousThunder;

    @Inject(method = "setWeatherParameters", at = @At("HEAD"))
    private void skript$captureWeather(int clearDuration, int rainDuration, boolean raining, boolean thundering, CallbackInfo callbackInfo) {
        ServerLevel self = (ServerLevel) (Object) this;
        skript$previousRain = self.isRaining();
        skript$previousThunder = self.isThundering();
    }

    @Inject(method = "setWeatherParameters", at = @At("TAIL"))
    private void skript$dispatchWeather(int clearDuration, int rainDuration, boolean raining, boolean thundering, CallbackInfo callbackInfo) {
        ServerLevel self = (ServerLevel) (Object) this;
        boolean currentRain = self.isRaining();
        boolean currentThunder = self.isThundering();
        if (skript$previousRain == currentRain && skript$previousThunder == currentThunder) {
            return;
        }
        SkriptFabricEventBridge.dispatchWeatherChange(self, currentRain, currentThunder);
    }
}
