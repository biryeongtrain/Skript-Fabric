package org.skriptlang.skript.common.properties.expressions;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.*;
import ch.njol.skript.expressions.base.PropertyExpression;
import org.jetbrains.annotations.NotNull;
import org.skriptlang.skript.lang.properties.Property;
import org.skriptlang.skript.lang.properties.PropertyBaseExpression;
import org.skriptlang.skript.lang.properties.handlers.base.ExpressionPropertyHandler;

@Name("Scale")
@Description({
	"Represents the physical size/scale of something.",
	"For example, the scale of a display entity would be a vector containing multipliers on its size in the x, y, and z axis."
})
@Example("set the scale of {_display} to vector(0,2,0)")
@Since("2.14")
public class PropExprScale extends PropertyBaseExpression<ExpressionPropertyHandler<?,?>> {

	static {
		Skript.registerExpression(PropExprScale.class, Object.class,
			PropertyExpression.getPatterns("scale[s]", "objects"));
	}

	@Override
	public @NotNull Property<ExpressionPropertyHandler<?, ?>> getProperty() {
		return Property.SCALE;
	}

}
