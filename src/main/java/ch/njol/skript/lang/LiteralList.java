package ch.njol.skript.lang;

import ch.njol.skript.util.Utils;
import org.jetbrains.annotations.Nullable;

/**
 * A list of literals.
 */
public class LiteralList<T> extends ExpressionList<T> implements Literal<T> {

    public LiteralList(Literal<? extends T>[] literals, Class<T> returnType, boolean and) {
        super(literals, returnType, and);
    }

    public LiteralList(Literal<? extends T>[] literals, Class<T> returnType, Class<?>[] possibleReturnTypes, boolean and) {
        super(literals, returnType, possibleReturnTypes, and);
    }

    public LiteralList(Literal<? extends T>[] literals, Class<T> returnType, boolean and, LiteralList<?> source) {
        super(literals, returnType, and, source);
    }

    public LiteralList(
            Literal<? extends T>[] literals,
            Class<T> returnType,
            Class<?>[] possibleReturnTypes,
            boolean and,
            LiteralList<?> source
    ) {
        super(literals, returnType, possibleReturnTypes, and, source);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <R> @Nullable Literal<? extends R> getConvertedExpression(Class<R>... to) {
        Literal<? extends R>[] converted = new Literal[expressions.length];
        Class<?>[] returnTypes = new Class<?>[expressions.length];

        for (int i = 0; i < expressions.length; i++) {
            Expression<? extends R> expression = expressions[i].getConvertedExpression(to);
            if (!(expression instanceof Literal<? extends R> literal)) {
                return null;
            }
            converted[i] = literal;
            returnTypes[i] = literal.getReturnType();
        }

        Class<R> superType = (Class<R>) Utils.getSuperType(returnTypes);
        if (superType == Object.class && to != null && to.length == 1 && to[0] != Object.class) {
            superType = to[0];
        }
        return new LiteralList<>(converted, superType, returnTypes, and, this);
    }

    @Override
    @SuppressWarnings("unchecked")
    public Literal<? extends T>[] getExpressions() {
        return (Literal<? extends T>[]) super.getExpressions();
    }

    @Override
    public Expression<T> simplify() {
        return this;
    }
}
