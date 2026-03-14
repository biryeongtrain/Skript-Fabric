package ch.njol.skript.expressions;

import java.util.*;
import net.minecraft.network.protocol.game.ClientboundPlayerInfoRemovePacket;
import net.minecraft.network.protocol.game.ClientboundPlayerInfoUpdatePacket;
import net.minecraft.server.level.ServerPlayer;

final class TabListedPlayerSupport {

	private static final Map<Object, Set<UUID>> UNLISTED = Collections.synchronizedMap(new WeakHashMap<>());

	private TabListedPlayerSupport() {}

	static boolean isListed(Object viewer, UUID target) {
		Set<UUID> set = UNLISTED.get(viewer);
		return set == null || !set.contains(target);
	}

	static void unlist(ServerPlayer viewer, ServerPlayer target) {
		UNLISTED.computeIfAbsent(viewer, k -> new LinkedHashSet<>()).add(target.getUUID());
		viewer.connection.send(new ClientboundPlayerInfoRemovePacket(java.util.List.of(target.getUUID())));
	}

	static void list(ServerPlayer viewer, ServerPlayer target) {
		Set<UUID> set = UNLISTED.get(viewer);
		if (set != null) set.remove(target.getUUID());
		viewer.connection.send(ClientboundPlayerInfoUpdatePacket.createPlayerInitializing(java.util.List.of(target)));
	}

	static void resetAll(ServerPlayer viewer) {
		UNLISTED.remove(viewer);
	}
}
