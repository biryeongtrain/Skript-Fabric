package kim.biryeong.skriptFabricPort.gametest;

import ch.njol.skript.Skript;
import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.skript.variables.Variables;
import com.mojang.authlib.GameProfile;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.SharedConstants;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.GameType;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.fabric.runtime.SkriptRuntime;
import org.skriptlang.skript.lang.event.SkriptEvent;

public final class SkriptFabricExpressionServerPropertiesGameTest extends AbstractSkriptFabricGameTestSupport {

    private static final AtomicBoolean SUPPORT_REGISTERED = new AtomicBoolean(false);

    @GameTest
    public void laneBServerExpressionsExecuteRealScripts(GameTestHelper helper) {
        ensureSupportRegistered();
        runWithRuntimeLock(helper, () -> {
            SkriptRuntime runtime = SkriptRuntime.instance();
            runtime.clearScripts();
            loadScripts(
                    runtime,
                    "skript/gametest/expression/server-properties/client_view_distance_records_value.sk",
                    "skript/gametest/expression/server-properties/ip_records_value.sk",
                    "skript/gametest/expression/server-properties/language_records_value.sk",
                    "skript/gametest/expression/server-properties/max_players_records_value.sk",
                    "skript/gametest/expression/server-properties/motd_records_value.sk",
                    "skript/gametest/expression/server-properties/mods_marks_loaded_ids.sk",
                    "skript/gametest/expression/server-properties/online_players_count_records_value.sk",
                    "skript/gametest/expression/server-properties/ping_records_value.sk",
                    "skript/gametest/expression/server-properties/platform_versions_record_values.sk",
                    "skript/gametest/expression/server-properties/player_protocol_version_records_value.sk",
                    "skript/gametest/expression/server-properties/protocol_version_records_value.sk",
                    "skript/gametest/expression/server-properties/view_distance_records_value.sk"
            );

            ServerPlayer player = helper.makeMockServerPlayerInLevel();
            player.setGameMode(GameType.CREATIVE);

            int executed = runtime.dispatch(new SkriptEvent(
                    new ServerPropertiesHandle(),
                    helper.getLevel().getServer(),
                    helper.getLevel(),
                    player
            ));
            assertExecuted(helper, executed, 12, "lane B server expression bundle");

            assertNumber(helper, "serverprops::client_view_distance", player.clientInformation().viewDistance());
            helper.assertTrue(
                    player.getIpAddress().equals(Variables.getVariable("serverprops::ip", null, false)),
                    Component.literal("Expected IP expression to record the player's current IP address.")
            );
            helper.assertTrue(
                    player.clientInformation().language().equals(Variables.getVariable("serverprops::language", null, false)),
                    Component.literal("Expected language expression to record the client's language.")
            );
            assertNumber(helper, "serverprops::max_players", helper.getLevel().getServer().getMaxPlayers());
            assertText(helper, "serverprops::motd", helper.getLevel().getServer().getMotd());
            helper.assertTrue(
                    loadedMods().containsValue("fabricloader"),
                    Component.literal("Expected ExprMods to include fabricloader.")
            );
            helper.assertTrue(
                    loadedMods().containsValue("skript-fabric-port-gametest"),
                    Component.literal("Expected ExprMods to include the gametest mod id.")
            );
            assertNumber(helper, "serverprops::online_players_count", helper.getLevel().getServer().getPlayerCount());
            assertNumber(helper, "serverprops::ping", player.connection.latency());
            assertText(
                    helper,
                    "serverprops::platform_version",
                    FabricLoader.getInstance()
                            .getModContainer("fabricloader")
                            .map(container -> container.getMetadata().getVersion().getFriendlyString())
                            .orElse("unknown")
            );
            assertText(helper, "serverprops::fabric_version", Variables.getVariable("serverprops::platform_version", null, false));
            assertText(helper, "serverprops::minecraft_version", ch.njol.skript.Skript.getMinecraftVersion().toString());
            assertText(
                    helper,
                    "serverprops::skript_version",
                    FabricLoader.getInstance()
                            .getModContainer("skript-fabric-port")
                            .map(container -> container.getMetadata().getVersion().getFriendlyString())
                            .orElse("unknown")
            );
            assertNumber(helper, "serverprops::player_protocol_version", net.minecraft.SharedConstants.getProtocolVersion());
            assertNumber(helper, "serverprops::protocol_version", net.minecraft.SharedConstants.getProtocolVersion());
            assertNumber(helper, "serverprops::view_distance", player.clientInformation().viewDistance());
            runtime.clearScripts();
        });
    }

    @GameTest
    public void laneBOperatorAndWhitelistExpressionsExecuteRealScripts(GameTestHelper helper) {
        ensureSupportRegistered();
        runWithRuntimeLock(helper, () -> {
            SkriptRuntime runtime = SkriptRuntime.instance();
            runtime.clearScripts();

            ServerPlayer eventPlayer = helper.makeMockServerPlayerInLevel();
            eventPlayer.setGameMode(GameType.CREATIVE);
            ServerPlayer operator = helper.makeMockServerPlayerInLevel();
            operator.setGameMode(GameType.CREATIVE);
            helper.getLevel().getServer().getPlayerList().op(operator.getGameProfile());

            executeScripts(
                    runtime,
                    helper,
                    eventPlayer,
                    2,
                    "lane B operator bundle",
                    "skript/gametest/expression/server-properties/ops_records_players.sk",
                    "skript/gametest/expression/server-properties/non_ops_records_players.sk"
            );
            assertProfileListContains(helper, "serverprops::ops::", operator.getGameProfile());
            assertProfileListContains(helper, "serverprops::non_ops::", eventPlayer.getGameProfile());

            GameProfile listed = offlineProfile("LaneBListed");
            executeScripts(
                    runtime,
                    helper,
                    eventPlayer,
                    1,
                    "lane B whitelist add",
                    "skript/gametest/expression/server-properties/whitelist_adds_literal_profile.sk"
            );
            helper.assertTrue(
                    helper.getLevel().getServer().getPlayerList().getWhiteList().isWhiteListed(listed),
                    Component.literal("Expected whitelist expression to add the literal offline player to the server whitelist.")
            );
            helper.assertTrue(
                    containsProfile(java.util.List.of(runtimeWhitelist(helper)), listed),
                    Component.literal("Expected runtime whitelist helper to contain the added literal offline player.")
            );
            assertProfileListContains(helper, "serverprops::whitelist_after_add::", listed);

            executeScripts(
                    runtime,
                    helper,
                    eventPlayer,
                    1,
                    "lane B whitelist remove",
                    "skript/gametest/expression/server-properties/whitelist_removes_literal_profile.sk"
            );
            helper.assertTrue(
                    !helper.getLevel().getServer().getPlayerList().getWhiteList().isWhiteListed(listed),
                    Component.literal("Expected whitelist expression to remove the literal offline player from the server whitelist.")
            );
            assertProfileListMissing(helper, "serverprops::whitelist_after_remove::", listed);

            executeScripts(
                    runtime,
                    helper,
                    eventPlayer,
                    1,
                    "lane B whitelist enable",
                    "skript/gametest/expression/server-properties/whitelist_enable_server.sk"
            );
            helper.assertTrue(
                    helper.getLevel().getServer().getPlayerList().isUsingWhitelist(),
                    Component.literal("Expected whitelist expression to enable server whitelist mode.")
            );

            executeScripts(
                    runtime,
                    helper,
                    eventPlayer,
                    1,
                    "lane B whitelist disable",
                    "skript/gametest/expression/server-properties/whitelist_disable_server.sk"
            );
            helper.assertTrue(
                    !helper.getLevel().getServer().getPlayerList().isUsingWhitelist(),
                    Component.literal("Expected whitelist expression to disable server whitelist mode.")
            );

            helper.getLevel().getServer().getPlayerList().deop(operator.getGameProfile());
            runtime.clearScripts();
        });
    }

    private static void ensureSupportRegistered() {
        if (!SUPPORT_REGISTERED.compareAndSet(false, true)) {
            return;
        }
        Skript.registerEvent(GameTestServerPropertiesEvent.class, "gametest server properties context");
        Skript.registerExpression(ServerPropsListedProfileExpression.class, GameProfile.class, "serverprops-listed-profile");
    }

    private static void loadScripts(SkriptRuntime runtime, String... paths) {
        for (String path : paths) {
            runtime.loadFromResource(path);
        }
    }

    private static void executeScripts(
            SkriptRuntime runtime,
            GameTestHelper helper,
            ServerPlayer player,
            int expected,
            String description,
            String... paths
    ) {
        runtime.clearScripts();
        loadScripts(runtime, paths);
        int executed = dispatch(runtime, helper, player);
        assertExecuted(helper, executed, expected, description);
    }

    private static int dispatch(SkriptRuntime runtime, GameTestHelper helper, ServerPlayer player) {
        return runtime.dispatch(new SkriptEvent(
                new ServerPropertiesHandle(),
                helper.getLevel().getServer(),
                helper.getLevel(),
                player
        ));
    }

    private static void assertExecuted(GameTestHelper helper, int executed, int expected, String description) {
        helper.assertTrue(
                executed == expected,
                Component.literal("Expected " + expected + " triggers for " + description + " but got " + executed + ".")
        );
    }

    private static void assertNumber(GameTestHelper helper, String variable, double expected) {
        Object value = Variables.getVariable(variable, null, false);
        helper.assertTrue(
                value instanceof Number && Double.compare(((Number) value).doubleValue(), expected) == 0,
                Component.literal("Expected " + variable + " to equal " + expected + " but got " + value + ".")
        );
    }

    private static void assertText(GameTestHelper helper, String variable, Object expected) {
        Object value = Variables.getVariable(variable, null, false);
        helper.assertTrue(
                java.util.Objects.equals(expected, value),
                Component.literal("Expected " + variable + " to equal " + expected + " but got " + value + ".")
        );
    }

    private static void assertProfileListContains(GameTestHelper helper, String prefix, GameProfile expected) {
        Collection<Object> values = profileValues(prefix);
        helper.assertTrue(
                containsProfile(values, expected),
                Component.literal("Expected " + prefix + " to contain " + expected.getName() + " but got " + describeValues(values) + ".")
        );
    }

    private static void assertProfileListMissing(GameTestHelper helper, String prefix, GameProfile expected) {
        helper.assertTrue(
                !containsProfile(profileValues(prefix), expected),
                Component.literal("Expected " + prefix + " not to contain " + expected.getName() + ".")
        );
    }

    private static Map<String, Object> loadedMods() {
        return Variables.getVariablesWithPrefix("serverprops::mods::", null, false);
    }

    private static Collection<Object> profileValues(String prefix) {
        return Variables.getVariablesWithPrefix(prefix, null, false).values();
    }

    private static boolean containsProfile(Collection<Object> values, GameProfile expected) {
        for (Object value : values) {
            if (!(value instanceof GameProfile profile)) {
                continue;
            }
            if (expected.getId() != null && expected.getId().equals(profile.getId())) {
                return true;
            }
            if (expected.getName() != null && expected.getName().equals(profile.getName())) {
                return true;
            }
        }
        return false;
    }

    private static GameProfile[] runtimeWhitelist(GameTestHelper helper) {
        try {
            Class<?> supportClass = Class.forName("ch.njol.skript.expressions.ExpressionRuntimeSupport");
            java.lang.reflect.Method method = supportClass.getDeclaredMethod("whitelist", net.minecraft.server.MinecraftServer.class);
            method.setAccessible(true);
            return (GameProfile[]) method.invoke(null, helper.getLevel().getServer());
        } catch (ReflectiveOperationException exception) {
            throw new AssertionError("Failed to read runtime whitelist helper", exception);
        }
    }

    private static String describeValues(Collection<Object> values) {
        StringBuilder builder = new StringBuilder("[");
        boolean first = true;
        for (Object value : values) {
            if (!first) {
                builder.append(", ");
            }
            first = false;
            if (value instanceof GameProfile profile) {
                builder.append(profile.getName()).append("/").append(profile.getId());
            } else if (value == null) {
                builder.append("null");
            } else {
                builder.append(value.getClass().getSimpleName()).append(":").append(value);
            }
        }
        return builder.append("]").toString();
    }

    private static GameProfile offlineProfile(String name) {
        UUID uuid = UUID.nameUUIDFromBytes(("OfflinePlayer:" + name).getBytes(StandardCharsets.UTF_8));
        return new GameProfile(uuid, name);
    }

    private record ServerPropertiesHandle() {
    }

    public static final class GameTestServerPropertiesEvent extends ch.njol.skript.lang.SkriptEvent {

        @Override
        public boolean init(Literal<?>[] args, int matchedPattern, ParseResult parseResult) {
            return args.length == 0;
        }

        @Override
        public boolean check(SkriptEvent event) {
            return event.handle() instanceof ServerPropertiesHandle;
        }

        @Override
        public Class<?>[] getEventClasses() {
            return new Class<?>[]{ServerPropertiesHandle.class};
        }

        @Override
        public String toString(@Nullable SkriptEvent event, boolean debug) {
            return "gametest server properties context";
        }
    }

    public static final class ServerPropsListedProfileExpression extends SimpleExpression<GameProfile> {
        @Override
        protected GameProfile[] get(SkriptEvent event) {
            return new GameProfile[]{offlineProfile("LaneBListed")};
        }

        @Override
        public boolean isSingle() {
            return true;
        }

        @Override
        public Class<? extends GameProfile> getReturnType() {
            return GameProfile.class;
        }

        @Override
        public String toString(@Nullable SkriptEvent event, boolean debug) {
            return "serverprops-listed-profile";
        }
    }
}
