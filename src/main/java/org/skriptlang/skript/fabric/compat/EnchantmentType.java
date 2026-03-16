package org.skriptlang.skript.fabric.compat;

import net.minecraft.core.Holder;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentInstance;

/**
 * Wrapper pairing an Enchantment with a level.
 * Fabric equivalent of Bukkit Skript's EnchantmentType.
 */
public record EnchantmentType(Holder<Enchantment> enchantment, int level) {

	public EnchantmentInstance toInstance() {
		return new EnchantmentInstance(enchantment, level);
	}

}
