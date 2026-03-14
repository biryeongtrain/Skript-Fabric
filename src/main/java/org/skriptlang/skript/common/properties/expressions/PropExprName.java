package org.skriptlang.skript.common.properties.expressions;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.*;
import ch.njol.skript.expressions.base.PropertyExpression;
import org.jetbrains.annotations.NotNull;
import org.skriptlang.skript.lang.properties.Property;
import org.skriptlang.skript.lang.properties.PropertyBaseExpression;
import org.skriptlang.skript.lang.properties.handlers.base.ExpressionPropertyHandler;

@Name("Name")
@Description({
	"Represents the Minecraft account name of a player, or the custom name of an item, entity, "
		+ "block, inventory, gamerule, world, script or function.",
	"",
	"<strong>Players:</strong> The Minecraft account name of the player. Can't be changed.",
	"",
	"<strong>Entities:</strong> The custom name of the entity. Can be changed. But for living entities, " +
		"the players will have to target the entity to see its name tag. For non-living entities, the name will not be visible at all. To prevent this, use 'display name'.",
	"",
	"<strong>Items:</strong> The <em>custom</em> name of the item (not the Minecraft locale name). Can be changed.",
	"",
	"<strong>Worlds:</strong> The name of the world. Cannot be changed.",
})
@Example("set the name of the player's tool to \"Legendary Sword of Awesomeness\"")
@Since("before 2.1")
public class PropExprName extends PropertyBaseExpression<ExpressionPropertyHandler<?,?>> {

	static {
		Skript.registerExpression(PropExprName.class, Object.class,
			PropertyExpression.getPatterns("name[s]", "objects"));
	}

	@Override
	public @NotNull Property<ExpressionPropertyHandler<?, ?>> getProperty() {
		return Property.NAME;
	}

}
