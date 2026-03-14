package ch.njol.skript.conditions;

import ch.njol.skript.Skript;
import ch.njol.skript.lang.Condition;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Kleenean;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.function.Predicate;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.lang.event.SkriptEvent;

public class CondPermission extends Condition {

    private static final String[] PATTERNS = new String[]{
            "%players% (has|have) [the] permission[s] %strings%",
            "%players% (doesn't|does not|do not|don't) have [the] permission[s] %strings%"
    };
    private static volatile @Nullable LuckPermsBridge luckPermsBridge;
    private static volatile boolean bridgeResolved;

    private Expression<ServerPlayer> players;
    private Expression<String> permissions;

    public static void register() {
        Skript.registerCondition(CondPermission.class, PATTERNS);
    }

    @Override
    @SuppressWarnings("unchecked")
    public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
        players = (Expression<ServerPlayer>) exprs[0];
        permissions = (Expression<String>) exprs[1];
        setNegated(matchedPattern == 1);
        return true;
    }

    @Override
    public boolean check(SkriptEvent event) {
        return players.check(event, player -> permissions.check(event, permission -> hasPermission(player, permission)), isNegated());
    }

    public static boolean hasPermission(ServerPlayer player, String permission) {
        return matchesPermission(permission, candidate -> checkPermission(player, candidate));
    }

    static boolean matchesPermission(String permission, Predicate<String> permissionChecker) {
        if (permissionChecker.test(permission)) {
            return true;
        }
        if (!permission.startsWith("skript.")) {
            return false;
        }
        for (int i = permission.lastIndexOf('.'); i != -1; i = permission.lastIndexOf('.', i - 1)) {
            if (permissionChecker.test(permission.substring(0, i + 1) + "*")) {
                return true;
            }
        }
        return false;
    }

    private static boolean checkPermission(ServerPlayer player, String permission) {
        LuckPermsBridge bridge = resolveLuckPermsBridge();
        if (bridge == null) {
            return false;
        }
        return bridge.hasPermission(player, permission);
    }

    private static @Nullable LuckPermsBridge resolveLuckPermsBridge() {
        if (bridgeResolved) {
            return luckPermsBridge;
        }
        synchronized (CondPermission.class) {
            if (!bridgeResolved) {
                luckPermsBridge = LuckPermsBridge.tryCreate();
                bridgeResolved = true;
            }
            return luckPermsBridge;
        }
    }

    @Override
    public String toString(@Nullable SkriptEvent event, boolean debug) {
        return players.toString(event, debug)
                + " "
                + (isNegated() ? "does not have" : "has")
                + " permission "
                + permissions.toString(event, debug);
    }

    private static final class LuckPermsBridge {

        private final Method getProvider;
        private final Method getPlayerAdapter;
        private final Method getPermissionData;
        private final Method checkPermission;
        private final Method asBoolean;

        private LuckPermsBridge(
                Method getProvider,
                Method getPlayerAdapter,
                Method getPermissionData,
                Method checkPermission,
                Method asBoolean
        ) {
            this.getProvider = getProvider;
            this.getPlayerAdapter = getPlayerAdapter;
            this.getPermissionData = getPermissionData;
            this.checkPermission = checkPermission;
            this.asBoolean = asBoolean;
        }

        static @Nullable LuckPermsBridge tryCreate() {
            try {
                Class<?> providerClass = Class.forName("net.luckperms.api.LuckPermsProvider");
                Class<?> luckPermsClass = Class.forName("net.luckperms.api.LuckPerms");
                Class<?> playerAdapterClass = Class.forName("net.luckperms.api.platform.PlayerAdapter");
                Class<?> cachedPermissionDataClass = Class.forName("net.luckperms.api.cacheddata.CachedPermissionData");
                Class<?> tristateClass = Class.forName("net.luckperms.api.util.Tristate");
                return new LuckPermsBridge(
                        providerClass.getMethod("get"),
                        luckPermsClass.getMethod("getPlayerAdapter", Class.class),
                        playerAdapterClass.getMethod("getPermissionData", Object.class),
                        cachedPermissionDataClass.getMethod("checkPermission", String.class),
                        tristateClass.getMethod("asBoolean")
                );
            } catch (ReflectiveOperationException | LinkageError ignored) {
                return null;
            }
        }

        boolean hasPermission(ServerPlayer player, String permission) {
            try {
                Object provider = getProvider.invoke(null);
                Object adapter = getPlayerAdapter.invoke(provider, ServerPlayer.class);
                Object permissionData = getPermissionData.invoke(adapter, player);
                Object result = checkPermission.invoke(permissionData, permission);
                return Boolean.TRUE.equals(asBoolean.invoke(result));
            } catch (InvocationTargetException e) {
                Throwable cause = e.getCause();
                if (cause instanceof IllegalStateException) {
                    return false;
                }
                throw new IllegalStateException("Unable to check Fabric permissions through LuckPerms", cause);
            } catch (ReflectiveOperationException e) {
                throw new IllegalStateException("Unable to check Fabric permissions through LuckPerms", e);
            }
        }
    }
}
