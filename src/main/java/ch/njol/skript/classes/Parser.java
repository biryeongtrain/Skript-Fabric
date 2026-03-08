package ch.njol.skript.classes;

import ch.njol.skript.lang.ParseContext;
import ch.njol.skript.util.StringMode;
import org.jetbrains.annotations.Nullable;

/**
 * Legacy parser wrapper kept for upstream compatibility.
 *
 * @param <T> parsed type
 */
public abstract class Parser<T> implements ClassInfo.Parser<T> {

    @Override
    public @Nullable T parse(String input, ParseContext context) {
        throw new UnsupportedOperationException(
                "Parsing not implemented (remember to override parse method): " + getClass().getName()
        );
    }

    @Override
    public boolean canParse(ParseContext context) {
        return true;
    }

    public abstract String toString(T object, int flags);

    public final String toString(T object, StringMode mode) {
        return switch (mode) {
            case MESSAGE -> toString(object, 0);
            case DEBUG -> getDebugMessage(object);
            case VARIABLE_NAME -> toVariableNameString(object);
        };
    }

    public String toCommandString(T object) {
        return toString(object, 0);
    }

    public abstract String toVariableNameString(T object);

    public String getDebugMessage(T object) {
        return toString(object, 0);
    }
}
