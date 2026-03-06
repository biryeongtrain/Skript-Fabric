package ch.njol.skript.expressions;

import ch.njol.skript.classes.ClassInfo;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.lang.converter.Converters;

public final class ExprSubnodeValue {

    private ExprSubnodeValue() {
    }

    public static <T> @Nullable T convertedValue(Object value, ClassInfo<T> expected) {
        if (expected.getC().isInstance(value)) {
            return expected.getC().cast(value);
        }
        return Converters.convert(value, expected.getC());
    }
}
