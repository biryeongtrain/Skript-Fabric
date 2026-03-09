package ch.njol.skript.classes.data;

import ch.njol.skript.registrations.Converters;

/**
 * Java-only subset of upstream default converters.
 */
public final class DefaultConverters {

    private DefaultConverters() {
    }

    public static void register() {
        registerIfMissing(Number.class, Byte.class, Number::byteValue);
        registerIfMissing(Number.class, Double.class, Number::doubleValue);
        registerIfMissing(Number.class, Float.class, Number::floatValue);
        registerIfMissing(Number.class, Integer.class, Number::intValue);
        registerIfMissing(Number.class, Long.class, Number::longValue);
        registerIfMissing(Number.class, Short.class, Number::shortValue);
    }

    private static <F, T> void registerIfMissing(
            Class<F> from,
            Class<T> to,
            ch.njol.skript.classes.Converter<F, T> converter
    ) {
        if (!org.skriptlang.skript.lang.converter.Converters.exactConverterExists(from, to)) {
            Converters.registerConverter(from, to, converter);
        }
    }
}
