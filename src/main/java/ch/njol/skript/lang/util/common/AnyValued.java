package ch.njol.skript.lang.util.common;

import ch.njol.skript.classes.ClassInfo;
import ch.njol.skript.expressions.ExprSubnodeValue;
import ch.njol.skript.lang.ParseContext;
import ch.njol.skript.registrations.Classes;
import ch.njol.skript.util.StringMode;
import org.jetbrains.annotations.UnknownNullability;
import org.skriptlang.skript.lang.converter.Converters;

/**
 * Provider for values that can be read and optionally changed.
 *
 * @deprecated Use {@link org.skriptlang.skript.lang.properties.Property#TYPED_VALUE} instead.
 */
@Deprecated(since = "2.13", forRemoval = true)
public interface AnyValued<Type> extends AnyProvider {

    @UnknownNullability
    Type value();

    default <Converted> Converted convertedValue(ClassInfo<Converted> expected) {
        Type value = value();
        if (value == null) {
            return null;
        }
        return ExprSubnodeValue.convertedValue(value, expected);
    }

    default boolean supportsValueChange() {
        return false;
    }

    default void changeValue(Type value) throws UnsupportedOperationException {
        throw new UnsupportedOperationException();
    }

    Class<Type> valueType();

    default void resetValue() throws UnsupportedOperationException {
        changeValueSafely(null);
    }

    @SuppressWarnings("unchecked")
    default void changeValueSafely(Object value) throws UnsupportedOperationException {
        Class<Type> typeClass = valueType();
        ClassInfo<? super Type> classInfo = Classes.getSuperClassInfo(typeClass);
        if (value == null) {
            changeValue(null);
        } else if (typeClass == String.class) {
            changeValue(typeClass.cast(Classes.toString(value, StringMode.MESSAGE)));
        } else if (value instanceof String string
                && classInfo.getParser() != null
                && classInfo.getParser().canParse(ParseContext.CONFIG)) {
            Type parsed = (Type) classInfo.getParser().parse(string, ParseContext.CONFIG);
            changeValue(parsed);
        } else {
            Type converted = Converters.convert(value, typeClass);
            changeValue(converted);
        }
    }
}
