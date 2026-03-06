package org.skriptlang.skript.util;

public interface Validated {
    boolean valid();

    void invalidate();
}
