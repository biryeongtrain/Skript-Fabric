package kim.biryeong.skriptFabricPort.gametest;

import ch.njol.skript.Skript;
import ch.njol.skript.config.Config;
import ch.njol.skript.config.EntryNode;
import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.variables.Variables;
import java.lang.reflect.Method;
import java.util.concurrent.atomic.AtomicBoolean;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.network.chat.Component;
import org.skriptlang.skript.fabric.runtime.SkriptRuntime;
import org.skriptlang.skript.lang.event.SkriptEvent;

public final class SkriptFabricExpressionCycleHSyntax3GameTest extends AbstractSkriptFabricGameTestSupport {

    private static final AtomicBoolean SUPPORT_REGISTERED = new AtomicBoolean(false);

    @GameTest
    public void configNodeAndScriptsExecuteRealScripts(GameTestHelper helper) {
        ensureSupportRegistered();
        runWithRuntimeLock(helper, () -> {
            Config config = new Config("main", "main.sk", null);
            config.getMainNode().set("language", "english");
            setMainConfig(config);

            SkriptRuntime runtime = SkriptRuntime.instance();
            runtime.clearScripts();
            runtime.loadFromResource("skript/gametest/expression/cycle-h/syntax3/config_records_value.sk");
            runtime.loadFromResource("skript/gametest/expression/cycle-h/syntax3/node_records_value.sk");
            runtime.loadFromResource("skript/gametest/expression/cycle-h/syntax3/scripts_count_records_value.sk");

            int executed = runtime.dispatch(new SkriptEvent(
                    new CycleHSyntax3Handle(),
                    helper.getLevel().getServer(),
                    helper.getLevel(),
                    null
            ));
            helper.assertTrue(
                    executed == 3,
                    Component.literal("Expected 3 triggers for cycle h syntax3 scripts but got " + executed + ".")
            );

            Object configValue = Variables.getVariable("cycleh::syntax3::config", null, false);
            helper.assertTrue(
                    configValue == config,
                    Component.literal("Expected ExprConfig fixture to store the active config instance.")
            );

            Object nodeValue = Variables.getVariable("cycleh::syntax3::node", null, false);
            helper.assertTrue(
                    nodeValue instanceof EntryNode entryNode && "english".equals(entryNode.getValue()),
                    Component.literal("Expected ExprNode fixture to store the language entry node but got " + nodeValue + ".")
            );

            Object scriptCount = Variables.getVariable("cycleh::syntax3::script_count", null, false);
            helper.assertTrue(
                    scriptCount instanceof Number number && number.intValue() == 3,
                    Component.literal("Expected ExprScripts fixture to count 3 loaded scripts but got " + scriptCount + ".")
            );

            runtime.clearScripts();
            setMainConfig(null);
        });
    }

    private static void ensureSupportRegistered() {
        if (!SUPPORT_REGISTERED.compareAndSet(false, true)) {
            return;
        }
        Skript.registerEvent(CycleHSyntax3Event.class, "gametest cycle h syntax3");
    }

    private record CycleHSyntax3Handle() {
    }

    private static void setMainConfig(Config config) {
        try {
            Method method = ch.njol.skript.expressions.ExprConfig.class.getDeclaredMethod("setMainConfig", Config.class);
            method.setAccessible(true);
            method.invoke(null, config);
        } catch (ReflectiveOperationException exception) {
            throw new IllegalStateException("Failed to set ExprConfig main config for GameTest.", exception);
        }
    }

    public static final class CycleHSyntax3Event extends ch.njol.skript.lang.SkriptEvent {
        @Override
        public boolean init(Literal<?>[] args, int matchedPattern, ParseResult parseResult) {
            return true;
        }

        @Override
        public boolean check(org.skriptlang.skript.lang.event.SkriptEvent event) {
            return event.handle() instanceof CycleHSyntax3Handle;
        }

        @Override
        public Class<?>[] getEventClasses() {
            return new Class<?>[]{CycleHSyntax3Handle.class};
        }

        @Override
        public String toString(org.skriptlang.skript.lang.event.SkriptEvent event, boolean debug) {
            return "gametest cycle h syntax3";
        }
    }
}
