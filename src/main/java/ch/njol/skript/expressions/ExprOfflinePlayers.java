package ch.njol.skript.expressions;

import ch.njol.skript.Skript;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.util.Kleenean;
import com.mojang.authlib.GameProfile;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.storage.LevelResource;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.lang.event.SkriptEvent;

public class ExprOfflinePlayers extends SimpleExpression<GameProfile> {

    static {
        Skript.registerExpression(ExprOfflinePlayers.class, GameProfile.class, "[(all [[of] the]|the)] offline[ ]players");
    }

    @Override
    public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
        return true;
    }

    @Override
    protected GameProfile @Nullable [] get(SkriptEvent event) {
        MinecraftServer server = ExpressionRuntimeSupport.resolveServer(event);
        if (server == null) {
            return new GameProfile[0];
        }
        return resolveOfflinePlayers(
                server.getWorldPath(LevelResource.PLAYER_DATA_DIR),
                currentPlayers(server),
                uuid -> profile(server, uuid)
        );
    }

    static GameProfile[] resolveOfflinePlayers(
            Path playerDataDirectory,
            Iterable<GameProfile> onlinePlayers,
            java.util.function.Function<UUID, @Nullable GameProfile> resolver
    ) {
        Map<UUID, GameProfile> profiles = new LinkedHashMap<>();
        for (GameProfile onlinePlayer : onlinePlayers) {
            if (onlinePlayer.getId() != null) {
                profiles.put(onlinePlayer.getId(), onlinePlayer);
            }
        }
        if (!Files.isDirectory(playerDataDirectory)) {
            return profiles.values().toArray(GameProfile[]::new);
        }
        try (var stream = Files.list(playerDataDirectory)) {
            stream.filter(path -> path.getFileName().toString().endsWith(".dat"))
                    .map(path -> parseUuid(path.getFileName().toString()))
                    .filter(java.util.Objects::nonNull)
                    .forEach(uuid -> profiles.computeIfAbsent(uuid, ignored -> {
                        GameProfile resolved = resolver.apply(uuid);
                        return resolved != null ? resolved : new GameProfile(uuid, uuid.toString());
                    }));
        } catch (IOException exception) {
            throw new IllegalStateException("Unable to enumerate offline players from " + playerDataDirectory, exception);
        }
        return profiles.values().toArray(GameProfile[]::new);
    }

    private static List<GameProfile> currentPlayers(MinecraftServer server) {
        List<GameProfile> profiles = new ArrayList<>();
        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            profiles.add(player.getGameProfile());
        }
        return profiles;
    }

    private static @Nullable UUID parseUuid(String fileName) {
        String base = fileName.substring(0, fileName.length() - 4);
        try {
            return UUID.fromString(base);
        } catch (IllegalArgumentException ignored) {
            return null;
        }
    }

    private static @Nullable GameProfile profile(MinecraftServer server, UUID uuid) {
        ServerPlayer online = server.getPlayerList().getPlayer(uuid);
        if (online != null) {
            return online.getGameProfile();
        }
        Object cache = ExpressionHandleSupport.invoke(server, "getProfileCache");
        Object lookedUp = ExpressionHandleSupport.invoke(cache, "get", uuid);
        if (lookedUp instanceof Optional<?> optional && optional.orElse(null) instanceof GameProfile profile) {
            return profile;
        }
        if (lookedUp instanceof GameProfile profile) {
            return profile;
        }
        return null;
    }

    @Override
    public boolean isSingle() {
        return false;
    }

    @Override
    public Class<? extends GameProfile> getReturnType() {
        return GameProfile.class;
    }

    @Override
    public String toString(@Nullable SkriptEvent event, boolean debug) {
        return "offline players";
    }
}
