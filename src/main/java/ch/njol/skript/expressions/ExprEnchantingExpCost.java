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
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.lang.event.SkriptEvent;

@Name("Enchanting Experience Cost")
@Description({"The cost of enchanting in an enchant event.",
		"This is number that was displayed in the enchantment table, not the actual number of levels removed."})
@Example("""
	on enchant:
		send "Cost: %the displayed enchanting cost%" to player
	""")
@Events("enchant")
@Since("2.5, Fabric")
public class ExprEnchantingExpCost extends SimpleExpression<Long> implements EventRestrictedSyntax {

	static {
		Skript.registerExpression(ExprEnchantingExpCost.class, Long.class,
				"[the] [displayed] ([e]xp[erience]|enchanting) cost");
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
	protected Long @Nullable [] get(SkriptEvent event) {
		if (!(event.handle() instanceof FabricEventCompatHandles.EnchantApply handle)) {
			return null;
		}
		return new Long[]{(long) handle.cost()};
	}

	@Override
	public boolean isSingle() {
		return true;
	}

	@Override
	public Class<? extends Long> getReturnType() {
		return Long.class;
	}

	@Override
	public String toString(@Nullable SkriptEvent event, boolean debug) {
		return "the displayed cost of enchanting";
	}
}
