package org.skriptlang.skript.common.function;

import java.util.Set;
import java.util.function.Supplier;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;

public sealed interface FunctionArguments permits FunctionArgumentsImpl {

    <T> T get(@NotNull String name);

    <T> T getOrDefault(@NotNull String name, T defaultValue);

    <T> T getOrDefault(@NotNull String name, Supplier<T> defaultValue);

    @Unmodifiable
    @NotNull Set<String> names();
}
