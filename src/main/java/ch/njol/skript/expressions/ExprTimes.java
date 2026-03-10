package ch.njol.skript.expressions;

import ch.njol.skript.Skript;
import ch.njol.skript.config.Node;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.skript.lang.util.SimpleLiteral;
import ch.njol.skript.log.SkriptLogger;
import ch.njol.util.Kleenean;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.stream.LongStream;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.lang.event.SkriptEvent;

public class ExprTimes extends SimpleExpression<Long> {

    private Expression<Number> end;

    @Override
    @SuppressWarnings("unchecked")
    public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
        end = matchedPattern == 0 ? (Expression<Number>) exprs[0] : new SimpleLiteral<>((long) matchedPattern, false);

        if (end instanceof Literal<?> literal) {
            Number value = ((Literal<Number>) literal).getSingle(SkriptEvent.EMPTY);
            int amount = value == null ? 0 : value.intValue();
            if (amount == 0 && isInLoop()) {
                Skript.warning("Looping zero times makes the code inside of the loop useless");
            } else if (amount == 1 && isInLoop()) {
                Skript.warning("Since you're looping exactly one time, you could simply remove the loop instead");
            } else if (amount < 0) {
                if (isInLoop()) {
                    Skript.error("Looping a negative amount of times is impossible");
                } else {
                    Skript.error("The times expression only supports positive numbers");
                }
                return false;
            }
        }
        return true;
    }

    private boolean isInLoop() {
        Node node = SkriptLogger.getNode();
        if (node == null) {
            return false;
        }
        String key = node.getKey();
        return key != null && key.startsWith("loop ");
    }

    @Override
    protected Long @Nullable [] get(SkriptEvent event) {
        Iterator<? extends Long> iterator = iterator(event);
        if (iterator == null) {
            return null;
        }
        List<Long> values = new ArrayList<>();
        iterator.forEachRemaining(values::add);
        return values.toArray(Long[]::new);
    }

    @Override
    public @Nullable Iterator<? extends Long> iterator(SkriptEvent event) {
        Number endValue = end.getSingle(event);
        if (endValue == null) {
            return null;
        }
        long fixed = (long) (endValue.doubleValue() + Skript.EPSILON);
        return LongStream.range(1, fixed + 1).boxed().iterator();
    }

    @Override
    public boolean isSingle() {
        return false;
    }

    @Override
    public Class<? extends Long> getReturnType() {
        return Long.class;
    }

    @Override
    public Expression<? extends Long> simplify() {
        return this;
    }

    @Override
    public String toString(@Nullable SkriptEvent event, boolean debug) {
        return end.toString(event, debug) + " times";
    }
}
