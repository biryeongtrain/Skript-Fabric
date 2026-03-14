package ch.njol.skript.expressions.arithmetic;

import ch.njol.skript.lang.Expression;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.lang.arithmetic.Arithmetics;
import org.skriptlang.skript.lang.event.SkriptEvent;

/**
 * Arithmetic gettable wrapped around an expression.
 *
 * @param <T> expression type
 */
public record ArithmeticExpressionInfo<T>(Expression<? extends T> expression) implements ArithmeticGettable<T> {

	@Override
	public @Nullable T get(SkriptEvent event) {
		T object = expression.getSingle(event);
		return object == null ? Arithmetics.getDefaultValue(expression.getReturnType()) : object;
	}

	@Override
	public Class<? extends T> getReturnType() {
		return expression.getReturnType();
	}

}
