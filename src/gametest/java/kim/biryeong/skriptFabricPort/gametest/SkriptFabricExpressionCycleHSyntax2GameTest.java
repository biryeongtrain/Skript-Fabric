package kim.biryeong.skriptFabricPort.gametest;

import ch.njol.skript.Skript;
import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.variables.Variables;
import java.util.concurrent.atomic.AtomicBoolean;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.handshake.ClientIntent;
import net.minecraft.network.protocol.handshake.ClientIntentionPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.GameType;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.fabric.runtime.SkriptRuntime;
import org.skriptlang.skript.lang.event.SkriptEvent;

public final class SkriptFabricExpressionCycleHSyntax2GameTest extends AbstractSkriptFabricGameTestSupport {

    private static final AtomicBoolean SUPPORT_REGISTERED = new AtomicBoolean(false);

    @GameTest
    public void hostnameExpressionExecutesRealScript(GameTestHelper helper) {
        ensureSupportRegistered();
        runWithRuntimeLock(helper, () -> {
            SkriptRuntime runtime = SkriptRuntime.instance();
            runtime.clearScripts();
            runtime.loadFromResource("skript/gametest/expression/cycle-h/syntax2/hostname_records_value.sk");

            ClientIntentionPacket packet = new ClientIntentionPacket(765, "cycle-h.example.test", 25565, ClientIntent.LOGIN);
            int executed = dispatch(runtime, helper, packet);
            assertExecuted(helper, executed, "cycle h hostname");
            helper.assertTrue(
                    "cycle-h.example.test".equals(Variables.getVariable("cycleh::syntax2::hostname", null, false)),
                    Component.literal("Expected hostname GameTest fixture to record the current hostname.")
            );
            runtime.clearScripts();
        });
    }

    @GameTest
    public void tpsExpressionExecutesRealScript(GameTestHelper helper) {
        ensureSupportRegistered();
        runWithRuntimeLock(helper, () -> {
            SkriptRuntime runtime = SkriptRuntime.instance();
            runtime.clearScripts();
            runtime.loadFromResource("skript/gametest/expression/cycle-h/syntax2/tps_records_value.sk");

            int executed = dispatch(runtime, helper, new TestServerHandle());
            assertExecuted(helper, executed, "cycle h tps");

            Object value = Variables.getVariable("cycleh::syntax2::tps", null, false);
            helper.assertTrue(
                    value instanceof Number number && number.doubleValue() >= 0.0D && number.doubleValue() <= 20.0D,
                    Component.literal("Expected TPS GameTest fixture to record a bounded TPS value but got " + value + ".")
            );
            runtime.clearScripts();
        });
    }

    @GameTest
    public void permissionsExpressionExecutesRealScript(GameTestHelper helper) {
        ensureSupportRegistered();
        runWithRuntimeLock(helper, () -> {
            SkriptRuntime runtime = SkriptRuntime.instance();
            runtime.clearScripts();
            runtime.loadFromResource("skript/gametest/expression/cycle-h/syntax2/permissions_records_count.sk");

            ServerPlayer player = helper.makeMockServerPlayerInLevel();
            player.setGameMode(GameType.SURVIVAL);
            Variables.setVariable("cycleh::syntax2::player", player, SkriptEvent.EMPTY, false);

            int executed = dispatch(runtime, helper, new TestServerHandle());
            assertExecuted(helper, executed, "cycle h permissions");

            Object count = Variables.getVariable("cycleh::syntax2::permission_count", null, false);
            helper.assertTrue(
                    count instanceof Number number && number.intValue() == 0,
                    Component.literal("Expected permissions GameTest fixture to observe zero permissions without LuckPerms but got " + count + ".")
            );

            Variables.setVariable("cycleh::syntax2::player", null, SkriptEvent.EMPTY, false);
            runtime.clearScripts();
        });
    }

    private static void ensureSupportRegistered() {
        if (!SUPPORT_REGISTERED.compareAndSet(false, true)) {
            return;
        }
        Skript.registerEvent(GameTestHostnameEvent.class, "gametest cycle h hostname context");
        Skript.registerEvent(GameTestServerEvent.class, "gametest cycle h server context");
    }

    private static int dispatch(SkriptRuntime runtime, GameTestHelper helper, Object handle) {
        return runtime.dispatch(new SkriptEvent(
                handle,
                helper.getLevel().getServer(),
                helper.getLevel(),
                null
        ));
    }

    private static void assertExecuted(GameTestHelper helper, int executed, String description) {
        helper.assertTrue(
                executed == 1,
                Component.literal("Expected 1 trigger for " + description + " but got " + executed + ".")
        );
    }

    private record TestServerHandle() {
    }

    public static final class GameTestHostnameEvent extends ch.njol.skript.lang.SkriptEvent {

        @Override
        public boolean init(Literal<?>[] args, int matchedPattern, ParseResult parseResult) {
            return args.length == 0;
        }

        @Override
        public boolean check(SkriptEvent event) {
            return event.handle() instanceof ClientIntentionPacket;
        }

        @Override
        public Class<?>[] getEventClasses() {
            return new Class<?>[]{ClientIntentionPacket.class};
        }

        @Override
        public String toString(@Nullable SkriptEvent event, boolean debug) {
            return "gametest cycle h hostname context";
        }
    }

    public static final class GameTestServerEvent extends ch.njol.skript.lang.SkriptEvent {

        @Override
        public boolean init(Literal<?>[] args, int matchedPattern, ParseResult parseResult) {
            return args.length == 0;
        }

        @Override
        public boolean check(SkriptEvent event) {
            return event.handle() instanceof TestServerHandle;
        }

        @Override
        public Class<?>[] getEventClasses() {
            return new Class<?>[]{TestServerHandle.class};
        }

        @Override
        public String toString(@Nullable SkriptEvent event, boolean debug) {
            return "gametest cycle h server context";
        }
    }
}
