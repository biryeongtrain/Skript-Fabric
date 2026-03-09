package ch.njol.skript.util;

import ch.njol.skript.lang.Expression;
import org.jetbrains.annotations.Nullable;

public interface Contract {

    boolean isSingle(Expression<?>... arguments);

    @Nullable
    Class<?> getReturnType(Expression<?>... arguments);
}
