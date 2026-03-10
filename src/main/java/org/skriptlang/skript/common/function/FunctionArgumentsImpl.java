package org.skriptlang.skript.common.function;

import com.google.common.base.Preconditions;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;

public record FunctionArgumentsImpl(@Unmodifiable @NotNull Map<String, Object> arguments) implements FunctionArguments {

    public FunctionArgumentsImpl {
        Preconditions.checkNotNull(arguments, "arguments cannot be null");
    }

    @Override
    public <T> T get(@NotNull String name) {
        Preconditions.checkNotNull(name, "name cannot be null");
        @SuppressWarnings("unchecked")
        T value = (T) arguments.get(name);
        return value;
    }

    @Override
    public <T> T getOrDefault(@NotNull String name, T defaultValue) {
        Preconditions.checkNotNull(name, "name cannot be null");
        @SuppressWarnings("unchecked")
        T value = (T) arguments.getOrDefault(name, defaultValue);
        return value;
    }

    @Override
    public <T> T getOrDefault(@NotNull String name, Supplier<T> defaultValue) {
        Preconditions.checkNotNull(name, "name cannot be null");
        Object existing = arguments.get(name);
        if (existing == null) {
            return defaultValue.get();
        }
        @SuppressWarnings("unchecked")
        T value = (T) existing;
        return value;
    }

    @Override
    public @Unmodifiable @NotNull Set<String> names() {
        return Collections.unmodifiableSet(arguments.keySet());
    }
}
