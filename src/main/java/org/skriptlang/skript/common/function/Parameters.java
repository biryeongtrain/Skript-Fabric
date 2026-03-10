package org.skriptlang.skript.common.function;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.UnmodifiableView;

public final class Parameters {

    private final LinkedHashMap<String, Parameter<?>> named;
    private final Parameter<?>[] indexed;

    public Parameters(Map<String, Parameter<?>> parameters) {
        this.named = new LinkedHashMap<>(parameters);
        this.indexed = this.named.values().toArray(Parameter[]::new);
    }

    public Parameter<?> get(@NotNull String name) {
        return named.get(name);
    }

    public Parameter<?> getFirst() {
        return indexed[0];
    }

    public Parameter<?> get(int index) {
        return indexed[index];
    }

    public Parameter<?>[] all() {
        return Arrays.copyOf(indexed, indexed.length);
    }

    public int size() {
        return indexed.length;
    }

    public @UnmodifiableView Map<String, Parameter<?>> sequencedMap() {
        return Collections.unmodifiableMap(named);
    }
}
