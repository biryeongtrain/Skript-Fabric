package ch.njol.skript.lang.util;

import ch.njol.skript.SkriptAPIException;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.Literal;
import ch.njol.skript.registrations.Classes;
import java.util.Arrays;
import java.util.Iterator;
import java.util.function.Predicate;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.lang.converter.ConverterInfo;
import org.skriptlang.skript.lang.converter.Converters;
import org.skriptlang.skript.lang.event.SkriptEvent;

/**
 * Literal representation for values converted from another literal expression.
 */
public class ConvertedLiteral<F, T> extends ConvertedExpression<F, T> implements Literal<T> {

    protected transient T[] data;

    @SuppressWarnings("unchecked")
    public ConvertedLiteral(Literal<F> source, T[] data, Class<T> to) {
        super(source, to, new ConverterInfo<>((Class<F>) source.getReturnType(), to, value -> Converters.convert(value, to), 0));
        this.data = data == null ? empty(to) : Arrays.copyOf(data, data.length);
        if (this.data.length == 0) {
            throw new IllegalArgumentException("ConvertedLiteral data must not be empty");
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public <R> @Nullable Literal<? extends R> getConvertedExpression(Class<R>... to) {
        if (to != null) {
            for (Class<R> targetType : to) {
                if (targetType.isAssignableFrom(this.targetType)) {
                    return (Literal<? extends R>) this;
                }
            }
        }
        Expression<? extends R> converted = ((Literal<F>) source).getConvertedExpression(to);
        if (converted instanceof Literal<? extends R> literal) {
            return literal;
        }
        return null;
    }

    @Override
    public String toString(@Nullable SkriptEvent event, boolean debug) {
        return Classes.toString(data, getAnd());
    }

    @Override
    public T[] getArray(SkriptEvent event) {
        return Arrays.copyOf(data, data.length);
    }

    @Override
    public T[] getAll(SkriptEvent event) {
        return Arrays.copyOf(data, data.length);
    }

    @Override
    public @Nullable T getSingle(SkriptEvent event) {
        if (getAnd() && data.length > 1) {
            throw new SkriptAPIException("Call to getSingle on a non-single expression");
        }
        return data.length == 0 ? null : data[0];
    }

    @Override
    public @Nullable Iterator<T> iterator(SkriptEvent event) {
        if (data.length == 0) {
            return null;
        }
        return Arrays.asList(Arrays.copyOf(data, data.length)).iterator();
    }

    @Override
    public boolean check(SkriptEvent event, Predicate<? super T> checker) {
        for (T value : data) {
            if (checker.test(value)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean check(SkriptEvent event, Predicate<? super T> checker, boolean negated) {
        return negated ^ check(event, checker);
    }

    @SuppressWarnings("unchecked")
    private static <T> T[] empty(Class<T> type) {
        return (T[]) java.lang.reflect.Array.newInstance(type, 0);
    }
}
