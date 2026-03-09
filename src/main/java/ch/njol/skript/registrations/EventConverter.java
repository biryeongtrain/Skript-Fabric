package ch.njol.skript.registrations;

import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.lang.converter.Converter;
import org.skriptlang.skript.util.event.Event;

/**
 * Used as a converter in EventValue registration to allow for setting event-values.
 *
 * @param <E> event class to change value
 * @param <T> type of value to change
 */
public interface EventConverter<E extends Event, T> extends Converter<E, T> {

    void set(E event, @Nullable T value);
}
