package ch.njol.skript.patterns;

import java.util.List;

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
        return patternElements.toString();
    }
}
