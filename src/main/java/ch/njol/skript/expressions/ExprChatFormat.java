package ch.njol.skript.expressions;

import ch.njol.skript.Skript;
import ch.njol.skript.classes.Changer;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.util.Kleenean;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.fabric.runtime.FabricChatEventHandle;
import org.skriptlang.skript.lang.event.SkriptEvent;

@Name("Chat Format")
@Description("Can be used to get/retrieve the chat format. The sender of a message is " +
		"represented by [player] or [sender], and the message by [message] or [msg].")
@Example("set the chat format to \"<yellow>[player]<light gray>: <green>[message]\"")
@Since("2.2-dev31, Fabric")
public class ExprChatFormat extends SimpleExpression<String> {

	static {
		Skript.registerExpression(ExprChatFormat.class, String.class, "[the] (message|chat) format[ting]");
	}

	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, SkriptParser.ParseResult parseResult) {
		if (!getParser().isCurrentEvent(FabricChatEventHandle.class)) {
			Skript.error("The expression 'chat format' may only be used in chat events");
			return false;
		}
		return true;
	}

	@Override
	protected String @Nullable [] get(SkriptEvent event) {
		if (!(event.handle() instanceof FabricChatEventHandle handle)) {
			return null;
		}
		return new String[]{convertToFriendly(handle.format())};
	}

	@Override
	public @Nullable Class<?>[] acceptChange(Changer.ChangeMode mode) {
		if (mode == Changer.ChangeMode.SET || mode == Changer.ChangeMode.RESET) {
			return new Class<?>[]{String.class};
		}
		return null;
	}

	@Override
	public void change(SkriptEvent event, @Nullable Object[] delta, Changer.ChangeMode mode) {
		if (!(event.handle() instanceof FabricChatEventHandle handle)) {
			return;
		}
		String format;
		if (mode == Changer.ChangeMode.SET) {
			if (delta == null || delta.length == 0) {
				return;
			}
			String newFormat = (String) delta[0];
			if (newFormat == null) {
				return;
			}
			format = convertToNormal(newFormat);
		} else if (mode == Changer.ChangeMode.RESET) {
			format = "<%1$s> %2$s";
		} else {
			return;
		}
		handle.setFormat(format);
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
		return "chat format";
	}

	private static String convertToNormal(String format) {
		return format.replaceAll("%", "%%")
				.replaceAll("(?i)\\[(player|sender)]", "%1\\$s")
				.replaceAll("(?i)\\[(message|msg)]", "%2\\$s");
	}

	private static String convertToFriendly(String format) {
		format = format.replaceAll("%%", "%")
				.replaceAll("%1\\$s", "[player]")
				.replaceAll("%2\\$s", "[message]");
		if (format.contains("%s")) {
			int count = 0;
			for (int i = 0; i <= format.length() - 2; i++) {
				if (format.charAt(i) == '%' && format.charAt(i + 1) == 's') {
					count++;
				}
			}
			if (count >= 2) {
				format = format.replaceFirst("%s", "[player]");
				format = format.replaceFirst("%s", "[message]");
			} else {
				format = format.replaceFirst("%s",
						(format.contains("[player]") || format.contains("%1$s") ? "[message]" : "[player]"));
			}
		}
		return format;
	}
}
