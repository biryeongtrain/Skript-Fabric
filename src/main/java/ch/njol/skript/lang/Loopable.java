package ch.njol.skript.lang;

import java.util.Iterator;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.lang.event.SkriptEvent;

public interface Loopable<T> {

    default @Nullable Iterator<? extends T> iterator(SkriptEvent event) {
        return null;
    }

    default boolean isLoopOf(String input) {
        return false;
    }

    default boolean supportsLoopPeeking() {
        return false;
    }
}
