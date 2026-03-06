package ch.njol.skript.lang;

import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.util.Utils;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Function;
import java.util.function.Predicate;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.lang.event.SkriptEvent;

/**
 * A list of expressions.
 */
public class ExpressionList<T> implements Expression<T> {

    protected final Expression<? extends T>[] expressions;
    private final Class<T> returnType;
    private final Class<?>[] possibleReturnTypes;
    protected boolean and;
    private final boolean single;
    private final @Nullable ExpressionList<?> source;

    public ExpressionList(Expression<? extends T>[] expressions, Class<T> returnType, boolean and) {
        this(expressions, returnType, new Class[]{returnType}, and, null);
    }

    public ExpressionList(Expression<? extends T>[] expressions, Class<T> returnType, Class<?>[] possibleReturnTypes, boolean and) {
        this(expressions, returnType, possibleReturnTypes, and, null);
    }

    protected ExpressionList(Expression<? extends T>[] expressions, Class<T> returnType, boolean and, @Nullable ExpressionList<?> source) {
        this(expressions, returnType, new Class[]{returnType}, and, source);
    }

    protected ExpressionList(
            Expression<? extends T>[] expressions,
            Class<T> returnType,
            Class<?>[] possibleReturnTypes,
            boolean and,
            @Nullable ExpressionList<?> source
    ) {
        this.expressions = expressions == null ? emptyExpressions() : expressions;
        this.returnType = returnType;
        this.possibleReturnTypes = uniqueTypes(possibleReturnTypes);
        this.and = and;
        this.single = !and && Arrays.stream(this.expressions).allMatch(Expression::isSingle);
        this.source = source;
    }

    @SuppressWarnings("unchecked")
    private Expression<? extends T>[] emptyExpressions() {
        return (Expression<? extends T>[]) new Expression<?>[0];
    }

    private static Class<?>[] uniqueTypes(Class<?>[] types) {
        if (types == null || types.length == 0) {
            return new Class<?>[]{Object.class};
        }
        return Arrays.stream(types).distinct().toArray(Class<?>[]::new);
    }

    private @Nullable Expression<? extends T> selectExpression() {
        if (expressions.length == 0) {
            return null;
        }
        if (expressions.length == 1) {
            return expressions[0];
        }
        int index = ThreadLocalRandom.current().nextInt(expressions.length);
        return expressions[index];
    }

    @Override
    public @Nullable T getSingle(SkriptEvent event) {
        if (!single) {
            throw new UnsupportedOperationException("ExpressionList is not single");
        }
        Expression<? extends T> expression = selectExpression();
        return expression == null ? null : expression.getSingle(event);
    }

    @Override
    public T[] getArray(SkriptEvent event) {
        if (and) {
            return getAll(event);
        }
        Expression<? extends T> expression = selectExpression();
        if (expression == null) {
            @SuppressWarnings("unchecked")
            T[] empty = (T[]) Array.newInstance(returnType, 0);
            return empty;
        }
        return expression.getArray(event);
    }

    @Override
    public T[] getAll(SkriptEvent event) {
        List<T> values = new ArrayList<>();
        for (Expression<? extends T> expression : expressions) {
            values.addAll(Arrays.asList(expression.getAll(event)));
        }
        @SuppressWarnings("unchecked")
        T[] array = values.toArray((T[]) Array.newInstance(returnType, values.size()));
        return array;
    }

    @Override
    public @Nullable Iterator<? extends T> iterator(SkriptEvent event) {
        if (!and) {
            Expression<? extends T> expression = selectExpression();
            return expression == null ? null : expression.iterator(event);
        }
        return new Iterator<>() {
            private int index = 0;
            private @Nullable Iterator<? extends T> current;

            @Override
            public boolean hasNext() {
                Iterator<? extends T> iterator = current;
                while (index < expressions.length && (iterator == null || !iterator.hasNext())) {
                    iterator = expressions[index++].iterator(event);
                    current = iterator;
                }
                return iterator != null && iterator.hasNext();
            }

            @Override
            public T next() {
                if (!hasNext()) {
                    throw new NoSuchElementException();
                }
                assert current != null;
                return current.next();
            }
        };
    }

    @Override
    public boolean isSingle() {
        return single;
    }

    @Override
    public boolean check(SkriptEvent event, Predicate<? super T> checker, boolean negated) {
        if (and) {
            for (Expression<? extends T> expression : expressions) {
                if (expression.check(event, checker) ^ negated) {
                    return true;
                }
            }
            return false;
        }

        Expression<? extends T> expression = selectExpression();
        if (expression == null) {
            return false;
        }
        return expression.check(event, checker) ^ negated;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <R> @Nullable Expression<? extends R> getConvertedExpression(Class<R>... to) {
        if (to == null || to.length == 0) {
            return null;
        }

        Expression<? extends R>[] converted = new Expression[expressions.length];
        List<Class<?>> returnedTypes = new ArrayList<>();
        for (int i = 0; i < expressions.length; i++) {
            Expression<? extends R> convertedExpression = expressions[i].getConvertedExpression(to);
            if (convertedExpression == null) {
                return null;
            }
            converted[i] = convertedExpression;
            returnedTypes.addAll(Arrays.asList(convertedExpression.possibleReturnTypes()));
        }

        Class<?>[] possibleTypes = returnedTypes.toArray(Class<?>[]::new);
        Class<R> superType = (Class<R>) Utils.getSuperType(possibleTypes);
        if (superType == Object.class && to.length == 1 && to[0] != Object.class) {
            superType = to[0];
        }

        return new ExpressionList<>(converted, superType, possibleTypes, and, this);
    }

    @Override
    public Class<T> getReturnType() {
        return returnType;
    }

    @Override
    @SuppressWarnings("unchecked")
    public Class<? extends T>[] possibleReturnTypes() {
        return (Class<? extends T>[]) possibleReturnTypes;
    }

    @Override
    public boolean getAnd() {
        return and;
    }

    public void invertAnd() {
        and = !and;
    }

    @Override
    public Class<?> @Nullable [] acceptChange(ChangeMode mode) {
        Class<?>[] accepted = null;
        for (Expression<? extends T> expression : expressions) {
            Class<?>[] candidate = expression.acceptChange(mode);
            if (candidate == null) {
                return null;
            }
            if (accepted == null) {
                accepted = Arrays.copyOf(candidate, candidate.length);
                continue;
            }
            accepted = intersectAcceptedTypes(accepted, candidate);
            if (accepted.length == 0) {
                return accepted;
            }
        }
        return accepted == null ? new Class<?>[0] : accepted;
    }

    private static Class<?>[] intersectAcceptedTypes(Class<?>[] first, Class<?>[] second) {
        List<Class<?>> result = new ArrayList<>();
        for (Class<?> a : first) {
            for (Class<?> b : second) {
                if (a.isAssignableFrom(b)) {
                    result.add(b);
                } else if (b.isAssignableFrom(a)) {
                    result.add(a);
                }
            }
        }
        return result.stream().distinct().toArray(Class<?>[]::new);
    }

    @Override
    public void change(SkriptEvent event, Object @Nullable [] delta, ChangeMode mode) {
        if (expressions.length == 0) {
            return;
        }
        if (and) {
            for (Expression<? extends T> expression : expressions) {
                expression.change(event, delta, mode);
            }
            return;
        }
        Expression<? extends T> expression = selectExpression();
        if (expression != null) {
            expression.change(event, delta, mode);
        }
    }

    @Override
    @SuppressWarnings({"unchecked", "rawtypes"})
    public <R> void changeInPlace(SkriptEvent event, Function<T, R> changeFunction, boolean getAll) {
        if (and || getAll) {
            for (Expression<? extends T> expression : expressions) {
                expression.changeInPlace(event, (Function) changeFunction, getAll);
            }
            return;
        }
        Expression<? extends T> expression = selectExpression();
        if (expression != null) {
            expression.changeInPlace(event, (Function) changeFunction, false);
        }
    }

    private int time;

    @Override
    public boolean setTime(int time) {
        boolean accepted = false;
        for (Expression<? extends T> expression : expressions) {
            accepted |= expression.setTime(time);
        }
        if (accepted) {
            this.time = time;
        }
        return accepted;
    }

    @Override
    public int getTime() {
        return time;
    }

    @Override
    public boolean isDefault() {
        return false;
    }

    @Override
    public boolean isLoopOf(String input) {
        for (Expression<? extends T> expression : expressions) {
            if (expression.isLoopOf(input)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public Expression<?> getSource() {
        return source == null ? this : source;
    }

    @Override
    public String toString(@Nullable SkriptEvent event, boolean debug) {
        StringBuilder builder = new StringBuilder("(");
        for (int i = 0; i < expressions.length; i++) {
            if (i > 0) {
                builder.append(i == expressions.length - 1 ? (and ? " and " : " or ") : ", ");
            }
            builder.append(expressions[i].toString(event, debug));
        }
        builder.append(")");
        if (debug) {
            builder.append("[").append(returnType.getName()).append("]");
        }
        return builder.toString();
    }

    public Expression<? extends T>[] getExpressions() {
        return expressions;
    }

    public List<Expression<? extends T>> getAllExpressions() {
        List<Expression<? extends T>> all = new ArrayList<>();
        for (Expression<? extends T> expression : expressions) {
            if (expression instanceof ExpressionList<? extends T> nested) {
                all.addAll(nested.getAllExpressions());
            } else {
                all.add(expression);
            }
        }
        return all;
    }

    @Override
    @SuppressWarnings("unchecked")
    public Expression<T> simplify() {
        boolean allLiteral = true;
        for (int i = 0; i < expressions.length; i++) {
            expressions[i] = expressions[i].simplify();
            allLiteral &= expressions[i] instanceof Literal;
        }
        if (allLiteral) {
            Literal<? extends T>[] literals = Arrays.copyOf(expressions, expressions.length, Literal[].class);
            return new LiteralList<>(literals, returnType, and);
        }
        return this;
    }
}
