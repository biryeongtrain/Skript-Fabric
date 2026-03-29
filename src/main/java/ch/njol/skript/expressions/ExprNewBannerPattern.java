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
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.block.entity.BannerPattern;
import net.minecraft.world.level.block.entity.BannerPatternLayers;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.lang.event.SkriptEvent;

import java.util.Optional;

@Name("Banner Pattern")
@Description("Creates a new banner pattern from a pattern type name and a dye color.")
@Example("set {_pattern} to a \"creeper\" banner pattern colored red")
@Example("add {_pattern} to banner patterns of {_banneritem}")
@Since("2.10")
public class ExprNewBannerPattern extends SimpleExpression<BannerPatternLayers.Layer> {

	static {
		Skript.registerExpression(ExprNewBannerPattern.class, BannerPatternLayers.Layer.class,
			"[a] %string% [banner] pattern colo[u]red %dyecolor%");
	}

	private Expression<String> patternName;
	private Expression<DyeColor> selectedColor;

	@Override
	@SuppressWarnings("unchecked")
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		patternName = (Expression<String>) exprs[0];
		selectedColor = (Expression<DyeColor>) exprs[1];
		return true;
	}

	@Override
	protected BannerPatternLayers.Layer @Nullable [] get(SkriptEvent event) {
		String name = patternName.getSingle(event);
		DyeColor color = selectedColor.getSingle(event);
		if (name == null || color == null)
			return null;

		MinecraftServer server = event.server();
		if (server == null)
			return null;

		Holder<BannerPattern> patternHolder = resolvePattern(server, name);
		if (patternHolder == null)
			return null;

		return new BannerPatternLayers.Layer[]{new BannerPatternLayers.Layer(patternHolder, color)};
	}

	static @Nullable Holder<BannerPattern> resolvePattern(MinecraftServer server, String name) {
		Identifier location = Identifier.tryParse(name.contains(":") ? name : "minecraft:" + name);
		if (location == null)
			return null;
		Registry<BannerPattern> registry = server.registryAccess().lookupOrThrow(Registries.BANNER_PATTERN);
		Optional<Holder.Reference<BannerPattern>> holder = registry.get(ResourceKey.create(Registries.BANNER_PATTERN, location));
		return holder.map(ref -> (Holder<BannerPattern>) ref).orElse(null);
	}

	@Override
	public boolean isSingle() {
		return true;
	}

	@Override
	public Class<BannerPatternLayers.Layer> getReturnType() {
		return BannerPatternLayers.Layer.class;
	}

	@Override
	public String toString(@Nullable SkriptEvent event, boolean debug) {
		return "a " + patternName.toString(event, debug) + " banner pattern colored " + selectedColor.toString(event, debug);
	}
}
