package ch.njol.skript.localization;

import java.util.IllegalFormatException;

public final class FormattedMessage extends Message {

    private final Object[] args;

    public FormattedMessage(String key, Object... args) {
        super(key);
        this.args = args;
    }

    @Override
    public String toString() {
        try {
            String value = getValue();
            return value == null ? key : String.format(value, args);
        } catch (IllegalFormatException e) {
            return "[ERROR]";
        }
    }
}
