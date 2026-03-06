package ch.njol.skript.localization;

public class Noun {

    private final String key;

    public Noun(String key) {
        this.key = key;
    }

    @Override
    public String toString() {
        return key;
    }
}
