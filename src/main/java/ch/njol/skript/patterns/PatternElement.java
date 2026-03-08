package ch.njol.skript.patterns;

import org.jetbrains.annotations.Nullable;

public abstract class PatternElement {

    private @Nullable PatternElement originalNext;

    final void setOriginalNext(@Nullable PatternElement originalNext) {
        this.originalNext = originalNext;
    }

    public final @Nullable PatternElement getOriginalNext() {
        return originalNext;
    }
}
