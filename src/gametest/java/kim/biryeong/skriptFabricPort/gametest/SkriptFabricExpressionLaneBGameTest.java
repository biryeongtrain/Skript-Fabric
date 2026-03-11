package kim.biryeong.skriptFabricPort.gametest;

import ch.njol.skript.Skript;
import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.SkriptParser.ParseResult;
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

public final class SkriptFabricExpressionLaneBGameTest extends AbstractSkriptFabricGameTestSupport {

    private static final AtomicBoolean SUPPORT_REGISTERED = new AtomicBoolean(false);

    @GameTest
    public void laneBServerExpressionsExecuteRealScripts(GameTestHelper helper) {
        ensureSupportRegistered();
        runWithRuntimeLock(helper, () -> {
            SkriptRuntime runtime = SkriptRuntime.instance();
            runtime.clearScripts();
            loadScripts(
                    runtime,
                    "skript/gametest/expression/lane-b/client_view_distance_records_value.sk",
                    "skript/gametest/expression/lane-b/ip_records_value.sk",
                    "skript/gametest/expression/lane-b/language_records_value.sk",
                    "skript/gametest/expression/lane-b/max_players_records_value.sk",
                    "skript/gametest/expression/lane-b/motd_records_value.sk",
                    "skript/gametest/expression/lane-b/mods_marks_loaded_ids.sk",
                    "skript/gametest/expression/lane-b/online_players_count_records_value.sk",
                    "skript/gametest/expression/lane-b/ping_records_value.sk",
                    "skript/gametest/expression/lane-b/platform_versions_record_values.sk",
                    "skript/gametest/expression/lane-b/player_protocol_version_records_value.sk",
                    "skript/gametest/expression/lane-b/protocol_version_records_value.sk",
                    "skript/gametest/expression/lane-b/view_distance_records_value.sk"
            );

            ServerPlayer player = helper.makeMockServerPlayerInLevel();
            player.setGameMode(GameType.CREATIVE);

            int executed = runtime.dispatch(new SkriptEvent(
                    new LaneBContextHandle(),
                    helper.getLevel().getServer(),
                    helper.getLevel(),
                    player
            ));
            assertExecuted(helper, executed, 12, "lane B server expression bundle");

            assertNumber(helper, "laneb::client_view_distance", player.clientInformation().viewDistance());
            helper.assertTrue(
                    player.getIpAddress().equals(Variables.getVariable("laneb::ip", null, false)),
                    Component.literal("Expected IP expression to record the player's current IP address.")
            );
            helper.assertTrue(
                    player.clientInformation().language().equals(Variables.getVariable("laneb::language", null, false)),
                    Component.literal("Expected language expression to record the client's language.")
            );
            assertNumber(helper, "laneb::max_players", helper.getLevel().getServer().getMaxPlayers());
            assertText(helper, "laneb::motd", helper.getLevel().getServer().getMotd());
            helper.assertTrue(
                    loadedMods().containsValue("fabricloader"),
                    Component.literal("Expected ExprMods to include fabricloader.")
            );
            helper.assertTrue(
                    loadedMods().containsValue("skript-fabric-port-gametest"),
                    Component.literal("Expected ExprMods to include the gametest mod id.")
            );
            assertNumber(helper, "laneb::online_players_count", helper.getLevel().getServer().getPlayerCount());
            assertNumber(helper, "laneb::ping", player.connection.latency());
            assertText(
                    helper,
                    "laneb::platform_version",
                    FabricLoader.getInstance()
                            .getModContainer("fabricloader")
                            .map(container -> container.getMetadata().getVersion().getFriendlyString())
                            .orElse("unknown")
            );
            assertText(helper, "laneb::fabric_version", Variables.getVariable("laneb::platform_version", null, false));
            assertText(helper, "laneb::minecraft_version", ch.njol.skript.Skript.getMinecraftVersion().toString());
            assertText(
                    helper,
                    "laneb::skript_version",
                    FabricLoader.getInstance()
                            .getModContainer("skript-fabric-port")
                            .map(container -> container.getMetadata().getVersion().getFriendlyString())
                            .orElse("unknown")
            );
            assertNumber(helper, "laneb::player_protocol_version", net.minecraft.SharedConstants.getProtocolVersion());
            assertNumber(helper, "laneb::protocol_version", net.minecraft.SharedConstants.getProtocolVersion());
            assertNumber(helper, "laneb::view_distance", player.clientInformation().viewDistance());
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
                    "skript/gametest/expression/lane-b/ops_records_players.sk",
                    "skript/gametest/expression/lane-b/non_ops_records_players.sk"
            );
            assertProfileListContains(helper, "laneb::ops::", operator.getGameProfile());
            assertProfileListContains(helper, "laneb::non_ops::", eventPlayer.getGameProfile());

            GameProfile listed = offlineProfile("LaneBListed");
            executeScripts(
                    runtime,
                    helper,
                    eventPlayer,
                    1,
                    "lane B whitelist add",
                    "skript/gametest/expression/lane-b/whitelist_adds_literal_profile.sk"
            );
            helper.assertTrue(
                    helper.getLevel().getServer().getPlayerList().isWhiteListed(listed),
                    Component.literal("Expected whitelist expression to add the literal offline player to the server whitelist.")
            );
            assertProfileListContains(helper, "laneb::whitelist_after_add::", listed);

            executeScripts(
                    runtime,
                    helper,
                    eventPlayer,
                    1,
                    "lane B whitelist remove",
                    "skript/gametest/expression/lane-b/whitelist_removes_literal_profile.sk"
            );
            helper.assertTrue(
                    !helper.getLevel().getServer().getPlayerList().isWhiteListed(listed),
                    Component.literal("Expected whitelist expression to remove the literal offline player from the server whitelist.")
            );
            assertProfileListMissing(helper, "laneb::whitelist_after_remove::", listed);

            executeScripts(
                    runtime,
                    helper,
                    eventPlayer,
                    1,
                    "lane B whitelist enable",
                    "skript/gametest/expression/lane-b/whitelist_enable_server.sk"
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
                    "skript/gametest/expression/lane-b/whitelist_disable_server.sk"
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
        Skript.registerEvent(GameTestLaneBContextEvent.class, "gametest lane b context");
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
                new LaneBContextHandle(),
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
                expected != null && expected.equals(value),
                Component.literal("Expected " + variable + " to equal " + expected + " but got " + value + ".")
        );
    }

    private static void assertProfileListContains(GameTestHelper helper, String prefix, GameProfile expected) {
        helper.assertTrue(
                containsProfile(profileValues(prefix), expected),
                Component.literal("Expected " + prefix + " to contain " + expected.getName() + ".")
        );
    }

    private static void assertProfileListMissing(GameTestHelper helper, String prefix, GameProfile expected) {
        helper.assertTrue(
                !containsProfile(profileValues(prefix), expected),
                Component.literal("Expected " + prefix + " not to contain " + expected.getName() + ".")
        );
    }

    private static Map<String, Object> loadedMods() {
        return Variables.getVariablesWithPrefix("laneb::mods::", null, false);
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

    private static GameProfile offlineProfile(String name) {
        UUID uuid = UUID.nameUUIDFromBytes(("OfflinePlayer:" + name).getBytes(StandardCharsets.UTF_8));
        return new GameProfile(uuid, name);
    }

    private record LaneBContextHandle() {
    }

    public static final class GameTestLaneBContextEvent extends ch.njol.skript.lang.SkriptEvent {

        @Override
        public boolean init(Literal<?>[] args, int matchedPattern, ParseResult parseResult) {
            return args.length == 0;
        }

        @Override
        public boolean check(SkriptEvent event) {
            return event.handle() instanceof LaneBContextHandle;
        }

        @Override
        public Class<?>[] getEventClasses() {
            return new Class<?>[]{LaneBContextHandle.class};
        }

        @Override
        public String toString(@Nullable SkriptEvent event, boolean debug) {
            return "gametest lane b context";
        }
    }
}
