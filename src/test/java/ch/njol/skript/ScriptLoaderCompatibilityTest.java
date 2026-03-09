package ch.njol.skript;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import ch.njol.skript.config.EntryNode;
import ch.njol.skript.config.Node;
import ch.njol.skript.config.SectionNode;
import ch.njol.skript.config.SimpleNode;
import ch.njol.skript.expressions.base.SectionExpression;
import ch.njol.skript.lang.Condition;
import ch.njol.skript.lang.ExecutionIntent;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionSection;
import ch.njol.skript.lang.Section;
import ch.njol.skript.lang.Statement;
import ch.njol.skript.lang.TriggerItem;
import ch.njol.skript.lang.Variable;
import ch.njol.skript.lang.function.Function;
import ch.njol.skript.lang.function.FunctionEvent;
import ch.njol.skript.lang.function.FunctionRegistry;
import ch.njol.skript.lang.function.Functions;
import ch.njol.skript.lang.function.Parameter;
import ch.njol.skript.lang.function.Signature;
import ch.njol.skript.lang.parser.ParserInstance;
import ch.njol.skript.log.ErrorQuality;
import ch.njol.skript.log.LogEntry;
import ch.njol.skript.log.SkriptLogger;
import ch.njol.skript.registrations.Classes;
import ch.njol.skript.structures.StructOptions;
import ch.njol.skript.variables.Variables;
import ch.njol.util.Kleenean;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
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
        CaptureHintedObjectEffect.lastReturnType = null;
        CaptureHintedIntegerEffect.lastReturnType = null;
        CaptureHintedIntegerEffect.lastCapturedValue = null;
        CaptureHintedTextEffect.lastReturnType = null;
        CaptureHintedTextEffect.lastCapturedValue = null;
        TestNumberSectionExpression.reset();
        Variables.clearAll();
        FunctionRegistry.getRegistry().clear();
        Functions.clear();
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
    void failedSectionParseDoesNotLeakHintScopeIntoLaterLines() {
        ParserInstance parser = ParserInstance.get();
        parser.setCurrentScript(new Script(null, java.util.List.of()));
        Skript.registerSection(RejectingHintSection.class, "reject hinted section");
        Skript.registerEffect(ExpectTextEffect.class, "expect text %string%");

        List<TriggerItem> items = ScriptLoader.loadItems(root(
                section("reject hinted section", line("ignored child")),
                line("expect text {_value}")
        ));

        assertEquals(1, items.size());
        assertTrue(items.getFirst() instanceof ExpectTextEffect);
    }

    @Test
    void successfulSectionParsePropagatesHintsToLaterSiblingLines() {
        ParserInstance parser = ParserInstance.get();
        parser.setCurrentScript(new Script(null, java.util.List.of()));
        Skript.registerSection(HintingSection.class, "hinting section");
        Skript.registerEffect(CaptureHintedObjectEffect.class, "capture hinted object %object%");

        List<TriggerItem> items = ScriptLoader.loadItems(root(
                section("hinting section"),
                line("capture hinted object {_value}")
        ));

        assertEquals(2, items.size());
        assertEquals(Integer.class, CaptureHintedObjectEffect.lastReturnType);
    }

    @Test
    void successfulStatementFallbackSectionParsePropagatesHintsToLaterSiblingLines() {
        ParserInstance parser = ParserInstance.get();
        parser.setCurrentScript(new Script(null, java.util.List.of()));
        Skript.registerExpression(StatementManagedHintExpression.class, Integer.class, "statement hint value");
        Skript.registerStatement(StatementManagedHintStatement.class, "statement-managed hint %integer%");
        Skript.registerEffect(CaptureHintedIntegerEffect.class, "capture hinted integer %integer%");

        List<TriggerItem> items = ScriptLoader.loadItems(root(
                section("statement-managed hint statement hint value"),
                line("capture hinted integer {_value}")
        ));

        assertEquals(2, items.size());
        assertEquals(Integer.class, CaptureHintedIntegerEffect.lastReturnType);
    }

    @Test
    @SuppressWarnings({"rawtypes", "unchecked"})
    void builtInSetEffectHintsIntegerLocalsForLaterSiblingLines() {
        ParserInstance parser = ParserInstance.get();
        parser.setCurrentScript(new Script(null, java.util.List.of()));
        Skript.registerEffect(
                EffChange.class,
                "set %object% to %object%",
                "add %object% to %object%",
                "remove %object% from %object%",
                "reset %object%",
                "delete %object%"
        );
        Skript.registerEffect(CaptureHintedIntegerEffect.class, "capture hinted integer %integer%");

        List<TriggerItem> items = ScriptLoader.loadItems(root(
                line("set {_value} to 1"),
                line("capture hinted integer {_value}")
        ));

        assertEquals(2, items.size());
        assertEquals(Integer.class, CaptureHintedIntegerEffect.lastReturnType);

        TriggerItem.walk(items.getFirst(), SkriptEvent.EMPTY);
        assertEquals(1, CaptureHintedIntegerEffect.lastCapturedValue);
    }

    @Test
    @SuppressWarnings({"rawtypes", "unchecked"})
    void builtInSetEffectOverridesEarlierHintsWithLaterStringAssignment() {
        ParserInstance parser = ParserInstance.get();
        parser.setCurrentScript(new Script(null, java.util.List.of()));
        Skript.registerEffect(
                EffChange.class,
                "set %object% to %object%",
                "add %object% to %object%",
                "remove %object% from %object%",
                "reset %object%",
                "delete %object%"
        );
        Skript.registerEffect(CaptureHintedTextEffect.class, "capture hinted text %string%");

        List<TriggerItem> items = ScriptLoader.loadItems(root(
                line("set {_value} to 1"),
                line("set {_value} to \"text\""),
                line("capture hinted text {_value}")
        ));

        assertEquals(3, items.size());
        assertEquals(String.class, CaptureHintedTextEffect.lastReturnType);

        TriggerItem.walk(items.getFirst(), SkriptEvent.EMPTY);
        assertEquals("text", CaptureHintedTextEffect.lastCapturedValue);
    }

    @Test
    void stopTriggerFreezesSectionHintsBeforeLaterSiblingParsing() {
        ParserInstance parser = ParserInstance.get();
        parser.setCurrentScript(new Script(null, java.util.List.of()));
        registerExecutionIntentStatements();
        Skript.registerSection(LoadingHintSection.class, "outer hint section");
        Skript.registerEffect(HintIntegerEffect.class, "hint local integer");
        Skript.registerEffect(CaptureHintedObjectEffect.class, "capture hinted object %object%");

        List<TriggerItem> items = ScriptLoader.loadItems(root(
                section(
                        "outer hint section",
                        line("stop test trigger"),
                        line("hint local integer")
                ),
                line("capture hinted object {_value}")
        ));

        assertEquals(2, items.size());
        assertEquals(Object.class, CaptureHintedObjectEffect.lastReturnType);
    }

    @Test
    void stopSectionMergesCurrentHintsIntoResumeScope() {
        ParserInstance parser = ParserInstance.get();
        parser.setCurrentScript(new Script(null, java.util.List.of()));
        Skript.registerSection(LoadingHintSection.class, "outer hint section", "nested hint section");
        Skript.registerStatement(StopCurrentSectionStatement.class, "stop current section");
        Skript.registerEffect(HintIntegerEffect.class, "hint local integer");
        Skript.registerEffect(CaptureHintedObjectEffect.class, "capture hinted object %object%");

        List<TriggerItem> items = ScriptLoader.loadItems(root(
                section(
                        "outer hint section",
                        section(
                                "nested hint section",
                                line("hint local integer"),
                                line("stop current section")
                        ),
                        line("capture hinted object {_value}")
                )
        ));

        assertEquals(1, items.size());
        assertEquals(Integer.class, CaptureHintedObjectEffect.lastReturnType);
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
    void loadItemsLetsRegisteredStatementWinAfterFailedEffectParse() {
        Skript.registerEffect(RejectingAmbiguousEffect.class, "ambiguous loader syntax");
        Skript.registerStatement(AmbiguousStatement.class, "ambiguous loader syntax");

        List<TriggerItem> items;
        try (TestLogAppender logs = TestLogAppender.attach()) {
            items = ScriptLoader.loadItems(root(
                    line("ambiguous loader syntax")
            ));

            assertEquals(1, items.size());
            assertTrue(items.getFirst() instanceof AmbiguousStatement);
            assertFalse(
                    logs.messages().stream().anyMatch(message -> message.contains("ambiguous effect rejected"))
            );
        }

        TriggerItem.walk(items.getFirst(), SkriptEvent.EMPTY);
        assertEquals(List.of("ambiguous-statement"), statementExecution);
    }

    @Test
    void loadItemsLetsRegisteredStatementWinAfterFailedConditionParse() {
        Skript.registerCondition(RejectingAmbiguousCondition.class, "ambiguous condition syntax");
        Skript.registerStatement(AmbiguousStatement.class, "ambiguous condition syntax");

        List<TriggerItem> items;
        try (TestLogAppender logs = TestLogAppender.attach()) {
            items = ScriptLoader.loadItems(root(
                    line("ambiguous condition syntax")
            ));

            assertEquals(1, items.size());
            assertTrue(items.getFirst() instanceof AmbiguousStatement);
            assertFalse(
                    logs.messages().stream().anyMatch(message -> message.contains("ambiguous condition rejected"))
            );
        }

        TriggerItem.walk(items.getFirst(), SkriptEvent.EMPTY);
        assertEquals(List.of("ambiguous-statement"), statementExecution);
    }

    @Test
    void loadItemsKeepsSpecificEffectParseErrorWhenNoStatementMatches() {
        Skript.registerEffect(RejectingAmbiguousEffect.class, "ambiguous loader syntax");

        try (TestLogAppender logs = TestLogAppender.attach()) {
            List<TriggerItem> items = ScriptLoader.loadItems(root(
                    line("ambiguous loader syntax")
            ));

            assertTrue(items.isEmpty());
            assertTrue(
                    logs.messages().stream().anyMatch(message -> message.contains("ambiguous effect rejected"))
            );
            assertFalse(
                    logs.messages().stream().anyMatch(message ->
                            message.contains("Can't understand this condition/effect: ambiguous loader syntax")
                    )
            );
        }
    }

    @Test
    void loadItemsKeepsEarlierEffectErrorWhenEffectAndConditionFailuresTie() {
        Skript.registerEffect(RejectingAmbiguousEffect.class, "ambiguous tied syntax");
        Skript.registerCondition(RejectingAmbiguousCondition.class, "ambiguous tied syntax");

        try (TestLogAppender logs = TestLogAppender.attach()) {
            List<TriggerItem> items = ScriptLoader.loadItems(root(
                    line("ambiguous tied syntax")
            ));

            assertTrue(items.isEmpty());
            assertTrue(
                    logs.messages().stream().anyMatch(message -> message.contains("ambiguous effect rejected"))
            );
            assertFalse(
                    logs.messages().stream().anyMatch(message -> message.contains("ambiguous condition rejected"))
            );
            assertFalse(
                    logs.messages().stream().anyMatch(message ->
                            message.contains("Can't understand this condition/effect: ambiguous tied syntax")
                    )
            );
        }
    }

    @Test
    void loadItemsKeepsHigherQualityEffectErrorWhenLaterStatementAlsoFails() {
        Skript.registerEffect(HigherQualityRejectingAmbiguousEffect.class, "ambiguous statement priority syntax");
        Skript.registerStatement(LowerQualityRejectingAmbiguousStatement.class, "ambiguous statement priority syntax");

        try (TestLogAppender logs = TestLogAppender.attach()) {
            List<TriggerItem> items = ScriptLoader.loadItems(root(
                    line("ambiguous statement priority syntax")
            ));

            assertTrue(items.isEmpty());
            assertTrue(
                    logs.messages().stream().anyMatch(message ->
                            message.contains("higher-quality ambiguous effect rejected")
                    )
            );
            assertFalse(
                    logs.messages().stream().anyMatch(message ->
                            message.contains("lower-quality ambiguous statement rejected")
                    )
            );
            assertFalse(
                    logs.messages().stream().anyMatch(message ->
                            message.contains("Can't understand this condition/effect: ambiguous statement priority syntax")
                    )
            );
        }
    }

    @Test
    void loadItemsKeepsSemanticEffectErrorWhenLaterNotExpressionStatementAlsoFails() {
        Skript.registerEffect(SemanticRejectingAmbiguousEffect.class, "semantic statement priority syntax");
        Skript.registerStatement(NotExpressionRejectingAmbiguousStatement.class, "semantic statement priority syntax");

        try (TestLogAppender logs = TestLogAppender.attach()) {
            List<TriggerItem> items = ScriptLoader.loadItems(root(
                    line("semantic statement priority syntax")
            ));

            assertTrue(items.isEmpty());
            assertTrue(
                    logs.messages().stream().anyMatch(message ->
                            message.contains("semantic ambiguous effect rejected")
                    )
            );
            assertFalse(
                    logs.messages().stream().anyMatch(message ->
                            message.contains("not-expression ambiguous statement rejected")
                    )
            );
            assertFalse(
                    logs.messages().stream().anyMatch(message ->
                            message.contains("Can't understand this condition/effect: semantic statement priority syntax")
                    )
            );
        }
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
    @SuppressWarnings({"rawtypes", "unchecked"})
    void loadItemsKeepsSpecificSectionOwnershipErrorForSetTrueSyntax() {
        Skript.registerEffect(
                EffChange.class,
                "set %object% to %object%",
                "add %object% to %object%",
                "remove %object% from %object%",
                "reset %object%",
                "delete %object%"
        );

        try (TestLogAppender logs = TestLogAppender.attach()) {
            List<TriggerItem> items = ScriptLoader.loadItems(root(
                    section("set {_var} to true", line("mark inside"))
            ));

            assertTrue(items.isEmpty());
            assertTrue(
                    logs.messages().stream().anyMatch(message ->
                            message.contains("The line 'set {_var} to true' is a valid effect but cannot function as a section (:)")
                    )
            );
            assertFalse(
                    logs.messages().stream().anyMatch(message ->
                            message.contains("Can't understand this section: set {_var} to true")
                    )
            );
        }
    }

    @Test
    void loadItemsDoesNotLeakSectionOwnershipAcrossEffectCandidates() {
        Skript.registerExpression(LeakyOwnershipExpression.class, Object.class, "ownership token");
        Skript.registerEffect(RejectingClaimingEffect.class, "ownership leak %object%");
        Skript.registerEffect(LiteralFallbackEffect.class, "ownership leak ownership token");

        List<TriggerItem> items = ScriptLoader.loadItems(root(
                section("ownership leak ownership token", line("mark inside"))
        ));

        assertTrue(items.isEmpty());
    }

    @Test
    void loadItemsDoesNotLeakSectionOwnershipAcrossConditionCandidates() {
        Skript.registerExpression(LeakyOwnershipExpression.class, Object.class, "ownership token");
        Skript.registerCondition(RejectingClaimingCondition.class, "ownership leak %object%");
        Skript.registerCondition(LiteralFallbackCondition.class, "ownership leak ownership token");

        List<TriggerItem> items = ScriptLoader.loadItems(root(
                section("ownership leak ownership token", line("mark inside"))
        ));

        assertTrue(items.isEmpty());
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

    @Test
    void loadItemsKeepsSpecificSectionWarningWhenStatementFallbackSucceeds() {
        Skript.registerSection(RejectingWarningSection.class, "warn then fallback");
        Skript.registerStatement(DirectClaimFallbackStatement.class, "warn then fallback");

        try (TestLogAppender logs = TestLogAppender.attach()) {
            List<TriggerItem> items = ScriptLoader.loadItems(root(
                    section("warn then fallback", line("ignored child"))
            ));

            assertEquals(1, items.size());
            assertTrue(items.getFirst() instanceof DirectClaimFallbackStatement);
            assertTrue(
                    logs.messages().stream().anyMatch(message ->
                            message.contains("specific section warning before fallback")
                    )
            );
            assertFalse(
                    logs.messages().stream().anyMatch(message ->
                            message.contains("Can't understand this section: warn then fallback")
                    )
            );
        }
    }

    @Test
    void plainStatementParseClearsOuterExpressionSectionOwnershipForFunctionArguments() {
        Skript.registerExpression(TestNumberSectionExpression.class, Integer.class, "a test number");

        Signature<Object> signature = new Signature<>(
                null,
                "consume_number",
                new Parameter[]{new Parameter<>("value", Classes.getSuperClassInfo(Integer.class), true, null)},
                false,
                null,
                true
        );
        Functions.register(new ConsumeNumberFunction(signature));

        ParserInstance parser = ParserInstance.get();
        Section.SectionContext context = parser.getData(Section.SectionContext.class);
        ExpressionSection outerOwner = new ExpressionSection(new PassiveOuterSectionExpression());

        Statement parsed = context.modify(section("outer owner"), List.of(), () -> {
            context.claim(outerOwner, "outer owner");
            return Statement.parse("consume_number(a test number)", "failed");
        });

        assertTrue(parsed instanceof ch.njol.skript.lang.function.EffFunctionCall);
        assertEquals(1, TestNumberSectionExpression.initCalls);
        assertNull(TestNumberSectionExpression.lastSectionNode);
        assertNull(TestNumberSectionExpression.lastTriggerItems);
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

    public static final class ExpectTextEffect extends ch.njol.skript.lang.Effect {

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
        protected void execute(SkriptEvent event) {
        }

        @Override
        public String toString(@Nullable SkriptEvent event, boolean debug) {
            return "expect text";
        }
    }

    public static final class CaptureHintedObjectEffect extends ch.njol.skript.lang.Effect {

        private static @Nullable Class<?> lastReturnType;

        @Override
        public boolean init(
                Expression<?>[] expressions,
                int matchedPattern,
                Kleenean isDelayed,
                ch.njol.skript.lang.SkriptParser.ParseResult parseResult
        ) {
            lastReturnType = expressions[0].getReturnType();
            return true;
        }

        @Override
        protected void execute(SkriptEvent event) {
        }

        @Override
        public String toString(@Nullable SkriptEvent event, boolean debug) {
            return "capture hinted object";
        }
    }

    public static final class CaptureHintedIntegerEffect extends ch.njol.skript.lang.Effect {

        private static @Nullable Class<?> lastReturnType;
        private static @Nullable Integer lastCapturedValue;
        private Expression<Integer> value;

        @Override
        @SuppressWarnings("unchecked")
        public boolean init(
                Expression<?>[] expressions,
                int matchedPattern,
                Kleenean isDelayed,
                ch.njol.skript.lang.SkriptParser.ParseResult parseResult
        ) {
            lastReturnType = expressions[0].getReturnType();
            value = (Expression<Integer>) expressions[0];
            return true;
        }

        @Override
        protected void execute(SkriptEvent event) {
            lastCapturedValue = value.getSingle(event);
        }

        @Override
        public String toString(@Nullable SkriptEvent event, boolean debug) {
            return "capture hinted integer";
        }
    }

    public static final class CaptureHintedTextEffect extends ch.njol.skript.lang.Effect {

        private static @Nullable Class<?> lastReturnType;
        private static @Nullable String lastCapturedValue;
        private Expression<String> value;

        @Override
        @SuppressWarnings("unchecked")
        public boolean init(
                Expression<?>[] expressions,
                int matchedPattern,
                Kleenean isDelayed,
                ch.njol.skript.lang.SkriptParser.ParseResult parseResult
        ) {
            lastReturnType = expressions[0].getReturnType();
            value = (Expression<String>) expressions[0];
            return true;
        }

        @Override
        protected void execute(SkriptEvent event) {
            lastCapturedValue = value.getSingle(event);
        }

        @Override
        public String toString(@Nullable SkriptEvent event, boolean debug) {
            return "capture hinted text";
        }
    }

    public static final class HintIntegerEffect extends ch.njol.skript.lang.Effect {

        @Override
        public boolean init(
                Expression<?>[] expressions,
                int matchedPattern,
                Kleenean isDelayed,
                ch.njol.skript.lang.SkriptParser.ParseResult parseResult
        ) {
            ParserInstance.get().getHintManager().set("value", Integer.class);
            return true;
        }

        @Override
        protected void execute(SkriptEvent event) {
        }

        @Override
        public String toString(@Nullable SkriptEvent event, boolean debug) {
            return "hint local integer";
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

    public static final class LeakyOwnershipExpression extends SectionExpression<Object> {

        @Override
        public boolean init(
                Expression<?>[] expressions,
                int pattern,
                Kleenean delayed,
                ch.njol.skript.lang.SkriptParser.ParseResult parseResult,
                @Nullable SectionNode node,
                @Nullable List<TriggerItem> triggerItems
        ) {
            return true;
        }

        @Override
        protected Object @Nullable [] get(SkriptEvent event) {
            return new Object[]{"owned"};
        }

        @Override
        public boolean isSingle() {
            return true;
        }

        @Override
        public Class<?> getReturnType() {
            return Object.class;
        }

        @Override
        public String toString(@Nullable SkriptEvent event, boolean debug) {
            return "ownership token";
        }
    }

    public static final class RejectingClaimingEffect extends ch.njol.skript.lang.Effect {

        @Override
        public boolean init(
                Expression<?>[] expressions,
                int matchedPattern,
                Kleenean isDelayed,
                ch.njol.skript.lang.SkriptParser.ParseResult parseResult
        ) {
            Skript.error("rejecting claiming effect");
            return false;
        }

        @Override
        protected void execute(SkriptEvent event) {
        }

        @Override
        public String toString(@Nullable SkriptEvent event, boolean debug) {
            return "rejecting claiming effect";
        }
    }

    public static final class LiteralFallbackEffect extends ch.njol.skript.lang.Effect {

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
        protected void execute(SkriptEvent event) {
        }

        @Override
        public String toString(@Nullable SkriptEvent event, boolean debug) {
            return "literal fallback effect";
        }
    }

    public static final class RejectingClaimingCondition extends Condition {

        @Override
        public boolean init(
                Expression<?>[] expressions,
                int matchedPattern,
                Kleenean isDelayed,
                ch.njol.skript.lang.SkriptParser.ParseResult parseResult
        ) {
            Skript.error("rejecting claiming condition");
            return false;
        }

        @Override
        public boolean check(SkriptEvent event) {
            return false;
        }

        @Override
        public String toString(@Nullable SkriptEvent event, boolean debug) {
            return "rejecting claiming condition";
        }
    }

    public static final class LiteralFallbackCondition extends Condition {

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

        @Override
        public String toString(@Nullable SkriptEvent event, boolean debug) {
            return "literal fallback condition";
        }
    }

    public static final class RejectingAmbiguousEffect extends ch.njol.skript.lang.Effect {

        @Override
        public boolean init(
                Expression<?>[] expressions,
                int matchedPattern,
                Kleenean isDelayed,
                ch.njol.skript.lang.SkriptParser.ParseResult parseResult
        ) {
            Skript.error("ambiguous effect rejected");
            return false;
        }

        @Override
        protected void execute(SkriptEvent event) {
        }

        @Override
        public String toString(@Nullable SkriptEvent event, boolean debug) {
            return "ambiguous effect";
        }
    }

    public static final class RejectingAmbiguousCondition extends Condition {

        @Override
        public boolean init(
                Expression<?>[] expressions,
                int matchedPattern,
                Kleenean isDelayed,
                ch.njol.skript.lang.SkriptParser.ParseResult parseResult
        ) {
            Skript.error("ambiguous condition rejected");
            return false;
        }

        @Override
        public boolean check(SkriptEvent event) {
            return true;
        }

        @Override
        public String toString(@Nullable SkriptEvent event, boolean debug) {
            return "ambiguous condition";
        }
    }

    public static final class HigherQualityRejectingAmbiguousEffect extends ch.njol.skript.lang.Effect {

        @Override
        public boolean init(
                Expression<?>[] expressions,
                int matchedPattern,
                Kleenean isDelayed,
                ch.njol.skript.lang.SkriptParser.ParseResult parseResult
        ) {
            SkriptLogger.log(new LogEntry(
                    Level.SEVERE,
                    ErrorQuality.NOT_AN_EXPRESSION,
                    "higher-quality ambiguous effect rejected"
            ));
            return false;
        }

        @Override
        protected void execute(SkriptEvent event) {
        }

        @Override
        public String toString(@Nullable SkriptEvent event, boolean debug) {
            return "higher-quality ambiguous effect";
        }
    }

    public static final class LowerQualityRejectingAmbiguousStatement extends Statement {

        @Override
        public boolean init(
                Expression<?>[] expressions,
                int matchedPattern,
                Kleenean isDelayed,
                ch.njol.skript.lang.SkriptParser.ParseResult parseResult
        ) {
            Skript.error("lower-quality ambiguous statement rejected");
            return false;
        }

        @Override
        protected boolean run(SkriptEvent event) {
            return true;
        }

        @Override
        public String toString(@Nullable SkriptEvent event, boolean debug) {
            return "lower-quality ambiguous statement";
        }
    }

    public static final class SemanticRejectingAmbiguousEffect extends ch.njol.skript.lang.Effect {

        @Override
        public boolean init(
                Expression<?>[] expressions,
                int matchedPattern,
                Kleenean isDelayed,
                ch.njol.skript.lang.SkriptParser.ParseResult parseResult
        ) {
            SkriptLogger.log(new LogEntry(
                    Level.SEVERE,
                    ErrorQuality.SEMANTIC_ERROR,
                    "semantic ambiguous effect rejected"
            ));
            return false;
        }

        @Override
        protected void execute(SkriptEvent event) {
        }

        @Override
        public String toString(@Nullable SkriptEvent event, boolean debug) {
            return "semantic ambiguous effect";
        }
    }

    public static final class NotExpressionRejectingAmbiguousStatement extends Statement {

        @Override
        public boolean init(
                Expression<?>[] expressions,
                int matchedPattern,
                Kleenean isDelayed,
                ch.njol.skript.lang.SkriptParser.ParseResult parseResult
        ) {
            SkriptLogger.log(new LogEntry(
                    Level.SEVERE,
                    ErrorQuality.NOT_AN_EXPRESSION,
                    "not-expression ambiguous statement rejected"
            ));
            return false;
        }

        @Override
        protected boolean run(SkriptEvent event) {
            return true;
        }

        @Override
        public String toString(@Nullable SkriptEvent event, boolean debug) {
            return "not-expression ambiguous statement";
        }
    }

    public static final class AmbiguousStatement extends Statement {

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
            statementExecution.add("ambiguous-statement");
            return true;
        }

        @Override
        public String toString(@Nullable SkriptEvent event, boolean debug) {
            return "ambiguous statement";
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

    public static final class HintingSection extends Section {

        @Override
        public boolean init(
                Expression<?>[] expressions,
                int matchedPattern,
                Kleenean isDelayed,
                ch.njol.skript.lang.SkriptParser.ParseResult parseResult,
                @Nullable SectionNode sectionNode,
                @Nullable List<TriggerItem> triggerItems
        ) {
            ParserInstance.get().getHintManager().set("value", Integer.class);
            if (sectionNode != null) {
                loadCode(sectionNode);
            }
            return true;
        }

        @Override
        protected @Nullable TriggerItem walk(SkriptEvent event) {
            return walk(event, true);
        }

        @Override
        public String toString(@Nullable SkriptEvent event, boolean debug) {
            return "hinting section";
        }
    }

    public static final class RejectingHintSection extends Section {

        @Override
        public boolean init(
                Expression<?>[] expressions,
                int matchedPattern,
                Kleenean isDelayed,
                ch.njol.skript.lang.SkriptParser.ParseResult parseResult,
                @Nullable SectionNode sectionNode,
                @Nullable List<TriggerItem> triggerItems
        ) {
            ParserInstance.get().getHintManager().set("value", Integer.class);
            return false;
        }

        @Override
        protected @Nullable TriggerItem walk(SkriptEvent event) {
            return null;
        }

        @Override
        public String toString(@Nullable SkriptEvent event, boolean debug) {
            return "reject hinted section";
        }
    }

    public static final class LoadingHintSection extends Section {

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
            return "loading hint section";
        }
    }

    public static final class RejectingWarningSection extends Section {

        @Override
        public boolean init(
                Expression<?>[] expressions,
                int matchedPattern,
                Kleenean isDelayed,
                ch.njol.skript.lang.SkriptParser.ParseResult parseResult,
                @Nullable SectionNode sectionNode,
                @Nullable List<TriggerItem> triggerItems
        ) {
            Skript.warning("specific section warning before fallback");
            return false;
        }

        @Override
        protected @Nullable TriggerItem walk(SkriptEvent event) {
            return null;
        }

        @Override
        public String toString(@Nullable SkriptEvent event, boolean debug) {
            return "warn then fallback";
        }
    }

    public static final class DirectClaimFallbackStatement extends Statement {

        @Override
        public boolean init(
                Expression<?>[] expressions,
                int matchedPattern,
                Kleenean isDelayed,
                ch.njol.skript.lang.SkriptParser.ParseResult parseResult
        ) {
            Section.SectionContext sectionContext = ParserInstance.get().getData(Section.SectionContext.class);
            return sectionContext.claim(this, parseResult.expr);
        }

        @Override
        protected boolean run(SkriptEvent event) {
            statementExecution.add("direct-claim-fallback");
            return true;
        }

        @Override
        public String toString(@Nullable SkriptEvent event, boolean debug) {
            return "warn then fallback";
        }
    }

    public static final class StatementManagedHintStatement extends Statement {

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
            return true;
        }

        @Override
        public String toString(@Nullable SkriptEvent event, boolean debug) {
            return "statement-managed hint";
        }
    }

    public static final class StatementManagedHintExpression extends SectionExpression<Integer> {

        @Override
        public boolean init(
                Expression<?>[] expressions,
                int pattern,
                Kleenean delayed,
                ch.njol.skript.lang.SkriptParser.ParseResult parseResult,
                @Nullable SectionNode node,
                @Nullable List<TriggerItem> triggerItems
        ) {
            ParserInstance.get().getHintManager().set("value", Integer.class);
            if (node != null) {
                loadOptionalCode(node);
            }
            return true;
        }

        @Override
        protected Integer @Nullable [] get(SkriptEvent event) {
            return new Integer[]{1};
        }

        @Override
        public boolean isSingle() {
            return true;
        }

        @Override
        public Class<? extends Integer> getReturnType() {
            return Integer.class;
        }

        @Override
        public String toString(@Nullable SkriptEvent event, boolean debug) {
            return "statement hint value";
        }
    }

    private static final class ConsumeNumberFunction extends Function<Object> {

        private ConsumeNumberFunction(Signature<Object> signature) {
            super(signature);
        }

        @Override
        public Object @Nullable [] execute(FunctionEvent<?> event, Object[][] params) {
            return null;
        }

        @Override
        public boolean resetReturnValue() {
            return true;
        }
    }

    private static final class PassiveOuterSectionExpression extends SectionExpression<Integer> {

        @Override
        public boolean init(
                Expression<?>[] expressions,
                int pattern,
                Kleenean delayed,
                ch.njol.skript.lang.SkriptParser.ParseResult parseResult,
                @Nullable SectionNode node,
                @Nullable List<TriggerItem> triggerItems
        ) {
            return true;
        }

        @Override
        protected Integer @Nullable [] get(SkriptEvent event) {
            return new Integer[]{0};
        }

        @Override
        public boolean isSingle() {
            return true;
        }

        @Override
        public Class<? extends Integer> getReturnType() {
            return Integer.class;
        }

        @Override
        public String toString(@Nullable SkriptEvent event, boolean debug) {
            return "outer owner";
        }
    }

    private static final class TestNumberSectionExpression extends SectionExpression<Integer> {

        private static int initCalls;
        private static @Nullable SectionNode lastSectionNode;
        private static @Nullable List<TriggerItem> lastTriggerItems;

        private static void reset() {
            initCalls = 0;
            lastSectionNode = null;
            lastTriggerItems = null;
        }

        @Override
        public boolean init(
                Expression<?>[] expressions,
                int pattern,
                Kleenean delayed,
                ch.njol.skript.lang.SkriptParser.ParseResult parseResult,
                @Nullable SectionNode node,
                @Nullable List<TriggerItem> triggerItems
        ) {
            initCalls++;
            lastSectionNode = node;
            lastTriggerItems = triggerItems;
            return true;
        }

        @Override
        protected Integer @Nullable [] get(SkriptEvent event) {
            return new Integer[]{1};
        }

        @Override
        public boolean isSingle() {
            return true;
        }

        @Override
        public Class<? extends Integer> getReturnType() {
            return Integer.class;
        }

        @Override
        public String toString(@Nullable SkriptEvent event, boolean debug) {
            return "a test number";
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
