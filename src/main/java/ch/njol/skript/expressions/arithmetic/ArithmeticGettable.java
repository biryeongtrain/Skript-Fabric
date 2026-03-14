package ch.njol.skript.expressions.arithmetic;

import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.lang.event.SkriptEvent;

/**
 * Represents component that can can be used within an arithmetic context.
 *
 * @param <T> the return type of the gettable
 * @see ArithmeticExpressionInfo
 */
public interface ArithmeticGettable<T> {

	/**
	 * Retrieves the value based on the given event context.
	 *
	 * @param event event context
	 * @return the computed value
	 */
	@Nullable T get(SkriptEvent event);

	/**
	 * Return type of this gettable
	 *
	 * @return the return type of this gettable
	 */
	Class<? extends T> getReturnType();

}
