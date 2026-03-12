package kim.biryeong.skriptFabric.mixin;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.PlayerList;
import net.minecraft.world.entity.Entity;
import org.skriptlang.skript.fabric.runtime.SkriptFabricEventBridge;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(PlayerList.class)
abstract class PlayerListRespawnMixin {

    @Redirect(
            method = "respawn",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/server/level/ServerPlayer;snapTo(DDDFF)V"
            )
    )
    private void skript$dispatchRespawn(
            ServerPlayer player,
            double x,
            double y,
            double z,
            float yaw,
            float pitch,
            ServerPlayer previousPlayer,
            boolean alive,
            Entity.RemovalReason removalReason
    ) {
        player.snapTo(x, y, z, yaw, pitch);
        SkriptFabricEventBridge.dispatchRespawn(previousPlayer, player, alive, removalReason);
    }
}
