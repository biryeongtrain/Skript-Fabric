package ch.njol.skript.sections;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import ch.njol.skript.ScriptLoader;
import ch.njol.skript.Skript;
import ch.njol.skript.config.Node;
import ch.njol.skript.config.SectionNode;
import ch.njol.skript.config.SimpleNode;
import ch.njol.skript.lang.Condition;
import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.TriggerItem;
import ch.njol.skript.lang.parser.ParserInstance;
import ch.njol.util.Kleenean;
import java.util.ArrayList;
import java.util.List;
import org.jetbrains.annotations.Nullable;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.Appender;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.Logger;
import org.apache.logging.log4j.core.appender.AbstractAppender;
import org.apache.logging.log4j.core.layout.PatternLayout;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.skriptlang.skript.bukkit.base.conditions.CondCompare;
import org.skriptlang.skript.bukkit.base.effects.EffChange;
import org.skriptlang.skript.bukkit.base.effects.EffSetTestBlock;
import org.skriptlang.skript.lang.event.SkriptEvent;

class SecIfCompatibilityTest {

    @BeforeEach
    void registerSyntax() {
        Skript.instance().syntaxRegistry().clearAll();
        MarkEffect.executed.clear();
        TrackingEffect.reset();
        SecIf.register();
        Skript.registerCondition(TrueCondition.class, "always true");
        Skript.registerCondition(FalseCondition.class, "always false");
        Skript.registerEffect(
                MarkEffect.class,
                "mark if",
                "mark else if",
                "mark else",
                "mark after"
        );
        Skript.registerEffect(TrackingEffect.class, "track body");
    }

    @AfterEach
    void cleanup() {
        Skript.instance().syntaxRegistry().clearAll();
        ParserInstance.get().setNode(null);
        MarkEffect.executed.clear();
        TrackingEffect.reset();
    }

    @Test
    void ifBranchSkipsRemainingChain() {
        List<TriggerItem> items = ScriptLoader.loadItems(root(
                section("if always true", line("mark if")),
                section("else if always true", line("mark else if")),
                section("else", line("mark else")),
                line("mark after")
        ));

        TriggerItem.walk(items.getFirst(), SkriptEvent.EMPTY);

        assertEquals(List.of("if", "after"), MarkEffect.executed);
    }

    @Test
    void elseIfBranchRunsWhenPriorIfFails() {
        List<TriggerItem> items = ScriptLoader.loadItems(root(
                section("if always false", line("mark if")),
                section("else if always true", line("mark else if")),
                section("else", line("mark else")),
                line("mark after")
        ));

        TriggerItem.walk(items.getFirst(), SkriptEvent.EMPTY);

        assertEquals(List.of("else if", "after"), MarkEffect.executed);
    }

    @Test
    void elseBranchRunsWhenEarlierConditionsFail() {
        List<TriggerItem> items = ScriptLoader.loadItems(root(
                section("if always false", line("mark if")),
                section("else if always false", line("mark else if")),
                section("else", line("mark else")),
                line("mark after")
        ));

        TriggerItem.walk(items.getFirst(), SkriptEvent.EMPTY);

        assertEquals(List.of("else", "after"), MarkEffect.executed);
    }

    @Test
    void parseIfFalseSkipsBodyInitialization() {
        List<TriggerItem> items = ScriptLoader.loadItems(root(
                section("parse if always false", line("track body")),
                line("mark after")
        ));

        assertEquals(2, items.size());
        assertEquals(0, TrackingEffect.initCount);

        TriggerItem.walk(items.getFirst(), SkriptEvent.EMPTY);

        assertEquals(0, TrackingEffect.executeCount);
        assertEquals(List.of("after"), MarkEffect.executed);
    }

    @Test
    void parseIfTrueInitializesAndRunsBody() {
        List<TriggerItem> items = ScriptLoader.loadItems(root(
                section("parse if always true", line("track body")),
                line("mark after")
        ));

        assertEquals(2, items.size());
        assertEquals(1, TrackingEffect.initCount);

        TriggerItem.walk(items.getFirst(), SkriptEvent.EMPTY);

        assertEquals(1, TrackingEffect.executeCount);
        assertEquals(List.of("after"), MarkEffect.executed);
    }

    @Test
    void elseParseIfRunsWhenPriorIfFails() {
        List<TriggerItem> items = ScriptLoader.loadItems(root(
                section("if always false", line("mark if")),
                section("else parse if always true", line("track body")),
                section("else", line("mark else")),
                line("mark after")
        ));

        assertEquals(4, items.size());
        assertEquals(1, TrackingEffect.initCount);

        TriggerItem.walk(items.getFirst(), SkriptEvent.EMPTY);

        assertEquals(1, TrackingEffect.executeCount);
        assertEquals(List.of("after"), MarkEffect.executed);
    }

    @Test
    void elseParseIfFalseFallsThroughToElse() {
        List<TriggerItem> items = ScriptLoader.loadItems(root(
                section("if always false", line("mark if")),
                section("else parse if always false", line("track body")),
                section("else", line("mark else")),
                line("mark after")
        ));

        assertEquals(4, items.size());
        assertEquals(0, TrackingEffect.initCount);

        TriggerItem.walk(items.getFirst(), SkriptEvent.EMPTY);

        assertEquals(0, TrackingEffect.executeCount);
        assertEquals(List.of("else", "after"), MarkEffect.executed);
    }

    @Test
    void ifAnyRunsThenSectionWhenAnyConditionMatches() {
        List<TriggerItem> items = ScriptLoader.loadItems(root(
                section("if any", line("always false"), line("always true")),
                section("then", line("track body")),
                section("else", line("mark else")),
                line("mark after")
        ));

        assertEquals(4, items.size());
        assertEquals(1, TrackingEffect.initCount);

        TriggerItem.walk(items.getFirst(), SkriptEvent.EMPTY);

        assertEquals(1, TrackingEffect.executeCount);
        assertEquals(List.of("after"), MarkEffect.executed);
    }

    @Test
    void ifAllFallsThroughToElseWhenAnyConditionFails() {
        List<TriggerItem> items = ScriptLoader.loadItems(root(
                section("if all", line("always true"), line("always false")),
                section("then", line("track body")),
                section("else", line("mark else")),
                line("mark after")
        ));

        assertEquals(4, items.size());
        assertEquals(1, TrackingEffect.initCount);

        TriggerItem.walk(items.getFirst(), SkriptEvent.EMPTY);

        assertEquals(0, TrackingEffect.executeCount);
        assertEquals(List.of("else", "after"), MarkEffect.executed);
    }

    @Test
    void elseIfAnyRunsThenWhenPriorIfFails() {
        List<TriggerItem> items = ScriptLoader.loadItems(root(
                section("if always false", line("mark if")),
                section("else if any", line("always false"), line("always true")),
                section("then", line("track body")),
                section("else", line("mark else")),
                line("mark after")
        ));

        assertEquals(5, items.size());
        assertEquals(1, TrackingEffect.initCount);

        TriggerItem.walk(items.getFirst(), SkriptEvent.EMPTY);

        assertEquals(1, TrackingEffect.executeCount);
        assertEquals(List.of("after"), MarkEffect.executed);
    }

    @Test
    void implicitConditionalRunsAsIfSection() {
        List<TriggerItem> items = ScriptLoader.loadItems(root(
                section("always true", line("track body")),
                section("else", line("mark else")),
                line("mark after")
        ));

        assertEquals(3, items.size());
        assertEquals(1, TrackingEffect.initCount);

        TriggerItem.walk(items.getFirst(), SkriptEvent.EMPTY);

        assertEquals(1, TrackingEffect.executeCount);
        assertEquals(List.of("after"), MarkEffect.executed);
    }

    @Test
    void explicitInvalidIfRetainsSpecificConditionError() {
        try (TestLogAppender logs = TestLogAppender.attach()) {
            List<TriggerItem> items = ScriptLoader.loadItems(root(
                    section("if not a real condition", line("track body"))
            ));

            assertTrue(items.isEmpty());
            assertTrue(
                    logs.messages().stream().anyMatch(message ->
                            message.contains("Can't understand this condition: 'not a real condition'")
                    )
            );
        }
    }

    @Test
    void thenWithoutMultilineConditionalIsRejected() {
        List<TriggerItem> items = ScriptLoader.loadItems(root(
                section("then", line("track body"))
        ));

        assertTrue(items.isEmpty());
    }

    @Test
    void orphanElseSectionIsRejected() {
        List<TriggerItem> items = ScriptLoader.loadItems(root(
                section("else", line("mark else"))
        ));

        assertTrue(items.isEmpty());
    }

    @Test
    void realVariableCompareSyntaxStillBuildsConditionalChain() {
        Skript.instance().syntaxRegistry().clearAll();
        SecIf.register();
        Skript.registerCondition(
                CondCompare.class,
                "%objects% (is|are) %objects%",
                "%objects% (isn't|is not|aren't|are not) %objects%"
        );
        Skript.registerEffect(
                EffChange.class,
                "set %object% to %object%",
                "add %object% to %object%",
                "remove %object% from %object%",
                "reset %object%",
                "delete %object%"
        );
        Skript.registerEffect(
                EffSetTestBlock.class,
                "set test block at %integer% %integer% %integer% to %string%"
        );

        List<TriggerItem> items = ScriptLoader.loadItems(root(
                line("set {branch} to 2"),
                section("if {branch} is 1", line("set test block at 0 1 0 to diamond_block")),
                section("else if {branch} is 2", line("set test block at 0 1 0 to gold_block")),
                section("else", line("set test block at 0 1 0 to emerald_block"))
        ));

        assertEquals(4, items.size());
        assertTrue(items.get(1) instanceof SecIf);
        assertTrue(items.get(2) instanceof SecIf);
        assertTrue(items.get(3) instanceof SecIf);
    }

    @Test
    void parenthesizedVariableCompareSyntaxStillBuildsConditionalChain() {
        Skript.instance().syntaxRegistry().clearAll();
        SecIf.register();
        Skript.registerCondition(
                CondCompare.class,
                "%objects% (is|are) %objects%",
                "%objects% (isn't|is not|aren't|are not) %objects%"
        );
        Skript.registerEffect(
                EffChange.class,
                "set %object% to %object%",
                "add %object% to %object%",
                "remove %object% from %object%",
                "reset %object%",
                "delete %object%"
        );
        Skript.registerEffect(
                EffSetTestBlock.class,
                "set test block at %integer% %integer% %integer% to %string%"
        );

        List<TriggerItem> items = ScriptLoader.loadItems(root(
                line("set {branch} to 2"),
                section("if (({branch} is 1))", line("set test block at 0 1 0 to diamond_block")),
                section("else if ((({branch} is 2)))", line("set test block at 0 1 0 to gold_block")),
                section("else", line("set test block at 0 1 0 to emerald_block"))
        ));

        assertEquals(4, items.size());
        assertTrue(items.get(1) instanceof SecIf);
        assertTrue(items.get(2) instanceof SecIf);
        assertTrue(items.get(3) instanceof SecIf);
    }

    private static SectionNode root(Node... children) {
        SectionNode root = new SectionNode("root");
        for (Node child : children) {
            root.add(child);
        }
        return root;
    }

    private static SectionNode section(String key, Node... children) {
        SectionNode sectionNode = new SectionNode(key);
        for (Node child : children) {
            sectionNode.add(child);
        }
        return sectionNode;
    }

    private static SimpleNode line(String key) {
        return new SimpleNode(key);
    }

    public static final class TrueCondition extends Condition {

        @Override
        public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
            return true;
        }

        @Override
        public boolean check(SkriptEvent event) {
            return true;
        }

        @Override
        public String toString(@Nullable SkriptEvent event, boolean debug) {
            return "always true";
        }
    }

    public static final class FalseCondition extends Condition {

        @Override
        public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
            return true;
        }

        @Override
        public boolean check(SkriptEvent event) {
            return false;
        }

        @Override
        public String toString(@Nullable SkriptEvent event, boolean debug) {
            return "always false";
        }
    }

    public static final class MarkEffect extends Effect {

        private static final List<String> MARKERS = List.of("if", "else if", "else", "after");
        private static final List<String> executed = new ArrayList<>();

        private String marker = "unknown";

        @Override
        public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
            marker = MARKERS.get(matchedPattern);
            return true;
        }

        @Override
        protected void execute(SkriptEvent event) {
            executed.add(marker);
        }

        @Override
        public String toString(@Nullable SkriptEvent event, boolean debug) {
            return "mark " + marker;
        }
    }

    public static final class TrackingEffect extends Effect {

        private static int initCount;
        private static int executeCount;

        static void reset() {
            initCount = 0;
            executeCount = 0;
        }

        @Override
        public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
            initCount++;
            return true;
        }

        @Override
        protected void execute(SkriptEvent event) {
            executeCount++;
        }

        @Override
        public String toString(@Nullable SkriptEvent event, boolean debug) {
            return "track body";
        }
    }

    private static final class TestLogAppender extends AbstractAppender implements AutoCloseable {

        private final List<String> messages = new ArrayList<>();
        private final Logger logger;

        private TestLogAppender(Logger logger) {
            super("SecIfCompatibilityTest", null, PatternLayout.createDefaultLayout(), false, null);
            this.logger = logger;
        }

        static TestLogAppender attach() {
            Logger logger = (Logger) LogManager.getLogger("skfabric");
            TestLogAppender appender = new TestLogAppender(logger);
            appender.start();
            appender.logger.addAppender(appender);
            return appender;
        }

        @Override
        public void append(LogEvent event) {
            messages.add(event.getMessage().getFormattedMessage());
        }

        List<String> messages() {
            return List.copyOf(messages);
        }

        @Override
        public void close() {
            logger.removeAppender((Appender) this);
            stop();
        }
    }
}
