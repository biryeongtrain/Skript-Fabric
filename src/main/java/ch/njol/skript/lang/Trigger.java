package ch.njol.skript.lang;

import java.util.List;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.lang.script.Script;

public class Trigger extends TriggerSection {

    private final @Nullable Script script;
    private final String name;
    private final ch.njol.skript.lang.SkriptEvent event;
    private int lineNumber = -1;
    private String debugLabel = "unknown trigger";

    public Trigger(@Nullable Script script, String name, ch.njol.skript.lang.SkriptEvent event, List<TriggerItem> items) {
        super(items);
        this.script = script;
        this.name = name;
        this.event = event;
    }

    public boolean execute(org.skriptlang.skript.lang.event.SkriptEvent eventContext) {
        return TriggerItem.walk(this, eventContext);
    }

    @Override
    protected @Nullable TriggerItem walk(org.skriptlang.skript.lang.event.SkriptEvent eventContext) {
        return walk(eventContext, true);
    }

    @Override
    public String toString(@Nullable org.skriptlang.skript.lang.event.SkriptEvent eventContext, boolean debug) {
        return name + " (" + this.event.toString(eventContext, debug) + ")";
    }

    public @Nullable Script getScript() {
        return script;
    }

    public String getName() {
        return name;
    }

    public ch.njol.skript.lang.SkriptEvent getEvent() {
        return event;
    }

    public void setLineNumber(int lineNumber) {
        this.lineNumber = lineNumber;
    }

    public int getLineNumber() {
        return lineNumber;
    }

    public void setDebugLabel(String debugLabel) {
        this.debugLabel = debugLabel;
    }

    public String getDebugLabel() {
        return debugLabel;
    }
}
