package ch.njol.skript.expressions;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import ch.njol.skript.Skript;
import ch.njol.skript.literals.LitConsole;
import java.nio.file.Files;
import java.nio.file.Path;
import net.minecraft.SharedConstants;
import net.minecraft.server.Bootstrap;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.skriptlang.skript.fabric.runtime.SkriptFabricBootstrap;
import org.skriptlang.skript.fabric.runtime.SkriptRuntime;
import org.skriptlang.skript.lang.script.Script;
import org.skriptlang.skript.registration.SyntaxInfo;
import org.skriptlang.skript.registration.SyntaxRegistry;

final class ExpressionCycle20260313FBindingCompatibilityTest {

    @BeforeAll
    static void bootstrapMinecraft() {
        SharedConstants.tryDetectVersion();
        Bootstrap.bootStrap();
        SkriptFabricBootstrap.bootstrap();
    }

    @AfterEach
    void clearRuntime() {
        SkriptRuntime.instance().clearScripts();
    }

    @Test
    void bootstrapRegistersCycle20260313fSyntax() {
        assertExpressionRegistered(ExprMaxMinecartSpeed.class);
        assertExpressionRegistered(ExprMinecartDerailedFlyingVelocity.class);
        assertExpressionRegistered(ExprCompassTarget.class);
        assertExpressionRegistered(ExprPortal.class);
        assertExpressionRegistered(LitConsole.class);
    }

    @Test
    void parserBindsCycle20260313fSyntaxInScripts() throws Exception {
        Path scriptPath = Files.createTempFile("expr-cycle-f-binding", ".sk");
        Files.writeString(
                scriptPath,
                """
                on use entity:
                    set max minecart speed of event-entity to 0.8
                    set {_derailed} to derailed velocity of event-entity
                    set event-player's compass target to location of event-entity

                on portal:
                    loop portal blocks:
                        stop

                on server tick:
                    set {_console} to console
                """
        );

        Script script = SkriptRuntime.instance().loadFromPath(scriptPath);
        assertNotNull(script);
        assertEquals(3, script.getStructures().size());
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
