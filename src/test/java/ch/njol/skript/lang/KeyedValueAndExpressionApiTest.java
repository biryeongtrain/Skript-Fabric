package ch.njol.skript.lang;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.jetbrains.annotations.Nullable;
import org.junit.jupiter.api.Test;
import org.skriptlang.skript.lang.event.SkriptEvent;

class KeyedValueAndExpressionApiTest {

    @Test
    void zipWithoutKeysUsesNumericIndices() {
        KeyedValue<Integer>[] zipped = KeyedValue.zip(new Integer[]{10, 20}, null);
        assertEquals("1", zipped[0].key());
        assertEquals("2", zipped[1].key());
        assertEquals(10, zipped[0].value());
        assertEquals(20, zipped[1].value());
    }

    @Test
    void mapTransformsValuesAndPreservesKeys() {
        KeyedValue<Integer>[] source = KeyedValue.zip(new Integer[]{1, 2, 3}, new String[]{"a", "b", "c"});
        KeyedValue<String>[] mapped = KeyedValue.map(source, value -> "v" + value);
        assertEquals("a", mapped[0].key());
        assertEquals("v1", mapped[0].value());
        assertEquals("c", mapped[2].key());
        assertEquals("v3", mapped[2].value());
    }

    @Test
    void keyProviderDefaultIteratorUsesValuesAndKeys() {
        DummyKeyProvider provider = new DummyKeyProvider();
        Iterator<KeyedValue<Integer>> iterator = provider.keyedIterator(SkriptEvent.EMPTY);

        List<String> keys = new ArrayList<>();
        List<Integer> values = new ArrayList<>();
        while (iterator.hasNext()) {
            KeyedValue<Integer> next = iterator.next();
            keys.add(next.key());
            values.add(next.value());
        }

        assertArrayEquals(new String[]{"x", "y"}, keys.toArray(String[]::new));
        assertArrayEquals(new Integer[]{7, 8}, values.toArray(Integer[]::new));
        assertTrue(provider.canIterateWithKeys());
        assertTrue(KeyedIterableExpression.canIterateWithKeys(provider));
        assertTrue(KeyProviderExpression.canReturnKeys(provider));
    }

    private static class DummyKeyProvider implements KeyProviderExpression<Integer> {

        @Override
        public Integer[] getArray(SkriptEvent event) {
            return new Integer[]{7, 8};
        }

        @Override
        public String[] getArrayKeys(SkriptEvent event) {
            return new String[]{"x", "y"};
        }

        @Override
        public String toString(@Nullable SkriptEvent event, boolean debug) {
            return "dummy-key-provider";
        }
    }
}
