package org.skriptlang.skript.common.function;

import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.KeyProviderExpression;
import ch.njol.skript.lang.KeyedValue;
import ch.njol.skript.localization.Noun;
import ch.njol.skript.registrations.Classes;
import com.google.common.base.Preconditions;
import java.util.Set;
import java.util.StringJoiner;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;
import org.skriptlang.skript.lang.event.SkriptEvent;

public interface Parameter<T> {

    @NotNull String name();

    @NotNull Class<T> type();

    @Unmodifiable
    @NotNull Set<Modifier> modifiers();

    default boolean hasModifier(Modifier modifier) {
        return modifiers().contains(modifier);
    }

    default <M extends Modifier> M getModifier(Class<M> modifierClass) {
        return modifiers().stream()
                .filter(modifierClass::isInstance)
                .map(modifierClass::cast)
                .findFirst()
                .orElse(null);
    }

    default Object[] evaluate(@Nullable Expression<? extends T> argument, SkriptEvent event) {
        if (argument == null) {
            return null;
        }

        Object[] values = argument.getArray(event);
        for (int i = 0; i < values.length; i++) {
            values[i] = Classes.clone(values[i]);
        }

        if (!hasModifier(Modifier.KEYED)) {
            return values;
        }

        String[] keys = KeyProviderExpression.areKeysRecommended(argument)
                ? ((KeyProviderExpression<?>) argument).getArrayKeys(event)
                : null;
        return KeyedValue.zip(values, keys);
    }

    default boolean isSingle() {
        return !type().isArray();
    }

    default String toFormattedString() {
        StringJoiner joiner = new StringJoiner(" ");
        joiner.add(name() + ":");

        if (hasModifier(Modifier.OPTIONAL)) {
            joiner.add("optional");
        }

        Noun exact = Classes.getSuperClassInfo(type()).getName();
        joiner.add(type().isArray() ? exact.getPlural() : exact.getSingular());
        return joiner.toString();
    }

    interface Modifier {

        static Modifier of() {
            return new Modifier() {
            };
        }

        Modifier OPTIONAL = of();
        Modifier KEYED = of();
        Modifier RANGED = new RangedModifier<>(0, 0);

        static <T extends Comparable<T>> RangedModifier<T> ranged(T min, T max) {
            return new RangedModifier<>(min, max);
        }

        final class RangedModifier<T extends Comparable<T>> implements Modifier {
            private final T min;
            private final T max;

            private RangedModifier(T min, T max) {
                Preconditions.checkState(min.compareTo(max) <= 0, "Min value cannot be greater than max value!");
                this.min = min;
                this.max = max;
            }

            public T getMin() {
                return min;
            }

            public T getMax() {
                return max;
            }

            public boolean inRange(Object[] input) {
                if (input == null) {
                    return false;
                }
                for (Object value : input) {
                    if (!inRange(value)) {
                        return false;
                    }
                }
                return true;
            }

            @SuppressWarnings("unchecked")
            public boolean inRange(Object input) {
                if (input == null) {
                    return false;
                }
                T cast = (T) input;
                return cast.compareTo(min) >= 0 && cast.compareTo(max) <= 0;
            }

            @Override
            public boolean equals(Object object) {
                return object == this || object == Modifier.RANGED || object instanceof RangedModifier<?>;
            }

            @Override
            public int hashCode() {
                return Modifier.RANGED.hashCode();
            }
        }
    }
}
