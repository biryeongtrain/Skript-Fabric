package org.skriptlang.skript.common.properties.expressions;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.*;
import ch.njol.skript.expressions.base.PropertyExpression;
import org.jetbrains.annotations.NotNull;
import org.skriptlang.skript.lang.properties.Property;
import org.skriptlang.skript.lang.properties.PropertyBaseExpression;
import org.skriptlang.skript.lang.properties.handlers.base.ExpressionPropertyHandler;

@Name("Display Name")
@Description({
	"Represents the display name of a player, or the custom name of an item, entity, "
		+ "block, or inventory.",
	"",
	"<strong>Players:</strong> The name of the player that is displayed in messages. " +
		"This name can be changed freely and can include color codes, and is shared among all plugins (e.g. chat plugins will use the display name).",
	"",
	"<strong>Entities:</strong> The custom name of the entity. Can be changed, " +
		"which will also enable <em>custom name visibility</em> of the entity so name tag of the entity will be visible always.",
	"",
	"<strong>Items:</strong> The <em>custom</em> name of the item (not the Minecraft locale name). Can be changed.",
})
@Example("set the player's display name to \"<red>[admin] <gold>%name of player%\"")
@Since("before 2.1")
public class PropExprCustomName extends PropertyBaseExpression<ExpressionPropertyHandler<?,?>> {

	static {
		Skript.registerExpression(PropExprCustomName.class, Object.class,
			PropertyExpression.getPatterns("(display|nick|chat|custom)[ ]name[s]", "objects"));
	}

	@Override
	public @NotNull Property<ExpressionPropertyHandler<?, ?>> getProperty() {
		return Property.DISPLAY_NAME;
	}

}
