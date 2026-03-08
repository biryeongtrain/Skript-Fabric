package ch.njol.skript.registrations;

import ch.njol.skript.classes.Converter;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.lang.converter.ConverterInfo;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Legacy converter registry bridge kept on top of the current converter backend.
 */
@Deprecated(since = "2.10.0", forRemoval = true)
@SuppressWarnings("removal")
public final class Converters {

    private Converters() {
    }

    @SuppressWarnings("unchecked")
    public static <F, T> List<ConverterInfo<?, ?>> getConverters() {
        return org.skriptlang.skript.lang.converter.Converters.getConverterInfos().stream()
                .map(unknownInfo -> {
                    org.skriptlang.skript.lang.converter.ConverterInfo<F, T> info =
                            (org.skriptlang.skript.lang.converter.ConverterInfo<F, T>) unknownInfo;
                    return new ConverterInfo<>(info.getFrom(), info.getTo(), info.getConverter(), info.getFlags());
                })
                .collect(Collectors.toList());
    }

    public static <F, T> void registerConverter(Class<F> from, Class<T> to, Converter<F, T> converter) {
        registerConverter(from, to, converter, 0);
    }

    public static <F, T> void registerConverter(Class<F> from, Class<T> to, Converter<F, T> converter, int options) {
        org.skriptlang.skript.lang.converter.Converters.registerConverter(from, to, converter::convert, options);
    }

    public static <F, T> @Nullable T convert(@Nullable F value, Class<T> to) {
        return org.skriptlang.skript.lang.converter.Converters.convert(value, to);
    }

    public static <F, T> @Nullable T convert(@Nullable F value, Class<? extends T>[] to) {
        return org.skriptlang.skript.lang.converter.Converters.convert(value, to);
    }

    public static <T> @Nullable T[] convertArray(@Nullable Object[] value, Class<T> to) {
        T[] converted = org.skriptlang.skript.lang.converter.Converters.convert(value, to);
        return converted.length == 0 ? null : converted;
    }

    public static <T> T[] convertArray(@Nullable Object[] value, Class<? extends T>[] to, Class<T> superType) {
        return org.skriptlang.skript.lang.converter.Converters.convert(value, to, superType);
    }

    public static <T> T[] convertStrictly(Object[] original, Class<T> to) throws ClassCastException {
        return org.skriptlang.skript.lang.converter.Converters.convertStrictly(original, to);
    }

    public static <T> T convertStrictly(Object original, Class<T> to) throws ClassCastException {
        return org.skriptlang.skript.lang.converter.Converters.convertStrictly(original, to);
    }

    public static boolean converterExists(Class<?> from, Class<?> to) {
        return org.skriptlang.skript.lang.converter.Converters.converterExists(from, to);
    }

    public static boolean converterExists(Class<?> from, Class<?>... to) {
        return org.skriptlang.skript.lang.converter.Converters.converterExists(from, to);
    }

    @SuppressWarnings("unchecked")
    public static <F, T> @Nullable Converter<? super F, ? extends T> getConverter(Class<F> from, Class<T> to) {
        org.skriptlang.skript.lang.converter.Converter<F, T> converter =
                org.skriptlang.skript.lang.converter.Converters.getConverter(from, to);
        if (converter == null) {
            return null;
        }
        return (Converter<F, T>) converter::convert;
    }

    public static <F, T> @Nullable ConverterInfo<? super F, ? extends T> getConverterInfo(Class<F> from, Class<T> to) {
        org.skriptlang.skript.lang.converter.ConverterInfo<F, T> info =
                org.skriptlang.skript.lang.converter.Converters.getConverterInfo(from, to);
        if (info == null) {
            return null;
        }
        return new ConverterInfo<>(info.getFrom(), info.getTo(), info.getConverter()::convert, info.getFlags());
    }

    public static <F, T> T[] convertUnsafe(F[] from, Class<?> to, Converter<? super F, ? extends T> converter) {
        return org.skriptlang.skript.lang.converter.Converters.convertUnsafe(from, to, converter::convert);
    }

    public static <F, T> T[] convert(F[] from, Class<T> to, Converter<? super F, ? extends T> converter) {
        return org.skriptlang.skript.lang.converter.Converters.convert(from, to, converter::convert);
    }
}
