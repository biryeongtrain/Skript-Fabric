package ch.njol.skript.structures;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import ch.njol.skript.Skript;
import ch.njol.skript.config.SectionNode;
import ch.njol.skript.config.SimpleNode;
import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.function.FunctionEvent;
import ch.njol.skript.lang.parser.ParserInstance;
import ch.njol.util.Kleenean;
import java.util.List;
import org.jetbrains.annotations.Nullable;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.skriptlang.skript.lang.event.SkriptEvent;
import org.skriptlang.skript.lang.script.Script;
import org.skriptlang.skript.lang.structure.Structure;

class StructExampleCompatibilityTest {

    @BeforeEach
    void setUp() {
        Skript.instance().syntaxRegistry().clearAll();
        ExampleEffect.reset();
        ParserInstance parser = ParserInstance.get();
        parser.setCurrentScript(new Script(null, List.of()));
        parser.setCurrentEvent("outer", String.class);
        StructExample.register();
        Skript.registerEffect(ExampleEffect.class, "capture example context");
    }

    @AfterEach
    void tearDown() {
        Skript.instance().syntaxRegistry().clearAll();
        ExampleEffect.reset();
        ParserInstance.get().setCurrentScript(null);
    }

    @Test
    void exampleStructureLoadsUnderFunctionEventContextAndRestoresPreviousEvent() {
        SectionNode node = new SectionNode("example");
        node.add(new SimpleNode("capture example context"));

        Structure structure = Structure.parse("example", node, null);

        assertNotNull(structure);
        StructExample parsed = assertInstanceOf(StructExample.class, structure);
        assertEquals(StructExample.PRIORITY, parsed.getPriority());
        assertEquals(ch.njol.skript.registrations.Feature.EXAMPLES,
                parsed.getExperimentData().getRequired().iterator().next());

        assertTrue(parsed.load());
        assertEquals("example", ExampleEffect.eventNameSeenDuringInit);
        assertArrayEquals(new Class<?>[]{FunctionEvent.class}, ExampleEffect.eventClassesSeenDuringInit);
        assertEquals("outer", ParserInstance.get().getCurrentEventName());
        assertArrayEquals(new Class<?>[]{String.class}, ParserInstance.get().getCurrentEventClasses());
    }

    public static final class ExampleEffect extends Effect {

        private static @Nullable String eventNameSeenDuringInit;
        private static Class<?>[] eventClassesSeenDuringInit = new Class<?>[0];

        static void reset() {
            eventNameSeenDuringInit = null;
            eventClassesSeenDuringInit = new Class<?>[0];
        }

        @Override
        public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
            ParserInstance parser = ParserInstance.get();
            eventNameSeenDuringInit = parser.getCurrentEventName();
            eventClassesSeenDuringInit = parser.getCurrentEventClasses();
            return true;
        }

        @Override
        public String toString(@Nullable SkriptEvent event, boolean debug) {
            return "capture example context";
        }

        @Override
        protected void execute(SkriptEvent event) {
        }
    }
}
