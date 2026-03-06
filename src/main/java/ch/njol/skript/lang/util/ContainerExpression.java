package ch.njol.skript.lang.util;

import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.util.Container;
import ch.njol.util.Kleenean;
import java.util.Iterator;
import java.util.NoSuchElementException;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.lang.event.SkriptEvent;

/**
 * Flattens an expression of containers into an expression of container values for loop usage.
 */
public class ContainerExpression extends SimpleExpression<Object> {

    final Expression<? extends Container<?>> expr;
    private final Class<?> type;

    public ContainerExpression(Expression<? extends Container<?>> expr, Class<?> type) {
        this.expr = expr;
        this.type = type;
    }

    @Override
    protected Object[] get(SkriptEvent event) {
        throw new UnsupportedOperationException("ContainerExpression must only be used by loops");
    }

    @Override
    public @Nullable Iterator<? extends Object> iterator(SkriptEvent event) {
        Iterator<? extends Container<?>> iterator = expr.iterator(event);
        if (iterator == null) {
            return null;
        }
        return new Iterator<>() {
            private @Nullable Iterator<?> current;

            @Override
            public boolean hasNext() {
                Iterator<?> currentIterator = current;
                while (iterator.hasNext() && (currentIterator == null || !currentIterator.hasNext())) {
                    currentIterator = iterator.next().containerIterator();
                    current = currentIterator;
                }
                return currentIterator != null && currentIterator.hasNext();
            }

            @Override
            public Object next() {
                if (!hasNext()) {
                    throw new NoSuchElementException();
                }
                assert current != null;
                Object value = current.next();
                if (value == null) {
                    throw new NoSuchElementException("Container iterator returned null");
                }
                return value;
            }
        };
    }

    @Override
    public boolean isSingle() {
        return false;
    }

    @Override
    @SuppressWarnings("unchecked")
    public Class<? extends Object> getReturnType() {
        return (Class<? extends Object>) type;
    }

    @Override
    public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
        throw new UnsupportedOperationException();
    }

    @Override
    public String toString(@Nullable SkriptEvent event, boolean debug) {
        return expr.toString(event, debug);
    }
}
