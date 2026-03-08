package ch.njol.skript.patterns;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.HashSet;

public final class ChoicePatternElement extends PatternElement {

    private final List<PatternElement> patternElements;

    public ChoicePatternElement(List<PatternElement> patternElements) {
        this.patternElements = List.copyOf(patternElements);
    }

    public List<PatternElement> getPatternElements() {
        return patternElements;
    }

    @Override
    public String toString() {
        return patternElements.stream()
                .map(PatternElement::toFullString)
                .collect(Collectors.joining("|"));
    }

    @Override
    public Set<String> getCombinations(boolean clean) {
        Set<String> combinations = new HashSet<>();
        for (PatternElement patternElement : patternElements) {
            combinations.addAll(patternElement.getAllCombinations(clean));
        }
        return combinations;
    }
}
