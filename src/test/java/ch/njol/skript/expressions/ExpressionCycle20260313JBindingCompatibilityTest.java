package ch.njol.skript.expressions;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import ch.njol.skript.Skript;
import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.registrations.experiments.ReflectionExperimentSyntax;
import java.nio.file.Files;
import java.nio.file.Path;
import net.minecraft.SharedConstants;
import net.minecraft.server.Bootstrap;
import org.jetbrains.annotations.Nullable;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.skriptlang.skript.fabric.runtime.SkriptFabricBootstrap;
import org.skriptlang.skript.fabric.runtime.SkriptRuntime;
import org.skriptlang.skript.lang.event.SkriptEvent;
import org.skriptlang.skript.lang.script.Script;
import org.skriptlang.skript.registration.SyntaxInfo;
import org.skriptlang.skript.registration.SyntaxRegistry;

final class ExpressionCycle20260313JBindingCompatibilityTest {

    private static boolean supportRegistered;

    @BeforeAll
    static void bootstrapMinecraft() {
        SharedConstants.tryDetectVersion();
        Bootstrap.bootStrap();
        SkriptFabricBootstrap.bootstrap();
        ensureSupportRegistered();
    }

    @AfterEach
    void clearRuntime() {
        SkriptRuntime.instance().clearScripts();
    }

    @Test
    void bootstrapRegistersCycle20260313jExpressions() {
        assertExpressionRegistered(ExprAppliedEffect.class);
        assertExpressionRegistered(ExprNearestEntity.class);
        assertExpressionRegistered(ExprTargetedBlock.class);
    }

    @Test
    void parserBindsCycle20260313jExpressions() throws Exception {
        Path scriptPath = Files.createTempFile("expr-cycle-j-binding", ".sk");
        Files.writeString(
                scriptPath,
                """
                on primary beacon effect:
                    if applied effect is speed:
                        stop

                on gametest cycle j syntax1 context:
                    set {_nearest} to nearest cow relative to {cyclej::syntax1::origin}
                    set {_target} to target block of event-player
                    set {_actual} to actual target block of event-player
                """
        );

        Script script = SkriptRuntime.instance().loadFromPath(scriptPath);
        assertNotNull(script);
        assertEquals(2, script.getStructures().size());
    }

    private static void ensureSupportRegistered() {
        if (supportRegistered) {
            return;
        }
        Skript.registerEvent(GameTestCycleJSyntax1Event.class, "gametest cycle j syntax1 context");
        supportRegistered = true;
    }

    private static void assertExpressionRegistered(Class<?> type) {
        for (SyntaxInfo<?> syntax : Skript.instance().syntaxRegistry().syntaxes(SyntaxRegistry.EXPRESSION)) {
            if (syntax.type() == type) {
                return;
            }
        }
        throw new AssertionError(type.getSimpleName() + " was not registered by bootstrap");
    }

    private record CycleJSyntax1Handle() {
    }

    public static final class GameTestCycleJSyntax1Event extends ch.njol.skript.lang.SkriptEvent implements ReflectionExperimentSyntax {

        @Override
        public boolean init(Literal<?>[] args, int matchedPattern, ParseResult parseResult) {
            return args.length == 0;
        }

        @Override
        public boolean check(SkriptEvent event) {
            return event.handle() instanceof CycleJSyntax1Handle;
        }

        @Override
        public Class<?>[] getEventClasses() {
            return new Class<?>[]{CycleJSyntax1Handle.class};
        }

        @Override
        public String toString(@Nullable SkriptEvent event, boolean debug) {
            return "gametest cycle j syntax1 context";
        }
    }
}
