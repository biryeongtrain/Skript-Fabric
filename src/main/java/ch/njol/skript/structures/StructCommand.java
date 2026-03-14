package ch.njol.skript.structures;

import ch.njol.skript.ScriptLoader;
import ch.njol.skript.Skript;
import ch.njol.skript.classes.ClassInfo;
import ch.njol.skript.command.Argument;
import ch.njol.skript.command.Commands;
import ch.njol.skript.command.ScriptCommand;
import ch.njol.skript.command.ScriptCommandContext;
import ch.njol.skript.config.SectionNode;
import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.SkriptEvent;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.Trigger;
import ch.njol.skript.lang.TriggerItem;
import ch.njol.skript.registrations.Classes;
import com.mojang.brigadier.CommandDispatcher;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.MinecraftServer;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.lang.entry.EntryContainer;
import org.skriptlang.skript.lang.entry.EntryValidator;
import org.skriptlang.skript.lang.structure.Structure;
import org.skriptlang.skript.registration.SyntaxInfo;

public final class StructCommand extends Structure {

	public static final Priority PRIORITY = new Priority(500);

	private static final EntryValidator ENTRY_VALIDATOR = EntryValidator.builder()
			.addSection("trigger", false)
			.addEntry("permission", null, true)
			.addEntry("permission message", null, true)
			.addEntry("description", null, true)
			.addEntry("usage", null, true)
			.addEntry("aliases", null, true)
			.addEntry("executable by", null, true)
			.addEntry("cooldown", null, true)
			.addEntry("cooldown message", null, true)
			.addEntry("cooldown bypass", null, true)
			.build();

	// Pattern: command /<anything>
	// The regex captures everything after "command /"
	private static final Pattern ARGUMENT_PATTERN = Pattern.compile(
			"<(?<name>[a-zA-Z][a-zA-Z0-9]*)(?::(?<type>[a-zA-Z][a-zA-Z0-9]*))?(?:=(?<default>[^>]*))?>"
	);

	private @Nullable String commandName;
	private @Nullable SectionNode triggerSection;
	private final List<Argument> arguments = new ArrayList<>();
	private @Nullable String description;
	private @Nullable String usage;
	private @Nullable String permission;
	private @Nullable String permissionMessage;
	private final List<String> aliases = new ArrayList<>();
	private ScriptCommand.ExecutableBy executableBy = ScriptCommand.ExecutableBy.BOTH;
	private long cooldownMillis;
	private @Nullable String cooldownMessage;
	private @Nullable String cooldownBypass;

	private @Nullable ScriptCommand scriptCommand;

	public static void register() {
		Skript.registerStructure(
				StructCommand.class,
				SyntaxInfo.Structure.NodeType.SECTION,
				ENTRY_VALIDATOR,
				"command /<.+>"
		);
	}

	@Override
	public boolean init(
			Literal<?>[] literals,
			int matchedPattern,
			ParseResult parseResult,
			@Nullable EntryContainer entryContainer
	) {
		if (entryContainer == null) {
			return false;
		}

		// Parse the command signature from the header
		String expr = parseResult.expr;
		if (expr == null) {
			return false;
		}
		// Remove "command /" prefix
		String signature = expr;
		if (signature.startsWith("command /")) {
			signature = signature.substring("command /".length());
		} else if (signature.startsWith("command ")) {
			signature = signature.substring("command ".length());
			if (signature.startsWith("/")) {
				signature = signature.substring(1);
			}
		}

		if (!parseSignature(signature)) {
			return false;
		}

		// Get trigger section
		List<SectionNode> triggerNodes = entryContainer.getAll("trigger", SectionNode.class, false);
		if (triggerNodes.isEmpty()) {
			Skript.error("A command must have a trigger section.");
			return false;
		}
		triggerSection = triggerNodes.getFirst();

		// Optional entries
		String desc = entryContainer.getOptional("description", String.class, true);
		if (desc != null) description = desc;

		String usg = entryContainer.getOptional("usage", String.class, true);
		if (usg != null) usage = usg;

		String perm = entryContainer.getOptional("permission", String.class, true);
		if (perm != null) permission = perm;

		String permMsg = entryContainer.getOptional("permission message", String.class, true);
		if (permMsg != null) permissionMessage = permMsg;

		String aliasStr = entryContainer.getOptional("aliases", String.class, true);
		if (aliasStr != null && !aliasStr.isEmpty()) {
			for (String alias : aliasStr.split(",")) {
				String trimmed = alias.trim();
				if (trimmed.startsWith("/")) {
					trimmed = trimmed.substring(1);
				}
				if (!trimmed.isEmpty()) {
					aliases.add(trimmed);
				}
			}
		}

		String execBy = entryContainer.getOptional("executable by", String.class, true);
		if (execBy != null) {
			switch (execBy.toLowerCase().trim()) {
				case "players" -> executableBy = ScriptCommand.ExecutableBy.PLAYERS;
				case "console" -> executableBy = ScriptCommand.ExecutableBy.CONSOLE;
				case "players and console", "console and players" -> executableBy = ScriptCommand.ExecutableBy.BOTH;
				default -> Skript.warning("Invalid 'executable by' value: '" + execBy + "'. Expected 'players', 'console', or 'players and console'.");
			}
		}

		String cdStr = entryContainer.getOptional("cooldown", String.class, true);
		if (cdStr != null) {
			cooldownMillis = parseCooldown(cdStr);
		}

		String cdMsg = entryContainer.getOptional("cooldown message", String.class, true);
		if (cdMsg != null) cooldownMessage = cdMsg;

		String cdBypass = entryContainer.getOptional("cooldown bypass", String.class, true);
		if (cdBypass != null) cooldownBypass = cdBypass;

		// Generate usage if not specified
		if (usage == null) {
			StringBuilder sb = new StringBuilder("/").append(commandName);
			for (Argument arg : arguments) {
				sb.append(' ').append(arg);
			}
			usage = sb.toString();
		}

		return true;
	}

	private boolean parseSignature(String signature) {
		// Split name from arguments
		// The name is everything before the first space or angle bracket
		int nameEnd = signature.length();
		for (int i = 0; i < signature.length(); i++) {
			char c = signature.charAt(i);
			if (c == ' ' || c == '<' || c == '[') {
				nameEnd = i;
				break;
			}
		}
		commandName = signature.substring(0, nameEnd).trim();
		if (commandName.isEmpty()) {
			Skript.error("Command name cannot be empty.");
			return false;
		}

		// Parse arguments from the remainder
		String argPart = signature.substring(nameEnd).trim();
		if (!argPart.isEmpty()) {
			return parseArguments(argPart);
		}
		return true;
	}

	private boolean parseArguments(String argString) {
		int index = 0;
		int pos = 0;
		while (pos < argString.length()) {
			char c = argString.charAt(pos);
			if (c == ' ') {
				pos++;
				continue;
			}

			boolean optional = c == '[';
			if (optional) {
				pos++; // skip [
			}

			if (pos >= argString.length() || argString.charAt(pos) != '<') {
				pos++;
				continue;
			}

			// Find matching >
			int start = pos;
			int end = argString.indexOf('>', pos);
			if (end == -1) {
				Skript.error("Unclosed argument bracket in command definition.");
				return false;
			}

			String argContent = argString.substring(start + 1, end);
			Matcher matcher = ARGUMENT_PATTERN.matcher("<" + argContent + ">");
			if (!matcher.matches()) {
				Skript.error("Invalid argument syntax: <" + argContent + ">");
				return false;
			}

			String argName = matcher.group("name");
			String typeName = matcher.group("type");
			String defaultValue = matcher.group("default");

			if (typeName == null || typeName.isEmpty()) {
				typeName = "string"; // default type
			}

			ClassInfo<?> classInfo = Classes.getClassInfoNoError(typeName);
			if (classInfo == null) {
				// Try user input form
				classInfo = Classes.getClassInfoFromUserInput(typeName);
			}
			if (classInfo == null) {
				Skript.error("Unknown type '" + typeName + "' in command argument <" + argContent + ">.");
				return false;
			}

			// If there's a default value, the argument is implicitly optional
			if (defaultValue != null) {
				optional = true;
			}

			arguments.add(new Argument(argName, classInfo, optional, defaultValue, index));
			index++;

			pos = end + 1;
			if (optional && pos < argString.length() && argString.charAt(pos) == ']') {
				pos++; // skip ]
			}
		}

		return true;
	}

	@Override
	public boolean load() {
		if (commandName == null || triggerSection == null) {
			return false;
		}

		// Compile trigger
		getParser().setCurrentEvent("command", ScriptCommandContext.class);
		try {
			List<TriggerItem> items = ScriptLoader.loadItems(triggerSection);
			org.skriptlang.skript.lang.script.Script script = getParser().getCurrentScript();

			// We need a SkriptEvent instance for the Trigger constructor
			CommandSkriptEvent skriptEvent = new CommandSkriptEvent();
			Trigger trigger = new Trigger(script, "command /" + commandName, skriptEvent, items);
			int lineNumber = triggerSection.getLine();
			trigger.setLineNumber(lineNumber);
			String scriptLabel = script != null ? script.toString() : "unknown script";
			trigger.setDebugLabel(scriptLabel + ": line " + lineNumber);

			scriptCommand = new ScriptCommand(
					commandName, aliases, description, usage,
					permission, permissionMessage, arguments,
					executableBy, trigger,
					cooldownMillis, cooldownMessage, cooldownBypass
			);
		} finally {
			getParser().deleteCurrentEvent();
		}

		return true;
	}

	@Override
	public boolean postLoad() {
		if (scriptCommand == null) {
			return false;
		}

		// Register with Brigadier and command registry
		MinecraftServer server = findServer();
		if (server != null) {
			CommandDispatcher<CommandSourceStack> dispatcher = server.getCommands().getDispatcher();
			scriptCommand.register(dispatcher);
			Commands.register(scriptCommand);
			Commands.syncCommands(server);
		} else {
			// Server not yet available, register later
			Commands.register(scriptCommand);
		}

		return true;
	}

	@Override
	public void unload() {
		if (scriptCommand != null) {
			MinecraftServer server = findServer();
			if (server != null) {
				CommandDispatcher<CommandSourceStack> dispatcher = server.getCommands().getDispatcher();
				scriptCommand.unregister(dispatcher);
				Commands.unregister(scriptCommand);
				Commands.syncCommands(server);
			} else {
				Commands.unregister(scriptCommand);
			}
			scriptCommand = null;
		}
	}

	@Override
	public Priority getPriority() {
		return PRIORITY;
	}

	@Override
	public String toString(org.skriptlang.skript.lang.event.@Nullable SkriptEvent event, boolean debug) {
		return "command /" + (commandName != null ? commandName : "?");
	}

	private static long parseCooldown(String input) {
		String trimmed = input.trim().toLowerCase();
		long total = 0;
		StringBuilder number = new StringBuilder();
		for (int i = 0; i < trimmed.length(); i++) {
			char c = trimmed.charAt(i);
			if (Character.isDigit(c) || c == '.') {
				number.append(c);
			} else if (c == ' ') {
				continue;
			} else {
				if (number.isEmpty()) continue;
				double value = Double.parseDouble(number.toString());
				number.setLength(0);
				if (c == 's') {
					total += (long) (value * 1000);
				} else if (c == 'm') {
					total += (long) (value * 60000);
				} else if (c == 'h') {
					total += (long) (value * 3600000);
				} else if (c == 'd') {
					total += (long) (value * 86400000);
				} else if (c == 't') {
					// ticks: 1 tick = 50ms
					total += (long) (value * 50);
				}
			}
		}
		// If only a number was provided, treat as seconds
		if (!number.isEmpty()) {
			total += (long) (Double.parseDouble(number.toString()) * 1000);
		}
		return total;
	}

	private static @Nullable MinecraftServer findServer() {
		return Commands.getServer();
	}

	/**
	 * Minimal SkriptEvent subclass used as the event handler for command triggers.
	 */
	private static final class CommandSkriptEvent extends SkriptEvent {

		@Override
		public boolean init(Literal<?>[] args, int matchedPattern, ParseResult parseResult) {
			return true;
		}

		@Override
		public boolean check(org.skriptlang.skript.lang.event.SkriptEvent event) {
			return event.handle() instanceof ScriptCommandContext;
		}

		@Override
		public String toString(org.skriptlang.skript.lang.event.@Nullable SkriptEvent event, boolean debug) {
			return "script command";
		}
	}
}
