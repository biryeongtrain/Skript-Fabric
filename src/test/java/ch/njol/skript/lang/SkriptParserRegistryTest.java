package ch.njol.skript.lang;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import ch.njol.skript.ScriptLoader;
import ch.njol.skript.Skript;
import ch.njol.skript.classes.ClassInfo;
import ch.njol.skript.config.SectionNode;
import ch.njol.skript.config.SimpleNode;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.function.Function;
import ch.njol.skript.lang.function.FunctionEvent;
import ch.njol.skript.lang.function.FunctionRegistry;
import ch.njol.skript.lang.function.Functions;
import ch.njol.skript.lang.function.Parameter;
import ch.njol.skript.lang.function.Signature;
import ch.njol.skript.lang.parser.DefaultValueData;
import ch.njol.skript.lang.parser.ParserInstance;
import ch.njol.skript.lang.util.SimpleLiteral;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.skript.registrations.Classes;
import java.util.List;
import net.minecraft.world.damagesource.DamageSource;
import org.jetbrains.annotations.Nullable;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.skriptlang.skript.bukkit.base.types.LocationClassInfo;
import org.skriptlang.skript.lang.entry.EntryContainer;
import org.skriptlang.skript.registration.SyntaxInfo;
import org.skriptlang.skript.bukkit.damagesource.elements.ExprSecDamageSource;
import org.skriptlang.skript.bukkit.loottables.LootContextWrapper;
import org.skriptlang.skript.bukkit.loottables.elements.expressions.ExprSecCreateLootContext;
import org.skriptlang.skript.bukkit.potion.elements.expressions.ExprSecPotionEffect;
import org.skriptlang.skript.bukkit.potion.util.SkriptPotionEffect;

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
        MarkAwareEffect.lastMark = 0;
        OrderedTagAwareEffect.lastTags = List.of();
        BranchTagAwareSection.lastFirst = false;
        BranchTagAwareSection.lastSecond = false;
        AutoTagAwareSection.lastMin = false;
        AutoTagAwareSection.lastMax = false;
        OptionalRegexSection.lastCapturedText = null;
        TimeAwareCaptureEffect.lastTime = 0;
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
    void effectPatternPreservesSlashSeparatedPlaceholderUnions() {
        Skript.registerEffect(TypedArgsEffect.class, "union %integer/boolean%");

        Statement integer = Statement.parse("union 5", "failed");
        assertNotNull(integer);
        assertInstanceOf(TypedArgsEffect.class, integer);
        assertNotNull(TypedArgsEffect.lastExpressions);
        assertEquals(5, TypedArgsEffect.lastExpressions[0].getSingle(org.skriptlang.skript.lang.event.SkriptEvent.EMPTY));

        Statement bool = Statement.parse("union true", "failed");
        assertNotNull(bool);
        assertInstanceOf(TypedArgsEffect.class, bool);
        assertNotNull(TypedArgsEffect.lastExpressions);
        assertEquals(true, TypedArgsEffect.lastExpressions[0].getSingle(org.skriptlang.skript.lang.event.SkriptEvent.EMPTY));

        Statement string = Statement.parse("union hello", null);
        assertNull(string);
    }

    @Test
    void literalOnlyPlaceholderRejectsRegisteredExpressions() {
        Skript.registerExpression(NamedIntegerExpression.class, Integer.class, "named number");
        Skript.registerEffect(TypedArgsEffect.class, "literal only %*integer%");

        Statement expression = Statement.parse("literal only named number", null);
        Statement literal = Statement.parse("literal only 5", "failed");

        assertNull(expression);
        assertNotNull(literal);
        assertInstanceOf(TypedArgsEffect.class, literal);
        assertNotNull(TypedArgsEffect.lastExpressions);
        assertEquals(5, TypedArgsEffect.lastExpressions[0].getSingle(org.skriptlang.skript.lang.event.SkriptEvent.EMPTY));
    }

    @Test
    void expressionOnlyPlaceholderRejectsLiterals() {
        Skript.registerExpression(NamedIntegerExpression.class, Integer.class, "named number");
        Skript.registerEffect(TypedArgsEffect.class, "expression only %~integer%");

        Statement literal = Statement.parse("expression only 5", null);
        Statement expression = Statement.parse("expression only named number", "failed");

        assertNull(literal);
        assertNotNull(expression);
        assertInstanceOf(TypedArgsEffect.class, expression);
        assertNotNull(TypedArgsEffect.lastExpressions);
        assertEquals(7, TypedArgsEffect.lastExpressions[0].getSingle(org.skriptlang.skript.lang.event.SkriptEvent.EMPTY));
    }

    @Test
    void placeholderTimeStatePropagatesToParsedExpression() {
        Skript.registerExpression(TimeAwareStringExpression.class, String.class, "time aware text");
        Skript.registerEffect(TimeAwareCaptureEffect.class, "timed %string@1%");

        Statement parsed = Statement.parse("timed time aware text", "failed");

        assertNotNull(parsed);
        assertInstanceOf(TimeAwareCaptureEffect.class, parsed);
        assertEquals(1, TimeAwareCaptureEffect.lastTime);
    }

    @Test
    @SuppressWarnings("unchecked")
    void expressionPatternUsesParserDefaultValueForOmittedPlaceholder() {
        Classes.clearClassInfos();
        Classes.registerClassInfo(new ClassInfo<>(Integer.class, "number").defaultExpression(new SimpleLiteral<>(11, true)));
        Skript.registerExpression(DefaultNumberExpression.class, Integer.class, "default number [%number%]");

        DefaultValueData data = ParserInstance.get().getData(DefaultValueData.class);
        data.addDefaultValue(Integer.class, new SimpleLiteral<>(7, true));
        try {
            Expression<? extends Integer> omitted = new SkriptParser(
                    "default number",
                    SkriptParser.ALL_FLAGS,
                    ParseContext.DEFAULT
            ).parseExpression(new Class[]{Integer.class});
            Expression<? extends Integer> explicit = new SkriptParser(
                    "default number 5",
                    SkriptParser.ALL_FLAGS,
                    ParseContext.DEFAULT
            ).parseExpression(new Class[]{Integer.class});

            assertNotNull(omitted);
            assertInstanceOf(DefaultNumberExpression.class, omitted);
            assertEquals(7, omitted.getSingle(org.skriptlang.skript.lang.event.SkriptEvent.EMPTY));

            assertNotNull(explicit);
            assertInstanceOf(DefaultNumberExpression.class, explicit);
            assertEquals(5, explicit.getSingle(org.skriptlang.skript.lang.event.SkriptEvent.EMPTY));
        } finally {
            data.removeDefaultValue(Integer.class);
            Classes.clearClassInfos();
        }
    }

    @Test
    @SuppressWarnings("unchecked")
    void expressionPatternUsesClassInfoDefaultValueForExactOmittedPlaceholderForm() {
        Classes.clearClassInfos();
        Classes.registerClassInfo(new ClassInfo<>(Integer.class, "number").defaultExpression(new SimpleLiteral<>(11, true)));
        Skript.registerExpression(DefaultNumberExpression.class, Integer.class, "default number [%number%]");

        try {
            Expression<? extends Integer> omitted = new SkriptParser(
                    "default number",
                    SkriptParser.ALL_FLAGS,
                    ParseContext.DEFAULT
            ).parseExpression(new Class[]{Integer.class});
            Expression<? extends Integer> explicit = new SkriptParser(
                    "default number 5",
                    SkriptParser.ALL_FLAGS,
                    ParseContext.DEFAULT
            ).parseExpression(new Class[]{Integer.class});

            assertNotNull(omitted);
            assertInstanceOf(DefaultNumberExpression.class, omitted);
            assertEquals(11, omitted.getSingle(org.skriptlang.skript.lang.event.SkriptEvent.EMPTY));

            assertNotNull(explicit);
            assertInstanceOf(DefaultNumberExpression.class, explicit);
            assertEquals(5, explicit.getSingle(org.skriptlang.skript.lang.event.SkriptEvent.EMPTY));
        } finally {
            Classes.clearClassInfos();
        }
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
    void sectionPatternProvidesRawRegexCapturesInParseResult() {
        RegexCaptureSection.lastCapturedText = null;
        Skript.registerSection(RegexCaptureSection.class, "capture <.+>");

        SectionNode node = new SectionNode("capture hello world");
        Section parsed = Section.parse("capture hello world", null, node, List.of());

        assertNotNull(parsed);
        assertInstanceOf(RegexCaptureSection.class, parsed);
        assertEquals("hello world", RegexCaptureSection.lastCapturedText);
    }

    @Test
    void sectionPatternProvidesImplicitTagInParseResult() {
        TagAwareSection.lastImplicit = false;
        Skript.registerSection(TagAwareSection.class, "implicit:<.+>");

        SectionNode node = new SectionNode("always true");
        Section parsed = Section.parse("always true", null, node, List.of());

        assertNotNull(parsed);
        assertInstanceOf(TagAwareSection.class, parsed);
        assertTrue(TagAwareSection.lastImplicit);
    }

    @Test
    void effectPatternProvidesXorParseMarkInParseResult() {
        Skript.registerEffect(MarkAwareEffect.class, "1¦ping 3¦pong");

        Statement parsed = Statement.parse("ping pong", "failed");

        assertNotNull(parsed);
        assertInstanceOf(MarkAwareEffect.class, parsed);
        assertEquals(2, MarkAwareEffect.lastMark);
    }

    @Test
    void effectPatternPreservesRepeatedTagOrderInParseResult() {
        Skript.registerEffect(OrderedTagAwareEffect.class, "repeat:alpha unique:beta repeat:gamma");

        Statement parsed = Statement.parse("alpha beta gamma", "failed");

        assertNotNull(parsed);
        assertInstanceOf(OrderedTagAwareEffect.class, parsed);
        assertEquals(List.of("repeat", "unique", "repeat"), OrderedTagAwareEffect.lastTags);
    }

    @Test
    void effectPatternStillParsesChoiceKeywordSurfaceWithTrailingLiteral() {
        Skript.registerEffect(TestEffect.class, "(alpha|beta) gamma");

        Statement alpha = Statement.parse("alpha gamma", "failed");
        Statement beta = Statement.parse("beta gamma", "failed");

        assertNotNull(alpha);
        assertInstanceOf(TestEffect.class, alpha);
        assertNotNull(beta);
        assertInstanceOf(TestEffect.class, beta);
    }

    @Test
    void sectionPatternProvidesMatchedBranchParseTagInParseResult() {
        Skript.registerSection(BranchTagAwareSection.class, "guard (first:alpha|second:beta|gamma)");

        SectionNode node = new SectionNode("guard beta");
        Section parsed = Section.parse("guard beta", null, node, List.of());

        assertNotNull(parsed);
        assertInstanceOf(BranchTagAwareSection.class, parsed);
        assertFalse(BranchTagAwareSection.lastFirst);
        assertTrue(BranchTagAwareSection.lastSecond);
    }

    @Test
    void sectionPatternProvidesAutoDerivedChoiceBranchParseTagInParseResult() {
        Skript.registerSection(AutoTagAwareSection.class, "pick [:(min|max)[imum]] value");

        SectionNode minimumNode = new SectionNode("pick minimum value");
        Section minimum = Section.parse("pick minimum value", null, minimumNode, List.of());

        assertNotNull(minimum);
        assertInstanceOf(AutoTagAwareSection.class, minimum);
        assertTrue(AutoTagAwareSection.lastMin);
        assertFalse(AutoTagAwareSection.lastMax);

        AutoTagAwareSection.lastMin = false;
        AutoTagAwareSection.lastMax = false;

        SectionNode maximumNode = new SectionNode("pick maximum value");
        Section maximum = Section.parse("pick maximum value", null, maximumNode, List.of());

        assertNotNull(maximum);
        assertInstanceOf(AutoTagAwareSection.class, maximum);
        assertFalse(AutoTagAwareSection.lastMin);
        assertTrue(AutoTagAwareSection.lastMax);
    }

    @Test
    void sectionPatternAllowsOptionalRegexCaptureToBeOmitted() {
        Skript.registerSection(OptionalRegexSection.class, "watch [<.+>] target");

        SectionNode omittedNode = new SectionNode("watch target");
        Section omitted = Section.parse("watch target", null, omittedNode, List.of());

        assertNotNull(omitted);
        assertInstanceOf(OptionalRegexSection.class, omitted);
        assertNull(OptionalRegexSection.lastCapturedText);

        SectionNode presentNode = new SectionNode("watch nearby target");
        Section present = Section.parse("watch nearby target", null, presentNode, List.of());

        assertNotNull(present);
        assertInstanceOf(OptionalRegexSection.class, present);
        assertEquals("nearby", OptionalRegexSection.lastCapturedText);
    }

    @Test
    @SuppressWarnings({"rawtypes", "unchecked"})
    void customDamageSourceExpressionClaimsSectionWhenParsedAsObject() {
        Skript.registerExpression(
                ExprSecDamageSource.class,
                DamageSource.class,
                "[a] [custom] damage source [of [damage] type %-objects%]"
        );

        ParserInstance parser = ParserInstance.get();
        Section.SectionContext sectionContext = parser.getData(Section.SectionContext.class);
        SectionNode node = new SectionNode("set {_source} to a custom damage source:");
        node.add(new SimpleNode("set {_state} to \"inside\""));

        sectionContext.modify(node, List.of(), () -> {
            Expression<?> parsed = new SkriptParser(
                    "a custom damage source",
                    SkriptParser.ALL_FLAGS,
                    ParseContext.DEFAULT
            ).parseExpression(new Class[]{Object.class});

            assertNotNull(parsed);
            assertInstanceOf(ExprSecDamageSource.class, parsed);
            assertTrue(sectionContext.claimed());
            return null;
        });
    }

    @Test
    @SuppressWarnings({"rawtypes", "unchecked"})
    void potionEffectExpressionClaimsSectionWhenOptionalPartsAreOmitted() {
        Skript.registerExpression(
                ExprSecPotionEffect.class,
                (Class) SkriptPotionEffect.class,
                "[a[n]] [ambient] potion effect of %objects% [[of tier] %-number%] [for %-timespan%]",
                "[an] (infinite|permanent) [ambient] potion effect of %objects% [[of tier] %-number%]",
                "[an] (infinite|permanent) [ambient] %objects% [[of tier] %-number%] [potion [effect]]",
                "[a] potion effect [of %-objects%] (from|using|based on) %objects%"
        );

        ParserInstance parser = ParserInstance.get();
        Section.SectionContext sectionContext = parser.getData(Section.SectionContext.class);
        SectionNode node = new SectionNode("set {_effect} to a potion effect of poison:");
        node.add(new SimpleNode("set {_state} to \"inside\""));

        sectionContext.modify(node, List.of(), () -> {
            Expression<?> parsed = new SkriptParser(
                    "a potion effect of poison",
                    SkriptParser.ALL_FLAGS,
                    ParseContext.DEFAULT
            ).parseExpression(new Class[]{Object.class});

            assertNotNull(parsed);
            assertInstanceOf(ExprSecPotionEffect.class, parsed);
            assertTrue(sectionContext.claimed());
            return null;
        });
    }

    @Test
    @SuppressWarnings({"rawtypes", "unchecked"})
    void lootContextExpressionClaimsSectionWhenParsedAsObject() {
        LocationClassInfo.register();
        Skript.registerExpression(
                ExprSecCreateLootContext.class,
                (Class) LootContextWrapper.class,
                "[a] loot[ ]context at %locations%"
        );

        ParserInstance parser = ParserInstance.get();
        Section.SectionContext sectionContext = parser.getData(Section.SectionContext.class);
        SectionNode node = new SectionNode("set {_context} to a loot context at 1, 2, 3:");
        node.add(new SimpleNode("set {_state} to \"inside\""));

        sectionContext.modify(node, List.of(), () -> {
            Expression<?> parsed = new SkriptParser(
                    "a loot context at 1, 2, 3",
                    SkriptParser.ALL_FLAGS,
                    ParseContext.DEFAULT
            ).parseExpression(new Class[]{Object.class});

            assertNotNull(parsed);
            assertInstanceOf(ExprSecCreateLootContext.class, parsed);
            assertTrue(sectionContext.claimed());
            return null;
        });
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

    @Test
    void statementParseMatchesConditionWhenTerminalOptionalWordsAreOmitted() {
        Skript.registerExpression(DummyEventExpression.class, Object.class, "event-thing");
        Skript.registerCondition(DummyCondition.class, "%objects% is in lov(e|ing) [state|mode]");

        Statement parsed = Statement.parse("event-thing is in love", "failed");

        assertNotNull(parsed);
        assertInstanceOf(DummyCondition.class, parsed);
    }

    @Test
    void statementParseMatchesConditionWhenOptionalWhitespaceGroupIsOmitted() {
        Skript.registerExpression(DummyEventExpression.class, Object.class, "event-thing");
        Skript.registerCondition(
                DummyCondition.class,
                "[[the] text of] %objects% (has|have) [a] (drop|text) shadow"
        );

        Statement parsed = Statement.parse("event-thing has drop shadow", "failed");

        assertNotNull(parsed);
        assertInstanceOf(DummyCondition.class, parsed);
    }

    @Test
    void statementParseMatchesConditionWhenInlineOptionalSuffixIsOmitted() {
        Skript.registerExpression(DummyEventExpression.class, Object.class, "event-item");
        Skript.registerCondition(
                DummyCondition.class,
                "%objects% can be (equipped|put) on[to] entities"
        );

        Statement parsed = Statement.parse("event-item can be equipped on entities", "failed");

        assertNotNull(parsed);
        assertInstanceOf(DummyCondition.class, parsed);
    }

    @Test
    void statementParseMatchesConditionWhenOptionalMiddlePhraseIsOmitted() {
        Skript.registerExpression(DummyEventExpression.class, Object.class, "event-item");
        Skript.registerCondition(
                DummyCondition.class,
                "%objects% will (lose durability|be damaged) (on [wearer['s]] injury|when [[the] wearer [is]] (hurt|injured|damaged))"
        );

        Statement parsed = Statement.parse("event-item will lose durability when injured", "failed");

        assertNotNull(parsed);
        assertInstanceOf(DummyCondition.class, parsed);
    }

    @Test
    void statementParseMatchesEffectWhenInlineAlternativeNeedsTrailingWhitespace() {
        Skript.registerExpression(DummyEventExpression.class, Object.class, "event-entity");
        Skript.registerEffect(TestEffect.class, "make %objects% (not |non(-| )|un)breedable");

        Statement parsed = Statement.parse("make event-entity not breedable", "failed");

        assertNotNull(parsed);
        assertInstanceOf(TestEffect.class, parsed);
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

    public static class TagAwareSection extends Section {

        static boolean lastImplicit;

        @Override
        public boolean init(
                Expression<?>[] expressions,
                int matchedPattern,
                ch.njol.util.Kleenean isDelayed,
                ParseResult parseResult,
                @Nullable SectionNode sectionNode,
                @Nullable List<TriggerItem> triggerItems
        ) {
            lastImplicit = parseResult.hasTag("implicit");
            return true;
        }

        @Override
        protected @Nullable TriggerItem walk(org.skriptlang.skript.lang.event.SkriptEvent event) {
            return null;
        }

        @Override
        public String toString(@Nullable org.skriptlang.skript.lang.event.SkriptEvent event, boolean debug) {
            return "tag aware section";
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

    public static class MarkAwareEffect extends Effect {

        static int lastMark;

        @Override
        public boolean init(Expression<?>[] expressions, int matchedPattern, ch.njol.util.Kleenean isDelayed, ParseResult parseResult) {
            lastMark = parseResult.mark;
            return true;
        }

        @Override
        protected void execute(org.skriptlang.skript.lang.event.SkriptEvent event) {
        }

        @Override
        public String toString(@Nullable org.skriptlang.skript.lang.event.SkriptEvent event, boolean debug) {
            return "mark aware effect";
        }
    }

    public static class OrderedTagAwareEffect extends Effect {

        static List<String> lastTags = List.of();

        @Override
        public boolean init(Expression<?>[] expressions, int matchedPattern, ch.njol.util.Kleenean isDelayed, ParseResult parseResult) {
            lastTags = List.copyOf(parseResult.tags);
            return true;
        }

        @Override
        protected void execute(org.skriptlang.skript.lang.event.SkriptEvent event) {
        }

        @Override
        public String toString(@Nullable org.skriptlang.skript.lang.event.SkriptEvent event, boolean debug) {
            return "ordered tag aware effect";
        }
    }

    public static class BranchTagAwareSection extends Section {

        static boolean lastFirst;
        static boolean lastSecond;

        @Override
        public boolean init(
                Expression<?>[] expressions,
                int matchedPattern,
                ch.njol.util.Kleenean isDelayed,
                ParseResult parseResult,
                @Nullable SectionNode sectionNode,
                @Nullable List<TriggerItem> triggerItems
        ) {
            lastFirst = parseResult.hasTag("first");
            lastSecond = parseResult.hasTag("second");
            return true;
        }

        @Override
        protected @Nullable TriggerItem walk(org.skriptlang.skript.lang.event.SkriptEvent event) {
            return null;
        }

        @Override
        public String toString(@Nullable org.skriptlang.skript.lang.event.SkriptEvent event, boolean debug) {
            return "branch tag aware section";
        }
    }

    public static class AutoTagAwareSection extends Section {

        static boolean lastMin;
        static boolean lastMax;

        @Override
        public boolean init(
                Expression<?>[] expressions,
                int matchedPattern,
                ch.njol.util.Kleenean isDelayed,
                ParseResult parseResult,
                @Nullable SectionNode sectionNode,
                @Nullable List<TriggerItem> triggerItems
        ) {
            lastMin = parseResult.hasTag("min");
            lastMax = parseResult.hasTag("max");
            return true;
        }

        @Override
        protected @Nullable TriggerItem walk(org.skriptlang.skript.lang.event.SkriptEvent event) {
            return null;
        }

        @Override
        public String toString(@Nullable org.skriptlang.skript.lang.event.SkriptEvent event, boolean debug) {
            return "auto tag aware section";
        }
    }

    public static class DummyEventExpression extends SimpleExpression<Object> {

        @Override
        protected Object @Nullable [] get(org.skriptlang.skript.lang.event.SkriptEvent event) {
            return new Object[]{"event-thing"};
        }

        @Override
        public Class<?> getReturnType() {
            return Object.class;
        }

        @Override
        public boolean isSingle() {
            return true;
        }

        @Override
        public boolean init(
                Expression<?>[] expressions,
                int matchedPattern,
                ch.njol.util.Kleenean isDelayed,
                ParseResult parseResult
        ) {
            return expressions.length == 0;
        }

        @Override
        public String toString(@Nullable org.skriptlang.skript.lang.event.SkriptEvent event, boolean debug) {
            return "event-thing";
        }
    }

    public static class DummyCondition extends Condition {

        @Override
        public boolean init(
                Expression<?>[] expressions,
                int matchedPattern,
                ch.njol.util.Kleenean isDelayed,
                ParseResult parseResult
        ) {
            return expressions.length == 1;
        }

        @Override
        public boolean check(org.skriptlang.skript.lang.event.SkriptEvent event) {
            return true;
        }

        @Override
        public String toString(@Nullable org.skriptlang.skript.lang.event.SkriptEvent event, boolean debug) {
            return "dummy condition";
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

    public static class RegexCaptureSection extends Section {

        static @Nullable String lastCapturedText;

        @Override
        public boolean init(
                Expression<?>[] expressions,
                int matchedPattern,
                ch.njol.util.Kleenean isDelayed,
                ParseResult parseResult,
                @Nullable SectionNode sectionNode,
                @Nullable List<TriggerItem> triggerItems
        ) {
            if (parseResult.regexes.isEmpty()) {
                return false;
            }
            lastCapturedText = parseResult.regexes.getFirst().group();
            return sectionNode != null;
        }

        @Override
        protected @Nullable TriggerItem walk(org.skriptlang.skript.lang.event.SkriptEvent event) {
            return getNext();
        }

        @Override
        public String toString(@Nullable org.skriptlang.skript.lang.event.SkriptEvent event, boolean debug) {
            return "regex capture section";
        }
    }

    public static class OptionalRegexSection extends Section {

        static @Nullable String lastCapturedText;

        @Override
        public boolean init(
                Expression<?>[] expressions,
                int matchedPattern,
                ch.njol.util.Kleenean isDelayed,
                ParseResult parseResult,
                @Nullable SectionNode sectionNode,
                @Nullable List<TriggerItem> triggerItems
        ) {
            lastCapturedText = parseResult.regexes.isEmpty() ? null : parseResult.regexes.getFirst().group();
            return sectionNode != null;
        }

        @Override
        protected @Nullable TriggerItem walk(org.skriptlang.skript.lang.event.SkriptEvent event) {
            return getNext();
        }

        @Override
        public String toString(@Nullable org.skriptlang.skript.lang.event.SkriptEvent event, boolean debug) {
            return "optional regex section";
        }
    }

    public static class NamedIntegerExpression extends SimpleExpression<Integer> {

        @Override
        protected Integer @Nullable [] get(org.skriptlang.skript.lang.event.SkriptEvent event) {
            return new Integer[]{7};
        }

        @Override
        public Class<? extends Integer> getReturnType() {
            return Integer.class;
        }

        @Override
        public boolean isSingle() {
            return true;
        }

        @Override
        public boolean init(
                Expression<?>[] expressions,
                int matchedPattern,
                ch.njol.util.Kleenean isDelayed,
                ParseResult parseResult
        ) {
            return expressions.length == 0;
        }

        @Override
        public String toString(@Nullable org.skriptlang.skript.lang.event.SkriptEvent event, boolean debug) {
            return "named number";
        }
    }

    public static class DefaultNumberExpression extends SimpleExpression<Integer> {

        private @Nullable Expression<? extends Integer> value;

        @Override
        protected Integer @Nullable [] get(org.skriptlang.skript.lang.event.SkriptEvent event) {
            if (value == null) {
                return null;
            }
            Integer single = value.getSingle(event);
            return single == null ? null : new Integer[]{single};
        }

        @Override
        public Class<? extends Integer> getReturnType() {
            return Integer.class;
        }

        @Override
        public boolean isSingle() {
            return true;
        }

        @Override
        @SuppressWarnings("unchecked")
        public boolean init(
                Expression<?>[] expressions,
                int matchedPattern,
                ch.njol.util.Kleenean isDelayed,
                ParseResult parseResult
        ) {
            if (expressions.length != 1 || expressions[0] == null) {
                return false;
            }
            value = (Expression<? extends Integer>) expressions[0];
            return true;
        }

        @Override
        public String toString(@Nullable org.skriptlang.skript.lang.event.SkriptEvent event, boolean debug) {
            return "default number";
        }
    }

    public static class TimeAwareStringExpression extends SimpleExpression<String> {

        private int time;

        @Override
        protected String @Nullable [] get(org.skriptlang.skript.lang.event.SkriptEvent event) {
            return new String[]{"time-aware"};
        }

        @Override
        public Class<? extends String> getReturnType() {
            return String.class;
        }

        @Override
        public boolean isSingle() {
            return true;
        }

        @Override
        public boolean setTime(int time) {
            if (time != 1) {
                return false;
            }
            this.time = time;
            return true;
        }

        @Override
        public int getTime() {
            return time;
        }

        @Override
        public boolean init(
                Expression<?>[] expressions,
                int matchedPattern,
                ch.njol.util.Kleenean isDelayed,
                ParseResult parseResult
        ) {
            return expressions.length == 0;
        }

        @Override
        public String toString(@Nullable org.skriptlang.skript.lang.event.SkriptEvent event, boolean debug) {
            return "time aware text";
        }
    }

    public static class TimeAwareCaptureEffect extends Effect {

        static int lastTime;

        @Override
        public boolean init(
                Expression<?>[] expressions,
                int matchedPattern,
                ch.njol.util.Kleenean isDelayed,
                ParseResult parseResult
        ) {
            if (expressions.length != 1 || expressions[0] == null) {
                return false;
            }
            lastTime = expressions[0].getTime();
            return true;
        }

        @Override
        protected void execute(org.skriptlang.skript.lang.event.SkriptEvent event) {
        }

        @Override
        public String toString(@Nullable org.skriptlang.skript.lang.event.SkriptEvent event, boolean debug) {
            return "time aware capture effect";
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
