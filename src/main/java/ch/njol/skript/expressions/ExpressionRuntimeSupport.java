package ch.njol.skript.expressions;

import com.mojang.authlib.GameProfile;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
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

    static int onlinePlayerCount(MinecraftServer server) {
        Integer count = invokeInt(server.getPlayerList(), "getCurrentPlayerCount", "getPlayerCount");
        if (count != null) {
            return count;
        }
        Collection<?> players = invokeCollection(server.getPlayerList(), "getPlayerList", "getPlayers");
        return players == null ? 0 : players.size();
    }

    static @Nullable String motd(MinecraftServer server) {
        Object value = invokeNoArg(server, "getMotd");
        return value instanceof String string ? string : null;
    }

    static GameProfile[] operators(MinecraftServer server) {
        return configEntriesAsProfiles(invokeNoArg(server.getPlayerList(), "getOpList", "getOps"));
    }

    static GameProfile[] nonOperators(MinecraftServer server) {
        Collection<?> players = invokeCollection(server.getPlayerList(), "getPlayerList", "getPlayers");
        if (players == null || players.isEmpty()) {
            return new GameProfile[0];
        }
        List<GameProfile> profiles = new ArrayList<>();
        for (Object value : players) {
            if (!(value instanceof ServerPlayer player)) {
                continue;
            }
            GameProfile profile = player.getGameProfile();
            if (!isOperator(server, profile)) {
                profiles.add(profile);
            }
        }
        return profiles.toArray(GameProfile[]::new);
    }

    static boolean isOperator(MinecraftServer server, GameProfile profile) {
        Boolean result = invokeBoolean(server.getPlayerList(), profile, "isOperator", "isOp");
        return result != null && result;
    }

    static void op(MinecraftServer server, GameProfile profile) {
        invokeSingleArg(server.getPlayerList(), profile, "op", "addToOperators");
    }

    static void deop(MinecraftServer server, GameProfile profile) {
        invokeSingleArg(server.getPlayerList(), profile, "deop", "removeFromOperators");
    }

    static GameProfile[] whitelist(MinecraftServer server) {
        return configEntriesAsProfiles(invokeNoArg(server.getPlayerList(), "getWhitelist", "getWhiteList"));
    }

    static void addWhitelist(MinecraftServer server, GameProfile profile) {
        Object whitelist = invokeNoArg(server.getPlayerList(), "getWhitelist", "getWhiteList");
        Object entry = newWhitelistEntry(profile);
        if (whitelist == null || entry == null) {
            return;
        }
        invokeSingleArg(whitelist, entry, "add");
        reloadWhitelist(server);
    }

    static void removeWhitelist(MinecraftServer server, GameProfile profile) {
        Object whitelist = invokeNoArg(server.getPlayerList(), "getWhitelist", "getWhiteList");
        if (whitelist == null) {
            return;
        }
        invokeSingleArg(whitelist, profile, "remove");
        reloadWhitelist(server);
    }

    static void clearWhitelist(MinecraftServer server) {
        for (GameProfile profile : whitelist(server)) {
            removeWhitelist(server, profile);
        }
    }

    static void reloadWhitelist(MinecraftServer server) {
        invokeNoArg(server.getPlayerList(), "reloadWhitelist");
    }

    static void setWhitelistEnabled(MinecraftServer server, boolean enabled) {
        if (invokeBooleanSetter(server, enabled, "setUsingWhitelist", "setUsingWhiteList")) {
            return;
        }
        invokeBooleanSetter(server.getPlayerList(), enabled, "setWhitelistEnabled", "setUsingWhitelist", "setUsingWhiteList");
    }

    static void kickUnlistedPlayers(MinecraftServer server) {
        if (invokeNoArg(server, "kickUnlistedPlayers") != null) {
            return;
        }
        try {
            Method method = server.getClass().getMethod("kickUnlistedPlayers", net.minecraft.commands.CommandSourceStack.class);
            method.invoke(server, server.createCommandSourceStack());
        } catch (ReflectiveOperationException ignored) {
        }
    }

    static int viewDistance(MinecraftServer server) {
        Integer value = invokeInt(server.getPlayerList(), "getViewDistance");
        return value == null ? 10 : value;
    }

    static int playerViewDistance(ServerPlayer player) {
        Integer value = invokeInt(player, "getViewDistance", "requestedViewDistance");
        return value == null ? player.clientInformation().viewDistance() : value;
    }

    static void setViewDistance(MinecraftServer server, int value) {
        invokeIntSetter(server.getPlayerList(), value, "setViewDistance");
    }

    static boolean setPlayerViewDistance(ServerPlayer player, int value) {
        return invokeIntSetter(player, value, "setViewDistance");
    }

    private static GameProfile[] configEntriesAsProfiles(@Nullable Object configList) {
        if (configList == null) {
            return new GameProfile[0];
        }
        Collection<?> entries = invokeCollection(configList, "values", "getEntries");
        if (entries == null || entries.isEmpty()) {
            return new GameProfile[0];
        }
        List<GameProfile> profiles = new ArrayList<>();
        for (Object entry : entries) {
            Object key = invokeNoArg(entry, "getKey");
            if (key instanceof GameProfile profile) {
                profiles.add(profile);
            }
        }
        return profiles.toArray(GameProfile[]::new);
    }

    private static @Nullable Object newWhitelistEntry(GameProfile profile) {
        for (String className : new String[]{
                "net.minecraft.server.WhitelistEntry",
                "net.minecraft.server.players.WhitelistEntry",
                "net.minecraft.server.players.UserWhiteListEntry"
        }) {
            try {
                Class<?> type = Class.forName(className);
                Constructor<?> constructor = type.getConstructor(GameProfile.class);
                return constructor.newInstance(profile);
            } catch (ReflectiveOperationException ignored) {
            }
        }
        return null;
    }

    private static @Nullable Object invokeNoArg(@Nullable Object target, String... methodNames) {
        if (target == null) {
            return null;
        }
        for (String methodName : methodNames) {
            try {
                Method method = target.getClass().getMethod(methodName);
                return method.invoke(target);
            } catch (ReflectiveOperationException ignored) {
            }
        }
        return null;
    }

    private static @Nullable Collection<?> invokeCollection(@Nullable Object target, String... methodNames) {
        Object value = invokeNoArg(target, methodNames);
        return value instanceof Collection<?> collection ? collection : null;
    }

    private static @Nullable Integer invokeInt(@Nullable Object target, String... methodNames) {
        Object value = invokeNoArg(target, methodNames);
        return value instanceof Number number ? number.intValue() : null;
    }

    private static @Nullable Boolean invokeBoolean(@Nullable Object target, Object argument, String... methodNames) {
        Object value = invokeSingleArg(target, argument, methodNames);
        return value instanceof Boolean bool ? bool : null;
    }

    private static boolean invokeIntSetter(@Nullable Object target, int value, String... methodNames) {
        return invokeArgumentMethod(target, Integer.valueOf(value), methodNames);
    }

    private static boolean invokeBooleanSetter(@Nullable Object target, boolean value, String... methodNames) {
        return invokeArgumentMethod(target, Boolean.valueOf(value), methodNames);
    }

    private static @Nullable Object invokeSingleArg(@Nullable Object target, @Nullable Object argument, String... methodNames) {
        if (target == null) {
            return null;
        }
        for (String methodName : methodNames) {
            for (Method method : target.getClass().getMethods()) {
                if (!method.getName().equals(methodName) || method.getParameterCount() != 1) {
                    continue;
                }
                Class<?> parameterType = wrap(method.getParameterTypes()[0]);
                if (argument != null && !parameterType.isAssignableFrom(argument.getClass())) {
                    continue;
                }
                try {
                    return method.invoke(target, argument);
                } catch (ReflectiveOperationException ignored) {
                }
            }
        }
        return null;
    }

    private static boolean invokeArgumentMethod(@Nullable Object target, @Nullable Object argument, String... methodNames) {
        if (target == null) {
            return false;
        }
        for (String methodName : methodNames) {
            for (Method method : target.getClass().getMethods()) {
                if (!method.getName().equals(methodName) || method.getParameterCount() != 1) {
                    continue;
                }
                Class<?> parameterType = wrap(method.getParameterTypes()[0]);
                if (argument != null && !parameterType.isAssignableFrom(argument.getClass())) {
                    continue;
                }
                try {
                    method.invoke(target, argument);
                    return true;
                } catch (ReflectiveOperationException ignored) {
                }
            }
        }
        return false;
    }

    private static Class<?> wrap(Class<?> type) {
        if (!type.isPrimitive()) {
            return type;
        }
        if (type == boolean.class) {
            return Boolean.class;
        }
        if (type == byte.class) {
            return Byte.class;
        }
        if (type == short.class) {
            return Short.class;
        }
        if (type == int.class) {
            return Integer.class;
        }
        if (type == long.class) {
            return Long.class;
        }
        if (type == float.class) {
            return Float.class;
        }
        if (type == double.class) {
            return Double.class;
        }
        if (type == char.class) {
            return Character.class;
        }
        return type;
    }
}
