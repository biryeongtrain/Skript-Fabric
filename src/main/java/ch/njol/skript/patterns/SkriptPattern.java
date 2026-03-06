package ch.njol.skript.patterns;

public class SkriptPattern {

    private final String pattern;

    public SkriptPattern(String pattern) {
        this.pattern = pattern;
    }

    public Object match(String text) {
        return pattern.equalsIgnoreCase(text) ? Boolean.TRUE : null;
    }
}
