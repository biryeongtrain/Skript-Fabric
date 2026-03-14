package ch.njol.skript.expressions;

import ch.njol.skript.Skript;
import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.util.Kleenean;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.lang.event.SkriptEvent;

@Name("On-Screen Kick Message")
@Description("The kick message that is shown on the player's screen when they are kicked from the server.")
@Since("2.12")
public class ExprOnScreenKickMessage extends SimpleExpression<String> {

	private static final @Nullable Class<?> KICK_CLASS =
			ExpressionHandleSupport.resolveClass("ch.njol.skript.events.FabricPlayerEventHandles$Kick");

	static {
		Skript.registerExpression(ExprOnScreenKickMessage.class, String.class, "[the] on-screen kick message");
	}

	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		if (KICK_CLASS == null || !getParser().isCurrentEvent(KICK_CLASS)) {
			Skript.error("The 'on-screen kick message' expression can only be used in a kick event");
			return false;
		}
		return true;
	}

	@Override
	protected String @Nullable [] get(SkriptEvent event) {
		Object handle = event.handle();
		if (KICK_CLASS == null || !KICK_CLASS.isInstance(handle)) {
			return null;
		}
		Object reason = ExpressionHandleSupport.invoke(handle, "reason");
		if (reason instanceof Component component) {
			return new String[]{component.getString()};
		}
		return null;
	}

	@Override
	public Class<?> @Nullable [] acceptChange(ChangeMode mode) {
		return switch (mode) {
			case SET -> new Class[]{String.class};
			default -> null;
		};
	}

	@Override
	public void change(SkriptEvent event, Object @Nullable [] delta, ChangeMode mode) {
		Object handle = event.handle();
		if (KICK_CLASS == null || !KICK_CLASS.isInstance(handle)) {
			return;
		}
		if (mode == ChangeMode.SET && delta != null && delta.length > 0) {
			String text = (String) delta[0];
			ExpressionHandleSupport.set(handle, "setReason", Component.literal(text));
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
		return "the on-screen kick message";
	}
}
