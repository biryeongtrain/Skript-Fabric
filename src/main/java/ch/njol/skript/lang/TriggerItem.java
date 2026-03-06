package ch.njol.skript.lang;

import ch.njol.skript.Skript;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.lang.event.SkriptEvent;

public abstract class TriggerItem implements Debuggable {

    private @Nullable TriggerSection parent;
    private @Nullable TriggerItem next;
    private @Nullable String indentation;

    protected TriggerItem() {
    }

    protected @Nullable TriggerItem walk(SkriptEvent event) {
        if (run(event)) {
            debug(event, true);
            return next;
        }
        debug(event, false);
        TriggerSection parentSection = parent;
        return parentSection == null ? null : parentSection.getNext();
    }

    protected abstract boolean run(SkriptEvent event);

    public static boolean walk(TriggerItem start, SkriptEvent event) {
        TriggerItem current = start;
        while (current != null) {
            current = current.walk(event);
        }
        return true;
    }

    protected @Nullable ExecutionIntent executionIntent() {
        return null;
    }

    public String getIndentation() {
        if (indentation == null) {
            int level = 0;
            TriggerItem current = this;
            while ((current = current.parent) != null) {
                level++;
            }
            indentation = "  ".repeat(level);
        }
        return indentation;
    }

    protected final void debug(SkriptEvent event, boolean run) {
        if (!Skript.debug()) {
            return;
        }
        String prefix = run ? "" : "-";
        Skript.debug(getIndentation() + prefix + toString(event, true));
    }

    public TriggerItem setParent(@Nullable TriggerSection parent) {
        this.parent = parent;
        this.indentation = null;
        return this;
    }

    public final @Nullable TriggerSection getParent() {
        return parent;
    }

    public final @Nullable Trigger getTrigger() {
        TriggerItem current = this;
        while (current != null && !(current instanceof Trigger)) {
            current = current.getParent();
        }
        return (Trigger) current;
    }

    public TriggerItem setNext(@Nullable TriggerItem next) {
        this.next = next;
        return this;
    }

    public @Nullable TriggerItem getNext() {
        return next;
    }

    public @Nullable TriggerItem getActualNext() {
        return next;
    }

    @Override
    public final String toString() {
        return toString(null, false);
    }
}
