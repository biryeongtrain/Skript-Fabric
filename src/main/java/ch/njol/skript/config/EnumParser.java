package ch.njol.skript.config;

import ch.njol.skript.Skript;
import java.util.Locale;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.lang.converter.Converter;

@Deprecated(since = "2.12", forRemoval = true)
public class EnumParser<E extends Enum<E>> implements Converter<String, E> {

    private final Class<E> enumType;
    private final @Nullable String allowedValues;
    private final String type;

    public EnumParser(Class<E> enumType, String type) {
        this.enumType = enumType;
        this.type = type;
        if (enumType.getEnumConstants().length <= 12) {
            StringBuilder builder = new StringBuilder();
            for (E value : enumType.getEnumConstants()) {
                if (builder.length() != 0) {
                    builder.append(", ");
                }
                builder.append(value.name().toLowerCase(Locale.ENGLISH).replace('_', ' '));
            }
            allowedValues = builder.toString();
        } else {
            allowedValues = null;
        }
    }

    @Override
    public @Nullable E convert(String value) {
        try {
            return Enum.valueOf(enumType, value.toUpperCase(Locale.ENGLISH).replace(' ', '_'));
        } catch (IllegalArgumentException ignored) {
            Skript.error("'" + value + "' is not a valid value for " + type
                    + (allowedValues == null ? "" : ". Allowed values are: " + allowedValues));
            return null;
        }
    }

    @Override
    public String toString() {
        return "EnumParser{enum=" + enumType + ",allowedValues=" + allowedValues + ",type=" + type + "}";
    }
}
