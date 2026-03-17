package kim.biryeong.skriptFabric.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import org.skriptlang.skript.fabric.runtime.SkriptFabricEventBridge;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerLevel.class)
abstract class SpawnChangeMixin {

    @Inject(method = "setDefaultSpawnPos", at = @At("HEAD"))
    private void skript$dispatchSpawnChange(BlockPos pos, float angle, CallbackInfo ci) {
        ServerLevel level = (ServerLevel) (Object) this;
        SkriptFabricEventBridge.dispatchSpawnChange(level, pos);
    }
}
