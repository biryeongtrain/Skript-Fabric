package kim.biryeong.skriptFabricPort.gametest;

import ch.njol.skript.variables.Variables;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.network.chat.Component;
import org.skriptlang.skript.fabric.runtime.SkriptRuntime;

public final class SkriptFabricFunctionDeclarationGameTest extends AbstractSkriptFabricGameTestSupport {

    @GameTest
    public void functionDeclarationsExecuteRealScript(GameTestHelper helper) {
        runWithRuntimeLock(helper, () -> {
            SkriptRuntime runtime = SkriptRuntime.instance();
            runtime.clearScripts();
            Variables.clearAll();

            runtime.loadFromResource("skript/gametest/base/function_declaration_records_values.sk");

            helper.assertTrue(
                    Integer.valueOf(12).equals(Variables.getVariable("function::double", null, false)),
                    Component.literal("Expected declared function result to be 12.")
            );
            helper.assertTrue(
                    "head:tail".equals(Variables.getVariable("function::local", null, false)),
                    Component.literal("Expected local declared function result to be head:tail.")
            );

            runtime.clearScripts();
            Variables.clearAll();
        });
    }
}
