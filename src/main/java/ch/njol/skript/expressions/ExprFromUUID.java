package ch.njol.skript.expressions;

import ch.njol.skript.Skript;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.SyntaxStringBuilder;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.util.Kleenean;
import com.mojang.authlib.GameProfile;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.lang.event.SkriptEvent;

public class ExprFromUUID extends SimpleExpression<Object> {

    static {
        register(GameProfile.class, "offline:offline player[s] from %uuids%");
        register(ServerPlayer.class, "player:player[s] from %uuids%");
        register(Entity.class, "entity:entit(y|ies) from %uuids%");
        register(ServerLevel.class, "world:world[s] from %uuids%");
    }

    private Expression<UUID> uuids;
    private boolean player;
    private boolean offline;
    private boolean world;

    @Override
    @SuppressWarnings("unchecked")
    public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
        uuids = (Expression<UUID>) exprs[0];
        offline = parseResult.hasTag("offline");
        player = offline || parseResult.hasTag("player");
        world = parseResult.hasTag("world");
        return true;
    }

    @Override
    protected Object @Nullable [] get(SkriptEvent event) {
        List<Object> resolved = new ArrayList<>();
        MinecraftServer server = ExpressionRuntimeSupport.resolveServer(event);

        for (UUID uuid : uuids.getArray(event)) {
            if (player) {
                if (offline) {
                    GameProfile profile = offlinePlayer(server, uuid);
                    if (profile != null) {
                        resolved.add(profile);
                    }
                    continue;
                }
                if (server == null) {
                    continue;
                }
                ServerPlayer onlinePlayer = server.getPlayerList().getPlayer(uuid);
                if (onlinePlayer != null) {
                    resolved.add(onlinePlayer);
                }
                continue;
            }

            if (world) {
                ServerLevel level = server == null ? null : worldByUuid(server, uuid);
                if (level != null) {
                    resolved.add(level);
                }
                continue;
            }

            Entity entity = server == null ? null : entityByUuid(server, uuid);
            if (entity != null) {
                resolved.add(entity);
            }
        }

        if (player) {
            return offline ? resolved.toArray(GameProfile[]::new) : resolved.toArray(ServerPlayer[]::new);
        }
        if (world) {
            return resolved.toArray(ServerLevel[]::new);
        }
        return resolved.toArray(Entity[]::new);
    }

    private @Nullable GameProfile offlinePlayer(@Nullable MinecraftServer server, UUID uuid) {
        if (server != null) {
            ServerPlayer onlinePlayer = server.getPlayerList().getPlayer(uuid);
            if (onlinePlayer != null) {
                return onlinePlayer.getGameProfile();
            }
            Object cache = ExpressionHandleSupport.invoke(server, "getProfileCache");
            Object lookedUp = ExpressionHandleSupport.invoke(cache, "get", uuid);
            if (lookedUp instanceof Optional<?> optional && optional.orElse(null) instanceof GameProfile profile) {
                return profile;
            }
            if (lookedUp instanceof GameProfile profile) {
                return profile;
            }
        }
        return new GameProfile(uuid, uuid.toString());
    }

    private static @Nullable Entity entityByUuid(MinecraftServer server, UUID uuid) {
        for (ServerLevel level : server.getAllLevels()) {
            Entity entity = level.getEntity(uuid);
            if (entity != null) {
                return entity;
            }
        }
        return null;
    }

    private static @Nullable ServerLevel worldByUuid(MinecraftServer server, UUID uuid) {
        for (ServerLevel level : server.getAllLevels()) {
            if (worldUuid(level).equals(uuid)) {
                return level;
            }
        }
        return null;
    }

    private static UUID worldUuid(ServerLevel level) {
        return UUID.nameUUIDFromBytes(level.dimension().identifier().toString().getBytes(StandardCharsets.UTF_8));
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private static void register(Class<?> returnType, String pattern) {
        Skript.registerExpression((Class) ExprFromUUID.class, (Class) returnType, pattern);
    }

    @Override
    public boolean isSingle() {
        return uuids.isSingle();
    }

    @Override
    public Class<?> getReturnType() {
        if (world) {
            return ServerLevel.class;
        }
        if (player) {
            return offline ? GameProfile.class : ServerPlayer.class;
        }
        return Entity.class;
    }

    @Override
    public String toString(@Nullable SkriptEvent event, boolean debug) {
        SyntaxStringBuilder builder = new SyntaxStringBuilder(event, debug);
        if (world) {
            builder.append("worlds");
        } else if (player) {
            builder.appendIf(offline, "offline");
            builder.append("players");
        } else {
            builder.append("entities");
        }
        builder.append("from", uuids);
        return builder.toString();
    }
}
