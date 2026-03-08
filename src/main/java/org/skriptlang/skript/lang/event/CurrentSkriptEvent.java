package org.skriptlang.skript.lang.event;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.function.Supplier;
import org.jetbrains.annotations.Nullable;

/**
 * Tracks the currently executing Skript event on the calling thread.
 */
public final class CurrentSkriptEvent {

    private static final ThreadLocal<Deque<SkriptEvent>> EVENTS =
            ThreadLocal.withInitial(ArrayDeque::new);

    private CurrentSkriptEvent() {
    }

    public static void with(SkriptEvent event, Runnable action) {
        Deque<SkriptEvent> events = EVENTS.get();
        events.push(event);
        try {
            action.run();
        } finally {
            events.pop();
        }
    }

    public static <T> T with(SkriptEvent event, Supplier<T> action) {
        Deque<SkriptEvent> events = EVENTS.get();
        events.push(event);
        try {
            return action.get();
        } finally {
            events.pop();
        }
    }

    public static @Nullable SkriptEvent get() {
        return EVENTS.get().peek();
    }
}
