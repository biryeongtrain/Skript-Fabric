package ch.njol.skript.command;

import net.minecraft.commands.CommandSourceStack;
import org.jetbrains.annotations.Nullable;

/**
 * Execution context stored as the event handle when a script command is triggered.
 */
public record ScriptCommandContext(
		CommandSourceStack source,
		String commandLabel,
		String arguments,
		ScriptCommand command,
		Object @Nullable [] parsedArguments
) {
}
