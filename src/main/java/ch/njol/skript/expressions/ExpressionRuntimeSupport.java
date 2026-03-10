package ch.njol.skript.expressions;

import com.mojang.authlib.GameProfile;
import java.nio.file.Path;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.storage.LevelResource;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.lang.event.SkriptEvent;

final class ExpressionRuntimeSupport {

    private ExpressionRuntimeSupport() {
    }

    static @Nullable MinecraftServer resolveServer(@Nullable SkriptEvent event) {
        if (event == null) {
            return null;
        }
        if (event.server() != null) {
            return event.server();
        }
        if (event.player() != null && event.player().getServer() != null) {
            return event.player().getServer();
        }
        if (event.level() != null) {
            return event.level().getServer();
        }
        return null;
    }

    static @Nullable Path playerDataFile(@Nullable SkriptEvent event, GameProfile profile) {
        MinecraftServer server = resolveServer(event);
        if (server == null || profile.getId() == null) {
            return null;
        }
        return server.getWorldPath(LevelResource.PLAYER_DATA_DIR).resolve(profile.getId() + ".dat");
    }
}
