package ch.njol.skript.structures;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import ch.njol.skript.Skript;
import ch.njol.skript.config.SectionNode;
import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.SkriptEvent;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.parser.ParserInstance;
import java.util.List;
import org.jetbrains.annotations.Nullable;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.skriptlang.skript.lang.entry.EntryContainer;
import org.skriptlang.skript.lang.script.Script;
import org.skriptlang.skript.lang.structure.Structure;

class StructEventCompatibilityTest {

    @BeforeEach
    void setUp() {
        Skript.instance().syntaxRegistry().clearAll();
        TrackingEvent.reset();
        ParserInstance.get().setCurrentScript(new Script(null, List.of()));
        StructEvent.register();
        Skript.registerEvent(TrackingEvent.class, "on tracked event");
    }

    @AfterEach
    void tearDown() {
        Skript.instance().syntaxRegistry().clearAll();
        TrackingEvent.reset();
        ParserInstance.get().setCurrentScript(null);
    }

    @Test
    void eventStructureForwardsBehaviorPriorityAndOnFallback() {
        SectionNode node = new SectionNode("cancelled tracked event with priority highest");

        Structure structure = Structure.parse("cancelled tracked event with priority highest", node, null);

        assertNotNull(structure);
        StructEvent parsed = assertInstanceOf(StructEvent.class, structure);
        assertInstanceOf(TrackingEvent.class, parsed.getSkriptEvent());
        assertEquals(SkriptEvent.ListeningBehavior.CANCELLED, TrackingEvent.lastBehavior);
        assertEquals(SkriptEvent.EventPriority.HIGHEST, TrackingEvent.lastPriority);
        assertTrue(parsed.load());
        assertEquals(1, TrackingEvent.loadCalls);
    }

    public static final class TrackingEvent extends SkriptEvent {

        private static @Nullable ListeningBehavior lastBehavior;
        private static @Nullable EventPriority lastPriority;
        private static int loadCalls;

        static void reset() {
            lastBehavior = null;
            lastPriority = null;
            loadCalls = 0;
        }

        @Override
        public boolean init(Literal<?>[] args, int matchedPattern, ParseResult parseResult) {
            StructEvent.EventData data = ParserInstance.get().getData(StructEvent.EventData.class);
            lastBehavior = data.getListenerBehavior();
            lastPriority = data.getPriority();
            return true;
        }

        @Override
        public boolean load() {
            loadCalls++;
            return true;
        }

        @Override
        public boolean check(org.skriptlang.skript.lang.event.SkriptEvent event) {
            return true;
        }

        @Override
        public String toString(@Nullable org.skriptlang.skript.lang.event.SkriptEvent event, boolean debug) {
            return "tracked event";
        }
    }
}
