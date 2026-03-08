package ch.njol.skript.patterns;

import java.util.Set;

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

    @Override
    public Set<String> getCombinations(boolean clean) {
        return Set.of(literal);
    }
}
