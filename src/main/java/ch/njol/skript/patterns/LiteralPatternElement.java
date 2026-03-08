package ch.njol.skript.patterns;

public final class LiteralPatternElement extends PatternElement {

    private final String literal;

    public LiteralPatternElement(String literal) {
        this.literal = literal == null ? "" : literal;
    }

    public String literal() {
        return literal;
    }

    @Override
    public String toString() {
        return literal;
    }
}
