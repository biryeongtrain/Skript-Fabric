package ch.njol.skript.patterns;

public final class RegexPatternElement extends PatternElement {

    private final String pattern;

    public RegexPatternElement(String pattern) {
        this.pattern = pattern == null ? "" : pattern;
    }

    public String pattern() {
        return pattern;
    }

    @Override
    public String toString() {
        return "<" + pattern + ">";
    }
}
