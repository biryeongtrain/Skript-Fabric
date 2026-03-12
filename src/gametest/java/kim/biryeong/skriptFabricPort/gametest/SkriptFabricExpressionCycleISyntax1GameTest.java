package kim.biryeong.skriptFabricPort.gametest;

import ch.njol.skript.variables.Variables;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.network.chat.Component;
import org.skriptlang.skript.fabric.runtime.SkriptRuntime;
import org.skriptlang.skript.lang.event.SkriptEvent;

public final class SkriptFabricExpressionCycleISyntax1GameTest extends AbstractSkriptFabricGameTestSupport {

    @GameTest
    public void exprNumbersExecutesRealScript(GameTestHelper helper) {
        runWithRuntimeLock(helper, () -> {
            SkriptRuntime runtime = SkriptRuntime.instance();
            runtime.clearScripts();
            runtime.loadFromResource("skript/gametest/expression/cycle-i/syntax1/expr_numbers_records_values.sk");

            int executed = runtime.dispatch(new SkriptEvent(
                    null,
                    helper.getLevel().getServer(),
                    helper.getLevel(),
                    null
            ));
            assertExecuted(helper, executed, 1, "cycle i syntax1 ExprNumbers");

            assertNumber(helper, "cyclei::numbers::first", 2.5D);
            assertNumber(helper, "cyclei::numbers::last", 5.5D);
            assertNumber(helper, "cyclei::numbers::count", 4.0D);
            assertApproxNumber(helper, "cyclei::numbers::sum", 16.0D);

            assertNumber(helper, "cyclei::integers::first", 3.0D);
            assertNumber(helper, "cyclei::integers::last", 5.0D);
            assertNumber(helper, "cyclei::integers::count", 3.0D);
            assertApproxNumber(helper, "cyclei::integers::sum", 12.0D);

            assertApproxNumber(helper, "cyclei::decimals::first", 3.94D);
            assertApproxNumber(helper, "cyclei::decimals::last", 4.0D);
            assertNumber(helper, "cyclei::decimals::count", 7.0D);
            assertApproxNumber(helper, "cyclei::decimals::sum", 27.79D);
            runtime.clearScripts();
        });
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

    private static void assertApproxNumber(GameTestHelper helper, String variable, double expected) {
        Object value = Variables.getVariable(variable, null, false);
        helper.assertTrue(
                value instanceof Number && Math.abs(((Number) value).doubleValue() - expected) < 0.0001D,
                Component.literal("Expected " + variable + " to equal " + expected + " but got " + value + ".")
        );
    }
}
