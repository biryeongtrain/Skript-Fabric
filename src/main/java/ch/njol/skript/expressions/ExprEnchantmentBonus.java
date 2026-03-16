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

@Name("Enchantment Bonus")
@Description("The enchantment bonus in an enchant prepare event. This represents the number of bookshelves affecting/surrounding the enchantment table.")
@Example("""
	on enchant prepare:
		send "There are %enchantment bonus% bookshelves surrounding this enchantment table!" to player
	""")
@Events("enchant prepare")
@Since("2.5, Fabric")
public class ExprEnchantmentBonus extends SimpleExpression<Long> implements EventRestrictedSyntax {

	static {
		Skript.registerExpression(ExprEnchantmentBonus.class, Long.class, "[the] enchantment bonus");
	}

	@Override
	public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		return true;
	}

	@Override
	public Class<?>[] supportedEvents() {
		return new Class<?>[]{FabricEventCompatHandles.EnchantPrepare.class};
	}

	@Override
	protected Long @Nullable [] get(SkriptEvent event) {
		if (!(event.handle() instanceof FabricEventCompatHandles.EnchantPrepare handle)) {
			return null;
		}
		return new Long[]{(long) handle.enchantmentBonus()};
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
		return "enchantment bonus";
	}
}
