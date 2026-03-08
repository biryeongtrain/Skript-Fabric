package ch.njol.skript.patterns;

import org.jetbrains.annotations.Nullable;

public final class ParseTagPatternElement extends PatternElement {

    private final @Nullable String tag;
    private final int mark;

    public ParseTagPatternElement(@Nullable String tag, int mark) {
        this.tag = tag;
        this.mark = mark;
    }

    public @Nullable String tag() {
        return tag;
    }

    public int mark() {
        return mark;
    }

    @Override
    public String toString() {
        if (tag != null && !tag.isBlank()) {
            return tag + ":";
        }
        return mark + "¦";
    }
}
