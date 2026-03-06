package ch.njol.skript.lang;

import ch.njol.skript.util.StringMode;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.lang.event.SkriptEvent;

public final class VariableString {

    private final String value;
    private final StringMode mode;

    private VariableString(String value, StringMode mode) {
        this.value = value;
        this.mode = mode;
    }

    public static String quote(String value) {
        return '"' + value + '"';
    }

    public static VariableString newInstance(String value, StringMode mode) {
        if (value == null) {
            return null;
        }
        return new VariableString(value, mode);
    }

    public String value() {
        return value;
    }

    public StringMode mode() {
        return mode;
    }

    public StringMode getMode() {
        return mode;
    }

    public boolean isSimple() {
        return !value.contains("%");
    }

    public String toString(@Nullable SkriptEvent event, boolean debug) {
        return value;
    }

    public String toString(@Nullable SkriptEvent event) {
        return value;
    }

    @Override
    public String toString() {
        return value;
    }
}
