package org.skriptlang.skript.common.function;

import ch.njol.skript.doc.Documentable;
import org.jetbrains.annotations.NotNull;
import org.skriptlang.skript.addon.SkriptAddon;
import org.skriptlang.skript.common.function.Parameter.Modifier;

public sealed interface DefaultFunction<T> extends Function<T>, Documentable permits DefaultFunctionImpl {

    static <T> @NotNull Builder<T> builder(@NotNull SkriptAddon source, @NotNull String name, @NotNull Class<T> returnType) {
        return new DefaultFunctionImpl.BuilderImpl<>(source, name, returnType);
    }

    @NotNull SkriptAddon source();

    interface Builder<T> {
        Builder<T> contract(@NotNull ch.njol.skript.util.Contract contract);

        Builder<T> description(@NotNull String @NotNull ... description);

        Builder<T> since(@NotNull String @NotNull ... since);

        Builder<T> examples(@NotNull String @NotNull ... examples);

        Builder<T> keywords(@NotNull String @NotNull ... keywords);

        Builder<T> requires(@NotNull String @NotNull ... requires);

        Builder<T> parameter(@NotNull String name, @NotNull Class<?> type, Modifier @NotNull ... modifiers);

        DefaultFunction<T> build(@NotNull java.util.function.Function<FunctionArguments, T> execute);
    }
}
