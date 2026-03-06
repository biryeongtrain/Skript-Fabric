package ch.njol.util.coll.iterator;

import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.function.Predicate;

public final class CheckedIterator<T> implements Iterator<T> {

    private final Iterator<? extends T> delegate;
    private final Predicate<? super T> predicate;
    private T next;
    private boolean hasBuffered;

    public CheckedIterator(Iterator<? extends T> delegate, Predicate<? super T> predicate) {
        this.delegate = delegate;
        this.predicate = predicate;
    }

    @Override
    public boolean hasNext() {
        if (hasBuffered) {
            return true;
        }
        while (delegate.hasNext()) {
            T candidate = delegate.next();
            if (predicate.test(candidate)) {
                next = candidate;
                hasBuffered = true;
                return true;
            }
        }
        return false;
    }

    @Override
    public T next() {
        if (!hasNext()) {
            throw new NoSuchElementException();
        }
        hasBuffered = false;
        return next;
    }
}
