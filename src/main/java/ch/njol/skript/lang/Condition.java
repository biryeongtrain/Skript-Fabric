package ch.njol.skript.lang;

import ch.njol.skript.Skript;
import java.util.Iterator;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.lang.event.SkriptEvent;
import org.skriptlang.skript.registration.SyntaxRegistry;

public abstract class Condition extends Statement {

    private boolean negated;

    public abstract boolean check(SkriptEvent event);

    @Override
    protected final boolean run(SkriptEvent event) {
        return check(event);
    }

    public boolean isNegated() {
        return negated;
    }

    protected final void setNegated(boolean negated) {
        this.negated = negated;
    }

    public static @Nullable Condition parse(String input, @Nullable String defaultError) {
        if (input == null || input.isBlank()) {
            return null;
        }
        String expression = input.trim();
        var iterator = Skript.instance().syntaxRegistry().syntaxes(SyntaxRegistry.CONDITION).iterator();
        @SuppressWarnings({"rawtypes", "unchecked"})
        Condition condition = (Condition) SkriptParser.parseModern(
                expression,
                (Iterator) iterator,
                ParseContext.DEFAULT,
                defaultError
        );
        return condition;
    }

    @Override
    public String toString(@Nullable SkriptEvent event, boolean debug) {
        return getClass().getSimpleName();
    }
}
