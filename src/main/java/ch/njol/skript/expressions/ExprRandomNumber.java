package ch.njol.skript.expressions;

import ch.njol.skript.Skript;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.util.Kleenean;
import java.util.Arrays;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.lang.event.SkriptEvent;

public class ExprRandomNumber extends SimpleExpression<Number> {

    static {
        Skript.registerExpression(ExprRandomNumber.class, Number.class,
                "[a|%-integer%] random (:integer|number)[s] (from|between) %number% (to|and) %number%");
    }

    private @Nullable Expression<Integer> amount;
    private Expression<Number> lower;
    private Expression<Number> upper;
    private boolean integer;

    @Override
    @SuppressWarnings("unchecked")
    public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
        amount = (Expression<Integer>) exprs[0];
        lower = (Expression<Number>) exprs[1];
        upper = (Expression<Number>) exprs[2];
        integer = parseResult.hasTag("integer");
        return true;
    }

    @Override
    protected Number @Nullable [] get(SkriptEvent event) {
        Number lowerNumber = lower.getSingle(event);
        Number upperNumber = upper.getSingle(event);
        if (lowerNumber == null || upperNumber == null) {
            return new Number[0];
        }

        double lowerValue = lowerNumber.doubleValue();
        double upperValue = upperNumber.doubleValue();
        if (!Double.isFinite(lowerValue) || !Double.isFinite(upperValue)) {
            return new Number[0];
        }

        Integer requestedAmount = amount == null ? 1 : amount.getSingle(event);
        if (requestedAmount == null || requestedAmount <= 0) {
            return new Number[0];
        }

        double min = Math.min(lowerValue, upperValue);
        double max = Math.max(lowerValue, upperValue);
        Random random = ThreadLocalRandom.current();
        if (integer) {
            return randomIntegers(requestedAmount, min, max, random);
        }
        return randomDoubles(requestedAmount, min, max, random);
    }

    private Number[] randomIntegers(int amount, double lower, double upper, Random random) {
        long flooredUpper = (long) Math.floor(upper);
        long ceiledLower = (long) Math.ceil(lower);

        if (upper - lower < 1 && ceiledLower - flooredUpper <= 1) {
            if (flooredUpper == ceiledLower || lower == ceiledLower) {
                Long[] values = new Long[amount];
                Arrays.fill(values, ceiledLower);
                return values;
            }
            if (upper == flooredUpper) {
                Long[] values = new Long[amount];
                Arrays.fill(values, flooredUpper);
                return values;
            }
            return new Number[0];
        }

        long bound = flooredUpper - ceiledLower + 1;
        Long[] values = new Long[amount];
        for (int i = 0; i < amount; i++) {
            values[i] = ceiledLower + Math.floorMod(random.nextLong(), bound);
        }
        return values;
    }

    private Number[] randomDoubles(int amount, double lower, double upper, Random random) {
        Double[] values = new Double[amount];
        for (int i = 0; i < amount; i++) {
            values[i] = Math.min(lower + random.nextDouble() * (upper - lower), upper);
        }
        return values;
    }

    @Override
    @SuppressWarnings("unchecked")
    public boolean isSingle() {
        if (amount instanceof Literal<?> literal) {
            Integer literalAmount = ((Literal<Integer>) literal).getSingle(SkriptEvent.EMPTY);
            return literalAmount != null && literalAmount == 1;
        }
        return amount == null;
    }

    @Override
    public Class<? extends Number> getReturnType() {
        return integer ? Long.class : Double.class;
    }

    @Override
    public String toString(@Nullable SkriptEvent event, boolean debug) {
        return (amount == null ? "a" : amount.toString(event, debug))
                + " random "
                + (integer ? "integer" : "number")
                + (amount == null ? "" : "s")
                + " between "
                + lower.toString(event, debug)
                + " and "
                + upper.toString(event, debug);
    }
}
