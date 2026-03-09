package ch.njol.skript.localization;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Locale;

public class Message {

    private static final Collection<Message> messages = new ArrayList<>(50);

    static {
        Language.addListener(() -> {
            for (Message message : messages) {
                synchronized (message) {
                    message.revalidate = true;
                }
            }
        });
    }

    public final String key;
    private String value;
    boolean revalidate = true;

    public Message(String key) {
        this.key = key.toLowerCase(Locale.ENGLISH);
        messages.add(this);
    }

    @Override
    public String toString() {
        validate();
        return value == null ? key : value;
    }

    public final String getValue() {
        validate();
        return value;
    }

    public final String getValueOrDefault(String defaultValue) {
        validate();
        return value == null ? defaultValue : value;
    }

    public final boolean isSet() {
        validate();
        return value != null;
    }

    protected synchronized void validate() {
        if (!revalidate) {
            return;
        }
        revalidate = false;
        value = Language.get_(key);
        onValueChange();
    }

    protected void onValueChange() {
    }
}
