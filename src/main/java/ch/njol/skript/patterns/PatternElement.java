package ch.njol.skript.patterns;

import org.jetbrains.annotations.Nullable;
import java.util.HashSet;
import java.util.Set;

public abstract class PatternElement {

    private @Nullable PatternElement originalNext;

    final void setOriginalNext(@Nullable PatternElement originalNext) {
        this.originalNext = originalNext;
    }

    public final @Nullable PatternElement getOriginalNext() {
        return originalNext;
    }

    @Override
    public abstract String toString();

    public final String toFullString() {
        StringBuilder builder = new StringBuilder(toString());
        PatternElement next = this;
        while ((next = next.originalNext) != null) {
            builder.append(next);
        }
        return builder.toString();
    }

    public abstract Set<String> getCombinations(boolean clean);

    public final Set<String> getAllCombinations(boolean clean) {
        Set<String> combinations = new HashSet<>(getCombinations(clean));
        if (combinations.isEmpty()) {
            combinations.add("");
        }
        PatternElement next = this;
        while ((next = next.originalNext) != null) {
            Set<String> nextCombinations = next.getCombinations(clean);
            if (nextCombinations.isEmpty()) {
                continue;
            }
            Set<String> combined = new HashSet<>();
            for (String base : combinations) {
                for (String add : nextCombinations) {
                    combined.add(combine(base, add));
                }
            }
            combinations = combined;
        }
        return combinations;
    }

    private static String combine(String first, String second) {
        if (first.isBlank()) {
            return second.stripLeading();
        }
        if (second.isEmpty()) {
            return first.stripTrailing();
        }
        if (first.endsWith(" ") && second.startsWith(" ")) {
            return first + second.stripLeading();
        }
        return first + second;
    }
}
