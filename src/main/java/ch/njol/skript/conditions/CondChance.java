package ch.njol.skript.conditions;

import ch.njol.skript.Skript;
import ch.njol.skript.lang.Condition;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Kleenean;
import java.util.concurrent.ThreadLocalRandom;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.lang.event.SkriptEvent;

public class CondChance extends Condition {

    static {
        Skript.registerCondition(CondChance.class, "chance of %number%(1:\\%|) [fail:(fails|failed)]");
    }

    private Expression<Number> chance;
    private boolean percent;

    @Override
    @SuppressWarnings("unchecked")
    public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
        chance = (Expression<Number>) exprs[0];
        percent = parseResult.mark == 1;
        setNegated(parseResult.hasTag("fail"));
        return true;
    }

    @Override
    public boolean check(SkriptEvent event) {
        Number value = chance.getSingle(event);
        if (value == null) {
            return false;
        }
        double normalized = percent ? value.doubleValue() / 100D : value.doubleValue();
        boolean result = ThreadLocalRandom.current().nextDouble() < normalized;
        return result != isNegated();
    }

    @Override
    public String toString(@Nullable SkriptEvent event, boolean debug) {
        String rendered = "chance of " + chance.toString(event, debug) + (percent ? "%" : "");
        if (isNegated()) {
            rendered += " failed";
        }
        return rendered;
    }
}
