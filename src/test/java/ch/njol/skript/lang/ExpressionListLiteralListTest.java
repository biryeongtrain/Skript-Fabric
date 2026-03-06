package ch.njol.skript.lang;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.jetbrains.annotations.Nullable;
import org.junit.jupiter.api.Test;
import org.skriptlang.skript.lang.event.SkriptEvent;

class ExpressionListLiteralListTest {

    @Test
    void andExpressionListConcatenatesAllValues() {
        ExpressionList<Integer> list = new ExpressionList<>(
                expressions(new StaticExpression<>(Integer.class, 1), new StaticExpression<>(Integer.class, 2, 3)),
                Integer.class,
                true
        );

        assertArrayEquals(new Integer[]{1, 2, 3}, list.getAll(SkriptEvent.EMPTY));
        assertTrue(list.getAnd());
    }

    @Test
    void andExpressionListIteratorTraversesAllExpressions() {
        ExpressionList<Integer> list = new ExpressionList<>(
                expressions(new StaticExpression<>(Integer.class, 4), new StaticExpression<>(Integer.class, 5)),
                Integer.class,
                true
        );

        Iterator<? extends Integer> iterator = list.iterator(SkriptEvent.EMPTY);
        List<Integer> values = new ArrayList<>();
        while (iterator != null && iterator.hasNext()) {
            values.add(iterator.next());
        }

        assertArrayEquals(new Integer[]{4, 5}, values.toArray(Integer[]::new));
    }

    @Test
    void simplifyReturnsLiteralListWhenAllEntriesAreLiteral() {
        ExpressionList<String> list = new ExpressionList<>(
                expressions(new StaticLiteral<>(String.class, "a"), new StaticLiteral<>(String.class, "b")),
                String.class,
                true
        );

        Expression<String> simplified = list.simplify();
        assertInstanceOf(LiteralList.class, simplified);
        assertArrayEquals(new String[]{"a", "b"}, simplified.getAll(SkriptEvent.EMPTY));
    }

    @Test
    void literalListConvertedExpressionProducesLiteralList() {
        LiteralList<String> list = new LiteralList<>(
                literals(new StaticLiteral<>(String.class, "x"), new StaticLiteral<>(String.class, "y")),
                String.class,
                true
        );

        Literal<? extends Object> converted = list.getConvertedExpression(Object.class);
        assertNotNull(converted);
        assertInstanceOf(LiteralList.class, converted);
        assertEquals(String.class, converted.getReturnType());
        assertArrayEquals(new Object[]{"x", "y"}, converted.getAll(SkriptEvent.EMPTY));
    }

    @SafeVarargs
    private static <T> Expression<? extends T>[] expressions(Expression<? extends T>... expressions) {
        return expressions;
    }

    @SafeVarargs
    private static <T> Literal<? extends T>[] literals(Literal<? extends T>... literals) {
        return literals;
    }

    private static class StaticExpression<T> implements Expression<T> {

        private final Class<T> type;
        private final T[] values;

        @SafeVarargs
        private StaticExpression(Class<T> type, T... values) {
            this.type = type;
            this.values = values;
        }

        @Override
        public T[] getArray(SkriptEvent event) {
            return values;
        }

        @Override
        public T[] getAll(SkriptEvent event) {
            return values;
        }

        @Override
        public boolean isSingle() {
            return values.length <= 1;
        }

        @Override
        public Class<? extends T> getReturnType() {
            return type;
        }

        @Override
        public String toString(@Nullable SkriptEvent event, boolean debug) {
            return "static-expression";
        }
    }

    private static class StaticLiteral<T> extends StaticExpression<T> implements Literal<T> {

        @SafeVarargs
        private StaticLiteral(Class<T> type, T... values) {
            super(type, values);
        }
    }
}
