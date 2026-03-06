package ch.njol.skript.lang.util;

import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.lang.KeyProviderExpression;
import ch.njol.skript.lang.KeyReceiverExpression;
import ch.njol.skript.lang.KeyedValue;
import ch.njol.skript.util.Utils;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.WeakHashMap;
import org.jetbrains.annotations.NotNull;
import org.skriptlang.skript.lang.converter.Converter;
import org.skriptlang.skript.lang.converter.ConverterInfo;
import org.skriptlang.skript.lang.event.SkriptEvent;

/**
 * Converted expression variant for keyed source expressions.
 */
public class ConvertedKeyProviderExpression<F, T> extends ConvertedExpression<F, T>
        implements KeyProviderExpression<T>, KeyReceiverExpression<T> {

    private final WeakHashMap<SkriptEvent, String[]> arrayKeysCache = new WeakHashMap<>();
    private final WeakHashMap<SkriptEvent, String[]> allKeysCache = new WeakHashMap<>();
    private final boolean supportsKeyedChange;

    public ConvertedKeyProviderExpression(
            KeyProviderExpression<? extends F> source,
            Class<T> to,
            ConverterInfo<? super F, ? extends T> info
    ) {
        super(source, to, info);
        this.supportsKeyedChange = source instanceof KeyReceiverExpression<?>;
    }

    @SuppressWarnings("unchecked")
    public ConvertedKeyProviderExpression(
            KeyProviderExpression<? extends F> source,
            Class<T>[] toExact,
            Collection<ConverterInfo<? super F, ? extends T>> converterInfos,
            boolean performFromCheck
    ) {
        super(
                source,
                (Class<T>) Utils.getSuperType(toExact),
                toExact,
                selectConverter(converterInfos, performFromCheck)
        );
        this.supportsKeyedChange = source instanceof KeyReceiverExpression<?>;
    }

    @Override
    public T[] getArray(SkriptEvent event) {
        if (!canReturnKeys()) {
            return super.getArray(event);
        }
        return convertWithKeys(getSource().getArray(event), getSource().getArrayKeys(event), keys -> arrayKeysCache.put(event, keys));
    }

    @Override
    public T[] getAll(SkriptEvent event) {
        if (!canReturnKeys()) {
            return super.getAll(event);
        }
        return convertWithKeys(getSource().getAll(event), getSource().getAllKeys(event), keys -> allKeysCache.put(event, keys));
    }

    @SuppressWarnings("unchecked")
    private T[] convertWithKeys(F[] sourceValues, String[] sourceKeys, java.util.function.Consumer<String[]> keysConsumer) {
        int length = Math.min(sourceValues.length, sourceKeys.length);
        List<T> convertedValues = new ArrayList<>(length);
        List<String> convertedKeys = new ArrayList<>(length);
        for (int i = 0; i < length; i++) {
            F from = sourceValues[i];
            if (from == null) {
                continue;
            }
            T converted = converter.convert(from);
            if (converted == null) {
                continue;
            }
            convertedValues.add(converted);
            convertedKeys.add(sourceKeys[i]);
        }
        keysConsumer.accept(convertedKeys.toArray(String[]::new));
        T[] array = (T[]) Array.newInstance(targetType, convertedValues.size());
        return convertedValues.toArray(array);
    }

    @Override
    @SuppressWarnings("unchecked")
    public KeyProviderExpression<? extends F> getSource() {
        return (KeyProviderExpression<? extends F>) super.getSource();
    }

    @Override
    public @NotNull String @NotNull [] getArrayKeys(SkriptEvent event) throws IllegalStateException {
        String[] keys = arrayKeysCache.remove(event);
        if (keys == null) {
            throw new IllegalStateException();
        }
        return keys;
    }

    @Override
    public @NotNull String @NotNull [] getAllKeys(SkriptEvent event) {
        String[] keys = allKeysCache.remove(event);
        if (keys == null) {
            throw new IllegalStateException();
        }
        return keys;
    }

    @Override
    public boolean canReturnKeys() {
        return getSource().canReturnKeys();
    }

    @Override
    public boolean areKeysRecommended() {
        return getSource().areKeysRecommended();
    }

    @Override
    public void change(SkriptEvent event, Object @NotNull [] delta, ChangeMode mode, @NotNull String @NotNull [] keys) {
        if (supportsKeyedChange) {
            ((KeyReceiverExpression<?>) getSource()).change(event, delta, mode, keys);
        } else {
            getSource().change(event, delta, mode);
        }
    }

    @Override
    public boolean isIndexLoop(String input) {
        return getSource().isIndexLoop(input);
    }

    @Override
    public boolean isLoopOf(String input) {
        return KeyProviderExpression.super.isLoopOf(input);
    }

    @Override
    public Iterator<KeyedValue<T>> keyedIterator(SkriptEvent event) {
        Iterator<? extends KeyedValue<? extends F>> sourceIterator = getSource().keyedIterator(event);
        return new Iterator<>() {
            private KeyedValue<T> next;

            @Override
            public boolean hasNext() {
                if (next != null) {
                    return true;
                }
                while (sourceIterator.hasNext()) {
                    KeyedValue<? extends F> sourceValue = sourceIterator.next();
                    T converted = converter.convert(sourceValue.value());
                    if (converted != null) {
                        next = sourceValue.withValue(converted);
                        return true;
                    }
                }
                return false;
            }

            @Override
            public KeyedValue<T> next() {
                if (!hasNext()) {
                    throw new NoSuchElementException();
                }
                KeyedValue<T> value = next;
                next = null;
                return value;
            }
        };
    }

    private static <F, T> Converter<? super F, ? extends T> selectConverter(
            Collection<ConverterInfo<? super F, ? extends T>> infos,
            boolean performFromCheck
    ) {
        List<ConverterInfo<? super F, ? extends T>> candidates = new ArrayList<>(infos);
        return value -> {
            for (ConverterInfo<? super F, ? extends T> info : candidates) {
                if (performFromCheck && !info.getFrom().isInstance(value)) {
                    continue;
                }
                T converted = info.getConverter().convert(value);
                if (converted != null) {
                    return converted;
                }
            }
            return null;
        };
    }
}
