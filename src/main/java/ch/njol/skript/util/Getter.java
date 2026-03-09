package ch.njol.skript.util;

import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.lang.converter.Converter;

/**
 * Used to get a specific value from instances of some type.
 *
 * @deprecated use {@link Converter} instead.
 * @param <R> the returned value type
 * @param <A> the type which holds the value
 */
@Deprecated(since = "2.10.0", forRemoval = true)
public abstract class Getter<R, A> implements Converter<A, R> {

    @Nullable
    public abstract R get(A arg);

    @Override
    @Nullable
    public final R convert(A value) {
        return get(value);
    }
}
