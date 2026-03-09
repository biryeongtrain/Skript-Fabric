package ch.njol.skript.literals;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import ch.njol.skript.Skript;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ParseContext;
import ch.njol.skript.lang.SkriptParser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.skriptlang.skript.lang.event.SkriptEvent;

class LiteralsCompatibilityTest {

    @BeforeEach
    void resetRegistry() {
        Skript.instance().syntaxRegistry().clearAll();
    }

    @Test
    void numericBoundaryLiteralsParseToExpectedValues() {
        LitDoubleMaxValue.register();
        LitDoubleMinValue.register();
        LitFloatMaxValue.register();
        LitFloatMinValue.register();
        LitIntMaxValue.register();
        LitIntMinValue.register();
        LitLongMaxValue.register();
        LitLongMinValue.register();

        assertLiteralValue("maximum double value", Double.class, Double.MAX_VALUE, LitDoubleMaxValue.class);
        assertLiteralValue("minimum double value", Double.class, Double.MIN_VALUE, LitDoubleMinValue.class);
        assertLiteralValue("maximum float value", Float.class, Float.MAX_VALUE, LitFloatMaxValue.class);
        assertLiteralValue("minimum float value", Float.class, Float.MIN_VALUE, LitFloatMinValue.class);
        assertLiteralValue("maximum integer value", Integer.class, Integer.MAX_VALUE, LitIntMaxValue.class);
        assertLiteralValue("minimum integer value", Integer.class, Integer.MIN_VALUE, LitIntMinValue.class);
        assertLiteralValue("maximum long value", Long.class, Long.MAX_VALUE, LitLongMaxValue.class);
        assertLiteralValue("minimum long value", Long.class, Long.MIN_VALUE, LitLongMinValue.class);
    }

    @Test
    void specialNumericLiteralsPreserveInfinityNanAndPi() {
        LitInfinity.register();
        LitNegativeInfinity.register();
        LitNaN.register();
        LitPi.register();

        assertEquals(Double.POSITIVE_INFINITY, parse("positive infinity", Double.class).getSingle(SkriptEvent.EMPTY));
        assertEquals(Double.NEGATIVE_INFINITY, parse("negative infinity", Double.class).getSingle(SkriptEvent.EMPTY));
        assertTrue(Double.isNaN(parse("NaN", Double.class).getSingle(SkriptEvent.EMPTY)));
        assertEquals(Math.PI, parse("pi", Double.class).getSingle(SkriptEvent.EMPTY));
    }

    @Test
    void newlineLiteralReturnsLineBreak() {
        LitNewLine.register();

        Expression<? extends String> newline = parse("line break", String.class);

        assertEquals("\n", newline.getSingle(SkriptEvent.EMPTY));
        assertInstanceOf(LitNewLine.class, newline);
    }

    private static <T> void assertLiteralValue(
            String input,
            Class<T> type,
            T expected,
            Class<?> implementation
    ) {
        Expression<? extends T> parsed = parse(input, type);
        assertEquals(expected, parsed.getSingle(SkriptEvent.EMPTY));
        assertInstanceOf(implementation, parsed);
    }

    @SuppressWarnings("unchecked")
    private static <T> Expression<? extends T> parse(String input, Class<T> type) {
        Expression<? extends T> parsed = new SkriptParser(input, SkriptParser.ALL_FLAGS, ParseContext.DEFAULT)
                .parseExpression(new Class[]{type});
        assertNotNull(parsed);
        return parsed;
    }
}
