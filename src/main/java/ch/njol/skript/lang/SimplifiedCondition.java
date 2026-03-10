package ch.njol.skript.lang;

import ch.njol.skript.Skript;
import ch.njol.skript.lang.parser.ParserInstance;
import ch.njol.skript.lang.util.ContextlessEvent;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.lang.event.SkriptEvent;
import org.skriptlang.skript.lang.script.Script;
import org.skriptlang.skript.lang.script.ScriptWarning;

/**
 * Represents a condition that can be simplified during initialization.
 */
public class SimplifiedCondition extends Condition {

    public static Condition fromCondition(Condition original) {
        if (original instanceof SimplifiedCondition simplifiedCondition) {
            return simplifiedCondition;
        }

        SkriptEvent event = ContextlessEvent.get();
        boolean result = original.check(event);

        ParserInstance parser = ParserInstance.get();
        Script script = parser.isActive() ? parser.getCurrentScript() : null;
        if (script != null && !script.suppressesWarning(ScriptWarning.CONSTANT_CONDITION)) {
            Skript.warning("The condition '" + original.toString(event, Skript.debug()) + "' will always be " + (result ? "true" : "false") + ".");
        }

        return new SimplifiedCondition(original, result);
    }

    private final Condition source;
    private final boolean result;

    private SimplifiedCondition(Condition source, boolean result) {
        this.source = source;
        this.result = result;
    }

    public Condition getSource() {
        return source;
    }

    public boolean getResult() {
        return result;
    }

    @Override
    public boolean check(SkriptEvent event) {
        return result;
    }

    @Override
    public String toString(@Nullable SkriptEvent event, boolean debug) {
        return source.toString(event, debug);
    }
}
