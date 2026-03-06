package ch.njol.skript.lang;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import ch.njol.skript.ScriptLoader;
import ch.njol.skript.Skript;
import ch.njol.skript.config.SectionNode;
import ch.njol.skript.config.SimpleNode;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.function.Function;
import ch.njol.skript.lang.function.FunctionEvent;
import ch.njol.skript.lang.function.FunctionRegistry;
import ch.njol.skript.lang.function.Functions;
import ch.njol.skript.lang.function.Parameter;
import ch.njol.skript.lang.function.Signature;
import ch.njol.skript.lang.parser.ParserInstance;
import ch.njol.skript.registrations.Classes;
import java.util.List;
import org.jetbrains.annotations.Nullable;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.skriptlang.skript.lang.entry.EntryContainer;
import org.skriptlang.skript.registration.SyntaxInfo;

class SkriptParserRegistryTest {

    @BeforeEach
    void resetRegistry() {
        Skript.instance().syntaxRegistry().clearAll();
        FunctionRegistry.getRegistry().clear();
        Functions.clear();
        TestEffect.initCalls = 0;
        TypedArgsEffect.lastExpressions = null;
        TestStructure.initCalls = 0;
        TestEvent.initCalls = 0;
        NodeAwareEffect.lastNodeKey = null;
    }

    @Test
    void effectPatternParsesAndInitializes() {
        Skript.registerEffect(TestEffect.class, "[the] ping");

        Statement first = Statement.parse("ping", "failed");
        Statement second = Statement.parse("the ping", "failed");

        assertInstanceOf(TestEffect.class, first);
        assertInstanceOf(TestEffect.class, second);
        assertEquals(2, TestEffect.initCalls);
    }

    @Test
    void nonMatchingEffectReturnsNull() {
        Skript.registerEffect(TestEffect.class, "ping");
        assertNull(Statement.parse("pong", null));
    }

    @Test
    void effectPatternParsesTypedPlaceholderExpressions() {
        Skript.registerEffect(TypedArgsEffect.class, "typed %integer% and %string%");

        Statement parsed = Statement.parse("typed 5 and hello", "failed");

        assertNotNull(parsed);
        assertInstanceOf(TypedArgsEffect.class, parsed);
        assertNotNull(TypedArgsEffect.lastExpressions);
        assertEquals(2, TypedArgsEffect.lastExpressions.length);
        assertEquals(5, TypedArgsEffect.lastExpressions[0].getSingle(org.skriptlang.skript.lang.event.SkriptEvent.EMPTY));
        assertEquals("hello", TypedArgsEffect.lastExpressions[1].getSingle(org.skriptlang.skript.lang.event.SkriptEvent.EMPTY));
    }

    @Test
    void statementParseRecognisesFunctionCalls() {
        registerEchoFunction();

        Statement parsed = Statement.parse("echo(\"abc\")", "failed");

        assertNotNull(parsed);
        assertInstanceOf(ch.njol.skript.lang.function.EffFunctionCall.class, parsed);
    }

    @Test
    void statementParseRejectsFunctionCallsAsSectionsWithoutOwner() {
        registerEchoFunction();

        SectionNode node = new SectionNode("echo(\"abc\"):");

        Statement parsed = Statement.parse("echo(\"abc\")", null, node, List.of());

        assertNull(parsed);
    }

    @Test
    void structureParseUsesModernStructureInfo() {
        Skript.registerStructure(TestStructure.class, SyntaxInfo.Structure.NodeType.SIMPLE, "dummy structure");

        org.skriptlang.skript.lang.structure.Structure parsed = org.skriptlang.skript.lang.structure.Structure.parse(
                "dummy structure",
                new SimpleNode("dummy structure"),
                "failed"
        );

        assertNotNull(parsed);
        assertInstanceOf(TestStructure.class, parsed);
        assertEquals(1, TestStructure.initCalls);
    }

    @Test
    void eventParseDoesNotRequireLegacyEventInfoCast() {
        Skript.registerEvent(TestEvent.class, "dummy event");

        ch.njol.skript.lang.SkriptEvent parsed = ch.njol.skript.lang.SkriptEvent.parse(
                "dummy event",
                new SectionNode("dummy event"),
                "failed"
        );

        assertNotNull(parsed);
        assertInstanceOf(TestEvent.class, parsed);
        assertEquals(1, TestEvent.initCalls);
    }

    @Test
    void scriptLoaderChainsLoadedItemsInOrder() {
        Skript.registerEffect(TestEffect.class, "ping");
        Skript.registerEffect(SecondEffect.class, "pong");

        SectionNode root = new SectionNode("root");
        root.add(new SimpleNode("ping"));
        root.add(new SimpleNode("pong"));

        List<TriggerItem> items = ScriptLoader.loadItems(root);

        assertEquals(2, items.size());
        assertInstanceOf(TestEffect.class, items.get(0));
        assertInstanceOf(SecondEffect.class, items.get(1));
        assertEquals(items.get(1), items.get(0).getNext());
    }

    @Test
    void scriptLoaderSetsAndRestoresParserNodeContext() {
        Skript.registerEffect(NodeAwareEffect.class, "node aware");

        ParserInstance parser = ParserInstance.get();
        SimpleNode sentinel = new SimpleNode("sentinel");
        parser.setNode(sentinel);

        SectionNode root = new SectionNode("root");
        root.add(new SimpleNode("node aware"));
        ScriptLoader.loadItems(root);

        assertEquals("node aware", NodeAwareEffect.lastNodeKey);
        assertEquals(sentinel, parser.getNode());
    }

    public static class TestEffect extends Effect {

        static int initCalls;

        @Override
        public boolean init(Expression<?>[] expressions, int matchedPattern, ch.njol.util.Kleenean isDelayed, ParseResult parseResult) {
            initCalls++;
            return true;
        }

        @Override
        protected void execute(org.skriptlang.skript.lang.event.SkriptEvent event) {
        }

        @Override
        public String toString(@Nullable org.skriptlang.skript.lang.event.SkriptEvent event, boolean debug) {
            return "test effect";
        }
    }

    public static class TypedArgsEffect extends Effect {

        static @Nullable Expression<?>[] lastExpressions;

        @Override
        public boolean init(Expression<?>[] expressions, int matchedPattern, ch.njol.util.Kleenean isDelayed, ParseResult parseResult) {
            lastExpressions = expressions;
            return true;
        }

        @Override
        protected void execute(org.skriptlang.skript.lang.event.SkriptEvent event) {
        }

        @Override
        public String toString(@Nullable org.skriptlang.skript.lang.event.SkriptEvent event, boolean debug) {
            return "typed args effect";
        }
    }

    public static class TestStructure extends org.skriptlang.skript.lang.structure.Structure {

        static int initCalls;

        @Override
        public boolean init(
                Literal<?>[] args,
                int matchedPattern,
                ParseResult parseResult,
                @Nullable EntryContainer entryContainer
        ) {
            initCalls++;
            return true;
        }

        @Override
        public boolean load() {
            return true;
        }

        @Override
        public String toString(@Nullable org.skriptlang.skript.lang.event.SkriptEvent event, boolean debug) {
            return "test structure";
        }
    }

    public static class TestEvent extends ch.njol.skript.lang.SkriptEvent {

        static int initCalls;

        @Override
        public boolean init(Literal<?>[] args, int matchedPattern, ParseResult parseResult) {
            initCalls++;
            return true;
        }

        @Override
        public boolean check(org.skriptlang.skript.lang.event.SkriptEvent event) {
            return true;
        }

        @Override
        public String toString(@Nullable org.skriptlang.skript.lang.event.SkriptEvent event, boolean debug) {
            return "test event";
        }
    }

    public static class SecondEffect extends Effect {

        @Override
        protected void execute(org.skriptlang.skript.lang.event.SkriptEvent event) {
        }

        @Override
        public String toString(@Nullable org.skriptlang.skript.lang.event.SkriptEvent event, boolean debug) {
            return "second effect";
        }
    }

    public static class NodeAwareEffect extends Effect {

        static @Nullable String lastNodeKey;

        @Override
        public boolean init(Expression<?>[] expressions, int matchedPattern, ch.njol.util.Kleenean isDelayed, ParseResult parseResult) {
            var node = getParser().getNode();
            lastNodeKey = node != null ? node.getKey() : null;
            return true;
        }

        @Override
        protected void execute(org.skriptlang.skript.lang.event.SkriptEvent event) {
        }

        @Override
        public String toString(@Nullable org.skriptlang.skript.lang.event.SkriptEvent event, boolean debug) {
            return "node aware effect";
        }
    }

    private static void registerEchoFunction() {
        Signature<String> signature = new Signature<>(
                null,
                "echo",
                new Parameter[]{new Parameter<>("value", Classes.getSuperClassInfo(String.class), true, null)},
                false,
                Classes.getSuperClassInfo(String.class),
                true
        );
        Functions.register(new Function<>(signature) {
            @Override
            public String[] execute(FunctionEvent<?> event, Object[][] params) {
                if (params.length == 0 || params[0] == null || params[0].length == 0) {
                    return new String[0];
                }
                return new String[]{String.valueOf(params[0][0])};
            }

            @Override
            public boolean resetReturnValue() {
                return true;
            }
        });
    }
}
