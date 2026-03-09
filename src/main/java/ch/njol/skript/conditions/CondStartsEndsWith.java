package ch.njol.skript.conditions;

import ch.njol.skript.Skript;
import ch.njol.skript.lang.Condition;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Kleenean;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.lang.event.SkriptEvent;

public class CondStartsEndsWith extends Condition {

    static {
        Skript.registerCondition(CondStartsEndsWith.class,
                "%strings% (start|1¦end)[s] with %strings%",
                "%strings% (doesn't|does not|do not|don't) (start|1¦end) with %strings%");
    }

    private Expression<String> strings;
    private Expression<String> affix;
    private boolean usingEnds;

    @Override
    @SuppressWarnings("unchecked")
    public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
        strings = (Expression<String>) exprs[0];
        affix = (Expression<String>) exprs[1];
        usingEnds = parseResult.mark == 1;
        setNegated(matchedPattern == 1);
        return true;
    }

    @Override
    public boolean check(SkriptEvent event) {
        String[] affixes = affix.getAll(event);
        if (affixes.length == 0) {
            return false;
        }

        return strings.check(event, value -> {
            if (affix.getAnd()) {
                for (String candidate : affixes) {
                    if (!(usingEnds ? value.endsWith(candidate) : value.startsWith(candidate))) {
                        return false;
                    }
                }
                return true;
            }
            for (String candidate : affixes) {
                if (usingEnds ? value.endsWith(candidate) : value.startsWith(candidate)) {
                    return true;
                }
            }
            return false;
        }, isNegated());
    }

    @Override
    public String toString(@Nullable SkriptEvent event, boolean debug) {
        if (isNegated()) {
            return strings.toString(event, debug) + " doesn't " + (usingEnds ? "end" : "start") + " with " + affix.toString(event, debug);
        }
        return strings.toString(event, debug) + (usingEnds ? " ends" : " starts") + " with " + affix.toString(event, debug);
    }
}
