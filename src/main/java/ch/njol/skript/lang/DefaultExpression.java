package ch.njol.skript.lang;

/**
 * Represents an expression that can be used as a default value in a context.
 */
public interface DefaultExpression<T> extends Expression<T> {

    boolean init();

    @Override
    boolean isDefault();
}
