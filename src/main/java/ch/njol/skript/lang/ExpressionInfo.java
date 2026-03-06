package ch.njol.skript.lang;

import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.registration.SyntaxInfo;

/**
 * Represents expression metadata used for creating expression instances.
 *
 * @deprecated Use {@link SyntaxInfo.Expression} instead.
 */
@Deprecated(since = "2.14", forRemoval = true)
public class ExpressionInfo<E extends Expression<T>, T> extends SyntaxElementInfo<E> {

    public @Nullable ExpressionType expressionType;
    public Class<T> returnType;

    public ExpressionInfo(String[] patterns, Class<T> returnType, Class<E> expressionClass, String originClassPath) {
        this(patterns, returnType, expressionClass, originClassPath, null);
    }

    public ExpressionInfo(
            String[] patterns,
            Class<T> returnType,
            Class<E> expressionClass,
            String originClassPath,
            @Nullable ExpressionType expressionType
    ) {
        super(patterns, expressionClass, originClassPath);
        this.returnType = returnType;
        this.expressionType = expressionType;
    }

    @ApiStatus.Internal
    protected ExpressionInfo(SyntaxInfo.Expression<E, T> source) {
        super(source);
        this.returnType = source.returnType();
        this.expressionType = ExpressionType.fromModern(source.priority());
    }

    public Class<T> getReturnType() {
        return returnType;
    }

    public @Nullable ExpressionType getExpressionType() {
        return expressionType;
    }
}
