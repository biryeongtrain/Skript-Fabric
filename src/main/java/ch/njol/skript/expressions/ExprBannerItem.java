package ch.njol.skript.expressions;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.util.Kleenean;
import org.skriptlang.skript.fabric.compat.FabricItemType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.lang.event.SkriptEvent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Name("Banner Pattern Item")
@Description({
	"Gets the item from a banner pattern type name.",
	"Note that not all banner pattern types have an item.",
})
@Example("set {_item} to \"creeper\" banner pattern item")
@Example("set {_item} to \"globe\" banner pattern item")
@Since("2.10")
public class ExprBannerItem extends SimpleExpression<FabricItemType> {

	private static final Map<String, Item> bannerPatternItems = new HashMap<>();

	static {
		// Build mapping of pattern names to banner pattern items
		// Known banner pattern items follow the naming convention: <pattern>_banner_pattern
		for (var entry : BuiltInRegistries.ITEM.entrySet()) {
			Identifier key = entry.getKey().identifier();
			String path = key.getPath();
			if (path.endsWith("_banner_pattern")) {
				String patternName = path.substring(0, path.length() - "_banner_pattern".length());
				bannerPatternItems.put(patternName, entry.getValue());
			}
		}

		Skript.registerExpression(ExprBannerItem.class, FabricItemType.class,
			"[a[n]] %*string% [banner] pattern item[s]");
	}

	private Expression<String> patternNames;

	@Override
	@SuppressWarnings("unchecked")
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		patternNames = (Expression<String>) exprs[0];
		return true;
	}

	@Override
	protected FabricItemType @Nullable [] get(SkriptEvent event) {
		String[] names = patternNames.getArray(event);
		List<FabricItemType> items = new ArrayList<>();
		for (String name : names) {
			String normalized = name.toLowerCase().replace(" ", "_");
			Item item = bannerPatternItems.get(normalized);
			if (item != null) {
				items.add(new FabricItemType(new ItemStack(item)));
			}
		}
		return items.isEmpty() ? null : items.toArray(new FabricItemType[0]);
	}

	@Override
	public boolean isSingle() {
		return patternNames.isSingle();
	}

	@Override
	public Class<FabricItemType> getReturnType() {
		return FabricItemType.class;
	}

	@Override
	public String toString(@Nullable SkriptEvent event, boolean debug) {
		return patternNames.toString(event, debug) + " banner pattern items";
	}

}
