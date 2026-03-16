package ch.njol.skript.expressions;

import java.util.ArrayList;
import java.util.List;

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

@Name("Enchantment Offer")
@Description("The enchantment offer in enchant prepare events.")
@Example("""
	on enchant prepare:
		send "Your enchantment offers are: %the enchantment offers%" to player
	""")
@Events("enchant prepare")
@Since("2.5, Fabric")
public class ExprEnchantmentOffer extends SimpleExpression<EnchantmentInstance> implements EventRestrictedSyntax {

	static {
		Skript.registerExpression(ExprEnchantmentOffer.class, EnchantmentInstance.class,
				"[all [of]] [the] enchant[ment] offers",
				"enchant[ment] offer[s] %numbers%",
				"[the] %number%(st|nd|rd|th) enchant[ment] offer");
	}

	private @Nullable Expression<Number> exprOfferNumber;
	private boolean all;

	@SuppressWarnings("unchecked")
	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		if (matchedPattern == 0) {
			all = true;
		} else {
			exprOfferNumber = (Expression<Number>) exprs[0];
			all = false;
		}
		return true;
	}

	@Override
	public Class<?>[] supportedEvents() {
		return new Class<?>[]{FabricEventCompatHandles.EnchantPrepare.class};
	}

	@Override
	protected EnchantmentInstance @Nullable [] get(SkriptEvent event) {
		if (!(event.handle() instanceof FabricEventCompatHandles.EnchantPrepare handle)) {
			return null;
		}
		List<EnchantmentInstance> offers = handle.offers();
		if (offers == null) {
			return new EnchantmentInstance[0];
		}
		if (all) {
			return offers.toArray(new EnchantmentInstance[0]);
		}
		if (exprOfferNumber == null) {
			return new EnchantmentInstance[0];
		}
		if (exprOfferNumber.isSingle()) {
			Number offerNumber = exprOfferNumber.getSingle(event);
			if (offerNumber == null) {
				return new EnchantmentInstance[0];
			}
			int offer = offerNumber.intValue();
			if (offer < 1 || offer > offers.size()) {
				return new EnchantmentInstance[0];
			}
			return new EnchantmentInstance[]{offers.get(offer - 1)};
		}
		List<EnchantmentInstance> result = new ArrayList<>();
		for (Number n : exprOfferNumber.getArray(event)) {
			int i = n.intValue();
			if (i >= 1 && i <= offers.size()) {
				result.add(offers.get(i - 1));
			}
		}
		return result.toArray(new EnchantmentInstance[0]);
	}

	@Override
	public boolean isSingle() {
		return !all && exprOfferNumber != null && exprOfferNumber.isSingle();
	}

	@Override
	public Class<? extends EnchantmentInstance> getReturnType() {
		return EnchantmentInstance.class;
	}

	@Override
	public String toString(@Nullable SkriptEvent event, boolean debug) {
		return all ? "the enchantment offers" : "enchantment offer(s) " + (exprOfferNumber != null ? exprOfferNumber.toString(event, debug) : "");
	}
}
