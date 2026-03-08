package ch.njol.skript.patterns;

import java.util.Arrays;

public final class TypePatternElement extends PatternElement {

    private final String placeholder;
    private final Class<?>[] returnTypes;
    private final int expressionIndex;

    public TypePatternElement(String placeholder, Class<?>[] returnTypes, int expressionIndex) {
        this.placeholder = placeholder == null ? "" : placeholder;
        this.returnTypes = returnTypes == null ? new Class<?>[0] : Arrays.copyOf(returnTypes, returnTypes.length);
        this.expressionIndex = expressionIndex;
    }

    public String placeholder() {
        return placeholder;
    }

    public Class<?>[] returnTypes() {
        return Arrays.copyOf(returnTypes, returnTypes.length);
    }

    public int expressionIndex() {
        return expressionIndex;
    }

    @Override
    public String toString() {
        return "%" + placeholder + "%";
    }
}
