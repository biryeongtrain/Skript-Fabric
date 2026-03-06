package ch.njol.skript.lang.simplification;

import ch.njol.skript.classes.Changer;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.util.ContextlessEvent;
import ch.njol.skript.lang.util.SimpleLiteral;
import java.util.function.Function;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.lang.event.SkriptEvent;

/**
 * Literal created by simplifying an expression while retaining source expression metadata and changers.
 */
public class SimplifiedLiteral<T> extends SimpleLiteral<T> {

    public static <T> SimplifiedLiteral<T> fromExpression(Expression<T> original) {
        if (original instanceof SimplifiedLiteral<T> literal) {
            return literal;
        }

        T[] values = original.getAll(ContextlessEvent.get());
        @SuppressWarnings("unchecked")
        Class<T> type = values.length == 0
                ? (Class<T>) original.getReturnType()
                : (Class<T>) values.getClass().getComponentType();

        return new SimplifiedLiteral<>(values, type, original.getAnd(), original);
    }

    public SimplifiedLiteral(T[] data, Class<T> type, boolean and, Expression<T> source) {
        super(data, type, and, source);
    }

    @Override
    public Class<?> @Nullable [] acceptChange(Changer.ChangeMode mode) {
        return source.acceptChange(mode);
    }

    @Override
    public Object @Nullable [] beforeChange(Expression<?> changed, Object @Nullable [] delta) {
        return source.beforeChange(changed, delta);
    }

    @Override
    public void change(SkriptEvent event, Object @Nullable [] delta, Changer.ChangeMode mode) throws UnsupportedOperationException {
        source.change(event, delta, mode);
    }

    @Override
    public boolean isLoopOf(String input) {
        return source.isLoopOf(input);
    }

    @Override
    public <R> void changeInPlace(SkriptEvent event, Function<T, R> changeFunction) {
        getSource().changeInPlace(event, changeFunction);
    }

    @Override
    public <R> void changeInPlace(SkriptEvent event, Function<T, R> changeFunction, boolean getAll) {
        getSource().changeInPlace(event, changeFunction, getAll);
    }

    @Override
    @SuppressWarnings("unchecked")
    public Expression<T> getSource() {
        return (Expression<T>) source;
    }

    @Override
    public String toString(@Nullable SkriptEvent event, boolean debug) {
        if (debug) {
            return "[" + source.toString(event, true) + " (SIMPLIFIED)]";
        }
        return source.toString(event, false);
    }
}
