package org.skriptlang.skript.common.function;

import ch.njol.skript.util.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnmodifiableView;

public interface Signature<T> {

    @Nullable Class<T> returnType();

    @UnmodifiableView
    @NotNull Parameters parameters();

    @Nullable Contract contract();

    void addCall(FunctionReference<?> reference);

    default boolean isSingle() {
        return returnType() != null && !returnType().isArray();
    }
}
