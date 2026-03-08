package ch.njol.skript.classes;

import org.jetbrains.annotations.Nullable;

/**
 * Legacy converter wrapper kept on top of the current converter backend.
 *
 * @param <F> source type
 * @param <T> target type
 */
@Deprecated(since = "2.10.0", forRemoval = true)
@FunctionalInterface
public interface Converter<F, T> extends org.skriptlang.skript.lang.converter.Converter<F, T> {

    @Deprecated(since = "2.10.0", forRemoval = true)
    int NO_LEFT_CHAINING = org.skriptlang.skript.lang.converter.Converter.NO_LEFT_CHAINING;

    @Deprecated(since = "2.10.0", forRemoval = true)
    int NO_RIGHT_CHAINING = org.skriptlang.skript.lang.converter.Converter.NO_RIGHT_CHAINING;

    @Deprecated(since = "2.10.0", forRemoval = true)
    int NO_CHAINING = NO_LEFT_CHAINING | NO_RIGHT_CHAINING;

    // The local runtime does not expose the old command package; keep the legacy flag value.
    @Deprecated(since = "2.10.0", forRemoval = true)
    int NO_COMMAND_ARGUMENTS = 8;

    @Override
    @Deprecated(since = "2.10.0", forRemoval = true)
    @Nullable T convert(F from);

    @Deprecated(since = "2.10.0", forRemoval = true)
    final class ConverterUtils {

        private ConverterUtils() {
        }

        public static <F, T> Converter<?, T> createInstanceofConverter(Class<F> from, Converter<F, T> converter) {
            return value -> from.isInstance(value) ? converter.convert(from.cast(value)) : null;
        }

        public static <F, T> Converter<F, T> createInstanceofConverter(Converter<F, ?> converter, Class<T> to) {
            return value -> {
                Object converted = converter.convert(value);
                return to.isInstance(converted) ? to.cast(converted) : null;
            };
        }

        public static <F, T> Converter<?, T> createDoubleInstanceofConverter(
                Class<F> from,
                Converter<F, ?> converter,
                Class<T> to
        ) {
            return value -> {
                if (!from.isInstance(value)) {
                    return null;
                }
                Object converted = converter.convert(from.cast(value));
                return to.isInstance(converted) ? to.cast(converted) : null;
            };
        }
    }
}
