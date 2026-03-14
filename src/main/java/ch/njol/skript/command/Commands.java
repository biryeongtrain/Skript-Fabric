package ch.njol.skript.command;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.Nullable;

/**
 * Static registry for all script-defined commands.
 * Thread-safe via ConcurrentHashMap.
 */
public final class Commands {

	private static final ConcurrentHashMap<String, ScriptCommand> COMMANDS = new ConcurrentHashMap<>();
	private static final ConcurrentHashMap<String, Map<UUID, Long>> COOLDOWNS = new ConcurrentHashMap<>();
	private static final AtomicReference<MinecraftServer> SERVER = new AtomicReference<>();

	private Commands() {
	}

	/**
	 * Sets the current MinecraftServer reference.
	 * Called from lifecycle hooks when the server starts.
	 */
	public static void setServer(@Nullable MinecraftServer server) {
		SERVER.set(server);
	}

	/**
	 * Returns the current MinecraftServer, if available.
	 */
	public static @Nullable MinecraftServer getServer() {
		return SERVER.get();
	}

	public static void register(ScriptCommand command) {
		COMMANDS.put(command.getName().toLowerCase(), command);
		for (String alias : command.getAliases()) {
			COMMANDS.put(alias.toLowerCase(), command);
		}
	}

	public static void unregister(ScriptCommand command) {
		COMMANDS.remove(command.getName().toLowerCase());
		for (String alias : command.getAliases()) {
			COMMANDS.remove(alias.toLowerCase());
		}
	}

	public static @Nullable ScriptCommand getCommand(String name) {
		String normalized = name.startsWith("/") ? name.substring(1) : name;
		int space = normalized.indexOf(' ');
		if (space >= 0) {
			normalized = normalized.substring(0, space);
		}
		return COMMANDS.get(normalized.toLowerCase());
	}

	/**
	 * Returns all unique registered script commands.
	 */
	public static Collection<ScriptCommand> getCommands() {
		return Collections.unmodifiableCollection(
				new ArrayList<>(new java.util.LinkedHashSet<>(COMMANDS.values()))
		);
	}

	/**
	 * Returns all registered command names (including aliases).
	 */
	public static Collection<String> getCommandNames() {
		return Collections.unmodifiableCollection(COMMANDS.keySet());
	}

	/**
	 * Returns the persistent cooldown map for a command name.
	 * Survives script reloads since it is stored here rather than on the ScriptCommand instance.
	 */
	public static Map<UUID, Long> getCooldowns(String commandName) {
		return COOLDOWNS.computeIfAbsent(commandName.toLowerCase(), k -> new ConcurrentHashMap<>());
	}

	public static void clearAll() {
		COMMANDS.clear();
	}

	/**
	 * Resync the command tree to all online players.
	 */
	public static void syncCommands(MinecraftServer server) {
		List<ServerPlayer> players = server.getPlayerList().getPlayers();
		for (ServerPlayer player : players) {
			server.getCommands().sendCommands(player);
		}
	}
}
