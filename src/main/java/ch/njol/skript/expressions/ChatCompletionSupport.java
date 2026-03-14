package ch.njol.skript.expressions;

import java.util.*;
import net.minecraft.network.protocol.game.ClientboundCustomChatCompletionsPacket;
import net.minecraft.server.level.ServerPlayer;

final class ChatCompletionSupport {

	private static final Map<Object, Set<String>> COMPLETIONS = Collections.synchronizedMap(new WeakHashMap<>());

	private ChatCompletionSupport() {}

	static Set<String> get(Object player) {
		Set<String> set = COMPLETIONS.get(player);
		return set != null ? Collections.unmodifiableSet(new LinkedHashSet<>(set)) : Collections.emptySet();
	}

	static void add(ServerPlayer player, Collection<String> completions) {
		COMPLETIONS.computeIfAbsent(player, k -> new LinkedHashSet<>()).addAll(completions);
		player.connection.send(new ClientboundCustomChatCompletionsPacket(
			ClientboundCustomChatCompletionsPacket.Action.ADD, new ArrayList<>(completions)
		));
	}

	static void remove(ServerPlayer player, Collection<String> completions) {
		Set<String> set = COMPLETIONS.get(player);
		if (set != null) set.removeAll(completions);
		player.connection.send(new ClientboundCustomChatCompletionsPacket(
			ClientboundCustomChatCompletionsPacket.Action.REMOVE, new ArrayList<>(completions)
		));
	}

	static void set(ServerPlayer player, Collection<String> completions) {
		Set<String> old = COMPLETIONS.get(player);
		if (old != null && !old.isEmpty()) {
			player.connection.send(new ClientboundCustomChatCompletionsPacket(
				ClientboundCustomChatCompletionsPacket.Action.REMOVE, new ArrayList<>(old)
			));
		}
		Set<String> newSet = new LinkedHashSet<>(completions);
		COMPLETIONS.put(player, newSet);
		if (!newSet.isEmpty()) {
			player.connection.send(new ClientboundCustomChatCompletionsPacket(
				ClientboundCustomChatCompletionsPacket.Action.ADD, new ArrayList<>(newSet)
			));
		}
	}

	static void clear(ServerPlayer player) {
		Set<String> old = COMPLETIONS.remove(player);
		if (old != null && !old.isEmpty()) {
			player.connection.send(new ClientboundCustomChatCompletionsPacket(
				ClientboundCustomChatCompletionsPacket.Action.REMOVE, new ArrayList<>(old)
			));
		}
	}
}
