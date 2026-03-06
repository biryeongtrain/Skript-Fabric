package org.skriptlang.skript.util.event;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

public final class EventRegistry<E extends Event> {

    private final List<Consumer<E>> handlers = new CopyOnWriteArrayList<>();

    public void register(Consumer<E> handler) {
        handlers.add(handler);
    }

    public void unregister(Consumer<E> handler) {
        handlers.remove(handler);
    }

    public void fire(E event) {
        for (Consumer<E> handler : handlers) {
            handler.accept(event);
        }
    }
}
