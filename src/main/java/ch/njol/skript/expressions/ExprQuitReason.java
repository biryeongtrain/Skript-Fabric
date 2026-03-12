package ch.njol.skript.expressions;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.util.Kleenean;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.lang.event.SkriptEvent;

@Name("Quit Reason")
@Description("The quit or disconnect reason exposed by a quit-style event handle.")
@Example("quit reason is \"kicked\"")
@Since("2.8.0")
public final class ExprQuitReason extends SimpleExpression<String> {

    static {
        Skript.registerExpression(ExprQuitReason.class, String.class, "(quit|disconnect) (cause|reason)");
    }

    @Override
    public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
        if (!ReflectiveHandleAccess.currentEventSupports("reason", "getReason", "cause", "getCause")) {
            Skript.error("The 'quit reason' expression may only be used in a quit event.");
            return false;
        }
        return expressions.length == 0;
    }

    @Override
    protected String @Nullable [] get(SkriptEvent event) {
        Object value = ReflectiveHandleAccess.invokeNoArg(event.handle(), "reason", "getReason", "cause", "getCause");
        return value == null ? null : new String[]{String.valueOf(value)};
    }

    @Override
    public boolean isSingle() {
        return true;
    }

    @Override
    public Class<? extends String> getReturnType() {
        return String.class;
    }

    @Override
    public String toString(@Nullable SkriptEvent event, boolean debug) {
        return "quit reason";
    }
}
