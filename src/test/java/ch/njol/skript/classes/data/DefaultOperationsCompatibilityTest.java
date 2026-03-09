package ch.njol.skript.classes.data;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import ch.njol.skript.Skript;
import ch.njol.skript.util.Timespan;
import java.lang.reflect.Field;
import java.util.Map;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.skriptlang.skript.lang.arithmetic.Arithmetics;
import org.skriptlang.skript.lang.arithmetic.Operator;

class DefaultOperationsCompatibilityTest {

    @AfterEach
    void cleanup() throws Exception {
        clearArithmeticRegistry("OPERATIONS");
        clearArithmeticRegistry("CACHED_OPERATIONS");
        clearArithmeticRegistry("CACHED_CONVERTED_OPERATIONS");
        clearArithmeticRegistry("DIFFERENCES");
        clearArithmeticRegistry("CACHED_DIFFERENCES");
        clearArithmeticRegistry("DEFAULT_VALUES");
        clearArithmeticRegistry("CACHED_DEFAULT_VALUES");
        Skript.setAcceptRegistrations(false);
    }

    @Test
    void registersPureJavaArithmeticSubset() {
        DefaultOperations.register();

        assertEquals(3L, ((Number) Arithmetics.calculateUnsafe(Operator.ADDITION, 1L, 2L)).longValue());
        assertEquals(Double.MAX_VALUE * 2, Arithmetics.calculateUnsafe(Operator.ADDITION, Double.MAX_VALUE, Double.MAX_VALUE));
        assertEquals(6L, ((Number) Arithmetics.calculateUnsafe(Operator.MULTIPLICATION, 2L, 3L)).longValue());
        assertEquals(2.5D, Arithmetics.calculateUnsafe(Operator.DIVISION, 5, 2));
        assertEquals("ab", Arithmetics.calculateUnsafe(Operator.ADDITION, "a", "b"));

        Timespan left = new Timespan(Timespan.TimePeriod.MINUTE, 2);
        Timespan right = new Timespan(Timespan.TimePeriod.SECOND, 30);
        Timespan multiplied = Arithmetics.calculateUnsafe(Operator.MULTIPLICATION, left, 2);

        assertNotNull(multiplied);
        Timespan difference = Arithmetics.differenceUnsafe(left, right);

        assertNotNull(difference);
        assertEquals(90_000L, difference.millis());
        assertEquals(240_000L, multiplied.millis());
        assertNull(Arithmetics.calculateUnsafe(Operator.MULTIPLICATION, left, -1));
        assertEquals(0L, ((Number) Arithmetics.getDefaultValue(Number.class)).longValue());
        assertEquals(0L, ((Number) Arithmetics.getDefaultValue(Long.class)).longValue());
    }

    @SuppressWarnings("unchecked")
    private static void clearArithmeticRegistry(String fieldName) throws Exception {
        Field field = Arithmetics.class.getDeclaredField(fieldName);
        field.setAccessible(true);
        ((Map<?, ?>) field.get(null)).clear();
    }
}
