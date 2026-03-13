package ch.njol.skript.expressions;

import ch.njol.skript.Skript;
import ch.njol.skript.SkriptAPIException;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.KeyProviderExpression;
import ch.njol.skript.lang.KeyedValue;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.simplification.SimplifiedLiteral;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.skript.util.LiteralUtils;
import ch.njol.skript.util.Patterns;
import ch.njol.util.Kleenean;
import com.google.common.collect.Iterators;
import java.lang.reflect.Array;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.WeakHashMap;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.BiFunction;
import java.util.function.Function;
import org.apache.commons.lang3.function.TriFunction;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.lang.event.SkriptEvent;
import org.skriptlang.skript.lang.util.SkriptQueue;

@Name("Elements")
@Description({
        "The first, last, range or a random element of a set, e.g. a list variable, or a queue.",
        "Asking for elements from a queue will also remove them from the queue, see the new queue expression for more information."
})
@Example("broadcast the first 3 elements of {top players::*}")
@Example("set {_last} to last element of {top players::*}")
@Example("set {_random player} to random element out of all players")
@Example("send 2nd last element of {top players::*} to player")
@Example("set {page2::*} to elements from 11 to 20 of {top players::*}")
@Example("broadcast the 1st element in {queue}")
@Example("broadcast the first 3 elements in {queue}")
@Since("2.0, 2.7 (relative to last element), 2.8.0 (range of elements)")
public class ExprElement<T> extends SimpleExpression<T> implements KeyProviderExpression<T> {

    private static final Patterns<ElementType[]> PATTERNS = new Patterns<>(new Object[][]{
            {"[the] (first|1:last) element [out] of %objects%", new ElementType[]{ElementType.FIRST_ELEMENT, ElementType.LAST_ELEMENT}},
            {"[the] (first|1:last) %integer% elements [out] of %objects%", new ElementType[]{ElementType.FIRST_X_ELEMENTS, ElementType.LAST_X_ELEMENTS}},
            {"[a] random element [out] of %objects%", new ElementType[]{ElementType.RANDOM}},
            {"[the] %integer%(st|nd|rd|th) [1:[to] last] element [out] of %objects%", new ElementType[]{ElementType.ORDINAL, ElementType.TAIL_END_ORDINAL}},
            {"[the] elements (from|between) %integer% (to|and) %integer% [out] of %objects%", new ElementType[]{ElementType.RANGE}},

            {"[the] (first|next|1:last) element (of|in) %queue%", new ElementType[]{ElementType.FIRST_ELEMENT, ElementType.LAST_ELEMENT}},
            {"[the] (first|1:last) %integer% elements (of|in) %queue%", new ElementType[]{ElementType.FIRST_X_ELEMENTS, ElementType.LAST_X_ELEMENTS}},
            {"[a] random element (of|in) %queue%", new ElementType[]{ElementType.RANDOM}},
            {"[the] %integer%(st|nd|rd|th) [1:[to] last] element (of|in) %queue%", new ElementType[]{ElementType.ORDINAL, ElementType.TAIL_END_ORDINAL}},
            {"[the] elements (from|between) %integer% (to|and) %integer% (of|in) %queue%", new ElementType[]{ElementType.RANGE}},
    });

    static {
        Skript.registerExpression(ExprElement.class, Object.class, PATTERNS.getPatterns());
    }

    private enum ElementType {
        FIRST_ELEMENT(iterator -> Iterators.limit(iterator, 1)),
        LAST_ELEMENT(iterator -> Iterators.singletonIterator(Iterators.getLast(iterator))),
        FIRST_X_ELEMENTS(Iterators::limit),
        LAST_X_ELEMENTS((iterator, index) -> {
            Object[] array = Iterators.toArray(iterator, Object.class);
            index = Math.min(index, array.length);
            return Iterators.forArray(subarray(array, array.length - index, array.length));
        }),
        RANDOM(iterator -> {
            Object[] array = Iterators.toArray(iterator, Object.class);
            if (array.length == 0) {
                return Collections.emptyIterator();
            }
            return Iterators.singletonIterator(array[ThreadLocalRandom.current().nextInt(array.length)]);
        }),
        ORDINAL((iterator, index) -> {
            Iterators.advance(iterator, index - 1);
            if (!iterator.hasNext()) {
                return Collections.emptyIterator();
            }
            return Iterators.singletonIterator(iterator.next());
        }),
        TAIL_END_ORDINAL((iterator, index) -> {
            Object[] array = Iterators.toArray(iterator, Object.class);
            if (index > array.length) {
                return Collections.emptyIterator();
            }
            return Iterators.singletonIterator(array[array.length - index]);
        }),
        RANGE((iterator, startIndex, endIndex) -> {
            boolean reverse = startIndex > endIndex;
            int from = Math.max(Math.min(startIndex, endIndex) - 1, 0);
            int to = Math.max(Math.max(startIndex, endIndex), 0);
            if (reverse) {
                Object[] array = Iterators.toArray(iterator, Object.class);
                Object[] elements = subarray(array, from, to);
                reverse(elements);
                return Iterators.forArray(elements);
            }
            Iterators.advance(iterator, from);
            return Iterators.limit(iterator, to - from);
        });

        private final TriFunction<Iterator<?>, Integer, Integer, Iterator<?>> function;

        ElementType(Function<Iterator<?>, Iterator<?>> function) {
            this.function = (iterator, start, end) -> function.apply(iterator);
        }

        ElementType(BiFunction<Iterator<?>, Integer, Iterator<?>> function) {
            this.function = (iterator, start, end) -> function.apply(iterator, start);
        }

        ElementType(TriFunction<Iterator<?>, Integer, Integer, Iterator<?>> function) {
            this.function = function;
        }

        public <T> Iterator<T> apply(Iterator<T> iterator, int startIndex, int endIndex) {
            @SuppressWarnings("unchecked")
            Iterator<T> transformed = (Iterator<T>) function.apply(iterator, startIndex, endIndex);
            return transformed;
        }
    }

    private final Map<SkriptEvent, List<String>> cache = new WeakHashMap<>();

    private Expression<? extends T> expr;
    private @Nullable Expression<Number> startIndex;
    private @Nullable Expression<Number> endIndex;
    private ElementType type;
    private boolean queue;
    private boolean keyed;

    @Override
    @SuppressWarnings("unchecked")
    public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
        ElementType[] types = PATTERNS.getInfo(matchedPattern);
        queue = matchedPattern > 4;
        if (queue) {
            expr = (Expression<? extends T>) expressions[expressions.length - 1];
        } else {
            expr = (Expression<? extends T>) LiteralUtils.defendExpression(expressions[expressions.length - 1]);
        }
        switch (type = types[parseResult.mark]) {
            case RANGE:
                endIndex = (Expression<Number>) expressions[1];
            case FIRST_X_ELEMENTS, LAST_X_ELEMENTS, ORDINAL, TAIL_END_ORDINAL:
                startIndex = (Expression<Number>) expressions[0];
                break;
            default:
                startIndex = null;
                break;
        }
        keyed = KeyProviderExpression.canReturnKeys(expr);
        return queue || LiteralUtils.canInitSafely(expr);
    }

    @Override
    protected T @Nullable [] get(SkriptEvent event) {
        if (queue) {
            return getFromQueue(event);
        }
        if (keyed) {
            KeyedValue.UnzippedKeyValues<T> unzipped = KeyedValue.unzip(keyedIterator(event));
            cache.put(event, unzipped.keys());
            @SuppressWarnings("unchecked")
            T[] empty = (T[]) Array.newInstance(getReturnType(), 0);
            return unzipped.values().toArray(empty);
        }
        Iterator<? extends T> iterator = iterator(event);
        if (iterator == null) {
            return null;
        }
        @SuppressWarnings("unchecked")
        T[] values = Iterators.toArray(iterator, (Class<T>) getReturnType());
        return values;
    }

    @Override
    public @NotNull String[] getArrayKeys(SkriptEvent event) throws IllegalStateException {
        if (!keyed) {
            throw new UnsupportedOperationException();
        }
        if (!cache.containsKey(event)) {
            throw new SkriptAPIException("Cannot call getArrayKeys() before calling getArray() or getAll()");
        }
        return cache.remove(event).toArray(new String[0]);
    }

    @Override
    public @Nullable Iterator<? extends T> iterator(SkriptEvent event) {
        if (queue) {
            return Optional.ofNullable(getFromQueue(event)).map(Iterators::forArray).orElse(null);
        }
        return transformIterator(event, expr.iterator(event));
    }

    @Override
    public Iterator<KeyedValue<T>> keyedIterator(SkriptEvent event) {
        if (!keyed) {
            throw new UnsupportedOperationException();
        }
        @SuppressWarnings("unchecked")
        Iterator<KeyedValue<T>> iterator = ((KeyProviderExpression<T>) expr).keyedIterator(event);
        return transformIterator(event, iterator);
    }

    private <A> Iterator<A> transformIterator(SkriptEvent event, @Nullable Iterator<A> iterator) {
        if (iterator == null || !iterator.hasNext()) {
            return Collections.emptyIterator();
        }
        Integer resolvedStartIndex = 0;
        Integer resolvedEndIndex = 0;
        if (startIndex != null) {
            Number startNumber = startIndex.getSingle(event);
            if (startNumber == null) {
                return Collections.emptyIterator();
            }
            resolvedStartIndex = startNumber.intValue();
            if (resolvedStartIndex <= 0 && type != ElementType.RANGE) {
                return Collections.emptyIterator();
            }
        }
        if (endIndex != null) {
            Number endNumber = endIndex.getSingle(event);
            if (endNumber == null) {
                return Collections.emptyIterator();
            }
            resolvedEndIndex = endNumber.intValue();
        }
        return type.apply(iterator, resolvedStartIndex, resolvedEndIndex);
    }

    @SuppressWarnings("unchecked")
    private T @Nullable [] getFromQueue(SkriptEvent event) {
        SkriptQueue queueValue = (SkriptQueue) expr.getSingle(event);
        if (queueValue == null) {
            return null;
        }
        Integer resolvedStartIndex = 0;
        Integer resolvedEndIndex = 0;
        if (startIndex != null) {
            Number startNumber = startIndex.getSingle(event);
            if (startNumber == null) {
                return null;
            }
            resolvedStartIndex = startNumber.intValue();
            if (resolvedStartIndex <= 0 && type != ElementType.RANGE) {
                return null;
            }
        }
        if (endIndex != null) {
            Number endNumber = endIndex.getSingle(event);
            if (endNumber == null) {
                return null;
            }
            resolvedEndIndex = endNumber.intValue();
        }
        return switch (type) {
            case FIRST_ELEMENT -> singletonArray((T) queueValue.pollFirst());
            case LAST_ELEMENT -> singletonArray((T) queueValue.pollLast());
            case RANDOM -> singletonArray((T) queueValue.removeSafely(ThreadLocalRandom.current().nextInt(0, queueValue.size())));
            case ORDINAL -> singletonArray((T) queueValue.removeSafely(resolvedStartIndex - 1));
            case TAIL_END_ORDINAL -> singletonArray((T) queueValue.removeSafely(queueValue.size() - resolvedStartIndex));
            case FIRST_X_ELEMENTS -> (T[]) queueValue.removeRangeSafely(0, resolvedStartIndex);
            case LAST_X_ELEMENTS -> (T[]) queueValue.removeRangeSafely(queueValue.size() - resolvedStartIndex, queueValue.size());
            case RANGE -> {
                boolean reverse = resolvedStartIndex > resolvedEndIndex;
                T[] elements = (T[]) queueValue.removeRangeSafely(Math.min(resolvedStartIndex, resolvedEndIndex) - 1, Math.max(resolvedStartIndex, resolvedEndIndex));
                if (reverse) {
                    reverse(elements);
                }
                yield elements;
            }
        };
    }

    @Override
    public Class<? extends T> getReturnType() {
        return expr.getReturnType();
    }

    @Override
    public boolean isSingle() {
        return type != ElementType.FIRST_X_ELEMENTS && type != ElementType.LAST_X_ELEMENTS && type != ElementType.RANGE;
    }

    @Override
    public Expression<? extends T> simplify() {
        if (startIndex instanceof ch.njol.skript.lang.Literal
                && (endIndex == null || endIndex instanceof ch.njol.skript.lang.Literal)
                && expr instanceof ch.njol.skript.lang.Literal) {
            return SimplifiedLiteral.fromExpression(this);
        }
        return super.simplify();
    }

    @Override
    public String toString(@Nullable SkriptEvent event, boolean debug) {
        return switch (type) {
            case FIRST_ELEMENT -> "the first element out of " + expr.toString(event, debug);
            case LAST_ELEMENT -> "the last element out of " + expr.toString(event, debug);
            case FIRST_X_ELEMENTS -> "the first " + expressionText(startIndex, event, debug) + " elements out of " + expr.toString(event, debug);
            case LAST_X_ELEMENTS -> "the last " + expressionText(startIndex, event, debug) + " elements out of " + expr.toString(event, debug);
            case RANDOM -> "a random element out of " + expr.toString(event, debug);
            case ORDINAL -> "the " + expressionText(startIndex, event, debug) + " element out of " + expr.toString(event, debug);
            case TAIL_END_ORDINAL -> "the " + expressionText(startIndex, event, debug) + " to last element out of " + expr.toString(event, debug);
            case RANGE -> "the elements from " + expressionText(startIndex, event, debug) + " to " + expressionText(endIndex, event, debug) + " out of " + expr.toString(event, debug);
        };
    }

    private static Object[] subarray(Object[] array, int from, int to) {
        int safeFrom = Math.max(0, Math.min(from, array.length));
        int safeTo = Math.max(safeFrom, Math.min(to, array.length));
        Object[] copy = new Object[safeTo - safeFrom];
        System.arraycopy(array, safeFrom, copy, 0, copy.length);
        return copy;
    }

    private static void reverse(Object[] array) {
        for (int left = 0, right = array.length - 1; left < right; left++, right--) {
            Object value = array[left];
            array[left] = array[right];
            array[right] = value;
        }
    }

    @SuppressWarnings("unchecked")
    private T[] singletonArray(@Nullable T value) {
        T[] one = (T[]) Array.newInstance(getReturnType(), 1);
        one[0] = value;
        return one;
    }

    private static String expressionText(@Nullable Expression<?> expression, @Nullable SkriptEvent event, boolean debug) {
        return expression == null ? "null" : expression.toString(event, debug);
    }
}
