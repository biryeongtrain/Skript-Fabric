package ch.njol.skript.lang;

import java.util.Map;
import java.util.WeakHashMap;
import org.skriptlang.skript.lang.event.SkriptEvent;

/**
 * Represents a loop section.
 */
public abstract class LoopSection extends Section implements SyntaxElement, Debuggable, SectionExitHandler {

    protected final transient Map<SkriptEvent, Long> currentLoopCounter = new WeakHashMap<>();

    public long getLoopCounter(SkriptEvent event) {
        return currentLoopCounter.getOrDefault(event, 1L);
    }

    @Override
    public abstract TriggerItem getActualNext();

    @Override
    public void exit(SkriptEvent event) {
        currentLoopCounter.remove(event);
    }
}
