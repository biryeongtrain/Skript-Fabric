package ch.njol.skript.patterns;

import java.util.HashSet;
import java.util.Set;
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
        return "[" + (patternElement == null ? "" : patternElement.toFullString()) + "]";
    }

    @Override
    public Set<String> getCombinations(boolean clean) {
        Set<String> combinations = new HashSet<>();
        if (patternElement != null) {
            combinations.addAll(patternElement.getAllCombinations(clean));
        }
        combinations.add("");
        return combinations;
    }
}
