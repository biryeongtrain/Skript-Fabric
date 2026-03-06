package ch.njol.skript.lang;

import java.util.Arrays;
import java.util.Iterator;
import org.jetbrains.annotations.NotNull;
import org.skriptlang.skript.lang.event.SkriptEvent;

public interface KeyProviderExpression<T> extends Expression<T>, KeyedIterableExpression<T> {

    @NotNull String[] getArrayKeys(SkriptEvent event) throws IllegalStateException;

    default @NotNull String[] getAllKeys(SkriptEvent event) {
        return getArrayKeys(event);
    }

    @Override
    default Iterator<KeyedValue<T>> keyedIterator(SkriptEvent event) {
        return Arrays.asList(KeyedValue.zip(getArray(event), getArrayKeys(event))).iterator();
    }

    @Override
    default boolean isSingle() {
        return false;
    }

    default boolean canReturnKeys() {
        return true;
    }

    @Override
    default boolean canIterateWithKeys() {
        return canReturnKeys();
    }

    default boolean areKeysRecommended() {
        return true;
    }

    static boolean canReturnKeys(Expression<?> expression) {
        return expression instanceof KeyProviderExpression<?> provider && provider.canReturnKeys();
    }

    static boolean areKeysRecommended(Expression<?> expression) {
        return canReturnKeys(expression) && ((KeyProviderExpression<?>) expression).areKeysRecommended();
    }
}
