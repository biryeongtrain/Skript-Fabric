package ch.njol.skript.lang.parser;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

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

    private static class BaseEvent {
    }

    private static final class SubEvent extends BaseEvent {
    }
}
