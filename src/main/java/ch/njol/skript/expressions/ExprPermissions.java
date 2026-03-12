package ch.njol.skript.expressions;

import ch.njol.skript.Skript;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.util.Kleenean;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.lang.event.SkriptEvent;

public class ExprPermissions extends SimpleExpression<String> {

    private static volatile @Nullable LuckPermsPermissionBridge luckPermsBridge;
    private static volatile boolean bridgeResolved;

    static {
        Skript.registerExpression(
                ExprPermissions.class,
                String.class,
                "[(all [[of] the]|the)] permissions (from|of) %players%",
                "[(all [[of] the]|the)] %players%'[s] permissions"
        );
    }

    private Expression<ServerPlayer> players;

    @Override
    @SuppressWarnings("unchecked")
    public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
        players = (Expression<ServerPlayer>) exprs[0];
        return true;
    }

    @Override
    protected String @Nullable [] get(SkriptEvent event) {
        Set<String> permissions = new LinkedHashSet<>();
        for (ServerPlayer player : players.getArray(event)) {
            permissions.addAll(resolvePermissions(player));
        }
        return permissions.toArray(String[]::new);
    }

    static Set<String> resolvePermissions(ServerPlayer player) {
        LuckPermsPermissionBridge bridge = resolveLuckPermsBridge();
        if (bridge == null) {
            return Set.of();
        }
        return bridge.permissions(player);
    }

    static String[] collectPermissionKeys(Map<String, Boolean> permissionMap) {
        return permissionMap.entrySet().stream()
                .filter(entry -> Boolean.TRUE.equals(entry.getValue()))
                .map(Map.Entry::getKey)
                .distinct()
                .sorted()
                .toArray(String[]::new);
    }

    private static @Nullable LuckPermsPermissionBridge resolveLuckPermsBridge() {
        if (bridgeResolved) {
            return luckPermsBridge;
        }
        synchronized (ExprPermissions.class) {
            if (!bridgeResolved) {
                luckPermsBridge = LuckPermsPermissionBridge.tryCreate();
                bridgeResolved = true;
            }
            return luckPermsBridge;
        }
    }

    @Override
    public boolean isSingle() {
        return false;
    }

    @Override
    public Class<? extends String> getReturnType() {
        return String.class;
    }

    @Override
    public String toString(@Nullable SkriptEvent event, boolean debug) {
        return "permissions of " + players.toString(event, debug);
    }

    private static final class LuckPermsPermissionBridge {

        private final Method getProvider;
        private final Method getPlayerAdapter;
        private final Method getPermissionData;
        private final Method getPermissionMap;

        private LuckPermsPermissionBridge(
                Method getProvider,
                Method getPlayerAdapter,
                Method getPermissionData,
                Method getPermissionMap
        ) {
            this.getProvider = getProvider;
            this.getPlayerAdapter = getPlayerAdapter;
            this.getPermissionData = getPermissionData;
            this.getPermissionMap = getPermissionMap;
        }

        static @Nullable LuckPermsPermissionBridge tryCreate() {
            try {
                Class<?> providerClass = Class.forName("net.luckperms.api.LuckPermsProvider");
                Class<?> luckPermsClass = Class.forName("net.luckperms.api.LuckPerms");
                Class<?> playerAdapterClass = Class.forName("net.luckperms.api.platform.PlayerAdapter");
                Class<?> cachedPermissionDataClass = Class.forName("net.luckperms.api.cacheddata.CachedPermissionData");
                return new LuckPermsPermissionBridge(
                        providerClass.getMethod("get"),
                        luckPermsClass.getMethod("getPlayerAdapter", Class.class),
                        playerAdapterClass.getMethod("getPermissionData", Object.class),
                        cachedPermissionDataClass.getMethod("getPermissionMap")
                );
            } catch (ReflectiveOperationException | LinkageError ignored) {
                return null;
            }
        }

        Set<String> permissions(ServerPlayer player) {
            try {
                Object provider = getProvider.invoke(null);
                Object adapter = getPlayerAdapter.invoke(provider, ServerPlayer.class);
                Object permissionData = getPermissionData.invoke(adapter, player);
                Object rawMap = getPermissionMap.invoke(permissionData);
                if (!(rawMap instanceof Map<?, ?> map) || map.isEmpty()) {
                    return Set.of();
                }
                @SuppressWarnings("unchecked")
                Map<String, Boolean> permissionMap = (Map<String, Boolean>) map;
                return Set.of(collectPermissionKeys(permissionMap));
            } catch (InvocationTargetException e) {
                Throwable cause = e.getCause();
                if (cause instanceof IllegalStateException) {
                    return Set.of();
                }
                throw new IllegalStateException("Unable to collect Fabric permissions through LuckPerms", cause);
            } catch (ReflectiveOperationException e) {
                throw new IllegalStateException("Unable to collect Fabric permissions through LuckPerms", e);
            }
        }
    }
}
