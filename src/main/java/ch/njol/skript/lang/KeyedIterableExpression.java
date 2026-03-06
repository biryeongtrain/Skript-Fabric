package ch.njol.skript.lang;

import java.util.Iterator;
import java.util.Spliterators;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
import org.skriptlang.skript.lang.event.SkriptEvent;

public interface KeyedIterableExpression<T> extends Expression<T> {

    boolean canIterateWithKeys();

    Iterator<KeyedValue<T>> keyedIterator(SkriptEvent event);

    default Stream<KeyedValue<T>> keyedStream(SkriptEvent event) {
        return StreamSupport.stream(Spliterators.spliteratorUnknownSize(keyedIterator(event), 0), false);
    }

    @Override
    default boolean isLoopOf(String input) {
        return canIterateWithKeys() && isIndexLoop(input);
    }

    default boolean isIndexLoop(String input) {
        return input.equalsIgnoreCase("index");
    }

    static boolean canIterateWithKeys(Expression<?> expression) {
        return expression instanceof KeyedIterableExpression<?> keyed && keyed.canIterateWithKeys();
    }
}
