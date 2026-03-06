package ch.njol.skript.lang.function;

import org.skriptlang.skript.lang.event.SkriptEvent;

/**
 * Event context wrapper for function execution.
 */
public final class FunctionEvent<T> {

    private final Function<? extends T> function;
    private final SkriptEvent context;

    public FunctionEvent(Function<? extends T> function) {
        this(function, SkriptEvent.EMPTY);
    }

    public FunctionEvent(Function<? extends T> function, SkriptEvent context) {
        this.function = function;
        this.context = context == null ? SkriptEvent.EMPTY : context;
    }

    public Function<? extends T> getFunction() {
        return function;
    }

    public SkriptEvent getContext() {
        return context;
    }
}
