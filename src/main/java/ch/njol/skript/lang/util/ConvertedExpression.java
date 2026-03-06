package ch.njol.skript.lang.util;

import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Kleenean;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.function.Predicate;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.lang.converter.Converter;
import org.skriptlang.skript.lang.converter.ConverterInfo;
import org.skriptlang.skript.lang.converter.Converters;
import org.skriptlang.skript.lang.event.SkriptEvent;

public class ConvertedExpression<F, T> implements Expression<T> {

    protected final Expression<? extends F> source;
    protected final Class<T> targetType;
    protected final Class<T>[] exactTargetTypes;
    protected final Converter<? super F, ? extends T> converter;

    public ConvertedExpression(Expression<? extends F> source, Class<T> targetType, ConverterInfo<? super F, ? extends T> converterInfo) {
        this(source, targetType, new Class[]{targetType}, converterInfo.getConverter());
    }

    public ConvertedExpression(
            Expression<? extends F> source,
            Class<T> targetType,
            Class<T>[] exactTargetTypes,
            Converter<? super F, ? extends T> converter
    ) {
        this.source = source;
        this.targetType = targetType;
        this.exactTargetTypes = exactTargetTypes;
        this.converter = converter;
    }

    @SafeVarargs
    public static <F, T> @Nullable ConvertedExpression<F, T> newInstance(Expression<F> source, Class<T>... toTypes) {
        List<ConverterInfo<? super F, ? extends T>> infos = new ArrayList<>();

        for (Class<? extends F> sourceType : source.possibleReturnTypes()) {
            for (Class<T> targetType : toTypes) {
                if (targetType.isAssignableFrom(sourceType)) {
                    @SuppressWarnings("unchecked")
                    ConverterInfo<? super F, ? extends T> castInfo =
                            (ConverterInfo<? super F, ? extends T>) new ConverterInfo<>(
                                    sourceType,
                                    targetType,
                                    targetType::cast,
                                    0
                            );
                    infos.add(castInfo);
                    continue;
                }
                ConverterInfo<? extends F, T> info = Converters.getConverterInfo(sourceType, targetType);
                if (info != null) {
                    @SuppressWarnings("unchecked")
                    ConverterInfo<? super F, ? extends T> castInfo = (ConverterInfo<? super F, ? extends T>) info;
                    infos.add(castInfo);
                }
            }
        }

        if (infos.isEmpty()) {
            return null;
        }

        Class<T> superTarget = toTypes[0];
        if (toTypes.length > 1) {
            @SuppressWarnings("unchecked")
            Class<T> inferred = (Class<T>) ch.njol.skript.util.Utils.getSuperType(toTypes);
            superTarget = inferred;
        }

        Converter<? super F, ? extends T> converter = value -> {
            for (ConverterInfo<? super F, ? extends T> info : infos) {
                if (!info.getFrom().isInstance(value)) {
                    continue;
                }
                T converted = info.getConverter().convert(value);
                if (converted != null) {
                    return converted;
                }
            }
            return null;
        };

        return new ConvertedExpression<>(source, superTarget, toTypes, converter);
    }

    @Override
    public final boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Class<T> getReturnType() {
        return targetType;
    }

    @Override
    public Class<? extends T>[] possibleReturnTypes() {
        return exactTargetTypes;
    }

    @Override
    public boolean isSingle() {
        return source.isSingle();
    }

    @Override
    @SuppressWarnings("unchecked")
    public <R> @Nullable Expression<? extends R> getConvertedExpression(Class<R>... to) {
        for (Class<R> target : to) {
            if (target.isAssignableFrom(targetType)) {
                return (Expression<? extends R>) this;
            }
        }
        return source.getConvertedExpression(to);
    }

    @Override
    public @Nullable T getSingle(SkriptEvent event) {
        F value = source.getSingle(event);
        if (value == null) {
            return null;
        }
        return converter.convert(value);
    }

    @Override
    @SuppressWarnings("unchecked")
    public T[] getArray(SkriptEvent event) {
        F[] sourceValues = source.getArray(event);
        if (sourceValues == null || sourceValues.length == 0) {
            return (T[]) Array.newInstance(targetType, 0);
        }

        List<T> converted = new ArrayList<>(sourceValues.length);
        for (F sourceValue : sourceValues) {
            if (sourceValue == null) {
                continue;
            }
            T value = converter.convert(sourceValue);
            if (value != null) {
                converted.add(value);
            }
        }

        T[] result = (T[]) Array.newInstance(targetType, converted.size());
        return converted.toArray(result);
    }

    @Override
    public T[] getAll(SkriptEvent event) {
        return getArray(event);
    }

    @Override
    public boolean check(SkriptEvent event, Predicate<? super T> checker, boolean negated) {
        return negated ^ check(event, checker);
    }

    @Override
    public boolean check(SkriptEvent event, Predicate<? super T> checker) {
        return source.check(event, sourceValue -> {
            if (sourceValue == null) {
                return false;
            }
            T converted = converter.convert(sourceValue);
            return converted != null && checker.test(converted);
        });
    }

    @Override
    public boolean getAnd() {
        return source.getAnd();
    }

    @Override
    public boolean setTime(int time) {
        return source.setTime(time);
    }

    @Override
    public int getTime() {
        return source.getTime();
    }

    @Override
    public boolean returnNestedStructures(boolean nested) {
        return source.returnNestedStructures(nested);
    }

    @Override
    public boolean returnsNestedStructures() {
        return source.returnsNestedStructures();
    }

    @Override
    public boolean isDefault() {
        return source.isDefault();
    }

    @Override
    public boolean isLoopOf(String input) {
        return false;
    }

    @Override
    public @Nullable Iterator<T> iterator(SkriptEvent event) {
        Iterator<? extends F> sourceIterator = source.iterator(event);
        if (sourceIterator == null) {
            return null;
        }

        return new Iterator<>() {
            private @Nullable T next;

            @Override
            public boolean hasNext() {
                if (next != null) {
                    return true;
                }
                while (sourceIterator.hasNext()) {
                    F sourceValue = sourceIterator.next();
                    if (sourceValue == null) {
                        continue;
                    }
                    T converted = converter.convert(sourceValue);
                    if (converted != null) {
                        next = converted;
                        return true;
                    }
                }
                return false;
            }

            @Override
            public T next() {
                if (!hasNext()) {
                    throw new NoSuchElementException();
                }
                T value = next;
                next = null;
                assert value != null;
                return value;
            }
        };
    }

    @Override
    public Class<?> @Nullable [] acceptChange(ChangeMode mode) {
        return source.acceptChange(mode);
    }

    @Override
    public void change(SkriptEvent event, Object @Nullable [] delta, ChangeMode mode) {
        source.change(event, delta, mode);
    }

    @Override
    public Expression<? extends F> getSource() {
        return source;
    }

    @Override
    public Expression<? extends T> simplify() {
        Expression<? extends T> converted = source.simplify().getConvertedExpression(exactTargetTypes);
        if (converted != null) {
            return converted;
        }
        return this;
    }

    @Override
    public String toString(@Nullable SkriptEvent event, boolean debug) {
        if (debug && event == null) {
            return "(" + source.toString(event, true) + " >> " + converter + ")";
        }
        return source.toString(event, debug);
    }

    @Override
    public String toString() {
        return toString(null, false);
    }
}
