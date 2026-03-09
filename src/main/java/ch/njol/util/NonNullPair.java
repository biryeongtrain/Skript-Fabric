package ch.njol.util;

import java.util.Objects;

public record NonNullPair<A, B>(A first, B second) {

    public NonNullPair {
        Objects.requireNonNull(first, "first");
        Objects.requireNonNull(second, "second");
    }
}
