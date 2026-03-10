package ch.njol.skript.conditions;

import ch.njol.skript.conditions.base.PropertyCondition;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.RequiredPlugins;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Kleenean;
import net.minecraft.core.component.DataComponents;
import org.skriptlang.skript.fabric.compat.FabricItemType;

@Name("Item Has Enchantment Glint Override")
@Description("Checks whether an item has the enchantment glint overridden, or is forced to glint or not.")
@Example("""
	if the player's tool has the enchantment glint override
		send "Your tool has the enchantment glint override." to player
	""")
@Example("""
	if {_item} is forced to glint:
		send "This item is forced to glint." to player
	else if {_item} is forced to not glint:
		send "This item is forced to not glint." to player
	else:
		send "This item does not have any glint override." to player
	""")
@RequiredPlugins("Spigot 1.20.5+")
@Since("2.10")
public class CondItemEnchantmentGlint extends PropertyCondition<FabricItemType> {

	static {
		ch.njol.skript.Skript.registerCondition(
			CondItemEnchantmentGlint.class,
			"%itemtypes% (has|have) enchantment glint overrid(den|e)",
			"%itemtypes% (doesn't|does not|do not|don't) have enchantment glint overrid(den|e)",
			"%itemtypes% (is|are) forced to [:not] glint",
			"%itemtypes% (isn't|is not|aren't|are not) forced to [:not] glint"
		);
	}

	private boolean overrideCheck;
	private boolean glint;

	@Override
	@SuppressWarnings("unchecked")
	public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		setExpr((Expression<? extends FabricItemType>) expressions[0]);
		overrideCheck = matchedPattern < 2;
		setNegated(matchedPattern % 2 == 1);
		glint = !parseResult.hasTag("not");
		return true;
	}

	@Override
	public boolean check(FabricItemType itemType) {
		Boolean override = itemType.toStack().get(DataComponents.ENCHANTMENT_GLINT_OVERRIDE);
		if (overrideCheck) {
			return override != null;
		}
		return override != null && override == glint;
	}

	@Override
	protected String getPropertyName() {
		if (overrideCheck)
			return "enchantment glint overridden";
		return "forced to " + (glint ? "" : "not ") + "glint";
	}

	@Override
	protected PropertyType getPropertyType() {
		return overrideCheck ? PropertyType.HAVE : PropertyType.BE;
	}

}
