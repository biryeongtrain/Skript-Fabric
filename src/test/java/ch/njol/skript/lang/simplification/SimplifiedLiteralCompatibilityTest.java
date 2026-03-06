package ch.njol.skript.lang.simplification;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.util.ContextlessEvent;
import java.util.Arrays;
import org.jetbrains.annotations.Nullable;
import org.junit.jupiter.api.Test;
import org.skriptlang.skript.lang.event.SkriptEvent;

class SimplifiedLiteralCompatibilityTest {

    @Test
    void contextlessEventReturnsEmptyEvent() {
        assertEquals(SkriptEvent.EMPTY, ContextlessEvent.get());
    }

    @Test
    void fromExpressionSnapshotsValuesAndKeepsSource() {
        MutableExpression source = new MutableExpression(1, 2);

        SimplifiedLiteral<Integer> simplified = SimplifiedLiteral.fromExpression(source);

        assertArrayEquals(new Integer[]{1, 2}, simplified.getAll(SkriptEvent.EMPTY));
        assertEquals(source, simplified.getSource());
        assertTrue(simplified.toString(SkriptEvent.EMPTY, true).contains("SIMPLIFIED"));
    }

    @Test
    void changeDelegatesBackToSourceExpression() {
        MutableExpression source = new MutableExpression(5);
        SimplifiedLiteral<Integer> simplified = SimplifiedLiteral.fromExpression(source);

        simplified.change(SkriptEvent.EMPTY, new Object[]{8}, ChangeMode.SET);
        assertArrayEquals(new Integer[]{8}, source.values());

        simplified.changeInPlace(SkriptEvent.EMPTY, value -> value + 1, true);
        assertArrayEquals(new Integer[]{9}, source.values());
    }

    private static class MutableExpression implements Expression<Integer> {

        private Integer[] values;

        private MutableExpression(Integer... values) {
            this.values = values;
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
        public boolean isSingle() {
            return values.length <= 1;
        }

        @Override
        public Class<? extends Integer> getReturnType() {
            return Integer.class;
        }

        @Override
        public Class<?> @Nullable [] acceptChange(ChangeMode mode) {
            return new Class[]{Integer.class};
        }

        @Override
        public void change(SkriptEvent event, Object @Nullable [] delta, ChangeMode mode) {
            if (mode == ChangeMode.SET && delta != null) {
                values = Arrays.stream(delta)
                        .filter(Integer.class::isInstance)
                        .map(Integer.class::cast)
                        .toArray(Integer[]::new);
            }
        }

        @Override
        public String toString(@Nullable SkriptEvent event, boolean debug) {
            return "mutable-expression";
        }

        private Integer[] values() {
            return values;
        }
    }
}
