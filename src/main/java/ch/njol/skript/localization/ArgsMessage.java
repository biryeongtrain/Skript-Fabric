package ch.njol.skript.localization;

import java.util.IllegalFormatException;

public final class ArgsMessage extends Message {

    public ArgsMessage(String key) {
        super(key);
    }

    @Override
    public String toString() {
        throw new UnsupportedOperationException();
    }

    public String toString(Object... args) {
        try {
            String value = getValue();
            return value == null ? key : String.format(value, args);
        } catch (IllegalFormatException e) {
            return "[ERROR]";
        }
    }
}
