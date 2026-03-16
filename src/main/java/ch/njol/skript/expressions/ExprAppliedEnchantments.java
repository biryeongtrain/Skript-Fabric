package ch.njol.skript.expressions;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Events;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.events.FabricEventCompatHandles;
import ch.njol.skript.lang.EventRestrictedSyntax;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.util.Kleenean;
import net.minecraft.world.item.enchantment.EnchantmentInstance;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.lang.event.SkriptEvent;

@Name("Applied Enchantments")
@Description({"The applied enchantments in an enchant event.",
		"Deleting or removing the applied enchantments will prevent the item's enchantment."})
@Example("""
	on enchant:
		send "Enchantments: %the applied enchantments%" to player
	""")
@Events("enchant")
@Since("2.5, Fabric")
public class ExprAppliedEnchantments extends SimpleExpression<EnchantmentInstance> implements EventRestrictedSyntax {

	static {
		Skript.registerExpression(ExprAppliedEnchantments.class, EnchantmentInstance.class, "[the] applied enchant[ment]s");
	}

	@Override
	public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		return true;
	}

	@Override
	public Class<?>[] supportedEvents() {
		return new Class<?>[]{FabricEventCompatHandles.EnchantApply.class};
	}

	@Override
	protected EnchantmentInstance @Nullable [] get(SkriptEvent event) {
		if (!(event.handle() instanceof FabricEventCompatHandles.EnchantApply handle)) {
			return null;
		}
		return handle.enchantments().toArray(new EnchantmentInstance[0]);
	}

	@Override
	public boolean isSingle() {
		return false;
	}

	@Override
	public Class<? extends EnchantmentInstance> getReturnType() {
		return EnchantmentInstance.class;
	}

	@Override
	public String toString(@Nullable SkriptEvent event, boolean debug) {
		return "applied enchantments";
	}
}
