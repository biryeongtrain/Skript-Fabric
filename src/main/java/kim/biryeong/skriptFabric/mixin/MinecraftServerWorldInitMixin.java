package kim.biryeong.skriptFabric.mixin;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import org.skriptlang.skript.fabric.runtime.SkriptFabricEventBridge;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MinecraftServer.class)
abstract class MinecraftServerWorldInitMixin {

    @Shadow
    public abstract Iterable<ServerLevel> getAllLevels();

    @Inject(
            method = "createLevels()V",
            at = @At("TAIL")
    )
    private void skript$dispatchWorldInitialization(CallbackInfo callbackInfo) {
        for (ServerLevel level : getAllLevels()) {
            SkriptFabricEventBridge.dispatchWorldInit((MinecraftServer) (Object) this, level);
        }
    }
}
