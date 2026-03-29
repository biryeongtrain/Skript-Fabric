package kim.biryeong.skriptFabric.mixin;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.storage.LevelData;
import org.skriptlang.skript.fabric.runtime.SkriptFabricEventBridge;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerLevel.class)
abstract class SpawnChangeMixin {

    @Inject(method = "setRespawnData", at = @At("HEAD"))
    private void skript$dispatchSpawnChange(LevelData.RespawnData respawnData, CallbackInfo ci) {
        ServerLevel level = (ServerLevel) (Object) this;
        SkriptFabricEventBridge.dispatchSpawnChange(level, respawnData.pos());
    }
}
