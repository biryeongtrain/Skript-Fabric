package ch.njol.skript.expressions;

import ch.njol.skript.test.TestBootstrap;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import ch.njol.skript.Skript;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.skriptlang.skript.fabric.runtime.SkriptFabricBootstrap;
import org.skriptlang.skript.fabric.runtime.SkriptRuntime;
import org.skriptlang.skript.lang.script.Script;
import org.skriptlang.skript.registration.SyntaxInfo;
import org.skriptlang.skript.registration.SyntaxRegistry;

final class ExpressionCycle20260313LBindingCompatibilityTest {

    @BeforeAll
    static void bootstrapMinecraft() {
        TestBootstrap.bootstrap();
        SkriptFabricBootstrap.bootstrap();
    }

    @AfterEach
    void clearRuntime() {
        SkriptRuntime.instance().clearScripts();
    }

    @Disabled("Moved to GameTest")
    @Test
    void bootstrapRegistersCycle20260313lExpressions() {
        assertExpressionRegistered(ExprProjectileForce.class);
    }

    @Test
    void parserBindsCycle20260313lExpressions() throws Exception {
        Path scriptPath = Files.createTempFile("expr-cycle-l-binding", ".sk");
        Files.writeString(
                scriptPath,
                """
                on skeleton shoot bow:
                    set {_force} to projectile force
                """
        );

        Script script = SkriptRuntime.instance().loadFromPath(scriptPath);
        assertNotNull(script);
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
