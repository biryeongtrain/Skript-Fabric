package org.skriptlang.skript.fabric.runtime;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import ch.njol.skript.expressions.ExprRandomCharacter;
import ch.njol.skript.expressions.ExprTimes;
import ch.njol.skript.expressions.arithmetic.ExprArithmetic;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ParseContext;
import ch.njol.skript.lang.SkriptParser;
import net.minecraft.SharedConstants;
import net.minecraft.server.Bootstrap;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.skriptlang.skript.lang.event.SkriptEvent;

final class RandomExpressionSyntaxTest {

    @BeforeAll
    static void bootstrapMinecraft() {
        SharedConstants.tryDetectVersion();
        Bootstrap.bootStrap();
        SkriptFabricBootstrap.bootstrap();
    }

    @Test
    void randomCharacterAndTimesParseThroughBootstrap() {
        Expression<? extends String> randomCharacter = parse(
                "3 random alphanumeric characters between \"0\" and \"C\"",
                String.class
        );
        assertInstanceOf(ExprRandomCharacter.class, randomCharacter);
        String[] values = randomCharacter.getArray(SkriptEvent.EMPTY);
        assertEquals(3, values.length);
        for (String value : values) {
            assertEquals(1, value.length());
            assertTrue(Character.isLetterOrDigit(value.charAt(0)));
        }

        Expression<? extends Long> times = parse("twice", Long.class);
        assertInstanceOf(ExprTimes.class, times);
        assertArrayEquals(new Long[]{1L, 2L}, times.getArray(SkriptEvent.EMPTY));
    }

    /**
     * Verifies that "10 / 2 times" parses as ExprTimes wrapping an arithmetic division,
     * not as ExprArithmetic consuming "2 times" as its right operand.
     * This is the literal-only version of the "loop 1440 / {_stepSize} times" pattern.
     */
    @Test
    void arithmeticDivisionTimesIsNotConsumedByExprArithmetic() {
        // "10 / 2 times" should parse as ExprTimes with %number% = "10 / 2" (= 5)
        Expression<? extends Long> times = parse("10 / 2 times", Long.class);
        assertInstanceOf(ExprTimes.class, times);
        assertFalse(times.isSingle());
        assertArrayEquals(new Long[]{1L, 2L, 3L, 4L, 5L}, times.getArray(SkriptEvent.EMPTY));
    }

    /**
     * Verifies that a standalone arithmetic expression still works fine.
     */
    @Test
    void standaloneArithmeticStillParses() {
        Expression<? extends Number> arith = parse("10 / 2", Number.class);
        assertInstanceOf(ExprArithmetic.class, arith);
        assertTrue(arith.isSingle());
        assertEquals(5L, arith.getSingle(SkriptEvent.EMPTY));
    }

    @SuppressWarnings("unchecked")
    private static <T> Expression<? extends T> parse(String input, Class<T> type) {
        Expression<? extends T> parsed = new SkriptParser(input, SkriptParser.ALL_FLAGS, ParseContext.DEFAULT)
                .parseExpression(new Class[]{type});
        assertNotNull(parsed);
        return parsed;
    }
}
