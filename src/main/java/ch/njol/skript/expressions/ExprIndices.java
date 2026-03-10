package ch.njol.skript.expressions;

import ch.njol.skript.Skript;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.KeyProviderExpression;
import ch.njol.skript.lang.KeyedValue;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.Variable;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.skript.util.LiteralUtils;
import ch.njol.util.Kleenean;
import java.util.Arrays;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.lang.event.SkriptEvent;

public class ExprIndices extends SimpleExpression<String> {

    static {
        Skript.registerExpression(ExprIndices.class, String.class,
                "[(the|all [[of] the])] (indexes|indices) of %objects%",
                "%objects%'[s] (indexes|indices)",
                "[sorted] (indices|indexes) of %objects% in (ascending|1¦descending) order",
                "[sorted] %objects%'[s] (indices|indexes) in (ascending|1¦descending) order");
    }

    private KeyProviderExpression<?> keyedExpression;
    private boolean sort;
    private boolean descending;
    private boolean recursive;

    @Override
    public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
        sort = matchedPattern > 1;
        descending = parseResult.mark == 1;

        Expression<?> expression = LiteralUtils.defendExpression(exprs[0]);
        if (!KeyProviderExpression.canReturnKeys(expression)) {
            Skript.error("The indices expression may only be used with keyed expressions");
            return false;
        }

        keyedExpression = (KeyProviderExpression<?>) expression;
        recursive = expression.returnsNestedStructures();
        if (!sort) {
            expression.returnNestedStructures(true);
        }
        return LiteralUtils.canInitSafely(expression);
    }

    @Override
    protected String @Nullable [] get(SkriptEvent event) {
        Object[] values = keyedExpression.getArray(event);
        String[] keys = keyedExpression.getArrayKeys(event);
        if (sort) {
            try {
                int direction = descending ? -1 : 1;
                return Arrays.stream(KeyedValue.zip(values, keys))
                        .sorted((left, right) -> ExprSortedList.compare(left.value(), right.value()) * direction)
                        .map(KeyedValue::key)
                        .toArray(String[]::new);
            } catch (IllegalArgumentException | ClassCastException ignored) {
                return keys;
            }
        }

        if (recursive) {
            return keys;
        }

        for (int index = 0; index < keys.length; index++) {
            int separator = keys[index].indexOf(Variable.SEPARATOR);
            if (separator != -1) {
                keys[index] = keys[index].substring(0, separator);
            }
        }
        return Arrays.stream(keys).distinct().toArray(String[]::new);
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
    public boolean returnNestedStructures(boolean nested) {
        return keyedExpression.returnNestedStructures(nested);
    }

    @Override
    public boolean returnsNestedStructures() {
        return keyedExpression.returnsNestedStructures();
    }

    @Override
    public String toString(@Nullable SkriptEvent event, boolean debug) {
        String text = "indices of " + keyedExpression.toString(event, debug);
        if (sort) {
            text = "sorted " + text + " in " + (descending ? "descending" : "ascending") + " order";
        }
        return text;
    }
}
