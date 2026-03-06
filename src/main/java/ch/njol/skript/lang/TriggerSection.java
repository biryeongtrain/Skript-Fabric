package ch.njol.skript.lang;

import ch.njol.skript.ScriptLoader;
import ch.njol.skript.config.SectionNode;
import ch.njol.skript.lang.parser.ParserInstance;
import java.util.ArrayList;
import java.util.List;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.lang.event.SkriptEvent;

public abstract class TriggerSection extends TriggerItem {

    protected @Nullable TriggerItem first;
    protected @Nullable TriggerItem last;

    protected TriggerSection(List<TriggerItem> items) {
        setTriggerItems(items);
    }

    protected TriggerSection(SectionNode node) {
        ParserInstance parser = ParserInstance.get();
        List<TriggerSection> previousSections = parser.getCurrentSections();

        List<TriggerSection> currentSections = new ArrayList<>(previousSections);
        currentSections.add(this);
        parser.setCurrentSections(currentSections);

        try {
            setTriggerItems(ScriptLoader.loadTriggerItems(node));
        } finally {
            parser.setCurrentSections(previousSections);
        }
    }

    protected TriggerSection() {
    }

    protected void setTriggerItems(List<TriggerItem> items) {
        if (items == null || items.isEmpty()) {
            first = null;
            last = null;
            return;
        }

        first = items.getFirst();
        last = items.getLast();
        if (last != null) {
            last.setNext(getNext());
        }

        for (TriggerItem item : items) {
            item.setParent(this);
        }
    }

    @Override
    public TriggerSection setNext(@Nullable TriggerItem next) {
        super.setNext(next);
        if (last != null) {
            last.setNext(next);
        }
        return this;
    }

    @Override
    public TriggerSection setParent(@Nullable TriggerSection parent) {
        super.setParent(parent);
        return this;
    }

    @Override
    protected final boolean run(SkriptEvent event) {
        throw new UnsupportedOperationException();
    }

    @Override
    protected abstract @Nullable TriggerItem walk(SkriptEvent event);

    protected final @Nullable TriggerItem walk(SkriptEvent event, boolean run) {
        debug(event, run);
        if (run && first != null) {
            return first;
        }
        return getNext();
    }

    protected @Nullable ExecutionIntent triggerExecutionIntent() {
        TriggerItem current = first;
        while (current != null) {
            ExecutionIntent intent = current.executionIntent();
            if (intent != null) {
                return intent.use();
            }
            if (current == last) {
                break;
            }
            current = current.getActualNext();
        }
        return null;
    }
}
