package ch.njol.skript.expressions;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import ch.njol.skript.Skript;
import ch.njol.skript.classes.ClassInfo;
import ch.njol.skript.config.Config;
import ch.njol.skript.events.FabricPlayerEventHandles;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ParseContext;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.parser.ParserInstance;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.skript.registrations.Classes;
import ch.njol.util.Kleenean;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.SharedConstants;
import net.minecraft.server.Bootstrap;
import org.jetbrains.annotations.Nullable;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.skriptlang.skript.fabric.runtime.SkriptFabricBootstrap;
import org.skriptlang.skript.fabric.runtime.SkriptRuntime;
import org.skriptlang.skript.lang.event.SkriptEvent;
import org.skriptlang.skript.lang.script.Script;
import org.skriptlang.skript.util.Executable;

final class ExpressionCycle20260313FSafe2CompatibilityTest {

    private static boolean supportRegistered;
    private static final Class<?> COMMAND_EVENT_CLASS = commandEventClass();

    @BeforeAll
    static void bootstrapMinecraft() {
        SharedConstants.tryDetectVersion();
        Bootstrap.bootStrap();
        SkriptFabricBootstrap.bootstrap();
        ensureSupportRegistered();
    }

    @AfterEach
    void cleanupParserState() throws Exception {
        ParserInstance.get().deleteCurrentEvent();
        ParserInstance.get().setCurrentScript(null);
        setLoadedScripts(List.of());
    }

    @Disabled("Moved to GameTest")
    @Test
    void parserBindsSafeLaneTwoExpressions() {
        ParserInstance.get().setCurrentScript(script("lane-safe2", "lane-safe2.sk"));
        ParserInstance.get().setCurrentEvent("command", COMMAND_EVENT_CLASS);
        assertInstanceOf(ExprCommandInfo.class, parseExpression("label of command \"say\"", String.class));
        assertInstanceOf(ExprScript.class, parseExpression("current script", Script.class));
        assertInstanceOf(ExprScriptsOld.class, parseExpression("all scripts", String.class));
        ParserInstance.get().deleteCurrentEvent();
    }

    @Test
    void resultExpressionExecutesExecutableAndReturnsObjects() {
        ExprResult single = new ExprResult();
        assertTrue(single.init(
                new Expression[]{new TestExecutableExpression()},
                0,
                Kleenean.FALSE,
                parseResult("result of lane-safe2-executable")
        ));
        assertEquals("executed:0", single.getSingle(SkriptEvent.EMPTY));

        ParseResult pluralParse = parseResult("results of lane-safe2-executable with arguments lane-safe2-objects");
        pluralParse.tags.add("plural");
        pluralParse.tags.add("arguments");

        ExprResult plural = new ExprResult();
        assertTrue(plural.init(
                new Expression[]{new TestExecutableExpression(), new TestObjectExpression()},
                0,
                Kleenean.FALSE,
                pluralParse
        ));
        assertArrayEquals(new Object[]{"alpha", 7}, plural.getArray(SkriptEvent.EMPTY));
    }

    @Test
    void commandInfoExpressionStaysEmptyWithoutFabricMetadata() {
        ParserInstance.get().setCurrentEvent("command", COMMAND_EVENT_CLASS);

        ExprCommandInfo info = new ExprCommandInfo();
        assertTrue(info.init(new Expression[1], 4, Kleenean.FALSE, parseResult("label of command")));
        assertArrayEquals(
                new String[0],
                info.getArray(new SkriptEvent(FabricPlayerEventHandles.command("/say hi"), null, null, null))
        );
    }

    @Test
    void scriptExpressionsResolveCurrentNamedAndDirectoryScripts() throws Exception {
        Script current = script("current", "quests/current.sk");
        Script nested = script("nested", "quests/daily/nested.sk");
        Script elsewhere = script("elsewhere", "admin/tools.sk");
        setLoadedScripts(List.of(current, nested, elsewhere));
        ParserInstance.get().setCurrentScript(current);

        ExprScript currentExpr = new ExprScript();
        assertTrue(currentExpr.init(new Expression[0], 0, Kleenean.FALSE, parseResult("current script")));
        assertEquals(current, currentExpr.getSingle(SkriptEvent.EMPTY));
        assertEquals("the current script", currentExpr.toString(SkriptEvent.EMPTY, false));

        ExprScript namedExpr = new ExprScript();
        assertTrue(namedExpr.init(new Expression[]{literal("quests/current.sk")}, 1, Kleenean.FALSE, parseResult("script named")));
        assertEquals(current, namedExpr.getSingle(SkriptEvent.EMPTY));

        ExprScript multiNamedExpr = new ExprScript();
        assertTrue(multiNamedExpr.init(new Expression[]{literal("nested, elsewhere", false)}, 1, Kleenean.FALSE, parseResult("scripts named")));
        assertArrayEquals(new Script[]{nested, elsewhere}, multiNamedExpr.getArray(SkriptEvent.EMPTY));

        ExprScript directoryExpr = new ExprScript();
        assertTrue(directoryExpr.init(new Expression[]{literal("quests")}, 2, Kleenean.FALSE, parseResult("scripts in directory")));
        assertArrayEquals(new Script[]{current, nested}, directoryExpr.getArray(SkriptEvent.EMPTY));
        assertEquals("the scripts in directory \"quests\"", directoryExpr.toString(SkriptEvent.EMPTY, false));
    }

    @Test
    void scriptsOldFormatsLoadedScriptsLikeLegacyStrings() throws Exception {
        Script first = script("first", "quests/first.sk");
        Script second = script("second", "nested/second.sk");
        setLoadedScripts(List.of(first, second));

        ParseResult withPaths = parseResult("all scripts");
        ExprScriptsOld all = new ExprScriptsOld();
        assertTrue(all.init(new Expression[0], 0, Kleenean.FALSE, withPaths));
        assertArrayEquals(new String[]{"quests/first.sk", "nested/second.sk"}, all.getArray(SkriptEvent.EMPTY));
        assertEquals("all scripts", all.toString(SkriptEvent.EMPTY, false));

        ParseResult withoutPaths = parseResult("all enabled scripts without paths");
        withoutPaths.mark = 1;
        ExprScriptsOld enabled = new ExprScriptsOld();
        assertTrue(enabled.init(new Expression[0], 1, Kleenean.FALSE, withoutPaths));
        assertArrayEquals(new String[]{"first.sk", "second.sk"}, enabled.getArray(SkriptEvent.EMPTY));
        assertEquals("all enabled scripts without paths", enabled.toString(SkriptEvent.EMPTY, false));

        ExprScriptsOld disabled = new ExprScriptsOld();
        assertTrue(disabled.init(new Expression[0], 2, Kleenean.FALSE, parseResult("all disabled scripts")));
        assertArrayEquals(new String[0], disabled.getArray(SkriptEvent.EMPTY));
        assertEquals("all disabled scripts", disabled.toString(SkriptEvent.EMPTY, false));
    }

    private static void ensureSupportRegistered() {
        if (supportRegistered) {
            return;
        }
        registerClassInfo(Executable.class, "executable");
        registerClassInfo(Object.class, "object");
        registerClassInfo(Script.class, "script");
        Skript.registerExpression(TestExecutableExpression.class, Executable.class, "lane-safe2-executable");
        Skript.registerExpression(TestObjectExpression.class, Object.class, "lane-safe2-objects");
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

    private static ParseResult parseResult(String expr) {
        ParseResult result = new ParseResult();
        result.expr = expr;
        return result;
    }

    private static Expression<String> literal(String value) {
        return literal(value, true);
    }

    private static Expression<String> literal(String value, boolean single) {
        return new SimpleExpression<>() {
            @Override
            protected String @Nullable [] get(SkriptEvent event) {
                return single ? new String[]{value} : value.split(", ");
            }

            @Override
            public boolean isSingle() {
                return single;
            }

            @Override
            public Class<? extends String> getReturnType() {
                return String.class;
            }

            @Override
            public String toString(@Nullable SkriptEvent event, boolean debug) {
                return '"' + value + '"';
            }
        };
    }

    private static Script script(String name, String fileName) {
        return new Script(new Config(name, fileName, null), new ArrayList<>());
    }

    private static void setLoadedScripts(List<Script> scripts) throws Exception {
        Field field = SkriptRuntime.class.getDeclaredField("scripts");
        field.setAccessible(true);
        field.set(SkriptRuntime.instance(), new ArrayList<>(scripts));
    }

    private static Class<?> commandEventClass() {
        try {
            return Class.forName("ch.njol.skript.events.FabricPlayerEventHandles$Command");
        } catch (ClassNotFoundException exception) {
            throw new IllegalStateException(exception);
        }
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    public static final class TestExecutableExpression extends SimpleExpression<Executable> {
        @Override
        protected Executable @Nullable [] get(SkriptEvent event) {
            return new Executable[]{
                    (caller, arguments) -> arguments.length == 0 ? "executed:0" : new Object[]{arguments[0], arguments[1]}
            };
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

    public static final class TestObjectExpression extends SimpleExpression<Object> {
        @Override
        protected Object @Nullable [] get(SkriptEvent event) {
            return new Object[]{"alpha", 7};
        }

        @Override
        public boolean isSingle() {
            return false;
        }

        @Override
        public Class<?> getReturnType() {
            return Object.class;
        }
    }
}
