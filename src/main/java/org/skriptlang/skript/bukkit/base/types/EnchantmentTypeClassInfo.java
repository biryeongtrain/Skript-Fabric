package org.skriptlang.skript.bukkit.base.types;

import ch.njol.skript.classes.ClassInfo;
import ch.njol.skript.classes.Parser;
import ch.njol.skript.lang.ParseContext;
import ch.njol.skript.registrations.Classes;
import net.minecraft.core.Holder;
import net.minecraft.world.item.enchantment.Enchantment;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.fabric.compat.EnchantmentType;

/**
 * Registers the EnchantmentType type so strings like "sharpness 5" can be parsed.
 * Delegates enchantment name parsing to the Enchantment ClassInfo parser.
 * If no level is specified, defaults to 1.
 */
public final class EnchantmentTypeClassInfo {

	private EnchantmentTypeClassInfo() {}

	public static void register() {
		ClassInfo<EnchantmentType> info = new ClassInfo<>(EnchantmentType.class, "enchantmenttype");
		info.user("enchantment ?types?");
		info.parser(new Parser<>() {
			@Override
			@SuppressWarnings("unchecked")
			public @Nullable EnchantmentType parse(String input, ParseContext context) {
				String trimmed = input.trim();
				if (trimmed.isEmpty()) return null;

				// Try to split off trailing number as level
				String enchantName = trimmed;
				int level = 1;

				int lastSpace = trimmed.lastIndexOf(' ');
				if (lastSpace > 0) {
					String possibleLevel = trimmed.substring(lastSpace + 1);
					try {
						level = Integer.parseInt(possibleLevel);
						enchantName = trimmed.substring(0, lastSpace).trim();
					} catch (NumberFormatException ignored) {
						// Entire string is the enchantment name, level stays 1
					}
				}

				if (level < 1) level = 1;

				Holder<Enchantment> holder = (Holder<Enchantment>) Classes.parse(
						enchantName, Holder.class, ParseContext.DEFAULT);
				if (holder == null) return null;

				return new EnchantmentType(holder, level);
			}

			@Override
			public boolean canParse(ParseContext context) {
				return context == ParseContext.DEFAULT || context == ParseContext.CONFIG;
			}

			@Override
			public String toString(EnchantmentType type, int flags) {
				String name = type.enchantment().unwrapKey()
						.map(key -> (String) key.identifier().getPath())
						.orElse("unknown");
				return name + " " + type.level();
			}

			@Override
			public String toVariableNameString(EnchantmentType type) {
				return toString(type, 0);
			}
		});
		Classes.registerClassInfo(info);
	}

}
