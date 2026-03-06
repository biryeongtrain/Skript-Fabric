package ch.njol.skript.lang.util;

import ch.njol.skript.lang.Expression;
import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.Iterator;
import java.util.function.Predicate;
import java.util.stream.Stream;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.lang.event.SkriptEvent;

public abstract class SimpleExpression<T> implements Expression<T> {

    protected abstract T @Nullable [] get(SkriptEvent event);

    @Override
    @SuppressWarnings("unchecked")
    public T[] getArray(SkriptEvent event) {
        T[] values = get(event);
        if (values == null) {
            return (T[]) Array.newInstance(getReturnType(), 0);
        }
        return values;
    }

    @Override
    public T[] getAll(SkriptEvent event) {
        if (getAnd()) {
            return getArray(event);
        }
        T single = getSingle(event);
        if (single == null) {
            @SuppressWarnings("unchecked")
            T[] empty = (T[]) Array.newInstance(getReturnType(), 0);
            return empty;
        }
        @SuppressWarnings("unchecked")
        T[] one = (T[]) Array.newInstance(getReturnType(), 1);
        one[0] = single;
        return one;
    }

    @Override
    public Stream<? extends T> stream(SkriptEvent event) {
        T[] values = getArray(event);
        if (values.length == 0) {
            return Stream.empty();
        }
        return Arrays.stream(values);
    }

    @Override
    public @Nullable Iterator<? extends T> iterator(SkriptEvent event) {
        T[] values = getArray(event);
        if (values.length == 0) {
            return null;
        }
        return Arrays.asList(values).iterator();
    }

    @Override
    public @Nullable T getSingle(SkriptEvent event) {
        T[] values = getArray(event);
        if (values.length == 0) {
            return null;
        }
        return values[0];
    }

    @Override
    public boolean check(SkriptEvent event, Predicate<? super T> checker, boolean negated) {
        return negated ^ check(event, checker);
    }

    @Override
    public boolean check(SkriptEvent event, Predicate<? super T> checker) {
        return Expression.super.check(event, checker);
    }

    @Override
    public boolean getAnd() {
        return true;
    }

    @Override
    public String toString(SkriptEvent event, boolean debug) {
        return getClass().getSimpleName();
    }
}
