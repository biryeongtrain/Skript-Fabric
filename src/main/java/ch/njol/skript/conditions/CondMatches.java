package ch.njol.skript.conditions;

import ch.njol.skript.Skript;
import ch.njol.skript.lang.Condition;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Kleenean;
import java.util.Arrays;
import java.util.regex.Pattern;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.lang.event.SkriptEvent;

public class CondMatches extends Condition {

    static {
        Skript.registerCondition(CondMatches.class,
                "%strings% (1¦match[es]|2¦do[es](n't| not) match) %strings%",
                "%strings% (1¦partially match[es]|2¦do[es](n't| not) partially match) %strings%");
    }

    private Expression<String> strings;
    private Expression<String> regex;
    private boolean partial;

    @Override
    @SuppressWarnings("unchecked")
    public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
        strings = (Expression<String>) exprs[0];
        regex = (Expression<String>) exprs[1];
        partial = matchedPattern == 1;
        setNegated(parseResult.mark == 2);
        return true;
    }

    @Override
    public boolean check(SkriptEvent event) {
        String[] textValues = strings.getAll(event);
        String[] regexValues = regex.getAll(event);
        if (textValues.length == 0 || regexValues.length == 0) {
            return false;
        }

        boolean stringAnd = strings.getAnd();
        boolean regexAnd = regex.getAnd();
        boolean result;
        if (stringAnd) {
            result = regexAnd
                    ? Arrays.stream(textValues).allMatch(text -> Arrays.stream(regexValues).map(Pattern::compile).allMatch(pattern -> matches(text, pattern)))
                    : Arrays.stream(textValues).allMatch(text -> Arrays.stream(regexValues).map(Pattern::compile).anyMatch(pattern -> matches(text, pattern)));
        } else {
            result = regexAnd
                    ? Arrays.stream(textValues).anyMatch(text -> Arrays.stream(regexValues).map(Pattern::compile).allMatch(pattern -> matches(text, pattern)))
                    : Arrays.stream(textValues).anyMatch(text -> Arrays.stream(regexValues).map(Pattern::compile).anyMatch(pattern -> matches(text, pattern)));
        }
        return result != isNegated();
    }

    private boolean matches(String value, Pattern pattern) {
        return partial ? pattern.matcher(value).find() : pattern.matcher(value).matches();
    }

    @Override
    public String toString(@Nullable SkriptEvent event, boolean debug) {
        return strings.toString(event, debug) + " " + (isNegated() ? "doesn't match " : "matches ") + regex.toString(event, debug);
    }
}
