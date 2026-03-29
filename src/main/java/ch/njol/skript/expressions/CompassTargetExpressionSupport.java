package ch.njol.skript.expressions;

import java.util.Collections;
import java.util.Map;
import java.util.WeakHashMap;
import net.minecraft.core.BlockPos;
import net.minecraft.network.protocol.game.ClientboundSetDefaultSpawnPositionPacket;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.fabric.compat.FabricLocation;

final class CompassTargetExpressionSupport {

    private static final Map<Object, FabricLocation> TARGETS = Collections.synchronizedMap(new WeakHashMap<>());

    private CompassTargetExpressionSupport() {
    }

    static @Nullable FabricLocation get(Object player) {
        return TARGETS.get(player);
    }

    static void set(Object player, FabricLocation target) {
        TARGETS.put(player, target);
    }

    static void clear(Object player) {
        TARGETS.remove(player);
    }

    static FabricLocation defaultTarget(ServerPlayer player) {
        net.minecraft.world.level.storage.LevelData.RespawnData respawnData = player.level().getRespawnData();
        BlockPos spawnPos = respawnData != null ? respawnData.pos() : BlockPos.ZERO;
        return new FabricLocation(player.level(), spawnPos.getCenter());
    }

    static void sync(ServerPlayer player, FabricLocation target) {
        BlockPos position = BlockPos.containing(target.position());
        net.minecraft.world.level.storage.LevelData.RespawnData respawnData =
                net.minecraft.world.level.storage.LevelData.RespawnData.of(player.level().dimension(), position, 0.0F, 0.0F);
        player.connection.send(new ClientboundSetDefaultSpawnPositionPacket(respawnData));
    }
}
