package kim.biryeong.skriptFabric.mixin;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerPlayerGameMode;
import net.minecraft.world.level.GameType;
import org.skriptlang.skript.fabric.runtime.SkriptFabricEventBridge;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ServerPlayerGameMode.class)
abstract class ServerPlayerGameModeChangeMixin {

    @Shadow
    protected ServerPlayer player;

    @Unique
    private GameType skript$previousGameMode;

    @Inject(method = "changeGameModeForPlayer", at = @At("HEAD"))
    private void skript$capturePreviousGameMode(GameType gameType, CallbackInfoReturnable<Boolean> cir) {
        ServerPlayerGameMode self = (ServerPlayerGameMode) (Object) this;
        skript$previousGameMode = self.getGameModeForPlayer();
    }

    @Inject(method = "changeGameModeForPlayer", at = @At("TAIL"))
    private void skript$dispatchGameModeChange(GameType gameType, CallbackInfoReturnable<Boolean> cir) {
        GameType previousGameMode = skript$previousGameMode;
        skript$previousGameMode = null;
        if (!cir.getReturnValueZ()) {
            return;
        }
        ServerPlayerGameMode self = (ServerPlayerGameMode) (Object) this;
        GameType currentGameMode = self.getGameModeForPlayer();
        if (currentGameMode == previousGameMode) {
            return;
        }
        SkriptFabricEventBridge.dispatchGameMode(player, currentGameMode);
    }
}
