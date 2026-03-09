package ch.njol.skript.lang.parser;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import ch.njol.skript.config.Config;
import ch.njol.skript.config.SectionNode;
import ch.njol.skript.config.SimpleNode;
import ch.njol.skript.lang.InputSource;
import ch.njol.skript.lang.TriggerItem;
import ch.njol.skript.lang.TriggerSection;
import ch.njol.skript.log.HandlerList;
import ch.njol.skript.log.RetainingLogHandler;
import ch.njol.skript.log.SkriptLogger;
import ch.njol.util.Kleenean;
import java.io.File;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.lang.event.SkriptEvent;
import org.skriptlang.skript.lang.script.Script;
import org.skriptlang.skript.lang.structure.Structure;
import org.skriptlang.skript.lang.entry.EntryContainer;

class ParserInstanceCompatibilityTest {

    @AfterEach
    void cleanupParserState() {
        ParserInstance parser = ParserInstance.get();
        parser.deleteCurrentEvent();
        parser.setCurrentScript(null);
    }

    @Test
    void isCurrentEventOnlyMatchesCurrentEventSubtypesAgainstExpectedType() {
        ParserInstance parser = ParserInstance.get();
        parser.setCurrentEvent("base", BaseEvent.class);

        assertFalse(parser.isCurrentEvent(SubEvent.class));

        parser.setCurrentEvent("sub", SubEvent.class);

        assertTrue(parser.isCurrentEvent(BaseEvent.class));
    }

    @Test
    void setCurrentScriptClearsTransientParserData() {
        ParserInstance parser = ParserInstance.get();
        parser.setCurrentScript(new Script(null, List.of()));
        InputSource.InputData inputData = parser.getData(InputSource.InputData.class);
        inputData.setSource(new DummyInputSource());

        parser.setCurrentScript(null);

        assertNull(parser.getData(InputSource.InputData.class).getSource());
    }

    @Test
    void setCurrentScriptDoesNothingWhenReapplyingSameScript() {
        ParserInstance parser = ParserInstance.get();
        Script script = new Script(null, List.of());
        parser.setCurrentScript(script);
        parser.getHintManager().enterScope(false);
        parser.getHintManager().set("value", Integer.class);

        parser.setCurrentScript(script);

        assertSame(script, parser.getCurrentScript());
        assertTrue(parser.getHintManager().get("value").contains(Integer.class));
    }

    @Test
    void resetClearsTransientParserStateButKeepsCurrentScript() {
        ParserInstance parser = new ParserInstance();
        Script script = new Script(new Config("reset", "reset.sk", new File("reset.sk")), List.of());
        SectionNode root = new SectionNode("root");
        SimpleNode child = new SimpleNode("child");
        root.add(child);

        parser.setCurrentScript(script);
        parser.setCurrentStructure(new DummyStructure("dummy"));
        parser.setNode(child);
        parser.setCurrentEvent("sub", SubEvent.class);
        parser.setCurrentSections(List.of(new OuterSection(), new InnerSection()));
        parser.setHasDelayBefore(Kleenean.TRUE);
        parser.getHintManager().enterScope(false);
        parser.getHintManager().set("value", Integer.class);
        parser.getData(InputSource.InputData.class).setSource(new DummyInputSource());

        parser.reset();

        assertSame(script, parser.getCurrentScript());
        assertNull(parser.getCurrentStructure());
        assertNull(parser.getNode());
        assertNull(parser.getCurrentEventName());
        assertEquals(0, parser.getCurrentEventClasses().length);
        assertEquals(List.of(), parser.getCurrentSections());
        assertSame(Kleenean.FALSE, parser.getHasDelayBefore());
        assertTrue(parser.getHintManager().get("value").isEmpty());
        assertNull(parser.getData(InputSource.InputData.class).getSource());
    }

    @Test
    void setActiveAndSetInactiveRestoreUpstreamLifecycleHelpers() {
        ParserInstance parser = new ParserInstance();
        Script script = new Script(new Config("active", "active.sk", new File("active.sk")), List.of());

        parser.setCurrentEvent("sub", SubEvent.class);
        parser.setCurrentStructure(new DummyStructure("active"));
        parser.setCurrentSections(List.of(new OuterSection()));
        parser.setHasDelayBefore(Kleenean.TRUE);
        parser.setActive(script);

        assertSame(script, parser.getCurrentScript());
        assertNull(parser.getCurrentStructure());
        assertNull(parser.getCurrentEventName());
        assertEquals(List.of(), parser.getCurrentSections());
        assertSame(Kleenean.FALSE, parser.getHasDelayBefore());

        parser.setCurrentEvent("sub", SubEvent.class);
        parser.setHasDelayBefore(Kleenean.UNKNOWN);
        parser.setInactive();

        assertNull(parser.getCurrentScript());
        assertNull(parser.getCurrentStructure());
        assertNull(parser.getCurrentEventName());
        assertEquals(0, parser.getCurrentEventClasses().length);
        assertEquals(List.of(), parser.getCurrentSections());
        assertSame(Kleenean.FALSE, parser.getHasDelayBefore());
    }

    @Test
    void currentStructureBridgeStoresAndClearsAcrossScriptTransitions() {
        ParserInstance parser = new ParserInstance();
        DummyStructure structure = new DummyStructure("structure");
        Script script = new Script(new Config("structure", "structure.sk", new File("structure.sk")), List.of());

        parser.setCurrentStructure(structure);
        assertSame(structure, parser.getCurrentStructure());

        parser.setCurrentScript(script);
        assertNull(parser.getCurrentStructure());

        parser.setCurrentStructure(structure);
        parser.setCurrentScript(null);
        assertNull(parser.getCurrentStructure());
    }

    @Test
    void setCurrentScriptKeepsRegisteredParserDataAcrossScriptSwitchesAndNotifiesChanges() {
        ParserInstance.registerData(ScriptTrackingData.class, ScriptTrackingData::new);

        ParserInstance parser = new ParserInstance();
        ScriptTrackingData trackingData = parser.getData(ScriptTrackingData.class);
        Script firstScript = new Script(new Config("first", "first.sk", new File("first.sk")), List.of());
        Script secondScript = new Script(new Config("second", "second.sk", new File("second.sk")), List.of());

        parser.setCurrentScript(firstScript);
        assertSame(firstScript.getConfig(), trackingData.lastCurrentScript);

        parser.setCurrentScript(secondScript);
        assertSame(trackingData, parser.getData(ScriptTrackingData.class));
        assertSame(secondScript.getConfig(), trackingData.lastCurrentScript);

        parser.setCurrentScript(null);
        assertNull(trackingData.lastCurrentScript);
        assertNotSame(trackingData, parser.getData(ScriptTrackingData.class));
    }

    @Test
    void setNodeDropsParentlessRootNodesButKeepsChildNodes() {
        ParserInstance parser = ParserInstance.get();
        SectionNode root = new SectionNode("root");
        SimpleNode child = new SimpleNode("child");
        root.add(child);

        parser.setNode(root);
        assertNull(parser.getNode());

        parser.setNode(child);
        assertSame(child, parser.getNode());
    }

    @Test
    void setCurrentEventNotifiesRegisteredParserDataWhenSetAndCleared() {
        ParserInstance.registerData(EventTrackingData.class, EventTrackingData::new);

        ParserInstance parser = new ParserInstance();
        EventTrackingData trackingData = parser.getData(EventTrackingData.class);

        parser.setCurrentEvent("sub", SubEvent.class);
        assertSame(SubEvent.class, trackingData.lastCurrentEvents[0]);

        parser.deleteCurrentEvent();
        assertNull(trackingData.lastCurrentEvents);
    }

    @Test
    void isRegisteredTracksParserDataFactories() {
        assertFalse(ParserInstance.isRegistered(RegistrationProbeData.class));

        ParserInstance.registerData(RegistrationProbeData.class, RegistrationProbeData::new);

        assertTrue(ParserInstance.isRegistered(RegistrationProbeData.class));
    }

    @Test
    void parserDataExposesUpstreamGetParserAccessor() {
        ParserInstance parser = new ParserInstance();
        RegistrationProbeData data = new RegistrationProbeData(parser);

        assertSame(parser, data.parserFromUpstreamAccessor());
    }

    @Test
    void currentSectionHelpersReturnInnermostMatchAndFilteredCopies() {
        ParserInstance parser = new ParserInstance();
        OuterSection outer = new OuterSection();
        MiddleSection middle = new MiddleSection();
        InnerSection inner = new InnerSection();
        parser.setCurrentSections(List.of(outer, middle, inner));

        assertSame(inner, parser.getCurrentSection(TriggerSection.class));
        assertSame(inner, parser.getCurrentSection(MiddleSection.class));
        assertTrue(parser.isCurrentSection(OuterSection.class, InnerSection.class));
        assertFalse(parser.isCurrentSection(UnusedSection.class));
        assertEquals(List.of(outer, middle, inner), parser.getCurrentSections(TriggerSection.class));
        assertEquals(List.of(middle, inner), parser.getCurrentSections(MiddleSection.class));
    }

    @Test
    void sectionSliceHelpersFollowUpstreamInclusiveDepthRules() {
        ParserInstance parser = new ParserInstance();
        OuterSection outer = new OuterSection();
        MiddleSection middle = new MiddleSection();
        InnerSection inner = new InnerSection();
        parser.setCurrentSections(List.of(outer, middle, inner));

        assertEquals(List.of(middle, inner), parser.getSectionsUntil(outer));
        assertEquals(List.of(inner), parser.getSectionsUntil(middle));
        assertEquals(List.of(middle, inner), parser.getSections(2));
        assertEquals(List.of(inner), parser.getSections(1, MiddleSection.class));
        assertEquals(List.of(outer, middle, inner), parser.getSections(2, OuterSection.class));
    }

    @Test
    void sectionSliceHelpersRejectNonPositiveDepthAndReturnEmptyWhenNoMatchExists() {
        ParserInstance parser = new ParserInstance();
        OuterSection outer = new OuterSection();
        MiddleSection middle = new MiddleSection();
        InnerSection inner = new InnerSection();
        parser.setCurrentSections(List.of(outer, middle, inner));

        assertThrows(IllegalArgumentException.class, () -> parser.getSections(0));
        assertThrows(IllegalArgumentException.class, () -> parser.getSections(0, MiddleSection.class));
        assertEquals(List.of(), parser.getSections(1, UnusedSection.class));
        assertEquals(List.of(), parser.getSectionsUntil(inner));
    }

    @Test
    void delayStateBridgeResetsAcrossEventAndScriptTransitions() {
        ParserInstance parser = new ParserInstance();
        parser.setHasDelayBefore(Kleenean.TRUE);

        parser.setCurrentEvent("base", BaseEvent.class);
        assertSame(Kleenean.FALSE, parser.getHasDelayBefore());

        parser.setHasDelayBefore(Kleenean.UNKNOWN);
        parser.deleteCurrentEvent();
        assertSame(Kleenean.FALSE, parser.getHasDelayBefore());

        parser.setHasDelayBefore(Kleenean.TRUE);
        parser.setCurrentScript(new Script(null, List.of()));
        assertSame(Kleenean.FALSE, parser.getHasDelayBefore());
    }

    @Test
    void parserInstanceExposesDedicatedHandlerListPerParser() {
        ParserInstance outer = new ParserInstance();
        ParserInstance inner = new ParserInstance();

        ParserInstance.withInstance(outer, () -> {
            try (RetainingLogHandler outerHandler = SkriptLogger.startRetainingLog()) {
                HandlerList outerList = outer.getHandlers();
                HandlerList innerList = inner.getHandlers();

                assertTrue(outerList.contains(outerHandler));
                assertFalse(innerList.contains(outerHandler));

                ParserInstance.withInstance(inner, () -> {
                    try (RetainingLogHandler innerHandler = SkriptLogger.startRetainingLog()) {
                        assertTrue(inner.getHandlers().contains(innerHandler));
                        assertFalse(outer.getHandlers().contains(innerHandler));
                        return null;
                    }
                });
            }
            return null;
        });

        assertFalse(outer.getHandlers().iterator().hasNext());
        assertFalse(inner.getHandlers().iterator().hasNext());
    }

    private static class BaseEvent {
    }

    private static final class SubEvent extends BaseEvent {
    }

    private static final class DummyInputSource implements InputSource {

        @Override
        public java.util.Set<ch.njol.skript.expressions.ExprInput<?>> getDependentInputs() {
            return new java.util.HashSet<>();
        }

        @Override
        public Object getCurrentValue() {
            return "value";
        }
    }

    private static final class EventTrackingData extends ParserInstance.Data {

        private @Nullable Class<?>[] lastCurrentEvents;

        private EventTrackingData(ParserInstance parserInstance) {
            super(parserInstance);
        }

        @Override
        public void onCurrentEventsChange(@Nullable Class<?>[] currentEvents) {
            this.lastCurrentEvents = currentEvents;
        }
    }

    private static final class RegistrationProbeData extends ParserInstance.Data {

        private RegistrationProbeData(ParserInstance parserInstance) {
            super(parserInstance);
        }

        private ParserInstance parserFromUpstreamAccessor() {
            return getParser();
        }
    }

    private static final class ScriptTrackingData extends ParserInstance.Data {

        private @Nullable Config lastCurrentScript;

        private ScriptTrackingData(ParserInstance parserInstance) {
            super(parserInstance);
        }

        @Override
        public void onCurrentScriptChange(@Nullable Config currentScript) {
            this.lastCurrentScript = currentScript;
        }
    }

    private abstract static class BaseSection extends TriggerSection {

        @Override
        protected @Nullable TriggerItem walk(SkriptEvent event) {
            return getNext();
        }

        @Override
        public String toString(@Nullable SkriptEvent event, boolean debug) {
            return getClass().getSimpleName();
        }
    }

    private static final class OuterSection extends BaseSection {
    }

    private static class MiddleSection extends BaseSection {
    }

    private static final class InnerSection extends MiddleSection {
    }

    private static final class UnusedSection extends BaseSection {
    }

    private static final class DummyStructure extends Structure {

        private final String name;

        private DummyStructure(String name) {
            this.name = name;
        }

        @Override
        public boolean init(ch.njol.skript.lang.Literal<?>[] args, int matchedPattern,
                            ch.njol.skript.lang.SkriptParser.ParseResult parseResult,
                            @Nullable EntryContainer entryContainer) {
            return true;
        }

        @Override
        public boolean load() {
            return true;
        }

        @Override
        public String toString(@Nullable SkriptEvent event, boolean debug) {
            return name;
        }
    }
}
