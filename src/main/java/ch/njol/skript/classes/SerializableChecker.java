package ch.njol.skript.classes;

import java.util.function.Predicate;

/**
 * Legacy serializability checker alias kept for upstream compatibility.
 *
 * @deprecated use {@link Predicate} instead.
 */
@FunctionalInterface
@Deprecated(since = "2.10.0", forRemoval = true)
public interface SerializableChecker<T> extends Predicate<T> {
}
