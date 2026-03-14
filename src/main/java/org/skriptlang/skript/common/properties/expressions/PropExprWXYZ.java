package org.skriptlang.skript.common.properties.expressions;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.*;
import ch.njol.skript.expressions.base.PropertyExpression;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.util.Kleenean;
import org.jetbrains.annotations.NotNull;
import org.skriptlang.skript.lang.properties.Property;
import org.skriptlang.skript.lang.properties.PropertyBaseExpression;
import org.skriptlang.skript.lang.properties.handlers.WXYZHandler;
import org.skriptlang.skript.lang.properties.handlers.WXYZHandler.Axis;

import java.util.ArrayList;
import java.util.Locale;

@Name("WXYZ Component/Coordinate")
@Description({
	"Gets or changes the W, X, Y or Z component of anything with these components/coordinates, like locations, vectors, or quaternions.",
	"The W axis is only used for quaternions, currently."
})
@Example("set {_v} to vector(1, 2, 3)\nsend \"%x of {_v}%, %y of {_v}%, %z of {_v}%\"")
@Since("2.2-dev28, 2.10 (quaternions)")
public class PropExprWXYZ extends PropertyBaseExpression<WXYZHandler<?, ?>> {

	static {
		Skript.registerExpression(PropExprWXYZ.class, Object.class,
			PropertyExpression.getPatterns("(:x|:y|:z|:w)( |-)[component[s]|coord[inate][s]]", "objects"));
	}

	private Axis axis;

	@Override
	public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, SkriptParser.ParseResult parseResult) {
		axis = Axis.valueOf(parseResult.tags.get(0).toUpperCase(Locale.ENGLISH));
		if (!super.init(expressions, matchedPattern, isDelayed, parseResult))
			return false;

		// filter out unsupported handlers and set axis
		var tempProperties = new ArrayList<>(properties.entrySet());
		for (var entry : tempProperties) {
			var propertyInfo = entry.getValue();
			Class<?> type = entry.getKey();
			var handler = propertyInfo.handler();
			if (!handler.supportsAxis(axis)) {
				properties.remove(type);
				continue;
			}
			propertyInfo.handler().axis(axis);
		}

		// ensure we have at least one handler left
		if (properties.isEmpty()) {
			Skript.error("None of the types returned by " + expr + " have an " + axis.name().toLowerCase(Locale.ENGLISH) + " axis component.");
			return false;
		}
		return true;
	}

	public Axis axis() {
		return axis;
	}

	@Override
	public @NotNull Property<WXYZHandler<?, ?>> getProperty() {
		return Property.WXYZ;
	}

	@Override
	public String getPropertyName() {
		return axis.name();
	}

}
