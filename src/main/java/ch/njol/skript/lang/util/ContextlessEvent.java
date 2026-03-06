package ch.njol.skript.lang.util;

import org.skriptlang.skript.lang.event.SkriptEvent;

/**
 * Compatibility utility for places that need an event object while no concrete event context exists.
 */
public final class ContextlessEvent {

    private ContextlessEvent() {
    }

    public static SkriptEvent get() {
        return SkriptEvent.EMPTY;
    }
}
