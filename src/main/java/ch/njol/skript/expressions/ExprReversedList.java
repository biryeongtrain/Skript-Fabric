package ch.njol.skript.expressions;

import ch.njol.skript.Skript;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.KeyedIterableExpression;
import ch.njol.skript.lang.KeyedValue;
import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.simplification.SimplifiedLiteral;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.skript.util.LiteralUtils;
import ch.njol.util.Kleenean;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.lang.event.SkriptEvent;

public class ExprReversedList extends SimpleExpression<Object> implements KeyedIterableExpression<Object> {

    static {
        Skript.registerExpression(ExprReversedList.class, Object.class, "reversed %objects%");
    }

    private Expression<?> list;
    private boolean keyed;

    public ExprReversedList() {
    }

    public ExprReversedList(Expression<?> list) {
        this.list = list;
        this.keyed = KeyedIterableExpression.canIterateWithKeys(list);
    }

    @Override
    public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
        list = LiteralUtils.defendExpression(expressions[0]);
        if (list.isSingle()) {
            Skript.error("A single object cannot be reversed.");
            return false;
        }
        keyed = KeyedIterableExpression.canIterateWithKeys(list);
        return LiteralUtils.canInitSafely(list);
    }

    @Override
    protected Object @Nullable [] get(SkriptEvent event) {
        Object[] array = list.getArray(event);
        reverse(array);
        return array;
    }

    @Override
    public @Nullable Iterator<?> iterator(SkriptEvent event) {
        List<?> values = List.of(list.getArray(event));
        return new Iterator<>() {
            private final ListIterator<?> iterator = values.listIterator(values.size());

            @Override
            public boolean hasNext() {
                return iterator.hasPrevious();
            }

            @Override
            public Object next() {
                return iterator.previous();
            }
        };
    }

    @Override
    public boolean canIterateWithKeys() {
        return keyed;
    }

    @Override
    public Iterator<KeyedValue<Object>> keyedIterator(SkriptEvent event) {
        if (!keyed) {
            throw new UnsupportedOperationException();
        }
        Iterator<? extends KeyedValue<?>> source = ((KeyedIterableExpression<?>) list).keyedIterator(event);
        List<KeyedValue<?>> values = new ArrayList<>();
        while (source.hasNext()) {
            values.add(source.next());
        }
        return new Iterator<>() {
            private final ListIterator<KeyedValue<?>> iterator = values.listIterator(values.size());

            @Override
            public boolean hasNext() {
                return iterator.hasPrevious();
            }

            @Override
            @SuppressWarnings("unchecked")
            public KeyedValue<Object> next() {
                return (KeyedValue<Object>) iterator.previous();
            }
        };
    }

    @Override
    @SafeVarargs
    @SuppressWarnings("unchecked")
    public final <R> @Nullable Expression<? extends R> getConvertedExpression(Class<R>... to) {
        for (Class<R> type : to) {
            if (type.isAssignableFrom(getReturnType())) {
                return (Expression<? extends R>) this;
            }
        }
        Expression<? extends R> convertedList = list.getConvertedExpression(to);
        if (convertedList != null) {
            return (Expression<? extends R>) new ExprReversedList(convertedList);
        }
        return null;
    }

    private void reverse(Object[] array) {
        for (int i = 0; i < array.length / 2; i++) {
            Object temp = array[i];
            int reverseIndex = array.length - i - 1;
            array[i] = array[reverseIndex];
            array[reverseIndex] = temp;
        }
    }

    @Override
    public Class<?> getReturnType() {
        return list.getReturnType();
    }

    @Override
    public Class<?>[] possibleReturnTypes() {
        return list.possibleReturnTypes();
    }

    @Override
    public boolean canReturn(Class<?> returnType) {
        return list.canReturn(returnType);
    }

    @Override
    public boolean isSingle() {
        return false;
    }

    @Override
    public boolean isIndexLoop(String input) {
        if (!keyed) {
            throw new IllegalStateException();
        }
        return ((KeyedIterableExpression<?>) list).isIndexLoop(input);
    }

    @Override
    public boolean isLoopOf(String input) {
        return list.isLoopOf(input);
    }

    @Override
    public Expression<?> simplify() {
        if (list instanceof Literal<?>) {
            return SimplifiedLiteral.fromExpression(this);
        }
        return this;
    }

    @Override
    public String toString(@Nullable SkriptEvent event, boolean debug) {
        return "reversed " + list.toString(event, debug);
    }
}
