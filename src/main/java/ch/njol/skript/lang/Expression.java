package ch.njol.skript.lang;

import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.classes.Changer.ChangerUtils;
import ch.njol.skript.lang.simplification.Simplifiable;
import ch.njol.skript.lang.util.ConvertedExpression;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.lang.event.SkriptEvent;

public interface Expression<T> extends SyntaxElement, Debuggable, Loopable<T>, Simplifiable<Expression<? extends T>> {

    default @Nullable T getSingle(SkriptEvent event) {
        T[] values = getArray(event);
        if (values == null || values.length == 0) {
            return null;
        }
        return values[0];
    }

    default Optional<T> getOptionalSingle(SkriptEvent event) {
        return Optional.ofNullable(getSingle(event));
    }

    @SuppressWarnings("unchecked")
    default T[] getArray(SkriptEvent event) {
        T value = getSingle(event);
        if (value == null) {
            return (T[]) new Object[0];
        }
        return (T[]) new Object[]{value};
    }

    default T[] getAll(SkriptEvent event) {
        return getArray(event);
    }

    @Override
    default @Nullable Iterator<? extends T> iterator(SkriptEvent event) {
        T[] values = getArray(event);
        if (values == null || values.length == 0) {
            return null;
        }
        return Arrays.asList(values).iterator();
    }

    default Stream<? extends @NotNull T> stream(SkriptEvent event) {
        Iterator<? extends T> iterator = iterator(event);
        if (iterator == null) {
            return Stream.empty();
        }
        return StreamSupport.stream(
                java.util.Spliterators.spliteratorUnknownSize(iterator, 0),
                false
        );
    }

    default Stream<? extends @NotNull T> streamAll(SkriptEvent event) {
        return Arrays.stream(getAll(event));
    }

    default boolean isSingle() {
        return false;
    }

    default boolean canBeSingle() {
        return isSingle();
    }

    default boolean check(SkriptEvent event, Predicate<? super T> checker, boolean negated) {
        return negated ^ check(event, checker);
    }

    default boolean check(SkriptEvent event, Predicate<? super T> checker) {
        if (getAnd()) {
            return stream(event).anyMatch(checker);
        }
        T single = getSingle(event);
        return single != null && checker.test(single);
    }

    @SuppressWarnings("unchecked")
    default <R> @Nullable Expression<? extends R> getConvertedExpression(Class<R>... to) {
        for (Class<R> targetType : to) {
            if (targetType.isAssignableFrom(getReturnType())) {
                return (Expression<? extends R>) this;
            }
        }
        return ConvertedExpression.newInstance(this, to);
    }

    @SuppressWarnings("unchecked")
    default Class<? extends T> getReturnType() {
        return (Class<? extends T>) Object.class;
    }

    @SuppressWarnings("unchecked")
    default Class<? extends T>[] possibleReturnTypes() {
        return new Class[]{getReturnType()};
    }

    default boolean canReturn(Class<?> returnType) {
        for (Class<?> type : possibleReturnTypes()) {
            if (returnType.isAssignableFrom(type) || type == Object.class) {
                return true;
            }
        }
        return false;
    }

    default boolean canReturnAnyOf(Class<?>... returnTypes) {
        for (Class<?> returnType : returnTypes) {
            if (canReturn(returnType)) {
                return true;
            }
        }
        return false;
    }

    default boolean getAnd() {
        return isSingle();
    }

    default boolean setTime(int time) {
        return time == 0;
    }

    default int getTime() {
        return 0;
    }

    default boolean returnNestedStructures(boolean nested) {
        return false;
    }

    default boolean returnsNestedStructures() {
        return false;
    }

    default boolean isDefault() {
        return false;
    }

    default Expression<?> getSource() {
        return this;
    }

    @Override
    default Expression<? extends T> simplify() {
        return this;
    }

    default Class<?> @Nullable [] acceptChange(ChangeMode mode) {
        return null;
    }

    default Map<ChangeMode, Class<?>[]> getAcceptedChangeModes() {
        Map<ChangeMode, Class<?>[]> accepted = new HashMap<>();
        for (ChangeMode mode : ChangeMode.values()) {
            Class<?>[] valid = acceptChange(mode);
            if (valid != null) {
                accepted.put(mode, valid);
            }
        }
        return accepted;
    }

    default void change(SkriptEvent event, Object @Nullable [] delta, ChangeMode mode) {
    }

    @ApiStatus.Internal
    default <R> void changeInPlace(SkriptEvent event, Function<T, R> changeFunction) {
        changeInPlace(event, changeFunction, false);
    }

    @ApiStatus.Internal
    default <R> void changeInPlace(SkriptEvent event, Function<T, R> changeFunction, boolean getAll) {
        T[] values = getAll ? getAll(event) : getArray(event);
        if (values == null || values.length == 0) {
            return;
        }

        Class<?>[] accepted = acceptChange(ChangeMode.SET);
        if (accepted == null) {
            return;
        }

        Class<?>[] flattened = Arrays.stream(accepted)
                .map(type -> type.isArray() ? type.getComponentType() : type)
                .toArray(Class<?>[]::new);

        List<R> nextValues = new ArrayList<>();
        for (T value : values) {
            R newValue = changeFunction.apply(value);
            if (newValue != null && (flattened.length == 0 || ChangerUtils.acceptsChangeTypes(flattened, newValue.getClass()))) {
                nextValues.add(newValue);
            }
        }

        change(event, nextValues.toArray(), ChangeMode.SET);
    }

    default Object @Nullable [] beforeChange(Expression<?> changed, Object @Nullable [] delta) {
        return delta;
    }

    @Override
    default @NotNull String getSyntaxTypeName() {
        return "expression";
    }
}
