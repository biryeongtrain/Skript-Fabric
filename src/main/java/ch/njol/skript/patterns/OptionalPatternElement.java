package ch.njol.skript.patterns;

import org.jetbrains.annotations.Nullable;

public final class OptionalPatternElement extends PatternElement {

    private final @Nullable PatternElement patternElement;

    public OptionalPatternElement(@Nullable PatternElement patternElement) {
        this.patternElement = patternElement;
    }

    public @Nullable PatternElement getPatternElement() {
        return patternElement;
    }

    @Override
    public String toString() {
        return "[" + (patternElement == null ? "" : patternElement) + "]";
    }
}
