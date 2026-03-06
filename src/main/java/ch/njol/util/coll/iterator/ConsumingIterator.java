package ch.njol.util.coll.iterator;

import java.util.Iterator;
import java.util.function.Consumer;

public final class ConsumingIterator<T> implements Iterator<T> {

    private final Iterator<? extends T> delegate;
    private final Consumer<? super T> consumer;

    public ConsumingIterator(Iterator<? extends T> delegate, Consumer<? super T> consumer) {
        this.delegate = delegate;
        this.consumer = consumer;
    }

    @Override
    public boolean hasNext() {
        return delegate.hasNext();
    }

    @Override
    public T next() {
        T value = delegate.next();
        consumer.accept(value);
        return value;
    }
}
