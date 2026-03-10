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

@Name("Last Caught Errors")
@Description("Gets the last caught runtime errors from a 'catch runtime errors' section.")
@Example("""
	catch runtime errors:
		set worldborder center of {_border} to location(0, 0, NaN value)
	if last caught runtime errors contains "Your location can't have a NaN value as one of its components":
		set worldborder center of {_border} to location(0, 0, 0)
	""")
@Since("2.12")
public class ExprCaughtErrors extends SimpleExpression<String> {

    static {
        Skript.registerExpression(ExprCaughtErrors.class, String.class,
                "[the] last caught [run[ ]time] errors");
    }

    public static String[] lastErrors;

    @Override
    public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
        return true;
    }

    @Override
    protected String @Nullable [] get(SkriptEvent event) {
        return lastErrors;
    }

    @Override
    public boolean isSingle() {
        return false;
    }

    @Override
    public Class<? extends String> getReturnType() {
        return String.class;
    }

    @Override
    public String toString(@Nullable SkriptEvent event, boolean debug) {
        return "last caught runtime errors";
    }
}
