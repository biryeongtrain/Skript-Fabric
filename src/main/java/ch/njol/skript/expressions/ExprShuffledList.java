package ch.njol.skript.expressions;

import ch.njol.skript.Skript;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.KeyedIterableExpression;
import ch.njol.skript.lang.KeyedValue;
import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.simplification.SimplifiedLiteral;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.skript.util.LiteralUtils;
import ch.njol.util.Kleenean;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.lang.event.SkriptEvent;

public class ExprShuffledList extends SimpleExpression<Object> implements KeyedIterableExpression<Object> {

    static {
        Skript.registerExpression(ExprShuffledList.class, Object.class, "shuffled %objects%");
    }

    private Expression<?> list;
    private boolean keyed;

    public ExprShuffledList() {
    }

    public ExprShuffledList(Expression<?> list) {
        this.list = list;
        this.keyed = KeyedIterableExpression.canIterateWithKeys(list);
    }

    @Override
    public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
        list = LiteralUtils.defendExpression(expressions[0]);
        keyed = KeyedIterableExpression.canIterateWithKeys(list);
        return LiteralUtils.canInitSafely(list);
    }

    @Override
    protected Object @Nullable [] get(SkriptEvent event) {
        Object[] origin = list.getArray(event).clone();
        List<Object> shuffled = new ArrayList<>(List.of(origin));
        Collections.shuffle(shuffled);
        Object[] array = (Object[]) Array.newInstance(getReturnType(), origin.length);
        return shuffled.toArray(array);
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
        Iterator<? extends KeyedValue<?>> source = ((KeyedIterableExpression<?>) list).keyedIterator(event);
        List<KeyedValue<Object>> values = new ArrayList<>();
        while (source.hasNext()) {
            KeyedValue<?> value = source.next();
            values.add(new KeyedValue<>(value.key(), value.value()));
        }
        Collections.shuffle(values);
        return values.iterator();
    }

    @Override
    @SafeVarargs
    @SuppressWarnings("unchecked")
    public final <R> @Nullable Expression<? extends R> getConvertedExpression(Class<R>... to) {
        for (Class<R> type : to) {
            if (type.isAssignableFrom(getReturnType())) {
                return (Expression<? extends R>) this;
            }
        }
        Expression<? extends R> convertedList = list.getConvertedExpression(to);
        if (convertedList != null) {
            return (Expression<? extends R>) new ExprShuffledList(convertedList);
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
        return "shuffled " + list.toString(event, debug);
    }
}
