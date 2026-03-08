package ch.njol.skript;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import ch.njol.skript.config.EntryNode;
import ch.njol.skript.config.Node;
import ch.njol.skript.config.SectionNode;
import ch.njol.skript.config.SimpleNode;
import ch.njol.skript.lang.Condition;
import ch.njol.skript.lang.ExecutionIntent;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.Section;
import ch.njol.skript.lang.Statement;
import ch.njol.skript.lang.TriggerItem;
import ch.njol.skript.lang.Variable;
import ch.njol.skript.lang.parser.ParserInstance;
import ch.njol.skript.structures.StructOptions;
import ch.njol.skript.variables.Variables;
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
import org.junit.jupiter.api.Test;
import org.skriptlang.skript.bukkit.base.effects.EffChange;
import org.skriptlang.skript.bukkit.itemcomponents.equippable.EquippableWrapper;
import org.skriptlang.skript.bukkit.itemcomponents.equippable.elements.ExprSecBlankEquipComp;
import org.skriptlang.skript.lang.event.SkriptEvent;
import org.skriptlang.skript.lang.script.Script;
import org.skriptlang.skript.lang.script.ScriptWarning;
import org.skriptlang.skript.lang.structure.Structure;

class ScriptLoaderCompatibilityTest {

    private static final List<String> statementExecution = new ArrayList<>();

    @AfterEach
    void cleanupParserState() {
        Skript.instance().syntaxRegistry().clearAll();
        ParserInstance.get().setCurrentScript(null);
        RecordingSection.executed.clear();
        RecordingEffect.executed.clear();
        statementExecution.clear();
        Variables.clearAll();
    }

    @Test
    void replaceOptionsUsesCurrentScriptOptionsData() {
        ParserInstance parser = ParserInstance.get();
        Script script = new Script(null, java.util.List.of());
        ScriptLoader.OptionsData optionsData = script.getData(ScriptLoader.OptionsData.class, ScriptLoader.OptionsData::new);
        optionsData.put("message.prefix", "[fabric]");
        optionsData.put("marker", "emerald_block");
        parser.setCurrentScript(script);

        assertEquals(
                "set test block at 0 1 0 to emerald_block and name to [fabric]",
                ScriptLoader.replaceOptions("set test block at 0 1 0 to {@marker} and name to {@message.prefix}")
        );
    }

    @Test
    void replaceOptionsLeavesUnknownOptionsUntouched() {
        ParserInstance parser = ParserInstance.get();
        Script script = new Script(null, java.util.List.of());
        parser.setCurrentScript(script);

        assertEquals("send {@missing}", ScriptLoader.replaceOptions("send {@missing}"));
    }

    @Test
    void loadItemsParsesRegisteredSectionsBeforeFallingBackToStatements() {
        Skript.registerSection(RecordingSection.class, "record section");
        Skript.registerEffect(RecordingEffect.class, "mark inside", "mark after");

        List<TriggerItem> items = ScriptLoader.loadItems(root(
                section("record section", line("mark inside")),
                line("mark after")
        ));

        assertEquals(2, items.size());
        assertTrue(items.getFirst() instanceof RecordingSection);

        TriggerItem.walk(items.getFirst(), SkriptEvent.EMPTY);

        assertEquals(List.of("section", "inside", "after"), RecordingSection.executed);
    }

    @Test
    void optionsStructureConvertsSimpleNodesIntoEntriesBeforeLoading() {
        StructOptions.register();
        ParserInstance parser = ParserInstance.get();
        Script script = new Script(null, java.util.List.of());
        parser.setCurrentScript(script);

        SectionNode options = section(
                "options",
                line("marker: emerald_block"),
                section("blocks", line("nested: diamond_block"))
        );

        Structure structure = Structure.parse("options", options, null);

        assertTrue(structure instanceof StructOptions);
        assertTrue(structure.load());
        assertEquals(
                "set test block to emerald_block and diamond_block",
                ScriptLoader.replaceOptions("set test block to {@marker} and {@blocks.nested}")
        );
    }

    @Test
    void optionsStructureLoadsRuntimeStyleEntryNodesThroughValidator() {
        StructOptions.register();
        ParserInstance parser = ParserInstance.get();
        Script script = new Script(null, java.util.List.of());
        parser.setCurrentScript(script);

        SectionNode options = section(
                "options",
                section("blocks", new EntryNode("marker", "emerald_block"))
        );

        Structure structure = Structure.parse("options", options, null);

        assertTrue(structure instanceof StructOptions);
        assertTrue(structure.load());
        assertEquals(
                "set test block to emerald_block",
                ScriptLoader.replaceOptions("set test block to {@blocks.marker}")
        );
    }

    @Test
    void optionsStructureLogsInvalidNestedSimpleLinesWithoutRejectingValidEntries() {
        StructOptions.register();
        ParserInstance parser = ParserInstance.get();
        Script script = new Script(null, java.util.List.of());
        parser.setCurrentScript(script);

        SectionNode options = section(
                "options",
                section(
                        "blocks",
                        line("invalid nested option line"),
                        line("marker: emerald_block")
                )
        );

        try (TestLogAppender logs = TestLogAppender.attach()) {
            Structure structure = Structure.parse("options", options, null);

            assertTrue(structure instanceof StructOptions);
            assertTrue(structure.load());
            assertEquals("emerald_block", ScriptLoader.replaceOptions("{@blocks.marker}"));
            assertTrue(
                    logs.messages().stream().anyMatch(message -> message.contains("Invalid line in options"))
            );
        }
    }

    @Test
    void loadItemsLogsUnknownSimpleLine() {
        try (TestLogAppender logs = TestLogAppender.attach()) {
            List<TriggerItem> items = ScriptLoader.loadItems(root(
                    line("this syntax does not exist")
            ));

            assertTrue(items.isEmpty());
            assertTrue(
                    logs.messages().stream().anyMatch(message ->
                            message.contains("Can't understand this condition/effect: this syntax does not exist")
                    )
            );
        }
    }

    @Test
    void loadItemsLogsUnknownSectionLine() {
        try (TestLogAppender logs = TestLogAppender.attach()) {
            List<TriggerItem> items = ScriptLoader.loadItems(root(
                    section("this section syntax does not exist", line("mark inside"))
            ));

            assertTrue(items.isEmpty());
            assertTrue(
                    logs.messages().stream().anyMatch(message ->
                            message.contains("Can't understand this section: this section syntax does not exist")
                    )
            );
        }
    }

    @Test
    void loadItemsRejectsMalformedQuotedLineBeforeParsing() {
        try (TestLogAppender logs = TestLogAppender.attach()) {
            List<TriggerItem> items = ScriptLoader.loadItems(root(
                    line("set {value} to \"unterminated")
            ));

            assertTrue(items.isEmpty());
            assertTrue(
                    logs.messages().stream().anyMatch(message ->
                            message.contains("Unmatched double quotes in line: set {value} to \"unterminated")
                    )
            );
        }
    }

    @Test
    @SuppressWarnings({"rawtypes", "unchecked"})
    void loadItemsLetsPlainEffectsOwnSectionManagingExpressions() {
        Skript.registerEffect(
                EffChange.class,
                "set %object% to %object%",
                "add %object% to %object%",
                "remove %object% from %object%",
                "reset %object%",
                "delete %object%"
        );
        Skript.registerExpression(
                ExprSecBlankEquipComp.class,
                (Class) EquippableWrapper.class,
                "a (blank|empty) equippable component"
        );

        List<TriggerItem> items = ScriptLoader.loadItems(root(
                line("set {_state} to \"outside\""),
                section("set {_component} to a blank equippable component", line("set {_state} to \"inside\""))
        ));

        assertEquals(2, items.size());

        SkriptEvent event = new SkriptEvent(new Object(), null, null, null);
        TriggerItem.walk(items.getFirst(), event);
        Variable<String> state = Variable.newInstance("_state", new Class[]{String.class});

        assertEquals("inside", state == null ? null : state.getSingle(event));
    }

    @Test
    void loadItemsWarnsWhenLaterLineIsUnreachable() {
        registerExecutionIntentStatements();
        ParserInstance parser = ParserInstance.get();
        parser.setCurrentScript(new Script(null, java.util.List.of()));

        List<TriggerItem> items;
        try (TestLogAppender logs = TestLogAppender.attach()) {
            items = ScriptLoader.loadItems(root(
                    line("record before stop"),
                    line("stop test trigger"),
                    line("record after stop")
            ));

            assertEquals(3, items.size());
            assertEquals(
                    1L,
                    logs.messages().stream()
                            .filter(message -> message.contains("Unreachable code. The previous statement stops further execution."))
                            .count()
            );
        }

        TriggerItem.walk(items.getFirst(), SkriptEvent.EMPTY);
        assertEquals(List.of("before", "stop"), statementExecution);
    }

    @Test
    void loadItemsSkipsUnreachableWarningWhenScriptSuppressesIt() {
        registerExecutionIntentStatements();
        ParserInstance parser = ParserInstance.get();
        Script script = new Script(null, java.util.List.of());
        script.suppressWarning(ScriptWarning.UNREACHABLE_CODE);
        parser.setCurrentScript(script);

        try (TestLogAppender logs = TestLogAppender.attach()) {
            List<TriggerItem> items = ScriptLoader.loadItems(root(
                    line("record before stop"),
                    line("stop test trigger"),
                    line("record after stop")
            ));

            assertEquals(3, items.size());
            assertFalse(
                    logs.messages().stream()
                            .anyMatch(message -> message.contains("Unreachable code. The previous statement stops further execution."))
            );
        }
    }

    @Test
    void loadItemsWarnsWhenRegisteredSectionContainsStopTrigger() {
        registerExecutionIntentStatements();
        Skript.registerSection(StoppingSection.class, "stop section holder");
        ParserInstance parser = ParserInstance.get();
        parser.setCurrentScript(new Script(null, java.util.List.of()));

        List<TriggerItem> items;
        try (TestLogAppender logs = TestLogAppender.attach()) {
            items = ScriptLoader.loadItems(root(
                    line("record before stop"),
                    section("stop section holder", line("stop test trigger")),
                    line("record after stop")
            ));

            assertEquals(3, items.size());
            assertEquals(
                    1L,
                    logs.messages().stream()
                            .filter(message -> message.contains("Unreachable code. The previous statement stops further execution."))
                            .count()
            );
        }

        TriggerItem.walk(items.getFirst(), SkriptEvent.EMPTY);
        assertEquals(List.of("before", "stop"), statementExecution);
    }

    @Test
    void loadItemsDoesNotWarnWhenRegisteredSectionOnlyStopsItsOwnBody() {
        registerExecutionIntentStatements();
        Skript.registerSection(StoppingSection.class, "stop section holder");
        Skript.registerStatement(StopCurrentSectionStatement.class, "stop current section");
        ParserInstance parser = ParserInstance.get();
        parser.setCurrentScript(new Script(null, java.util.List.of()));

        List<TriggerItem> items;
        try (TestLogAppender logs = TestLogAppender.attach()) {
            items = ScriptLoader.loadItems(root(
                    section("stop section holder", line("stop current section")),
                    line("record after stop")
            ));

            assertEquals(2, items.size());
            assertFalse(
                    logs.messages().stream()
                            .anyMatch(message -> message.contains("Unreachable code. The previous statement stops further execution."))
            );
        }

        TriggerItem.walk(items.getFirst(), SkriptEvent.EMPTY);
        assertEquals(List.of("stop-section", "after"), statementExecution);
    }

    @Test
    void loadItemsKeepsSpecificSectionOwnershipError() {
        Skript.registerEffect(RecordingEffect.class, "mark inside", "mark after");

        try (TestLogAppender logs = TestLogAppender.attach()) {
            List<TriggerItem> items = ScriptLoader.loadItems(root(
                    section("mark after", line("mark inside"))
            ));

            assertTrue(items.isEmpty());
            assertTrue(
                    logs.messages().stream().anyMatch(message ->
                            message.contains("The line 'mark after' is a valid effect but cannot function as a section (:)")
                    )
            );
            assertFalse(
                    logs.messages().stream().anyMatch(message ->
                            message.contains("Can't understand this section: mark after")
                    )
            );
        }
    }

    @Test
    void loadItemsKeepsSpecificConditionSectionOwnershipError() {
        Skript.registerCondition(AlwaysTrueCondition.class, "always true");

        try (TestLogAppender logs = TestLogAppender.attach()) {
            List<TriggerItem> items = ScriptLoader.loadItems(root(
                    section("always true", line("mark inside"))
            ));

            assertTrue(items.isEmpty());
            assertTrue(
                    logs.messages().stream().anyMatch(message ->
                            message.contains("The line 'always true' is a valid condition but cannot function as a section (:)")
                    )
            );
            assertFalse(
                    logs.messages().stream().anyMatch(message ->
                            message.contains("Can't understand this section: always true")
                    )
            );
        }
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

    private static void registerExecutionIntentStatements() {
        Skript.registerStatement(RecordingStatement.class, "record before stop", "record after stop");
        Skript.registerStatement(StopTriggerStatement.class, "stop test trigger");
    }

    public static final class RecordingSection extends Section {

        private static final List<String> executed = new ArrayList<>();

        @Override
        public boolean init(
                Expression<?>[] expressions,
                int matchedPattern,
                Kleenean isDelayed,
                ch.njol.skript.lang.SkriptParser.ParseResult parseResult,
                @Nullable SectionNode sectionNode,
                @Nullable List<TriggerItem> triggerItems
        ) {
            if (sectionNode == null) {
                return false;
            }
            loadCode(sectionNode);
            return true;
        }

        @Override
        protected @Nullable TriggerItem walk(SkriptEvent event) {
            executed.add("section");
            return walk(event, true);
        }

        @Override
        public String toString(@Nullable SkriptEvent event, boolean debug) {
            return "record section";
        }
    }

    public static final class StoppingSection extends Section {

        @Override
        public boolean init(
                Expression<?>[] expressions,
                int matchedPattern,
                Kleenean isDelayed,
                ch.njol.skript.lang.SkriptParser.ParseResult parseResult,
                @Nullable SectionNode sectionNode,
                @Nullable List<TriggerItem> triggerItems
        ) {
            if (sectionNode == null) {
                return false;
            }
            loadCode(sectionNode);
            return true;
        }

        @Override
        protected @Nullable TriggerItem walk(SkriptEvent event) {
            return walk(event, true);
        }

        @Override
        public String toString(@Nullable SkriptEvent event, boolean debug) {
            return "stop section holder";
        }
    }

    public static final class RecordingEffect extends ch.njol.skript.lang.Effect {

        private static final List<String> executed = RecordingSection.executed;
        private String label;

        @Override
        public boolean init(
                Expression<?>[] expressions,
                int matchedPattern,
                Kleenean isDelayed,
                ch.njol.skript.lang.SkriptParser.ParseResult parseResult
        ) {
            label = matchedPattern == 0 ? "inside" : "after";
            return true;
        }

        @Override
        protected void execute(SkriptEvent event) {
            executed.add(label);
        }

        @Override
        public String toString(@Nullable SkriptEvent event, boolean debug) {
            return label == null ? "record effect" : label;
        }
    }

    public static final class AlwaysTrueCondition extends Condition {

        @Override
        public boolean init(
                Expression<?>[] expressions,
                int matchedPattern,
                Kleenean isDelayed,
                ch.njol.skript.lang.SkriptParser.ParseResult parseResult
        ) {
            return true;
        }

        @Override
        public boolean check(SkriptEvent event) {
            return true;
        }
    }

    public static final class RecordingStatement extends Statement {

        private String label;

        @Override
        public boolean init(
                Expression<?>[] expressions,
                int matchedPattern,
                Kleenean isDelayed,
                ch.njol.skript.lang.SkriptParser.ParseResult parseResult
        ) {
            label = matchedPattern == 0 ? "before" : "after";
            return true;
        }

        @Override
        protected boolean run(SkriptEvent event) {
            statementExecution.add(label);
            return true;
        }

        @Override
        public String toString(@Nullable SkriptEvent event, boolean debug) {
            return label == null ? "record statement" : label;
        }
    }

    public static final class StopTriggerStatement extends Statement {

        @Override
        public boolean init(
                Expression<?>[] expressions,
                int matchedPattern,
                Kleenean isDelayed,
                ch.njol.skript.lang.SkriptParser.ParseResult parseResult
        ) {
            return true;
        }

        @Override
        protected boolean run(SkriptEvent event) {
            statementExecution.add("stop");
            return false;
        }

        @Override
        protected @Nullable ExecutionIntent executionIntent() {
            return ExecutionIntent.stopTrigger();
        }

        @Override
        public String toString(@Nullable SkriptEvent event, boolean debug) {
            return "stop test trigger";
        }
    }

    public static final class StopCurrentSectionStatement extends Statement {

        @Override
        public boolean init(
                Expression<?>[] expressions,
                int matchedPattern,
                Kleenean isDelayed,
                ch.njol.skript.lang.SkriptParser.ParseResult parseResult
        ) {
            return true;
        }

        @Override
        protected boolean run(SkriptEvent event) {
            statementExecution.add("stop-section");
            return false;
        }

        @Override
        protected @Nullable ExecutionIntent executionIntent() {
            return ExecutionIntent.stopSection();
        }

        @Override
        public String toString(@Nullable SkriptEvent event, boolean debug) {
            return "stop current section";
        }
    }

    private static final class TestLogAppender extends AbstractAppender implements AutoCloseable {

        private final List<String> messages = new ArrayList<>();
        private final Logger logger;

        private TestLogAppender(Logger logger) {
            super("script-loader-test", null, PatternLayout.createDefaultLayout(), false, null);
            this.logger = logger;
        }

        static TestLogAppender attach() {
            Logger logger = (Logger) LogManager.getLogger("skript-fabric");
            TestLogAppender appender = new TestLogAppender(logger);
            appender.start();
            logger.addAppender(appender);
            return appender;
        }

        List<String> messages() {
            return messages;
        }

        @Override
        public void append(LogEvent event) {
            messages.add(event.getMessage().getFormattedMessage());
        }

        @Override
        public void close() {
            logger.removeAppender((Appender) this);
            stop();
        }
    }
}
