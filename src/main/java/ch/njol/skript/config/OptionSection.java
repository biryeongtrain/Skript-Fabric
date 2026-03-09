package ch.njol.skript.config;

import java.lang.reflect.Field;
import java.util.Locale;
import org.jetbrains.annotations.Nullable;

public class OptionSection {

    public final String key;

    public OptionSection(String key) {
        this.key = key;
    }

    @SuppressWarnings("unchecked")
    public final @Nullable <T> T get(String key) {
        if (getClass() == OptionSection.class) {
            return null;
        }
        String normalizedKey = key.toLowerCase(Locale.ENGLISH);
        for (Field field : getClass().getDeclaredFields()) {
            field.setAccessible(true);
            if (!Option.class.isAssignableFrom(field.getType())) {
                continue;
            }
            try {
                Option<?> option = (Option<?>) field.get(this);
                if (option.key.equals(normalizedKey)) {
                    return (T) option.value();
                }
            } catch (IllegalAccessException ignored) {
                throw new IllegalStateException(ignored);
            }
        }
        return null;
    }
}
