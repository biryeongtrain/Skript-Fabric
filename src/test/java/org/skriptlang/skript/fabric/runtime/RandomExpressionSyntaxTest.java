package org.skriptlang.skript.fabric.runtime;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import ch.njol.skript.expressions.ExprRandomCharacter;
import ch.njol.skript.expressions.ExprTimes;
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

    @SuppressWarnings("unchecked")
    private static <T> Expression<? extends T> parse(String input, Class<T> type) {
        Expression<? extends T> parsed = new SkriptParser(input, SkriptParser.ALL_FLAGS, ParseContext.DEFAULT)
                .parseExpression(new Class[]{type});
        assertNotNull(parsed);
        return parsed;
    }
}
