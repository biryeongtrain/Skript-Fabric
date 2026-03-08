package ch.njol.skript.patterns;

import java.util.Set;
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
        if (tag != null) {
            if (tag.isEmpty()) {
                return "";
            }
            return tag + ":";
        }
        return mark + "¦";
    }

    @Override
    public Set<String> getCombinations(boolean clean) {
        if (clean) {
            return Set.of();
        }
        return Set.of(toString());
    }
}
