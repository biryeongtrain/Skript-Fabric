package ch.njol.skript.command;

import ch.njol.skript.Skript;
import ch.njol.skript.conditions.CondPermission;
import ch.njol.skript.lang.Trigger;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import com.mojang.brigadier.tree.CommandNode;
import com.mojang.brigadier.tree.LiteralCommandNode;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import kim.biryeong.skriptFabric.mixin.CommandNodeAccessor;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.lang.event.SkriptEvent;

/**
 * Represents a script-defined command registered with Brigadier.
 */
public final class ScriptCommand {

	public enum ExecutableBy {
		PLAYERS, CONSOLE, BOTH
	}

	public static final ThreadLocal<Boolean> CANCEL_COOLDOWN = ThreadLocal.withInitial(() -> false);

	private final String name;
	private final List<String> aliases;
	private final @Nullable String description;
	private final @Nullable String usage;
	private final @Nullable String permission;
	private final @Nullable String permissionMessage;
	private final List<Argument> arguments;
	private final ExecutableBy executableBy;
	private final Trigger trigger;

	// Cooldown fields
	private final long cooldownMillis;
	private final @Nullable String cooldownMessage;
	private final @Nullable String cooldownBypass;

	public ScriptCommand(
			String name,
			List<String> aliases,
			@Nullable String description,
			@Nullable String usage,
			@Nullable String permission,
			@Nullable String permissionMessage,
			List<Argument> arguments,
			ExecutableBy executableBy,
			Trigger trigger,
			long cooldownMillis,
			@Nullable String cooldownMessage,
			@Nullable String cooldownBypass
	) {
		this.name = name;
		this.aliases = aliases;
		this.description = description;
		this.usage = usage;
		this.permission = permission;
		this.permissionMessage = permissionMessage;
		this.arguments = arguments;
		this.executableBy = executableBy;
		this.trigger = trigger;
		this.cooldownMillis = cooldownMillis;
		this.cooldownMessage = cooldownMessage;
		this.cooldownBypass = cooldownBypass;
	}

	public String getName() {
		return name;
	}

	public List<String> getAliases() {
		return aliases;
	}

	public @Nullable String getDescription() {
		return description;
	}

	public @Nullable String getUsage() {
		return usage;
	}

	public @Nullable String getPermission() {
		return permission;
	}

	public List<Argument> getArguments() {
		return arguments;
	}

	public @Nullable String getPermissionMessage() {
		return permissionMessage;
	}

	public ExecutableBy getExecutableBy() {
		return executableBy;
	}

	public long getCooldownMillis() {
		return cooldownMillis;
	}

	public @Nullable String getCooldownMessage() {
		return cooldownMessage;
	}

	public @Nullable String getCooldownBypass() {
		return cooldownBypass;
	}

	public Trigger getTrigger() {
		return trigger;
	}

	/**
	 * Register this command with the given Brigadier dispatcher.
	 */
	public void register(CommandDispatcher<CommandSourceStack> dispatcher) {
		LiteralCommandNode<CommandSourceStack> node = dispatcher.register(buildLiteral(name));
		for (String alias : aliases) {
			dispatcher.register(buildLiteral(alias));
		}
	}

	private LiteralArgumentBuilder<CommandSourceStack> buildLiteral(String label) {
		LiteralArgumentBuilder<CommandSourceStack> builder = LiteralArgumentBuilder.<CommandSourceStack>literal(label)
				.requires(this::checkPermission);

		if (!arguments.isEmpty()) {
			builder.then(
					RequiredArgumentBuilder.<CommandSourceStack, String>argument("args", StringArgumentType.greedyString())
							.suggests(this::suggest)
							.executes(this::executeCommand)
			);
		}

		// Always add a zero-argument executor
		builder.executes(this::executeCommand);

		return builder;
	}

	/**
	 * Unregister this command from the given dispatcher using mixin accessor.
	 */
	@SuppressWarnings("unchecked")
	public void unregister(CommandDispatcher<CommandSourceStack> dispatcher) {
		CommandNode<CommandSourceStack> root = dispatcher.getRoot();
		CommandNodeAccessor<CommandSourceStack> accessor = (CommandNodeAccessor<CommandSourceStack>) root;

		removeFromMaps(accessor, name);
		for (String alias : aliases) {
			removeFromMaps(accessor, alias);
		}
	}

	@SuppressWarnings("unchecked")
	private void removeFromMaps(CommandNodeAccessor<CommandSourceStack> accessor, String label) {
		accessor.skript$getChildren().remove(label);
		accessor.skript$getLiterals().remove(label);
		accessor.skript$getArguments().remove(label);
	}

	private boolean checkPermission(CommandSourceStack source) {
		if (permission == null || permission.isEmpty()) {
			return true;
		}
		ServerPlayer player = source.getPlayer();
		if (player != null) {
			return CondPermission.hasPermission(player, permission);
		}
		// Console always has permission; non-player non-console falls back to op level 2
		return source.hasPermission(2);
	}

	private int executeCommand(CommandContext<CommandSourceStack> context) {
		CommandSourceStack source = context.getSource();
		ServerPlayer player = source.getPlayer();

		// Check executable by
		if (executableBy == ExecutableBy.PLAYERS && player == null) {
			source.sendFailure(Component.literal("This command can only be executed by players."));
			return 0;
		}
		if (executableBy == ExecutableBy.CONSOLE && player != null) {
			source.sendFailure(Component.literal("This command can only be executed from the console."));
			return 0;
		}

		// Permission check
		if (permission != null && !permission.isEmpty()) {
			if (!checkPermission(source)) {
				String msg = permissionMessage != null ? permissionMessage : "You don't have permission to use this command.";
				source.sendFailure(Component.literal(msg));
				return 0;
			}
		}

		// Cooldown check (players only)
		if (cooldownMillis > 0 && player != null) {
			if (cooldownBypass == null || !CondPermission.hasPermission(player, cooldownBypass)) {
				Map<java.util.UUID, Long> cooldowns = Commands.getCooldowns(name);
				Long lastUsed = cooldowns.get(player.getUUID());
				if (lastUsed != null) {
					long remaining = cooldownMillis - (System.currentTimeMillis() - lastUsed);
					if (remaining > 0) {
						String msg = cooldownMessage != null
								? cooldownMessage.replace("%remaining%", formatTime(remaining))
								: "You must wait " + formatTime(remaining) + " before using this command again.";
						source.sendFailure(Component.literal(msg));
						return 0;
					}
				}
			}
		}

		// Parse arguments
		String rawArgs;
		try {
			rawArgs = StringArgumentType.getString(context, "args");
		} catch (IllegalArgumentException e) {
			rawArgs = "";
		}

		Object[] parsedArgs = parseArguments(rawArgs, source);
		if (parsedArgs == null) {
			return 0; // parsing error already reported
		}

		// Build context and execute
		String label = context.getNodes().getFirst().getNode().getName();
		ScriptCommandContext cmdContext = new ScriptCommandContext(source, label, rawArgs, this, parsedArgs);

		MinecraftServer server = source.getServer();
		ServerLevel level = source.getLevel();

		SkriptEvent event = new SkriptEvent(cmdContext, server, level, player);
		trigger.execute(event);

		// Update cooldown (stored in Commands registry to survive reloads)
		if (cooldownMillis > 0 && player != null && !CANCEL_COOLDOWN.get()) {
			Commands.getCooldowns(name).put(player.getUUID(), System.currentTimeMillis());
		}
		CANCEL_COOLDOWN.remove();

		return 1;
	}

	private Object @Nullable [] parseArguments(String rawArgs, CommandSourceStack source) {
		if (arguments.isEmpty()) {
			if (!rawArgs.isEmpty()) {
				source.sendFailure(Component.literal("This command does not accept any arguments."));
				if (usage != null) {
					source.sendFailure(Component.literal("Usage: " + usage));
				}
				return null;
			}
			return new Object[0];
		}

		String[] tokens = rawArgs.isEmpty() ? new String[0] : rawArgs.split("\\s+");
		Object[] parsed = new Object[arguments.size()];
		int tokenIndex = 0;

		for (int i = 0; i < arguments.size(); i++) {
			Argument arg = arguments.get(i);

			if (tokenIndex < tokens.length) {
				// For the last argument, consume all remaining tokens
				String token;
				if (i == arguments.size() - 1 && tokenIndex < tokens.length) {
					StringBuilder sb = new StringBuilder(tokens[tokenIndex]);
					for (int j = tokenIndex + 1; j < tokens.length; j++) {
						sb.append(' ').append(tokens[j]);
					}
					token = sb.toString();
				} else {
					token = tokens[tokenIndex];
				}

				Object value = arg.parse(token);
				if (value == null) {
					source.sendFailure(Component.literal("Invalid value for argument '" + arg.getName() + "': " + token));
					if (usage != null) {
						source.sendFailure(Component.literal("Usage: " + usage));
					}
					return null;
				}
				parsed[i] = value;
				tokenIndex++;
			} else if (arg.isOptional()) {
				parsed[i] = arg.parseDefault();
			} else {
				source.sendFailure(Component.literal("Missing required argument: " + arg.getName()));
				if (usage != null) {
					source.sendFailure(Component.literal("Usage: " + usage));
				}
				return null;
			}
		}

		return parsed;
	}

	private CompletableFuture<Suggestions> suggest(CommandContext<CommandSourceStack> context, SuggestionsBuilder builder) {
		String rawArgs;
		try {
			rawArgs = StringArgumentType.getString(context, "args");
		} catch (IllegalArgumentException e) {
			rawArgs = "";
		}

		String[] tokens = rawArgs.isEmpty() ? new String[0] : rawArgs.split("\\s+", -1);
		int argIndex = Math.max(0, tokens.length - 1);

		if (argIndex < arguments.size()) {
			Argument arg = arguments.get(argIndex);
			// Suggest based on the argument type name
			builder.suggest("<" + arg.getName() + ":" + arg.getClassInfo().getCodeName() + ">");
		}

		return builder.buildFuture();
	}

	private static String formatTime(long millis) {
		long seconds = millis / 1000;
		if (seconds < 60) {
			return seconds + " second" + (seconds != 1 ? "s" : "");
		}
		long minutes = seconds / 60;
		seconds = seconds % 60;
		if (minutes < 60) {
			return minutes + " minute" + (minutes != 1 ? "s" : "") +
					(seconds > 0 ? " " + seconds + " second" + (seconds != 1 ? "s" : "") : "");
		}
		long hours = minutes / 60;
		minutes = minutes % 60;
		return hours + " hour" + (hours != 1 ? "s" : "") +
				(minutes > 0 ? " " + minutes + " minute" + (minutes != 1 ? "s" : "") : "");
	}
}
