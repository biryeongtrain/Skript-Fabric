package ch.njol.skript.patterns;

import java.util.Set;
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
        return "(" + (patternElement == null ? "" : patternElement.toFullString()) + ")";
    }

    @Override
    public Set<String> getCombinations(boolean clean) {
        if (patternElement == null) {
            return Set.of("");
        }
        return patternElement.getAllCombinations(clean);
    }
}
