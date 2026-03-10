package ch.njol.skript.expressions;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertTrue;

import ch.njol.skript.classes.ClassInfo;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionList;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.lang.util.SimpleLiteral;
import ch.njol.util.Kleenean;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.skriptlang.skript.lang.event.SkriptEvent;

class ExpressionClosureCompatibilityTest {

    @Test
    void randomChoosesTypedValueFromExpressionList() {
        ClassInfo<String> stringInfo = new ClassInfo<>(String.class, "string");

        ExprRandom random = new ExprRandom();
        ExpressionList<Object> values = new ExpressionList<>(
                new Expression[]{
                        new SimpleLiteral<>("alpha", false),
                        new SimpleLiteral<>("beta", false)
                },
                Object.class,
                true
        );

        boolean initialized = random.init(new Expression[]{
                new SimpleLiteral<>(stringInfo, false),
                values
        }, 0, Kleenean.FALSE, parseResult(""));

        assertTrue(initialized);
        assertTrue(random.isSingle());
        assertTrue(List.of("alpha", "beta").contains(random.getSingle(SkriptEvent.EMPTY)));
    }

    @Test
    void randomCharacterAndTimesMatchLegacyHelpers() {
        ExprRandomCharacter randomCharacter = new ExprRandomCharacter();
        SkriptParser.ParseResult alphanumeric = parseResult("");
        alphanumeric.tags.add("alphanumeric");
        randomCharacter.init(new Expression[]{
                new SimpleLiteral<>(4, false),
                new SimpleLiteral<>("0", false),
                new SimpleLiteral<>("C", false)
        }, 0, Kleenean.FALSE, alphanumeric);

        String[] randomValues = randomCharacter.getArray(SkriptEvent.EMPTY);
        assertTrue(randomValues.length == 4);
        for (String value : randomValues) {
            assertTrue(value.length() == 1);
            assertTrue(Character.isLetterOrDigit(value.charAt(0)));
        }

        ExprTimes times = new ExprTimes();
        times.init(new Expression[]{new SimpleLiteral<>(3, false)}, 0, Kleenean.FALSE, parseResult(""));
        assertArrayEquals(new Long[]{1L, 2L, 3L}, times.getArray(SkriptEvent.EMPTY));

        ExprTimes twice = new ExprTimes();
        twice.init(new Expression[0], 2, Kleenean.FALSE, parseResult(""));
        assertArrayEquals(new Long[]{1L, 2L}, twice.getArray(SkriptEvent.EMPTY));
    }

    @Test
    void importedExpressionsInstantiate() {
        assertDoesNotThrow(ExprGlowing::new);
        assertDoesNotThrow(ExprRandom::new);
        assertDoesNotThrow(ExprRandomCharacter::new);
        assertDoesNotThrow(ExprTimes::new);
    }

    private static SkriptParser.ParseResult parseResult(String expr) {
        SkriptParser.ParseResult result = new SkriptParser.ParseResult();
        result.expr = expr;
        return result;
    }
}
