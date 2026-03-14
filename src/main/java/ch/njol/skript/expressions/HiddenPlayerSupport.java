package ch.njol.skript.expressions;

import java.util.*;
import net.minecraft.server.level.ServerPlayer;

final class HiddenPlayerSupport {

	private static final Map<Object, Set<UUID>> HIDDEN = Collections.synchronizedMap(new WeakHashMap<>());

	private HiddenPlayerSupport() {}

	static Set<UUID> getHidden(Object viewer) {
		Set<UUID> set = HIDDEN.get(viewer);
		return set != null ? Collections.unmodifiableSet(new LinkedHashSet<>(set)) : Collections.emptySet();
	}

	static void hide(Object viewer, UUID target) {
		HIDDEN.computeIfAbsent(viewer, k -> new LinkedHashSet<>()).add(target);
	}

	static void show(Object viewer, UUID target) {
		Set<UUID> set = HIDDEN.get(viewer);
		if (set != null) set.remove(target);
	}

	static boolean isHidden(Object viewer, UUID target) {
		Set<UUID> set = HIDDEN.get(viewer);
		return set != null && set.contains(target);
	}
}
