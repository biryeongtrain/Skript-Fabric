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
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.lang.event.SkriptEvent;

@Name("Enchant Item")
@Description({"The enchant item in an enchant prepare event or enchant event.",
		"It can be modified, but enchantments will still be applied in the enchant event."})
@Example("""
	on enchant:
		send "Enchanting: %the enchanted item%" to player
	""")
@Example("""
	on enchant prepare:
		send "Preparing to enchant: %the enchant item%" to player
	""")
@Events({"enchant prepare", "enchant"})
@Since("2.5, Fabric")
public class ExprEnchantItem extends SimpleExpression<ItemStack> implements EventRestrictedSyntax {

	static {
		Skript.registerExpression(ExprEnchantItem.class, ItemStack.class, "[the] enchant[ed] item");
	}

	@Override
	public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		return true;
	}

	@Override
	public Class<?>[] supportedEvents() {
		return new Class<?>[]{
				FabricEventCompatHandles.EnchantApply.class,
				FabricEventCompatHandles.EnchantPrepare.class
		};
	}

	@Override
	protected ItemStack @Nullable [] get(SkriptEvent event) {
		if (event.handle() instanceof FabricEventCompatHandles.EnchantApply handle) {
			return new ItemStack[]{handle.item()};
		}
		if (event.handle() instanceof FabricEventCompatHandles.EnchantPrepare handle) {
			return new ItemStack[]{handle.item()};
		}
		return null;
	}

	@Override
	public boolean isSingle() {
		return true;
	}

	@Override
	public Class<? extends ItemStack> getReturnType() {
		return ItemStack.class;
	}

	@Override
	public String toString(@Nullable SkriptEvent event, boolean debug) {
		return "enchanted item";
	}
}
