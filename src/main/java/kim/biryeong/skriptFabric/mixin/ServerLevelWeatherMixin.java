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

    @Inject(method = "advanceWeatherCycle", at = @At("HEAD"))
    private void skript$captureWeather(CallbackInfo callbackInfo) {
        ServerLevel self = (ServerLevel) (Object) this;
        skript$previousRain = self.isRaining();
        skript$previousThunder = self.isThundering();
    }

    @Inject(method = "advanceWeatherCycle", at = @At("TAIL"))
    private void skript$dispatchWeather(CallbackInfo callbackInfo) {
        ServerLevel self = (ServerLevel) (Object) this;
        boolean currentRain = self.isRaining();
        boolean currentThunder = self.isThundering();
        if (skript$previousRain == currentRain && skript$previousThunder == currentThunder) {
            return;
        }
        SkriptFabricEventBridge.dispatchWeatherChange(self, currentRain, currentThunder);
    }

    @Inject(method = "resetWeatherCycle", at = @At("HEAD"))
    private void skript$captureWeatherBeforeReset(CallbackInfo callbackInfo) {
        ServerLevel self = (ServerLevel) (Object) this;
        skript$previousRain = self.isRaining();
        skript$previousThunder = self.isThundering();
    }

    @Inject(method = "resetWeatherCycle", at = @At("TAIL"))
    private void skript$dispatchWeatherAfterReset(CallbackInfo callbackInfo) {
        ServerLevel self = (ServerLevel) (Object) this;
        boolean currentRain = self.isRaining();
        boolean currentThunder = self.isThundering();
        if (skript$previousRain == currentRain && skript$previousThunder == currentThunder) {
            return;
        }
        SkriptFabricEventBridge.dispatchWeatherChange(self, currentRain, currentThunder);
    }
}
