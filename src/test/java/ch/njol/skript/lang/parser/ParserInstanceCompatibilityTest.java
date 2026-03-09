package ch.njol.skript.lang.parser;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import ch.njol.skript.config.SectionNode;
import ch.njol.skript.config.SimpleNode;
import ch.njol.skript.lang.InputSource;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.lang.script.Script;

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
    }
}
