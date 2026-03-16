package ch.njol.skript.expressions;

import ch.njol.skript.Skript;
import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.PropertyExpression;
import ch.njol.skript.expressions.base.SimplePropertyExpression;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Kleenean;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.lang.event.SkriptEvent;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Name("Name / Display Name")
@Description({
	"Represents the Minecraft account name or display/custom name of a player or entity.",
	"",
	"<strong>Players:</strong>",
	"\t<strong>Name:</strong> The Minecraft account name of the player. Can't be changed, but 'display name' can be changed.",
	"\t<strong>Display Name:</strong> The custom name of the player. Can be changed.",
	"",
	"<strong>Entities:</strong>",
	"\t<strong>Name:</strong> The custom name of the entity. Can be changed. But for living entities, " +
		"the players will have to target the entity to see its name tag.",
	"\t<strong>Display Name:</strong> The custom name of the entity. Can be changed, " +
		"which will also enable <em>custom name visibility</em> of the entity so the name tag will always be visible.",
})
@Example("""
	on join:
		player has permission "name.red"
		set the player's display name to "<red>[admin] <gold>%name of player%"
	""")
@Example("set the name of the targeted entity to \"Custom Name\"")
@Since("before 2.1")
public class ExprName extends SimplePropertyExpression<Entity, String> {

	static {
		List<String> patterns = new ArrayList<>();
		patterns.addAll(Arrays.asList(PropertyExpression.getPatterns("name[s]", "entities")));
		patterns.addAll(Arrays.asList(PropertyExpression.getPatterns("(display|custom)[ ]name[s]", "entities")));
		Skript.registerExpression(ExprName.class, String.class, patterns.toArray(new String[0]));
	}

	/*
	 * 1 = "name",
	 * 2 = "display name" / "custom name"
	 */
	private int mark;

	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		this.mark = (matchedPattern / 2) + 1;
		return super.init(exprs, matchedPattern, isDelayed, parseResult);
	}

	@Override
	public @Nullable String convert(Entity entity) {
		if (entity instanceof ServerPlayer player) {
			return switch (mark) {
				case 1 -> player.getGameProfile().getName();
				case 2 -> {
					Component customName = player.getCustomName();
					yield customName != null ? customName.getString() : player.getGameProfile().getName();
				}
				default -> throw new IllegalStateException("Unexpected value: " + mark);
			};
		}
		// Non-player entities
		if (mark == 1) {
			// "name" - return custom name if set, otherwise the entity type name
			Component customName = entity.getCustomName();
			if (customName != null) {
				return customName.getString();
			}
			return entity.getName().getString();
		}
		// "display name" / "custom name"
		Component customName = entity.getCustomName();
		return customName != null ? customName.getString() : null;
	}

	@Override
	public Class<?> @Nullable [] acceptChange(ChangeMode mode) {
		if (mode == ChangeMode.SET || mode == ChangeMode.RESET) {
			if (mark == 1 && ServerPlayer.class.isAssignableFrom(getExpr().getReturnType())) {
				Skript.error("Can't change the Minecraft name of a player. Change the 'display name' instead.");
				return null;
			}
			return new Class[]{String.class};
		}
		return null;
	}

	@Override
	public void change(SkriptEvent event, Object @Nullable [] delta, ChangeMode mode) {
		String name = delta != null && delta.length > 0 ? (String) delta[0] : null;
		for (Entity entity : getExpr().getArray(event)) {
			if (entity instanceof ServerPlayer player) {
				if (mark == 2) {
					// Set display/custom name for player
					if (name != null) {
						player.setCustomName(Component.literal(name));
						player.setCustomNameVisible(true);
					} else {
						player.setCustomName(null);
						player.setCustomNameVisible(false);
					}
				}
				// mark == 1 for players is blocked by acceptChange
			} else {
				// Non-player entity
				if (name != null) {
					entity.setCustomName(Component.literal(name));
				} else {
					entity.setCustomName(null);
				}
				if (mark == 2 || mode == ChangeMode.RESET) {
					// "display name" - also toggle visibility
					entity.setCustomNameVisible(name != null);
				}
			}
		}
	}

	@Override
	public Class<String> getReturnType() {
		return String.class;
	}

	@Override
	protected String getPropertyName() {
		return mark == 2 ? "display name" : "name";
	}
}
