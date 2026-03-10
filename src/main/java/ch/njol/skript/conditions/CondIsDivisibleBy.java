package ch.njol.skript.conditions;

import ch.njol.skript.Skript;
import ch.njol.skript.config.Node;
import ch.njol.skript.lang.Condition;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.parser.ParserInstance;
import ch.njol.util.Kleenean;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.lang.event.SkriptEvent;

public class CondIsDivisibleBy extends Condition {

    static {
        Skript.registerCondition(CondIsDivisibleBy.class,
                "%numbers% (is|are) evenly divisible by %number% [with [a] tolerance [of] %-number%]",
                "%numbers% (isn't|is not|aren't|are not) evenly divisible by %number% [with [a] tolerance [of] %-number%]",
                "%numbers% can be evenly divided by %number% [with [a] tolerance [of] %-number%]",
                "%numbers% (can't|can[ ]not) be evenly divided by %number% [with [a] tolerance [of] %-number%]");
    }

    private Expression<Number> dividend;
    private Expression<Number> divisor;
    private @Nullable Expression<Number> epsilon;
    private @Nullable Node node;

    @Override
    @SuppressWarnings("unchecked")
    public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
        dividend = (Expression<Number>) exprs[0];
        divisor = (Expression<Number>) exprs[1];
        epsilon = (Expression<Number>) exprs[2];
        setNegated(matchedPattern == 1 || matchedPattern == 3);
        node = ParserInstance.get().getNode();
        return true;
    }

    @Override
    public boolean check(SkriptEvent event) {
        Number divisorNumber = divisor.getSingle(event);
        if (divisorNumber == null) {
            return isNegated();
        }
        double divisorValue = divisorNumber.doubleValue();
        if (divisorValue == 0) {
            return isNegated();
        }

        Number epsilonNumber = epsilon != null ? epsilon.getSingle(event) : Skript.EPSILON;
        if (epsilonNumber == null) {
            epsilonNumber = Skript.EPSILON;
        }
        double epsilonValue = epsilonNumber.doubleValue();
        if (epsilonValue <= 0 || Double.isNaN(epsilonValue)) {
            Skript.error("Tolerance must be a positive, non-zero number, but was " + epsilonNumber + ".");
            return isNegated();
        }
        if (divisorValue < epsilonValue) {
            return isNegated();
        }

        double divisor = divisorValue;
        double epsilon = epsilonValue;
        return dividend.check(event, dividendNumber -> {
            double remainder = Math.abs(dividendNumber.doubleValue() % divisor);
            return remainder <= epsilon || remainder >= divisor - epsilon;
        }, isNegated());
    }

    public @Nullable Node getNode() {
        return node;
    }

    @Override
    public String toString(@Nullable SkriptEvent event, boolean debug) {
        return dividend.toString(event, debug) + " is " + (isNegated() ? "not " : "")
                + "evenly divisible by " + divisor.toString(event, debug);
    }
}
