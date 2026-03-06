package ch.njol.skript.lang;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.skriptlang.skript.lang.event.SkriptEvent;

class LiteralStringCompatibilityTest {

    @Test
    void literalStringReturnsSingleAndArrayValues() {
        LiteralString literal = LiteralString.of("hello");

        assertEquals("hello", literal.getSingle(SkriptEvent.EMPTY));
        assertArrayEquals(new String[]{"hello"}, literal.getArray(SkriptEvent.EMPTY));
        assertArrayEquals(new String[]{"hello"}, literal.getAll(SkriptEvent.EMPTY));
        assertTrue(literal.getOptionalSingle(SkriptEvent.EMPTY).isPresent());
        assertEquals("\"hello\"", literal.toString(SkriptEvent.EMPTY, false));
    }
}
