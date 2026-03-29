package ch.njol.skript.expressions;

import ch.njol.skript.test.TestBootstrap;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import ch.njol.skript.Skript;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.skriptlang.skript.fabric.runtime.SkriptFabricBootstrap;
import org.skriptlang.skript.fabric.runtime.SkriptRuntime;
import org.skriptlang.skript.lang.script.Script;
import org.skriptlang.skript.registration.SyntaxInfo;
import org.skriptlang.skript.registration.SyntaxRegistry;

final class ExpressionCycle20260312IBindingCompatibilityTest {

    @BeforeAll
    static void bootstrapMinecraft() {
        TestBootstrap.bootstrap();
        SkriptFabricBootstrap.bootstrap();
    }

    @AfterEach
    void clearRuntime() {
        SkriptRuntime.instance().clearScripts();
    }

    @Test
    void bootstrapRegistersCycle20260312iExpressions() {
        assertExpressionRegistered(ExprNumbers.class);
    }

    @Test
    void parserBindsCycle20260312iExpressions() {
        Path scriptPath;
        try {
            scriptPath = Files.createTempFile("expr-numbers-binding", ".sk");
            Files.writeString(
                    scriptPath,
                    """
                    on gametest:
                        loop numbers from 2.5 to 5.5:
                            set {_numbers::*} to loop-value
                        loop integers from 2.9 to 5.1:
                            set {_integers::*} to loop-value
                        loop decimals from 3.94 to 4:
                            set {_decimals::*} to loop-value
                    """
            );
        } catch (Exception exception) {
            throw new AssertionError("Failed to write temporary ExprNumbers binding script.", exception);
        }

        Script script;
        try {
            script = SkriptRuntime.instance().loadFromPath(scriptPath);
        } catch (Exception exception) {
            throw new AssertionError("Failed to load ExprNumbers binding script.", exception);
        }
        assertNotNull(script, scriptPath.toString());
        assertEquals(1, script.getStructures().size());
    }

    private static void assertExpressionRegistered(Class<?> type) {
        for (SyntaxInfo<?> syntax : Skript.instance().syntaxRegistry().syntaxes(SyntaxRegistry.EXPRESSION)) {
            if (syntax.type() == type) {
                return;
            }
        }
        throw new AssertionError(type.getSimpleName() + " was not registered by bootstrap");
    }
}
