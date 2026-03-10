package ch.njol.skript.lang;

import ch.njol.skript.Skript;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.lang.event.CurrentSkriptEvent;
import org.skriptlang.skript.lang.event.SkriptEvent;

public abstract class TriggerItem implements Debuggable {

    private static final ThreadLocal<@Nullable Throwable> LAST_EXECUTION_FAILURE = new ThreadLocal<>();

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
        ExecutionIntent intent = executionIntent();
        if (intent instanceof ExecutionIntent.StopTrigger) {
            exitSections(event, parent);
            return null;
        }
        if (intent instanceof ExecutionIntent.StopSections stopSections) {
            return exitSections(event, parent, stopSections.levels());
        }
        TriggerSection parentSection = parent;
        return parentSection == null ? null : parentSection.getNext();
    }

    protected abstract boolean run(SkriptEvent event);

    public static boolean walk(TriggerItem start, SkriptEvent event) {
        LAST_EXECUTION_FAILURE.remove();
        try {
            return CurrentSkriptEvent.with(event, () -> {
                TriggerItem current = start;
                while (current != null) {
                    current = current.walk(event);
                }
                return true;
            });
        } catch (StackOverflowError err) {
            LAST_EXECUTION_FAILURE.set(err);
            if (Skript.debug()) {
                err.printStackTrace();
            }
            return false;
        } catch (Exception ex) {
            LAST_EXECUTION_FAILURE.set(ex);
            if (Skript.debug()) {
                ex.printStackTrace();
            }
            return false;
        } catch (Throwable throwable) {
            throw throwable;
        }
    }

    public static @Nullable Throwable consumeExecutionFailure() {
        Throwable failure = LAST_EXECUTION_FAILURE.get();
        LAST_EXECUTION_FAILURE.remove();
        return failure;
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

    private void exitSections(SkriptEvent event, @Nullable TriggerSection section) {
        TriggerSection current = section;
        while (current != null) {
            exitSection(event, current);
            current = current.getParent();
        }
    }

    private @Nullable TriggerItem exitSections(SkriptEvent event, @Nullable TriggerSection section, int levels) {
        TriggerSection current = section;
        int remaining = levels;
        while (current != null && remaining > 1) {
            exitSection(event, current);
            current = current.getParent();
            remaining--;
        }
        if (current == null) {
            return null;
        }
        exitSection(event, current);
        return current.getNext();
    }

    private void exitSection(SkriptEvent event, TriggerSection section) {
        if (section instanceof SectionExitHandler handler) {
            handler.exit(event);
        }
    }

    @Override
    public final String toString() {
        return toString(null, false);
    }
}
