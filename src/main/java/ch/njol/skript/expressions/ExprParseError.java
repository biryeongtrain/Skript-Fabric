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

@Name("Parse Error")
@Description("The error which caused the last parse operation to fail.")
@Example("""
        set {var} to line 1 parsed as integer
        if {var} is not set:
            if parse error is set:
                message "%parse error%"
        """)
@Since("2.0, Fabric")
public class ExprParseError extends SimpleExpression<String> {

    static {
        Skript.registerExpression(ExprParseError.class, String.class,
                "[the] [last] [parse] error");
    }

    @Override
    public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
        return true;
    }

    @Override
    protected String @Nullable [] get(SkriptEvent event) {
        return ExprParse.lastError == null ? new String[0] : new String[]{ExprParse.lastError};
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
        return "the last parse error";
    }
}
