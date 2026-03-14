package ch.njol.skript.expressions;

import ch.njol.skript.Skript;
import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Events;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.util.Kleenean;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.lang.event.SkriptEvent;

@Name("Message")
@Description(
	"The join message of a join event, the quit message of a quit event, " +
	"the death message of a death event, or the kick message of a kick event. " +
	"This expression is mostly useful for being changed."
)
@Example("""
	on first join:
		set join message to "Welcome %player% to our awesome server!"
	""")
@Example("""
	on quit:
		set quit message to "%player% left this awesome server!"
	""")
@Example("""
	on death:
		set the death message to "%player% died!"
	""")
@Since("1.4.6")
@Events({"join", "quit", "death", "kick"})
public class ExprMessage extends SimpleExpression<String> {

	private static final @Nullable Class<?> JOIN_CLASS =
			ExpressionHandleSupport.resolveClass("ch.njol.skript.events.FabricPlayerEventHandles$Join");
	private static final @Nullable Class<?> QUIT_CLASS =
			ExpressionHandleSupport.resolveClass("ch.njol.skript.events.FabricPlayerEventHandles$Quit");
	private static final @Nullable Class<?> KICK_CLASS =
			ExpressionHandleSupport.resolveClass("ch.njol.skript.events.FabricPlayerEventHandles$Kick");
	private static final @Nullable Class<?> DEATH_CLASS =
			ExpressionHandleSupport.resolveClass("ch.njol.skript.effects.FabricEffectEventHandles$EntityDeath");

	private enum MessageType {
		JOIN("join", "[the] (join|log[ ]in)( |-)message"),
		QUIT("quit", "[the] (quit|leave|log[ ]out|kick)( |-)message"),
		DEATH("death", "[the] death( |-)message");

		final String name;
		final String pattern;

		MessageType(String name, String pattern) {
			this.name = name;
			this.pattern = pattern;
		}

		static final String[] patterns;
		static {
			MessageType[] values = values();
			patterns = new String[values.length];
			for (int i = 0; i < patterns.length; i++) {
				patterns[i] = values[i].pattern;
			}
		}
	}

	static {
		Skript.registerExpression(ExprMessage.class, String.class, MessageType.patterns);
	}

	private MessageType type;

	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		type = MessageType.values()[matchedPattern];
		Class<?>[] eventClasses = switch (type) {
			case JOIN -> JOIN_CLASS != null ? new Class<?>[]{JOIN_CLASS} : null;
			case QUIT -> {
				if (QUIT_CLASS != null && KICK_CLASS != null) {
					yield new Class<?>[]{QUIT_CLASS, KICK_CLASS};
				} else if (QUIT_CLASS != null) {
					yield new Class<?>[]{QUIT_CLASS};
				} else {
					yield null;
				}
			}
			case DEATH -> DEATH_CLASS != null ? new Class<?>[]{DEATH_CLASS} : null;
		};
		if (eventClasses == null || !getParser().isCurrentEvent(eventClasses)) {
			Skript.error("The " + type.name + " message can only be used in a " + type.name + " event");
			return false;
		}
		return true;
	}

	@Override
	protected String @Nullable [] get(SkriptEvent event) {
		Object handle = event.handle();
		String message = getMessage(handle);
		return message != null ? new String[]{message} : new String[0];
	}

	private @Nullable String getMessage(Object handle) {
		return switch (type) {
			case JOIN -> {
				Object msg = ExpressionHandleSupport.invoke(handle, "message");
				yield msg instanceof Component c ? c.getString() : null;
			}
			case QUIT -> {
				if (KICK_CLASS != null && KICK_CLASS.isInstance(handle)) {
					Object msg = ExpressionHandleSupport.invoke(handle, "reason");
					yield msg instanceof Component c ? c.getString() : null;
				}
				Object msg = ExpressionHandleSupport.invoke(handle, "message");
				yield msg instanceof Component c ? c.getString() : null;
			}
			case DEATH -> {
				Object msg = ExpressionHandleSupport.invoke(handle, "deathMessage");
				yield msg instanceof Component c ? c.getString() : null;
			}
		};
	}

	@Override
	public Class<?> @Nullable [] acceptChange(ChangeMode mode) {
		if (mode == ChangeMode.SET || mode == ChangeMode.DELETE) {
			return new Class[]{String.class};
		}
		return null;
	}

	@Override
	public void change(SkriptEvent event, Object @Nullable [] delta, ChangeMode mode) {
		Object handle = event.handle();
		String text = mode == ChangeMode.DELETE ? "" : (delta != null && delta.length > 0 ? delta[0].toString() : "");
		Component component = mode == ChangeMode.DELETE ? null : Component.literal(text);
		switch (type) {
			case JOIN -> ExpressionHandleSupport.set(handle, "setMessage", component);
			case QUIT -> {
				if (KICK_CLASS != null && KICK_CLASS.isInstance(handle)) {
					ExpressionHandleSupport.set(handle, "setReason", component);
				} else {
					ExpressionHandleSupport.set(handle, "setMessage", component);
				}
			}
			case DEATH -> ExpressionHandleSupport.set(handle, "setDeathMessage", component);
		}
	}

	@Override
	public boolean isSingle() {
		return true;
	}

	@Override
	public Class<? extends String> getReturnType() {
		return String.class;
	}

	@Override
	public String toString(@Nullable SkriptEvent event, boolean debug) {
		return "the " + type.name + " message";
	}
}
