package ch.njol.skript.expressions;

import ch.njol.skript.test.TestBootstrap;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import ch.njol.skript.Skript;
import ch.njol.skript.classes.ClassInfo;
import ch.njol.skript.config.Config;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ParseContext;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.lang.parser.ParserInstance;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.skript.registrations.Classes;
import org.jetbrains.annotations.Nullable;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.skriptlang.skript.fabric.runtime.SkriptFabricBootstrap;
import org.skriptlang.skript.lang.event.SkriptEvent;
import org.skriptlang.skript.lang.script.Script;
import org.skriptlang.skript.registration.SyntaxInfo;
import org.skriptlang.skript.registration.SyntaxRegistry;
import org.skriptlang.skript.util.Executable;

final class ExpressionCycle20260313FSafe2BindingCompatibilityTest {

    private static boolean supportRegistered;
    private static final Class<?> COMMAND_EVENT_CLASS = commandEventClass();

    @BeforeAll
    static void bootstrapMinecraft() {
        TestBootstrap.bootstrap();
        SkriptFabricBootstrap.bootstrap();
        ensureSupportRegistered();
    }

    @AfterEach
    void clearParserState() {
        ParserInstance.get().deleteCurrentEvent();
        ParserInstance.get().setCurrentScript(null);
    }

    @Disabled("Moved to GameTest")
    @Test
    void bootstrapRegistersCycle20260313fExpressions() {
        assertExpressionRegistered(ExprCommandInfo.class);
        assertExpressionRegistered(ExprResult.class);
        assertExpressionRegistered(ExprScript.class);
        assertExpressionRegistered(ExprScriptsOld.class);
    }

    @Disabled("Moved to GameTest")
    @Test
    void parserBindsCycle20260313fExpressionsThroughBootstrap() {
        ParserInstance.get().setCurrentScript(new Script(new Config("lane-f-binding", "lane-f-binding.sk", null), java.util.List.of()));
        ParserInstance.get().setCurrentEvent("command", COMMAND_EVENT_CLASS);

        assertInstanceOf(ExprCommandInfo.class, parseExpression("main command label of command \"/say hello\"", String.class));
        assertInstanceOf(ExprScript.class, parseExpression("current script", Script.class));
        assertInstanceOf(ExprScriptsOld.class, parseExpression("all enabled scripts without paths", String.class));
        assertInstanceOf(ExprResult.class, parseExpression("result of lane-f-binding-executable", Object.class));
    }

    private static void ensureSupportRegistered() {
        if (supportRegistered) {
            return;
        }
        registerClassInfo(Executable.class, "executable");
        registerClassInfo(Object.class, "object");
        registerClassInfo(Script.class, "script");
        Skript.registerExpression(BindingExecutableExpression.class, Executable.class, "lane-f-binding-executable");
        supportRegistered = true;
    }

    private static <T> void registerClassInfo(Class<T> type, String codeName) {
        if (Classes.getExactClassInfo(type) == null) {
            Classes.registerClassInfo(new ClassInfo<>(type, codeName));
        }
    }

    private static Expression<?> parseExpression(String input, Class<?>... returnTypes) {
        Expression<?> parsed = new SkriptParser(input, SkriptParser.ALL_FLAGS, ParseContext.DEFAULT).parseExpression(returnTypes);
        assertNotNull(parsed, input);
        return parsed;
    }

    private static void assertExpressionRegistered(Class<?> type) {
        for (SyntaxInfo<?> syntax : Skript.instance().syntaxRegistry().syntaxes(SyntaxRegistry.EXPRESSION)) {
            if (syntax.type() == type) {
                return;
            }
        }
        throw new AssertionError(type.getSimpleName() + " was not registered by bootstrap");
    }

    private static Class<?> commandEventClass() {
        try {
            return Class.forName("ch.njol.skript.events.FabricPlayerEventHandles$Command");
        } catch (ClassNotFoundException exception) {
            throw new IllegalStateException(exception);
        }
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    public static final class BindingExecutableExpression extends SimpleExpression<Executable> {
        @Override
        protected Executable @Nullable [] get(SkriptEvent event) {
            return new Executable[]{(caller, arguments) -> "ok"};
        }

        @Override
        public boolean isSingle() {
            return true;
        }

        @Override
        public Class<? extends Executable> getReturnType() {
            return Executable.class;
        }
    }
}
