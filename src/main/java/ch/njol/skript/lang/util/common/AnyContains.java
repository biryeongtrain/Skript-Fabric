package ch.njol.skript.lang.util.common;

import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.UnknownNullability;
import org.skriptlang.skript.common.properties.conditions.PropCondContains;

/**
 * Provider for things that can test containment.
 *
 * @deprecated Use {@link org.skriptlang.skript.lang.properties.Property#CONTAINS} instead.
 */
@FunctionalInterface
@Deprecated(since = "2.13", forRemoval = true)
public interface AnyContains<Type> extends AnyProvider {

    boolean contains(@UnknownNullability Type value);

    default boolean isSafeToCheck(Object value) {
        return true;
    }

    @ApiStatus.Internal
    default boolean checkSafely(Object value) {
        //noinspection unchecked
        return isSafeToCheck(value) && contains((Type) value);
    }
}
