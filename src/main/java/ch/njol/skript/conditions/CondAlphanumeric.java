package ch.njol.skript.conditions;

import ch.njol.skript.Skript;
import ch.njol.skript.lang.Condition;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Kleenean;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.lang.event.SkriptEvent;

public class CondAlphanumeric extends Condition {

    static {
        Skript.registerCondition(CondAlphanumeric.class,
                "%strings% (is|are) alphanumeric",
                "%strings% (isn't|is not|aren't|are not) alphanumeric");
    }

    private Expression<String> strings;

    @Override
    @SuppressWarnings("unchecked")
    public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
        strings = (Expression<String>) exprs[0];
        setNegated(matchedPattern == 1);
        return true;
    }

    @Override
    public boolean check(SkriptEvent event) {
        return isNegated() ^ strings.check(event, StringUtils::isAlphanumeric);
    }

    @Override
    public String toString(@Nullable SkriptEvent event, boolean debug) {
        return strings.toString(event, debug) + " is" + (isNegated() ? "n't" : "") + " alphanumeric";
    }
}
