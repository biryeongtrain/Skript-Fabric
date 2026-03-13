package ch.njol.skript.expressions;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import ch.njol.skript.Skript;
import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.skript.lang.util.common.AnyValued;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.atomic.AtomicBoolean;
import net.minecraft.SharedConstants;
import net.minecraft.server.Bootstrap;
import org.jetbrains.annotations.Nullable;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.skriptlang.skript.fabric.runtime.SkriptFabricBootstrap;
import org.skriptlang.skript.fabric.runtime.SkriptRuntime;
import org.skriptlang.skript.lang.event.SkriptEvent;
import org.skriptlang.skript.lang.script.Script;
import org.skriptlang.skript.registration.SyntaxInfo;
import org.skriptlang.skript.registration.SyntaxRegistry;

@Tag("isolated-registry")
final class ExpressionCycle20260313FSafe1BindingCompatibilityTest {

    private static final AtomicBoolean SUPPORT_REGISTERED = new AtomicBoolean(false);
    private static final LaneFSafe1MutableValue VALUE_FIXTURE = new LaneFSafe1MutableValue("alpha");

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
        VALUE_FIXTURE.changeValue("alpha");
    }

    @Test
    void bootstrapRegistersCycle20260313fSafe1Expressions() {
        assertExpressionRegistered(ExprArgument.class);
        assertExpressionRegistered(ExprParse.class);
        assertExpressionRegistered(ExprParseError.class);
        assertExpressionRegistered(ExprValue.class);
    }

    @Test
    void parserBindsCycle20260313fSafe1ExpressionsThroughLoadedScripts() throws Exception {
        Path scriptPath = Files.createTempFile("expr-cycle-f-safe1-binding", ".sk");
        Files.writeString(
                scriptPath,
                """
                on command "say":
                    set {_first} to argument 1
                    set {_last} to last argument
                    set {_all::*} to arguments

                on gametest cycle f safe1 binding context:
                    set {_parsed} to "12" parsed as integer
                    set {_failed} to "oops" parsed as integer
                    set {_error} to parse error
                    set {_current} to string value of lane-f-safe1-valued
                    set string value of lane-f-safe1-valued to "beta"
                """
        );

        Script script = SkriptRuntime.instance().loadFromPath(scriptPath);
        assertNotNull(script);
        assertEquals(2, script.getStructures().size());
    }

    private static void ensureSupportRegistered() {
        if (!SUPPORT_REGISTERED.compareAndSet(false, true)) {
            return;
        }
        Skript.registerEvent(GameTestCycleFSafe1BindingEvent.class, "gametest cycle f safe1 binding context");
        Skript.registerExpression(LaneFSafe1ValuedExpression.class, LaneFSafe1MutableValue.class, "lane-f-safe1-valued");
    }

    private static void assertExpressionRegistered(Class<?> type) {
        for (SyntaxInfo<?> syntax : Skript.instance().syntaxRegistry().syntaxes(SyntaxRegistry.EXPRESSION)) {
            if (syntax.type() == type) {
                return;
            }
        }
        throw new AssertionError(type.getSimpleName() + " was not registered by bootstrap");
    }

    public static final class GameTestCycleFSafe1BindingEvent extends ch.njol.skript.lang.SkriptEvent {
        @Override
        public boolean init(Literal<?>[] args, int matchedPattern, ParseResult parseResult) {
            return args.length == 0;
        }

        @Override
        public boolean check(SkriptEvent event) {
            return event.handle() instanceof CycleFSafe1BindingHandle;
        }

        @Override
        public Class<?>[] getEventClasses() {
            return new Class<?>[]{CycleFSafe1BindingHandle.class};
        }

        @Override
        public String toString(@Nullable SkriptEvent event, boolean debug) {
            return "gametest cycle f safe1 binding context";
        }
    }

    private record CycleFSafe1BindingHandle() {
    }

    public static final class LaneFSafe1ValuedExpression extends SimpleExpression<LaneFSafe1MutableValue> {
        @Override
        protected LaneFSafe1MutableValue @Nullable [] get(SkriptEvent event) {
            return new LaneFSafe1MutableValue[]{VALUE_FIXTURE};
        }

        @Override
        public boolean isSingle() {
            return true;
        }

        @Override
        public Class<? extends LaneFSafe1MutableValue> getReturnType() {
            return LaneFSafe1MutableValue.class;
        }
    }

    public static final class LaneFSafe1MutableValue implements AnyValued<String> {
        private String value;

        private LaneFSafe1MutableValue(String value) {
            this.value = value;
        }

        @Override
        public String value() {
            return value;
        }

        @Override
        public boolean supportsValueChange() {
            return true;
        }

        @Override
        public void changeValue(String value) {
            this.value = value;
        }

        @Override
        public Class<String> valueType() {
            return String.class;
        }
    }
}
