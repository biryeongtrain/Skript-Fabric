package ch.njol.skript.util;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class UtilScaffoldingCompatibilityTest {

    @Test
    void getterAlsoImplementsConverter() {
        Getter<Integer, String> getter = new Getter<>() {
            @Override
            public Integer get(String arg) {
                return arg == null ? null : arg.length();
            }
        };

        assertEquals(4, getter.convert("test"));
        assertNull(getter.convert(null));
    }

    @Test
    void validationResultConvenienceConstructorsPreserveFields() {
        ValidationResult<String> validOnly = new ValidationResult<>(true);
        ValidationResult<String> withMessage = new ValidationResult<>(false, "broken");
        ValidationResult<Integer> withData = new ValidationResult<>(true, 7);

        assertTrue(validOnly.valid());
        assertNull(validOnly.message());
        assertNull(validOnly.data());
        assertFalse(withMessage.valid());
        assertEquals("broken", withMessage.message());
        assertNull(withMessage.data());
        assertTrue(withData.valid());
        assertNull(withData.message());
        assertEquals(7, withData.data());
    }

    @Test
    void emptyStacktraceExceptionSkipsStackCapture() {
        EmptyStacktraceException exception = new EmptyStacktraceException();

        assertEquals(0, exception.getStackTrace().length);
        assertNull(exception.getMessage());
    }

    @Test
    void patternsTrackInfoAndPatternIndexes() {
        Patterns<String> patterns = new Patterns<>(new Object[][]{
                {"first %string%", "alpha"},
                {"second %number%", "beta"},
                {"third %item%", "alpha"}
        });

        assertArrayEquals(new String[]{"first %string%", "second %number%", "third %item%"}, patterns.getPatterns());
        assertEquals("beta", patterns.getInfo(1));
        assertArrayEquals(new Integer[]{0, 2}, patterns.getMatchedPatterns("alpha"));
        assertEquals(2, patterns.getMatchedPattern("alpha", 1).orElseThrow());
        assertTrue(patterns.getMatchedPattern("missing", 0).isEmpty());
    }
}
