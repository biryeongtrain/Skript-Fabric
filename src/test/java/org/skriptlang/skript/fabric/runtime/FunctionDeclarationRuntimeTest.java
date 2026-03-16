package org.skriptlang.skript.fabric.runtime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import ch.njol.skript.lang.function.Functions;
import ch.njol.skript.variables.Variables;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import net.minecraft.SharedConstants;
import net.minecraft.server.Bootstrap;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

final class FunctionDeclarationRuntimeTest {

    @BeforeAll
    static void bootstrapMinecraft() {
        SharedConstants.tryDetectVersion();
        Bootstrap.bootStrap();
        SkriptFabricBootstrap.bootstrap();
    }

    @AfterEach
    void clearRuntime() {
        SkriptRuntime.instance().clearScripts();
        Variables.clearAll();
    }

    @Disabled("Moved to GameTest")
    @Test
    void runtimeLoadsFunctionDeclarationsAndUnloadsTheirRegistrations() throws IOException {
        Path script = Files.createTempFile("function-runtime", ".sk");
        Files.writeString(
                script,
                """
                function double_runtime(value: integer) :: integer:
                    return {_value} * 2

                local function decorate_runtime(prefix: text, suffix: text = "tail") :: text:
                    return "%{_prefix}%:%{_suffix}%"

                on load:
                    set {function::double} to the result of the function named "double_runtime" with arguments 6
                    set {function::local} to the result of the function named "decorate_runtime" with arguments "head"
                """
        );

        SkriptRuntime runtime = SkriptRuntime.instance();
        runtime.loadFromPath(script);

        assertEquals(12, Variables.getVariable("function::double", null, false));
        assertEquals("head:tail", Variables.getVariable("function::local", null, false));
        assertNotNull(Functions.getFunction("double_runtime"));
        assertNotNull(Functions.getLocalFunction("decorate_runtime", script.toString()));

        runtime.clearScripts();

        assertNull(Functions.getFunction("double_runtime"));
        assertNull(Functions.getLocalFunction("decorate_runtime", script.toString()));
    }
}
