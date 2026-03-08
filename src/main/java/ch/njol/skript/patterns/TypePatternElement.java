package ch.njol.skript.patterns;

import java.util.Arrays;
import java.util.Set;

public final class TypePatternElement extends PatternElement {

    private final String placeholder;
    private final Class<?>[] returnTypes;
    private final boolean[] pluralities;
    private final int flagMask;
    private final int time;
    private final boolean optional;
    private final int expressionIndex;

    public TypePatternElement(
            String placeholder,
            Class<?>[] returnTypes,
            boolean[] pluralities,
            int flagMask,
            int time,
            boolean optional,
            int expressionIndex
    ) {
        this.placeholder = placeholder == null ? "" : placeholder;
        this.returnTypes = returnTypes == null ? new Class<?>[0] : Arrays.copyOf(returnTypes, returnTypes.length);
        this.pluralities = pluralities == null ? new boolean[this.returnTypes.length] : Arrays.copyOf(pluralities, pluralities.length);
        this.flagMask = flagMask;
        this.time = time;
        this.optional = optional;
        this.expressionIndex = expressionIndex;
    }

    public String placeholder() {
        return placeholder;
    }

    public Class<?>[] returnTypes() {
        return Arrays.copyOf(returnTypes, returnTypes.length);
    }

    public boolean[] pluralities() {
        return Arrays.copyOf(pluralities, pluralities.length);
    }

    public int flagMask() {
        return flagMask;
    }

    public int time() {
        return time;
    }

    public boolean isOptional() {
        return optional;
    }

    public int expressionIndex() {
        return expressionIndex;
    }

    @Override
    public String toString() {
        return "%" + placeholder + "%";
    }

    @Override
    public Set<String> getCombinations(boolean clean) {
        return Set.of(toString());
    }
}
