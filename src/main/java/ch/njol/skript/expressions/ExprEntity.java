package ch.njol.skript.expressions;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.entity.EntityData;
import ch.njol.skript.expressions.base.EventValueExpression;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.skript.lang.util.SimpleLiteral;
import ch.njol.skript.log.RetainingLogHandler;
import ch.njol.skript.log.SkriptLogger;
import ch.njol.util.Kleenean;
import net.minecraft.world.entity.Entity;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.lang.event.SkriptEvent;

/**
 * @author Peter Güttinger
 */
@Name("Creature/Entity/Player/Projectile/Villager/Powered Creeper/etc.")
@Description({"The entity involved in an event (an entity is a player, a creature or an inanimate object like ignited TNT, a dropped item or an arrow).",
		"You can use the specific type of the entity that's involved in the event, e.g. in a 'death of a creeper' event you can use 'the creeper' instead of 'the entity'."})
@Example("give a diamond sword of sharpness 3 to the player")
@Example("kill the creeper")
@Example("kill all powered creepers in the wolf's world")
@Example("projectile is an arrow")
@Since("1.0")
public class ExprEntity extends SimpleExpression<Entity> {
	static {
		Skript.registerExpression(ExprEntity.class, Entity.class, "[the] [event-]<.+>");
	}

	@SuppressWarnings("null")
	private EntityData<?> type;

	@SuppressWarnings("null")
	private EventValueExpression<Entity> entity;

	@Override
	public boolean init(final Expression<?>[] exprs, final int matchedPattern, final Kleenean isDelayed, final ParseResult parseResult) {
		final RetainingLogHandler log = SkriptLogger.startRetainingLog();
		try {
			String s = parseResult.regexes.get(0).group();

			// Strip indefinite articles for EntityData parsing
			String stripped = stripIndefiniteArticle(s);

			final EntityData<?> type = EntityData.parse(stripped);
			log.clear();
			log.printLog();
			if (type == null)
				return false;
			this.type = type;
		} finally {
			log.stop();
		}
		entity = new EventValueExpression<>(type.getType());
		return entity.init();
	}

	/**
	 * Strips indefinite articles ("a ", "an ", "the ") from the beginning of the input string.
	 */
	private static String stripIndefiniteArticle(String input) {
		String lower = input.toLowerCase(java.util.Locale.ROOT);
		if (lower.startsWith("the "))
			return input.substring(4);
		if (lower.startsWith("an "))
			return input.substring(3);
		if (lower.startsWith("a "))
			return input.substring(2);
		return input;
	}

	@Override
	public boolean isSingle() {
		return true;
	}

	@Override
	public Class<? extends Entity> getReturnType() {
		return type.getType();
	}

	@Override
	@Nullable
	protected Entity[] get(final SkriptEvent event) {
		final Entity[] es = entity.getArray(event);
		if (es.length == 0 || type.isInstance(es[0]))
			return es;
		return null;
	}

	@SuppressWarnings("unchecked")
	@Override
	@Nullable
	public <R> Expression<? extends R> getConvertedExpression(Class<R>... to) {
		for (Class<R> t : to) {
			if (t.equals(EntityData.class)) {
				return new SimpleLiteral<>((R) type, false);
			}
		}
		return super.getConvertedExpression(to);
	}

	@Override
	public boolean setTime(int time) {
		return entity.setTime(time);
	}

	@Override
	public String toString(final @Nullable SkriptEvent event, final boolean debug) {
		return "the " + type;
	}

}
