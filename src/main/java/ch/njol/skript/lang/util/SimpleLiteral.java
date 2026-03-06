package ch.njol.skript.lang.util;

import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.lang.DefaultExpression;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.util.Utils;
import ch.njol.util.Kleenean;
import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.Iterator;
import java.util.concurrent.ThreadLocalRandom;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.lang.converter.Converters;
import org.skriptlang.skript.lang.event.SkriptEvent;

/**
 * Represents a literal, i.e. a static value like a number or a string.
 */
public class SimpleLiteral<T> implements Literal<T>, DefaultExpression<T> {

    protected final Class<T> type;

    private final boolean isDefault;
    private final boolean and;

    protected final Expression<?> source;

    /**
     * Literal values; never null, may be empty.
     */
    protected transient T[] data;

    public SimpleLiteral(T[] data, Class<T> type, boolean and) {
        this(data, type, and, null);
    }

    public SimpleLiteral(T[] data, Class<T> type, boolean and, @Nullable Expression<?> source) {
        this(data, type, and, false, source);
    }

    public SimpleLiteral(T[] data, Class<T> type, boolean and, boolean isDefault, @Nullable Expression<?> source) {
        this.data = data == null ? empty(type) : Arrays.copyOf(data, data.length);
        this.type = type;
        this.and = this.data.length <= 1 || and;
        this.isDefault = isDefault;
        this.source = source == null ? this : source;
    }

    @SuppressWarnings("unchecked")
    public SimpleLiteral(T data, boolean isDefault) {
        this(data, isDefault, null);
    }

    @SuppressWarnings("unchecked")
    public SimpleLiteral(T data, boolean isDefault, @Nullable Expression<?> source) {
        Class<T> dataType = (Class<T>) data.getClass();
        T[] values = (T[]) Array.newInstance(dataType, 1);
        values[0] = data;
        this.data = values;
        this.type = dataType;
        this.and = true;
        this.isDefault = isDefault;
        this.source = source == null ? this : source;
    }

    @Override
    public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean init() {
        return true;
    }

    protected T[] data() {
        return Arrays.copyOf(data, data.length);
    }

    @Override
    public T[] getArray(SkriptEvent event) {
        return data();
    }

    @Override
    public T[] getAll(SkriptEvent event) {
        return data();
    }

    @Override
    public @Nullable T getSingle(SkriptEvent event) {
        if (data.length == 0) {
            return null;
        }
        if (data.length == 1) {
            return data[0];
        }
        return data[ThreadLocalRandom.current().nextInt(data.length)];
    }

    @Override
    public Class<T> getReturnType() {
        return type;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <R> @Nullable Literal<? extends R> getConvertedExpression(Class<R>... to) {
        if (to == null || to.length == 0) {
            return null;
        }
        for (Class<R> targetType : to) {
            if (targetType.isAssignableFrom(type)) {
                return (Literal<? extends R>) this;
            }
        }
        Class<R> superType = (Class<R>) Utils.getSuperType(to);
        R[] converted = Converters.convert(data(), to, superType);
        if (converted.length != data.length) {
            return null;
        }
        return new SimpleLiteral<>(converted, superType, and, isDefault, this);
    }

    @Override
    public String toString(@Nullable SkriptEvent event, boolean debug) {
        return Arrays.toString(data);
    }

    @Override
    public String toString() {
        return toString(null, false);
    }

    @Override
    public boolean isSingle() {
        return !getAnd() || data.length <= 1;
    }

    @Override
    public boolean isDefault() {
        return isDefault;
    }

    @Override
    public Class<?> @Nullable [] acceptChange(ChangeMode mode) {
        return null;
    }

    @Override
    public void change(SkriptEvent event, Object @Nullable [] delta, ChangeMode mode) throws UnsupportedOperationException {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean getAnd() {
        return and;
    }

    @Override
    public boolean setTime(int time) {
        return false;
    }

    @Override
    public int getTime() {
        return 0;
    }

    @Override
    public @Nullable Iterator<? extends T> iterator(SkriptEvent event) {
        if (data.length == 0) {
            return null;
        }
        return Arrays.asList(data()).iterator();
    }

    @Override
    public boolean isLoopOf(String input) {
        return false;
    }

    @Override
    public Expression<?> getSource() {
        return source;
    }

    @Override
    public Expression<T> simplify() {
        return this;
    }

    @SuppressWarnings("unchecked")
    private static <T> T[] empty(Class<T> type) {
        return (T[]) Array.newInstance(type, 0);
    }
}
