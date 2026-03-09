package ch.njol.skript.config;

import ch.njol.skript.Skript;
import ch.njol.skript.classes.ClassInfo;
import ch.njol.skript.lang.ParseContext;
import ch.njol.skript.registrations.Classes;
import java.util.Locale;
import java.util.function.Consumer;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.lang.converter.Converter;

public class Option<T> {

    public final String key;
    private boolean optional;
    private @Nullable String value;
    private final Converter<String, ? extends T> parser;
    private final T defaultValue;
    private T parsedValue;
    private @Nullable Consumer<? super T> setter;

    public Option(String key, T defaultValue) {
        this.key = key.toLowerCase(Locale.ENGLISH);
        this.defaultValue = defaultValue;
        this.parsedValue = defaultValue;
        @SuppressWarnings("unchecked")
        Class<T> type = (Class<T>) defaultValue.getClass();
        if (type == String.class) {
            this.parser = s -> (T) s;
            return;
        }
        ClassInfo<T> classInfo = Classes.getExactClassInfo(type);
        ClassInfo.Parser<? extends T> classParser;
        if (classInfo == null || (classParser = classInfo.getParser()) == null) {
            throw new IllegalArgumentException(type.getName());
        }
        this.parser = value -> {
            T parsed = classParser.parse(value, ParseContext.CONFIG);
            if (parsed != null) {
                return parsed;
            }
            Skript.error("'" + value + "' is not " + classInfo.getCodeName());
            return null;
        };
    }

    public Option(String key, T defaultValue, Converter<String, ? extends T> parser) {
        this.key = key.toLowerCase(Locale.ENGLISH);
        this.defaultValue = defaultValue;
        this.parsedValue = defaultValue;
        this.parser = parser;
    }

    public final Option<T> setter(Consumer<? super T> setter) {
        this.setter = setter;
        return this;
    }

    public final Option<T> optional(boolean optional) {
        this.optional = optional;
        return this;
    }

    public final void set(Config config, String path) {
        String oldValue = value;
        value = config.getByPath(path + key);
        if (value == null && !optional) {
            Skript.error("Required entry '" + path + key + "' is missing in " + config.getFileName()
                    + ". Please make sure that you have the latest version of the config.");
        }
        if ((value == null) != (oldValue == null) || (value != null && !value.equals(oldValue))) {
            T next = value != null ? parser.convert(value) : defaultValue;
            if (next == null) {
                next = defaultValue;
            }
            parsedValue = next;
            onValueChange();
        }
    }

    protected void onValueChange() {
        if (setter != null) {
            setter.accept(parsedValue);
        }
    }

    public final T value() {
        return parsedValue;
    }

    public final T defaultValue() {
        return defaultValue;
    }

    public final boolean isOptional() {
        return optional;
    }
}
