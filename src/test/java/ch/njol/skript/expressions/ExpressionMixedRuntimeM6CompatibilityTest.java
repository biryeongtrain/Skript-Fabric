package ch.njol.skript.expressions;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import ch.njol.skript.Skript;
import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.classes.ClassInfo;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.KeyProviderExpression;
import ch.njol.skript.lang.KeyedValue;
import ch.njol.skript.lang.ParseContext;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.function.DynamicFunctionReference;
import ch.njol.skript.lang.function.Function;
import ch.njol.skript.lang.function.FunctionEvent;
import ch.njol.skript.lang.function.FunctionRegistry;
import ch.njol.skript.lang.function.Functions;
import ch.njol.skript.lang.function.Parameter;
import ch.njol.skript.lang.function.Signature;
import ch.njol.skript.lang.parser.ParserInstance;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.skript.lang.util.SimpleLiteral;
import ch.njol.skript.registrations.Classes;
import ch.njol.util.Kleenean;
import java.util.Arrays;
import java.util.Iterator;
import net.minecraft.SharedConstants;
import net.minecraft.server.Bootstrap;
import org.jetbrains.annotations.Nullable;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.skriptlang.skript.fabric.runtime.SkriptFabricBootstrap;
import org.skriptlang.skript.lang.event.SkriptEvent;
import org.skriptlang.skript.lang.util.SkriptQueue;

public final class ExpressionMixedRuntimeM6CompatibilityTest {

    private static boolean syntaxRegistered;

    @BeforeAll
    static void bootstrap() {
        SharedConstants.tryDetectVersion();
        Bootstrap.bootStrap();
        SkriptFabricBootstrap.bootstrap();
        ensureSyntax();
    }

    @AfterEach
    void cleanup() {
        ParserInstance.get().deleteCurrentEvent();
        ParserInstance.get().setCurrentScript(null);
        FunctionRegistry.getRegistry().clear();
        Functions.clear();
    }

    @Test
    void assignedExpressionsParseThroughRegistry() {
        assertInstanceOf(ExprCaughtErrors.class, parseExpression("last caught runtime errors", String.class));
        assertInstanceOf(ExprDequeuedQueue.class, parseExpression("dequeued lane-m6-queue", Object.class));
        assertInstanceOf(ExprFunction.class, parseExpression("the function named \"lane_m6_echo\"", DynamicFunctionReference.class));
        assertInstanceOf(ExprKeyed.class, parseExpression("keyed lane-m6-keyed-strings", Object.class));
        assertInstanceOf(ExprQueue.class, parseExpression("a queue of lane-m6-strings", SkriptQueue.class));
        assertInstanceOf(ExprQueueStartEnd.class, parseExpression("start of lane-m6-queue", Object.class));
        assertInstanceOf(ExprRecursive.class, parseExpression("recursive lane-m6-nested-strings", Object.class));
        assertInstanceOf(ExprRepeat.class, parseExpression("lane-m6-strings repeated 3 times", String.class));
        assertInstanceOf(ExprRound.class, parseExpression("rounded lane-m6-number-list", Long.class));
    }

    @Test
    void directExpressionBehaviorMatchesCompatSurface() {
        ExprCaughtErrors.lastErrors = new String[]{"first", "second"};
        ExprCaughtErrors caughtErrors = new ExprCaughtErrors();
        assertArrayEquals(new String[]{"first", "second"}, caughtErrors.getArray(SkriptEvent.EMPTY));

        ExprPercent percent = new ExprPercent();
        assertTrue(percent.init(
                new Expression[]{new SimpleLiteral<>(25, false), new SimpleLiteral<>(new Number[]{8, 20}, Number.class, false)},
                0,
                Kleenean.FALSE,
                new ParseResult()
        ));
        assertArrayEquals(new Number[]{2.0, 5.0}, percent.getArray(SkriptEvent.EMPTY));

        ExprRepeat repeat = new ExprRepeat();
        assertTrue(repeat.init(
                new Expression[]{new SimpleLiteral<>(new String[]{"ha", "xo"}, String.class, false), new SimpleLiteral<>(3, false)},
                0,
                Kleenean.FALSE,
                new ParseResult()
        ));
        assertArrayEquals(new String[]{"hahaha", "xoxoxo"}, repeat.getArray(SkriptEvent.EMPTY));

        ExprRound round = new ExprRound();
        assertTrue(round.init(
                new Expression[]{new SimpleLiteral<>(new Number[]{1.2, 1.8}, Number.class, false)},
                2,
                Kleenean.FALSE,
                new ParseResult()
        ));
        assertArrayEquals(new Long[]{1L, 2L}, round.getArray(SkriptEvent.EMPTY));

        ExprQueue queueExpression = new ExprQueue();
        assertTrue(queueExpression.init(
                new Expression[]{new SimpleLiteral<>(new String[]{"alpha", "beta"}, String.class, false)},
                0,
                Kleenean.FALSE,
                new ParseResult()
        ));
        SkriptQueue queue = queueExpression.getSingle(SkriptEvent.EMPTY);
        assertNotNull(queue);
        assertEquals(Arrays.asList("alpha", "beta"), queue);

        ExprDequeuedQueue dequeuedQueue = new ExprDequeuedQueue();
        assertTrue(dequeuedQueue.init(new Expression[]{new SimpleLiteral<>(queue, false)}, 0, Kleenean.FALSE, new ParseResult()));
        assertArrayEquals(new Object[]{"alpha", "beta"}, dequeuedQueue.getArray(SkriptEvent.EMPTY));

        ParseResult startParse = new ParseResult();
        startParse.tags.add("start");
        ExprQueueStartEnd start = new ExprQueueStartEnd();
        assertTrue(start.init(new Expression[]{new SimpleLiteral<>(queue(), false)}, 0, Kleenean.FALSE, startParse));
        assertEquals("first", start.getSingle(SkriptEvent.EMPTY));
        start.change(SkriptEvent.EMPTY, new Object[]{"zero"}, ChangeMode.ADD);
        assertEquals("zero", start.getSingle(SkriptEvent.EMPTY));

        ClassInfo<String> stringInfo = Classes.getExactClassInfo(String.class);
        assertNotNull(stringInfo);
        ParseResult eventParse = new ParseResult();
        eventParse.expr = "event-strings";
        ExprEventExpression eventExpression = new ExprEventExpression();
        assertTrue(eventExpression.init(new Expression[]{new SimpleLiteral<>(stringInfo, false)}, 0, Kleenean.FALSE, eventParse));

        ExprLoopIteration loopIteration = new ExprLoopIteration();
        assertFalse(loopIteration.init(new Expression<?>[1], 0, Kleenean.FALSE, new ParseResult()));
    }

    @Test
    void keyedRecursiveSetsFunctionAndFilterBehave() {
        TestKeyedStringExpression keyedSource = new TestKeyedStringExpression();

        ExprKeyed keyed = new ExprKeyed();
        assertTrue(keyed.init(new Expression[]{keyedSource}, 0, Kleenean.FALSE, new ParseResult()));
        assertArrayEquals(new String[]{"alpha", "beta"}, keyed.getArray(SkriptEvent.EMPTY));
        assertArrayEquals(new String[]{"a", "b"}, keyed.getArrayKeys(SkriptEvent.EMPTY));

        ExprRecursive recursive = new ExprRecursive();
        assertTrue(recursive.init(new Expression[]{new TestNestedStringExpression()}, 0, Kleenean.FALSE, new ParseResult()));
        assertArrayEquals(new String[]{"alpha", "beta"}, recursive.getArray(SkriptEvent.EMPTY));
        assertArrayEquals(new String[]{"branch::a", "branch::b"}, recursive.getArrayKeys(SkriptEvent.EMPTY));

        ClassInfo<String> stringInfo = Classes.getExactClassInfo(String.class);
        assertNotNull(stringInfo);
        ParseResult setsParse = new ParseResult();
        setsParse.expr = "every strings";
        ExprSets sets = new ExprSets();
        assertTrue(sets.init(new Expression[]{new SimpleLiteral<>(stringInfo, false)}, 0, Kleenean.FALSE, setsParse));
        assertArrayEquals(new String[]{"alpha", "beta"}, Arrays.copyOf(sets.getArray(SkriptEvent.EMPTY), 2, String[].class));

        registerEchoFunction();
        ExprFunction function = new ExprFunction();
        assertTrue(function.init(new Expression[]{new SimpleLiteral<>("lane_m6_echo", false), null}, 0, Kleenean.FALSE, new ParseResult()));
        DynamicFunctionReference<?> reference = function.getSingle(SkriptEvent.EMPTY);
        assertNotNull(reference);
        assertArrayEquals(new Object[]{"value"}, reference.execute(SkriptEvent.EMPTY, "value"));
    }

    private static void ensureSyntax() {
        if (syntaxRegistered) {
            return;
        }

        registerClassInfo(String.class, "string").supplier("alpha", "beta");
        registerClassInfo(Number.class, "number");
        registerClassInfo(Object.class, "object");
        registerClassInfo(SkriptQueue.class, "queue");

        Skript.registerExpression(TestStringExpression.class, String.class, "lane-m6-strings");
        Skript.registerExpression(TestNumberListExpression.class, Number.class, "lane-m6-number-list");
        Skript.registerExpression(TestQueueExpression.class, SkriptQueue.class, "lane-m6-queue");
        Skript.registerExpression(TestKeyedStringExpression.class, String.class, "lane-m6-keyed-strings");
        Skript.registerExpression(TestNestedStringExpression.class, String.class, "lane-m6-nested-strings");

        new ExprCaughtErrors();
        new ExprDequeuedQueue();
        new ExprEventExpression();
        new ExprFilter();
        new ExprFunction();
        new ExprKeyed();
        new ExprLoopIteration();
        new ExprQueue();
        new ExprQueueStartEnd();
        new ExprRecursive();
        new ExprRepeat();
        new ExprRound();
        new ExprSets();
        new ExprPercent();

        syntaxRegistered = true;
    }

    private static <T> ClassInfo<T> registerClassInfo(Class<T> type, String codeName) {
        ClassInfo<T> classInfo = Classes.getExactClassInfo(type);
        if (classInfo == null) {
            classInfo = new ClassInfo<>(type, codeName);
            Classes.registerClassInfo(classInfo);
        }
        return classInfo;
    }

    private static Expression<?> parseExpression(String input, Class<?>... returnTypes) {
        Expression<?> parsed = new SkriptParser(input, SkriptParser.ALL_FLAGS, ParseContext.DEFAULT).parseExpression(returnTypes);
        assertNotNull(parsed, input);
        return parsed;
    }

    private static void registerEchoFunction() {
        ClassInfo<String> stringInfo = Classes.getExactClassInfo(String.class);
        assertNotNull(stringInfo);
        Signature<String> signature = new Signature<>(
                null,
                "lane_m6_echo",
                new Parameter[]{new Parameter<>("value", stringInfo, true, null)},
                false,
                stringInfo,
                true
        );
        Functions.register(new Function<>(signature) {
            @Override
            public String @Nullable [] execute(FunctionEvent<?> event, Object[][] params) {
                return new String[]{String.valueOf(params[0][0])};
            }

            @Override
            public boolean resetReturnValue() {
                return true;
            }
        });
    }

    public static class TestStringExpression extends SimpleExpression<String> {
        @Override
        protected String[] get(SkriptEvent event) {
            return new String[]{"alpha", "beta"};
        }

        @Override
        public boolean isSingle() {
            return false;
        }

        @Override
        public Class<? extends String> getReturnType() {
            return String.class;
        }

        @Override
        public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
            return true;
        }

        @Override
        public String toString(@Nullable SkriptEvent event, boolean debug) {
            return "lane-m6-strings";
        }
    }

    public static class TestNumberListExpression extends SimpleExpression<Number> {
        @Override
        protected Number[] get(SkriptEvent event) {
            return new Number[]{1.2, 2.8};
        }

        @Override
        public boolean isSingle() {
            return false;
        }

        @Override
        public Class<? extends Number> getReturnType() {
            return Number.class;
        }

        @Override
        public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
            return true;
        }

        @Override
        public String toString(@Nullable SkriptEvent event, boolean debug) {
            return "lane-m6-number-list";
        }
    }

    public static class TestQueueExpression extends SimpleExpression<SkriptQueue> {
        @Override
        protected SkriptQueue[] get(SkriptEvent event) {
            return new SkriptQueue[]{queue()};
        }

        @Override
        public boolean isSingle() {
            return true;
        }

        @Override
        public Class<? extends SkriptQueue> getReturnType() {
            return SkriptQueue.class;
        }

        @Override
        public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
            return true;
        }

        @Override
        public String toString(@Nullable SkriptEvent event, boolean debug) {
            return "lane-m6-queue";
        }
    }

    public static class TestKeyedStringExpression extends SimpleExpression<String> implements KeyProviderExpression<String> {
        @Override
        protected String[] get(SkriptEvent event) {
            return new String[]{"alpha", "beta"};
        }

        @Override
        public String[] getArrayKeys(SkriptEvent event) {
            return new String[]{"a", "b"};
        }

        @Override
        public boolean isSingle() {
            return false;
        }

        @Override
        public Class<? extends String> getReturnType() {
            return String.class;
        }

        @Override
        public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
            return true;
        }

        @Override
        public String toString(@Nullable SkriptEvent event, boolean debug) {
            return "lane-m6-keyed-strings";
        }
    }

    public static final class TestNestedStringExpression extends TestKeyedStringExpression {
        @Override
        public String[] getArrayKeys(SkriptEvent event) {
            return new String[]{"branch::a", "branch::b"};
        }

        @Override
        public boolean returnNestedStructures(boolean nested) {
            return nested;
        }

        @Override
        public boolean returnsNestedStructures() {
            return true;
        }

        @Override
        public String toString(@Nullable SkriptEvent event, boolean debug) {
            return "lane-m6-nested-strings";
        }
    }

    private static SkriptQueue queue() {
        SkriptQueue queue = new SkriptQueue();
        queue.add("first");
        queue.add("second");
        return queue;
    }
}
