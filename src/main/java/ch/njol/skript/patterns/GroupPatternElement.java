package ch.njol.skript.patterns;

import org.jetbrains.annotations.Nullable;

public final class GroupPatternElement extends PatternElement {

    private final @Nullable PatternElement patternElement;

    public GroupPatternElement(@Nullable PatternElement patternElement) {
        this.patternElement = patternElement;
    }

    public @Nullable PatternElement getPatternElement() {
        return patternElement;
    }

    @Override
    public String toString() {
        return "(" + (patternElement == null ? "" : patternElement) + ")";
    }
}
