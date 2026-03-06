package ch.njol.skript.localization;

public class Message {

    private final String key;

    public Message(String key) {
        this.key = key;
    }

    @Override
    public String toString() {
        return key;
    }
}
