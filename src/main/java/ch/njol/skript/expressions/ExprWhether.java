package ch.njol.skript.expressions;

import ch.njol.skript.Skript;
import ch.njol.skript.lang.Condition;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.util.Kleenean;
import java.util.regex.Matcher;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnknownNullability;
import org.skriptlang.skript.lang.event.SkriptEvent;

public class ExprWhether extends SimpleExpression<Boolean> {

    static {
        Skript.registerExpression(ExprWhether.class, Boolean.class,
                "whether <.+>");
    }

    private @UnknownNullability Condition condition;

    @Override
    public boolean init(Expression<?>[] expressions, int pattern, Kleenean delayed, ParseResult result) {
        if (result.regexes.isEmpty()) {
            return false;
        }
        Matcher matcher = result.regexes.get(0);
        String input = matcher.group();
        condition = Condition.parse(input, "Can't understand this condition: " + input);
        return condition != null;
    }

    @Override
    protected Boolean[] get(SkriptEvent event) {
        return new Boolean[]{condition.check(event)};
    }

    @Override
    public Class<? extends Boolean> getReturnType() {
        return Boolean.class;
    }

    @Override
    public boolean isSingle() {
        return true;
    }

    @Override
    public String toString(@Nullable SkriptEvent event, boolean debug) {
        return "whether " + condition.toString(event, debug);
    }
}
