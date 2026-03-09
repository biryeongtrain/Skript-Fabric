package ch.njol.skript.expressions;

import ch.njol.skript.Skript;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.KeyedIterableExpression;
import ch.njol.skript.lang.KeyedValue;
import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.simplification.SimplifiedLiteral;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.util.Kleenean;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.lang.event.SkriptEvent;
import org.skriptlang.skript.lang.comparator.Comparators;

public class ExprSortedList extends SimpleExpression<Object> implements KeyedIterableExpression<Object> {

    static {
        Skript.registerExpression(ExprSortedList.class, Object.class, "sorted %objects%");
    }

    private Expression<?> list;
    private boolean keyed;

    public ExprSortedList() {
    }

    public ExprSortedList(Expression<?> list) {
        this.list = list;
        this.keyed = KeyedIterableExpression.canIterateWithKeys(list);
    }

    @Override
    public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
        list = expressions[0];
        if (list.isSingle()) {
            Skript.error("A single object cannot be sorted.");
            return false;
        }
        keyed = KeyedIterableExpression.canIterateWithKeys(list);
        return true;
    }

    @Override
    protected Object @Nullable [] get(SkriptEvent event) {
        try {
            return list.stream(event).sorted(ExprSortedList::compare).toArray();
        } catch (IllegalArgumentException | ClassCastException ignored) {
            return (Object[]) Array.newInstance(getReturnType(), 0);
        }
    }

    @Override
    public boolean canIterateWithKeys() {
        return keyed;
    }

    @Override
    @SuppressWarnings("unchecked")
    public Iterator<KeyedValue<Object>> keyedIterator(SkriptEvent event) {
        if (!keyed) {
            throw new UnsupportedOperationException();
        }
        try {
            Iterator<? extends KeyedValue<?>> iterator = ((KeyedIterableExpression<?>) list).keyedIterator(event);
            List<KeyedValue<Object>> values = new ArrayList<>();
            while (iterator.hasNext()) {
                KeyedValue<?> value = iterator.next();
                values.add(new KeyedValue<>(value.key(), value.value()));
            }
            values.sort((left, right) -> compare(left.value(), right.value()));
            return values.iterator();
        } catch (IllegalArgumentException | ClassCastException ignored) {
            return List.<KeyedValue<Object>>of().iterator();
        }
    }

    @SuppressWarnings("unchecked")
    public static <A, B> int compare(A left, B right) {
        if (left instanceof String leftString && right instanceof String rightString) {
            return leftString.compareToIgnoreCase(rightString);
        }

        var comparator = Comparators.getComparator((Class<A>) left.getClass(), (Class<B>) right.getClass());
        if (comparator != null && comparator.supportsOrdering()) {
            return comparator.compare(left, right).getRelation();
        }
        if (!(left instanceof Comparable<?> comparable)) {
            throw new IllegalArgumentException("Cannot compare " + left.getClass());
        }
        return ((Comparable<B>) comparable).compareTo(right);
    }

    @Override
    @SafeVarargs
    public final <R> @Nullable Expression<? extends R> getConvertedExpression(Class<R>... to) {
        for (Class<R> type : to) {
            if (type.isAssignableFrom(getReturnType())) {
                return (Expression<? extends R>) this;
            }
        }

        Expression<? extends R> convertedList = list.getConvertedExpression(to);
        if (convertedList != null) {
            return (Expression<? extends R>) new ExprSortedList(convertedList);
        }
        return null;
    }

    @Override
    public Class<?> getReturnType() {
        return list.getReturnType();
    }

    @Override
    public Class<?>[] possibleReturnTypes() {
        return list.possibleReturnTypes();
    }

    @Override
    public boolean canReturn(Class<?> returnType) {
        return list.canReturn(returnType);
    }

    @Override
    public boolean isSingle() {
        return false;
    }

    @Override
    public boolean isIndexLoop(String input) {
        if (!keyed) {
            throw new IllegalStateException();
        }
        return ((KeyedIterableExpression<?>) list).isIndexLoop(input);
    }

    @Override
    public boolean isLoopOf(String input) {
        return list.isLoopOf(input);
    }

    @Override
    public Expression<?> simplify() {
        if (list instanceof Literal<?>) {
            return SimplifiedLiteral.fromExpression(this);
        }
        return this;
    }

    @Override
    public String toString(@Nullable SkriptEvent event, boolean debug) {
        return "sorted " + list.toString(event, debug);
    }
}
