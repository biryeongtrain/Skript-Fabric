package org.skriptlang.skript.fabric.compat;

import net.minecraft.world.entity.Entity;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * A transient metadata store for Fabric entities, replacing Bukkit's Metadatable interface.
 * Uses UUID-based keys so metadata persists across ticks (but not server restarts).
 */
public final class FabricMetadataStore {

	private static final ConcurrentHashMap<UUID, ConcurrentHashMap<String, Object>> STORE = new ConcurrentHashMap<>();

	private FabricMetadataStore() {
		throw new UnsupportedOperationException("Utility class");
	}

	/**
	 * Sets a metadata value on the given entity.
	 */
	public static void setMetadata(Entity entity, String key, Object value) {
		STORE.computeIfAbsent(entity.getUUID(), k -> new ConcurrentHashMap<>()).put(key, value);
	}

	/**
	 * Gets a metadata value from the given entity, or null if not present.
	 */
	public static @Nullable Object getMetadata(Entity entity, String key) {
		Map<String, Object> entityMap = STORE.get(entity.getUUID());
		if (entityMap == null)
			return null;
		return entityMap.get(key);
	}

	/**
	 * Returns true if the entity has metadata under the given key.
	 */
	public static boolean hasMetadata(Entity entity, String key) {
		Map<String, Object> entityMap = STORE.get(entity.getUUID());
		return entityMap != null && entityMap.containsKey(key);
	}

	/**
	 * Removes a metadata key from the given entity.
	 */
	public static void removeMetadata(Entity entity, String key) {
		Map<String, Object> entityMap = STORE.get(entity.getUUID());
		if (entityMap != null) {
			entityMap.remove(key);
			if (entityMap.isEmpty()) {
				STORE.remove(entity.getUUID());
			}
		}
	}

	/**
	 * Removes all metadata for the given entity.
	 */
	public static void clearMetadata(Entity entity) {
		STORE.remove(entity.getUUID());
	}

}
