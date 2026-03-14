package ch.njol.skript.expressions;

import ch.njol.skript.Skript;
import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.events.EvtPlayerCommandSend;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.util.Kleenean;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Set;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.lang.event.SkriptEvent;
import org.skriptlang.skript.lang.structure.Structure;

@Name("Sent Command List")
@Description({
	"The commands that will be sent to the player in a send commands to player event.",
	"Modifications will affect what commands show up for the player to tab complete.",
	"Adding new commands to the list is illegal behavior and will be ignored."
})
@Since("2.8.0")
public class ExprSentCommands extends SimpleExpression<String> {

	private static final @Nullable Class<?> COMMAND_SEND_CLASS =
			ExpressionHandleSupport.resolveClass("ch.njol.skript.events.FabricPlayerEventHandles$CommandSend");

	static {
		Skript.registerExpression(ExprSentCommands.class, String.class, "[the] [sent] [server] command[s] list");
	}

	private EvtPlayerCommandSend parent;

	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		Structure structure = getParser().getCurrentStructure();
		if (!(structure instanceof EvtPlayerCommandSend)) {
			Skript.error("The 'command list' expression can only be used in a 'send command list' event");
			return false;
		}
		parent = (EvtPlayerCommandSend) structure;
		return true;
	}

	@Override
	@SuppressWarnings("unchecked")
	protected String @Nullable [] get(SkriptEvent event) {
		Object handle = event.handle();
		if (COMMAND_SEND_CLASS == null || !COMMAND_SEND_CLASS.isInstance(handle)) {
			return null;
		}
		Object commands = ExpressionHandleSupport.invoke(handle, "commands");
		if (commands instanceof Set<?> set) {
			return set.toArray(new String[0]);
		}
		return null;
	}

	@Override
	public Class<?> @Nullable [] acceptChange(ChangeMode mode) {
		return switch (mode) {
			case REMOVE, DELETE, SET, RESET -> new Class[]{String[].class};
			default -> null;
		};
	}

	@Override
	@SuppressWarnings("unchecked")
	public void change(SkriptEvent event, Object @Nullable [] delta, ChangeMode mode) {
		Object handle = event.handle();
		if (COMMAND_SEND_CLASS == null || !COMMAND_SEND_CLASS.isInstance(handle)) {
			return;
		}
		Object raw = ExpressionHandleSupport.invoke(handle, "commands");
		if (!(raw instanceof Set<?> rawSet)) {
			return;
		}
		Set<String> commands = (Set<String>) rawSet;
		switch (mode) {
			case DELETE:
				commands.clear();
				break;
			case SET:
				java.util.List<String> deltaCommands = delta != null
						? new ArrayList<>(Arrays.asList((String[]) delta)) : new ArrayList<>();
				java.util.List<String> newCommands = new ArrayList<>(deltaCommands);
				newCommands.removeAll(parent.getOriginalCommands());
				deltaCommands.removeAll(newCommands);
				commands.clear();
				commands.addAll(deltaCommands);
				break;
			case REMOVE:
				if (delta != null) {
					for (String command : (String[]) delta) {
						commands.remove(command);
					}
				}
				break;
			case RESET:
				commands.clear();
				commands.addAll(parent.getOriginalCommands());
				break;
			default:
				break;
		}
	}

	@Override
	public boolean isSingle() {
		return false;
	}

	@Override
	public Class<? extends String> getReturnType() {
		return String.class;
	}

	@Override
	public String toString(@Nullable SkriptEvent event, boolean debug) {
		return "the sent server command list";
	}
}
