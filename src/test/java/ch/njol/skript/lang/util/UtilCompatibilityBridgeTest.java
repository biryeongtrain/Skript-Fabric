package ch.njol.skript.lang.util;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import ch.njol.skript.SkriptAPIException;
import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.KeyProviderExpression;
import ch.njol.skript.lang.KeyReceiverExpression;
import ch.njol.skript.lang.KeyedValue;
import ch.njol.skript.lang.Literal;
import ch.njol.skript.util.Container;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import org.jetbrains.annotations.Nullable;
import org.junit.jupiter.api.Test;
import org.skriptlang.skript.lang.converter.ConverterInfo;
import org.skriptlang.skript.lang.event.SkriptEvent;

class UtilCompatibilityBridgeTest {

    @Test
    void containerExpressionFlattensContainerItems() {
        ContainerExpression expression = new ContainerExpression(
                new ContainerSourceExpression(
                        container(1, 2),
                        container(3)
                ),
                Integer.class
        );

        Iterator<? extends Object> iterator = expression.iterator(SkriptEvent.EMPTY);
        List<Integer> values = new ArrayList<>();
        while (iterator != null && iterator.hasNext()) {
            values.add((Integer) iterator.next());
        }

        assertArrayEquals(new Integer[]{1, 2, 3}, values.toArray(Integer[]::new));
    }

    @Test
    void convertedLiteralKeepsLiteralBehavior() {
        Literal<Integer> source = new SimpleLiteral<>(1, false);
        ConvertedLiteral<Integer, String> literal = new ConvertedLiteral<>(source, new String[]{"1"}, String.class);

        assertEquals("1", literal.getSingle(SkriptEvent.EMPTY));
        assertNotNull(literal.getConvertedExpression(Object.class));

        ConvertedLiteral<Integer, String> nonSingle = new ConvertedLiteral<>(source, new String[]{"1", "2"}, String.class);
        assertThrows(SkriptAPIException.class, () -> nonSingle.getSingle(SkriptEvent.EMPTY));
    }

    @Test
    void convertedKeyProviderExpressionPreservesKeysAndDelegatesKeyedChange() {
        KeyedSourceExpression source = new KeyedSourceExpression(
                new Integer[]{1, 2, 3},
                new String[]{"a", "b", "c"}
        );
        ConverterInfo<Integer, String> converterInfo = new ConverterInfo<>(Integer.class, String.class, String::valueOf, 0);
        ConvertedKeyProviderExpression<Integer, String> converted =
                new ConvertedKeyProviderExpression<>(source, String.class, converterInfo);

        assertThrows(IllegalStateException.class, () -> converted.getArrayKeys(SkriptEvent.EMPTY));

        assertArrayEquals(new String[]{"1", "2", "3"}, converted.getArray(SkriptEvent.EMPTY));
        assertArrayEquals(new String[]{"a", "b", "c"}, converted.getArrayKeys(SkriptEvent.EMPTY));

        Iterator<KeyedValue<String>> keyedIterator = converted.keyedIterator(SkriptEvent.EMPTY);
        List<String> keyedValues = new ArrayList<>();
        while (keyedIterator.hasNext()) {
            keyedValues.add(keyedIterator.next().value());
        }
        assertArrayEquals(new String[]{"1", "2", "3"}, keyedValues.toArray(String[]::new));

        converted.change(SkriptEvent.EMPTY, new Object[]{"x"}, ChangeMode.SET, new String[]{"index"});
        assertArrayEquals(new String[]{"index"}, source.lastChangedKeys);
        assertEquals(ChangeMode.SET, source.lastChangeMode);
    }

    @Test
    void sectionUtilsExecutesProvidedHooks() {
        AtomicBoolean beforeExecuted = new AtomicBoolean(false);
        AtomicBoolean afterExecuted = new AtomicBoolean(false);

        SectionUtils.loadLinkedCode("test", (before, after) -> {
            before.run();
            beforeExecuted.set(true);
            after.run();
            afterExecuted.set(true);
            return null;
        });

        assertTrue(beforeExecuted.get());
        assertTrue(afterExecuted.get());
    }

    @SafeVarargs
    private static Container<Integer> container(Integer... values) {
        return () -> Arrays.asList(values).iterator();
    }

    private static class ContainerSourceExpression implements Expression<Container<?>> {

        private final Container<?>[] values;

        private ContainerSourceExpression(Container<?>... values) {
            this.values = values;
        }

        @Override
        public Container<?>[] getArray(SkriptEvent event) {
            return values;
        }

        @Override
        public Container<?>[] getAll(SkriptEvent event) {
            return values;
        }

        @Override
        public boolean isSingle() {
            return values.length <= 1;
        }

        @Override
        public Class<? extends Container<?>> getReturnType() {
            @SuppressWarnings("unchecked")
            Class<? extends Container<?>> type = (Class<? extends Container<?>>) (Class<?>) Container.class;
            return type;
        }

        @Override
        public String toString(@Nullable SkriptEvent event, boolean debug) {
            return "container-source";
        }
    }

    private static class KeyedSourceExpression implements KeyProviderExpression<Integer>, KeyReceiverExpression<Integer> {

        private final Integer[] values;
        private final String[] keys;
        private String[] lastChangedKeys = new String[0];
        private ChangeMode lastChangeMode;

        private KeyedSourceExpression(Integer[] values, String[] keys) {
            this.values = values;
            this.keys = keys;
        }

        @Override
        public Integer[] getArray(SkriptEvent event) {
            return Arrays.copyOf(values, values.length);
        }

        @Override
        public Integer[] getAll(SkriptEvent event) {
            return Arrays.copyOf(values, values.length);
        }

        @Override
        public @org.jetbrains.annotations.NotNull String[] getArrayKeys(SkriptEvent event) throws IllegalStateException {
            return Arrays.copyOf(keys, keys.length);
        }

        @Override
        public @org.jetbrains.annotations.NotNull String[] getAllKeys(SkriptEvent event) {
            return Arrays.copyOf(keys, keys.length);
        }

        @Override
        public Class<? extends Integer> getReturnType() {
            return Integer.class;
        }

        @Override
        public boolean isSingle() {
            return false;
        }

        @Override
        public void change(SkriptEvent event, Object @org.jetbrains.annotations.NotNull [] delta, ChangeMode mode, @org.jetbrains.annotations.NotNull String @org.jetbrains.annotations.NotNull [] keys) {
            this.lastChangedKeys = Arrays.copyOf(keys, keys.length);
            this.lastChangeMode = mode;
        }

        @Override
        public String toString(@Nullable SkriptEvent event, boolean debug) {
            return "keyed-source";
        }
    }
}
