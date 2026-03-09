package ch.njol.skript.classes.data;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import ch.njol.skript.classes.ClassInfo;
import ch.njol.skript.lang.function.Function;
import ch.njol.skript.lang.function.FunctionRegistry;
import ch.njol.skript.lang.function.Functions;
import ch.njol.skript.registrations.Classes;
import ch.njol.skript.util.Date;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

class DefaultFunctionsCompatibilityTest {

    @AfterEach
    void cleanup() {
        FunctionRegistry.getRegistry().clear();
        Functions.clear();
        Classes.clearClassInfos();
    }

    @Test
    void registersPureJavaMathBaseAndFormattingFunctions() {
        registerFunctionClassInfos();
        DefaultFunctions.register();

        assertEquals(3L, single("floor", 3.9D));
        assertEquals(3.14D, ((Number) single("round", 3.14159D, 2)).doubleValue(), 1.0E-9);
        assertEquals(10.0D, ((Number) singlePlural("sum", 1, 2L, 3.5D, 3.5F)).doubleValue(), 1.0E-9);
        assertEquals(24.0D, ((Number) singlePlural("product", 2, 3, 4)).doubleValue(), 1.0E-9);
        assertEquals(9.0D, ((Number) singlePlural("max", 1, 9, 2)).doubleValue(), 1.0E-9);
        assertEquals(1.0D, ((Number) singlePlural("min", 1, 9, 2)).doubleValue(), 1.0E-9);
        assertEquals(-90.0D, ((Number) single("atan2", 0, -1)).doubleValue(), 1.0E-9);
        assertEquals(Boolean.TRUE, single("isNaN", Double.NaN));
        assertEquals(352L, single("calcExperience", 16L));
        assertEquals("1,234.5", single("formatNumber", 1234.5D, ""));
        assertEquals("0123", single("formatNumber", 123D, "0000"));
        assertEquals("value=7", singlePlural("concat", "value=", 7L));
    }

    @Test
    void baseAndDateFunctionsFollowCompatibilitySubset() {
        registerFunctionClassInfos();
        DefaultFunctions.register();

        assertArrayEquals(new String[]{"ff", "10"}, plural("toBase", new Object[]{255L, 16L}, new Object[]{16L}));
        assertArrayEquals(new Long[]{255L, 16L}, plural("fromBase", new Object[]{"ff", "10"}, new Object[]{16L}));
        assertNull(call("fromBase", new Object[]{"nope"}, new Object[]{2L}));

        Object value = single("date", 2026, 3, 10, 4, 5, 6, 7, 0, 0);
        assertTrue(value instanceof Date);
        assertEquals("2026-03-10T04:05:06.007Z", value.toString());
    }

    private static void registerFunctionClassInfos() {
        JavaClasses.register();
        registerIfMissing(Date.class, "date");
    }

    private static Object single(String name, Object... values) {
        Object[] result = call(name, wrapAll(values));
        assertNotNull(result);
        return result[0];
    }

    private static Object singlePlural(String name, Object... values) {
        Function<?> function = Functions.getFunction(name);
        assertNotNull(function, name);
        Object[] result = function.execute(new Object[][]{values});
        assertNotNull(result);
        return result[0];
    }

    private static Object[] plural(String name, Object[]... args) {
        Object[] result = call(name, args);
        assertNotNull(result);
        return result;
    }

    private static Object[] call(String name, Object[]... args) {
        Function<?> function = Functions.getFunction(name);
        assertNotNull(function, name);
        return function.execute(args);
    }

    private static Object[][] wrapAll(Object... values) {
        Object[][] wrapped = new Object[values.length][];
        for (int index = 0; index < values.length; index++) {
            wrapped[index] = new Object[]{values[index]};
        }
        return wrapped;
    }

    private static <T> void registerIfMissing(Class<T> type, String codeName) {
        if (Classes.getExactClassInfo(type) == null) {
            Classes.registerClassInfo(new ClassInfo<>(type, codeName));
        }
    }
}
