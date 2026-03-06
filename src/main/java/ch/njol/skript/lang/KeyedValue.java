package ch.njol.skript.lang;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Immutable key-value pair used for keyed expression iteration.
 */
public record KeyedValue<T>(@NotNull String key, @NotNull T value) implements Map.Entry<String, T> {

    public KeyedValue {
        Objects.requireNonNull(key, "key");
        Objects.requireNonNull(value, "value");
    }

    public KeyedValue(Map.Entry<String, T> entry) {
        this(entry.getKey(), entry.getValue());
    }

    @Override
    public String getKey() {
        return key();
    }

    @Override
    public T getValue() {
        return value();
    }

    @Override
    public T setValue(T value) {
        throw new UnsupportedOperationException("KeyedValue is immutable");
    }

    public KeyedValue<T> withKey(@NotNull String newKey) {
        return new KeyedValue<>(newKey, value());
    }

    public <U> KeyedValue<U> withValue(@NotNull U newValue) {
        return new KeyedValue<>(key(), newValue);
    }

    public static <T, U> KeyedValue<U>[] map(KeyedValue<T>[] source, Function<T, @Nullable U> mapper) {
        if (source == null) {
            @SuppressWarnings("unchecked")
            KeyedValue<U>[] empty = new KeyedValue[0];
            return empty;
        }
        @SuppressWarnings("unchecked")
        KeyedValue<U>[] mapped = new KeyedValue[source.length];
        for (int i = 0; i < source.length; i++) {
            KeyedValue<T> pair = source[i];
            if (pair == null) {
                continue;
            }
            U mappedValue = mapper.apply(pair.value());
            mapped[i] = mappedValue == null ? null : pair.withValue(mappedValue);
        }
        return mapped;
    }

    public static <T> KeyedValue<T>[] zip(T[] values, @Nullable String[] keys) {
        if (keys == null) {
            @SuppressWarnings("unchecked")
            KeyedValue<T>[] pairs = new KeyedValue[values.length];
            for (int i = 0; i < values.length; i++) {
                pairs[i] = new KeyedValue<>(String.valueOf(i + 1), values[i]);
            }
            return pairs;
        }
        if (values.length != keys.length) {
            throw new IllegalArgumentException("Values and keys must have the same length");
        }
        @SuppressWarnings("unchecked")
        KeyedValue<T>[] pairs = new KeyedValue[values.length];
        for (int i = 0; i < values.length; i++) {
            pairs[i] = new KeyedValue<>(keys[i], values[i]);
        }
        return pairs;
    }

    public static <T> UnzippedKeyValues<T> unzip(KeyedValue<T>[] keyedValues) {
        List<String> keys = new ArrayList<>(keyedValues.length);
        List<T> values = new ArrayList<>(keyedValues.length);
        for (KeyedValue<T> keyedValue : keyedValues) {
            keys.add(keyedValue.key());
            values.add(keyedValue.value());
        }
        return new UnzippedKeyValues<>(keys, values);
    }

    public static <T> UnzippedKeyValues<T> unzip(Iterator<KeyedValue<T>> keyedValues) {
        List<String> keys = new ArrayList<>();
        List<T> values = new ArrayList<>();
        while (keyedValues.hasNext()) {
            KeyedValue<T> keyedValue = keyedValues.next();
            keys.add(keyedValue.key());
            values.add(keyedValue.value());
        }
        return new UnzippedKeyValues<>(keys, values);
    }

    public record UnzippedKeyValues<T>(@NotNull List<@NotNull String> keys, @NotNull List<@NotNull T> values) {

        public UnzippedKeyValues(@Nullable List<@NotNull String> keys, @NotNull List<@NotNull T> values) {
            this.values = Objects.requireNonNull(values, "values");
            this.keys = keys != null ? keys : new ArrayList<>(values.size());
            if (keys == null) {
                for (int i = 1; i <= values.size(); i++) {
                    this.keys.add(String.valueOf(i));
                }
            } else if (keys.size() != values.size()) {
                throw new IllegalArgumentException("Keys and values must have the same length");
            }
        }

        public UnzippedKeyValues(@Nullable String[] keys, @NotNull T[] values) {
            this(keys != null ? Arrays.asList(keys) : null, Arrays.asList(values));
        }
    }
}
