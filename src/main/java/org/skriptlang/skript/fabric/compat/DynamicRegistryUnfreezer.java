package org.skriptlang.skript.fabric.compat;

import kim.biryeong.skriptFabric.mixin.MappedRegistryAccessor;
import net.minecraft.core.MappedRegistry;
import net.minecraft.core.Registry;

/**
 * Utility to temporarily unfreeze Minecraft's dynamic registries.
 * Required for registering custom enchantments at runtime.
 */
public final class DynamicRegistryUnfreezer {

	private DynamicRegistryUnfreezer() {}

	/**
	 * Unfreezes a registry so new entries can be registered.
	 */
	public static <T> void unfreeze(Registry<T> registry) {
		((MappedRegistryAccessor) registry).skript$setFrozen(false);
	}

	/**
	 * Re-freezes a registry after modifications are complete.
	 */
	public static <T> void refreeze(Registry<T> registry) {
		if (registry instanceof MappedRegistry<?> mapped) {
			mapped.freeze();
		}
	}

}
