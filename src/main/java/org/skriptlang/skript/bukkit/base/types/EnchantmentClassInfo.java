package org.skriptlang.skript.bukkit.base.types;

import ch.njol.skript.classes.ClassInfo;
import ch.njol.skript.classes.Parser;
import ch.njol.skript.command.Commands;
import ch.njol.skript.lang.ParseContext;
import ch.njol.skript.registrations.Classes;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.item.enchantment.Enchantment;
import org.jetbrains.annotations.Nullable;

/**
 * Registers the Enchantment type so strings like "sharpness" can be parsed
 * into Enchantment Holders via the dynamic registry.
 */
public final class EnchantmentClassInfo {

	private EnchantmentClassInfo() {}

	@SuppressWarnings("unchecked")
	public static void register() {
		ClassInfo<Holder<Enchantment>> info = new ClassInfo<>((Class<Holder<Enchantment>>) (Class<?>) Holder.class, "enchantment");
		info.user("enchantments?");
		info.parser(new Parser<>() {
			@Override
			public @Nullable Holder<Enchantment> parse(String input, ParseContext context) {
				MinecraftServer server = Commands.getServer();
				if (server == null) return null;
				Registry<Enchantment> reg = server.registryAccess()
						.lookupOrThrow(Registries.ENCHANTMENT);
				Identifier id = Identifier.tryParse(input.toLowerCase().replace(" ", "_"));
				if (id == null) return null;
				return reg.get(id).orElse(null);
			}

			@Override
			public boolean canParse(ParseContext context) {
				return context == ParseContext.DEFAULT || context == ParseContext.CONFIG;
			}

			@Override
			public String toString(Holder<Enchantment> holder, int flags) {
				return holder.unwrapKey()
						.map(key -> (String) key.identifier().getPath())
						.orElse("unknown enchantment");
			}

			@Override
			public String toVariableNameString(Holder<Enchantment> holder) {
				return toString(holder, 0);
			}
		});
		Classes.registerClassInfo(info);
	}

}
