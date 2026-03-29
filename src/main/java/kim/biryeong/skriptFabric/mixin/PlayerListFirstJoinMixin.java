package kim.biryeong.skriptFabric.mixin;

import net.minecraft.network.Connection;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.CommonListenerCookie;
import net.minecraft.server.players.PlayerList;
import net.minecraft.world.level.storage.LevelResource;
import org.skriptlang.skript.fabric.runtime.SkriptFabricEventBridge;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerList.class)
abstract class PlayerListFirstJoinMixin {

    @Unique
    private static final ThreadLocal<Boolean> SKRIPT_FIRST_JOIN = ThreadLocal.withInitial(() -> false);

    @Inject(method = "placeNewPlayer", at = @At("HEAD"))
    private void skript$captureFirstJoin(Connection connection, ServerPlayer player, CommonListenerCookie cookie, CallbackInfo ci) {
        SKRIPT_FIRST_JOIN.set(SkriptFabricEventBridge.isFirstJoin(
                player.level().getServer().getWorldPath(LevelResource.PLAYER_DATA_DIR),
                player.getGameProfile()
        ));
    }

    @Inject(method = "placeNewPlayer", at = @At("RETURN"))
    private void skript$dispatchFirstJoin(Connection connection, ServerPlayer player, CommonListenerCookie cookie, CallbackInfo ci) {
        boolean firstJoin = SKRIPT_FIRST_JOIN.get();
        SKRIPT_FIRST_JOIN.remove();
        if (firstJoin) {
            SkriptFabricEventBridge.dispatchFirstJoin(player, true);
        }
    }
}
