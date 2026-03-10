package org.skriptlang.skript.common.function;

import ch.njol.skript.lang.function.FunctionEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface Function<T> {

    T execute(@NotNull FunctionEvent<?> event, @NotNull FunctionArguments arguments);

    @NotNull Signature<T> signature();

    boolean resetReturnValue();

    @NotNull String @Nullable [] returnedKeys();
}
