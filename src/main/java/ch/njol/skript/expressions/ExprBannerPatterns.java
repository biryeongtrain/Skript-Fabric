package ch.njol.skript.expressions;

import ch.njol.skript.Skript;
import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.PropertyExpression;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.SyntaxStringBuilder;
import ch.njol.util.Kleenean;
import kim.biryeong.skriptFabric.mixin.BannerBlockEntityAccessor;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BannerBlockEntity;
import net.minecraft.world.level.block.entity.BannerPatternLayers;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.fabric.compat.FabricBlock;
import org.skriptlang.skript.lang.event.SkriptEvent;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Name("Banner Patterns")
@Description({
	"Gets or sets the banner patterns of a banner.",
	"In order to set a specific position of a banner, there needs to be that many patterns already on the banner.",
	"This expression will add filler patterns to the banner to allow the specified position to be set."
})
@Example("broadcast banner patterns of {_banneritem}")
@Example("broadcast 1st banner pattern of block at location(0,0,0)")
@Example("clear banner patterns of {_banneritem}")
@Since("2.10")
public class ExprBannerPatterns extends PropertyExpression<Object, BannerPatternLayers.Layer> {

	static {
		Skript.registerExpression(ExprBannerPatterns.class, BannerPatternLayers.Layer.class,
			"[all [[of] the]|the] banner pattern[s] of %itemstacks/blocks%",
			"%itemstacks/blocks%'[s] banner pattern[s]",
			"[the] %integer%[st|nd|rd|th] [banner] pattern of %itemstacks/blocks%",
			"%itemstacks/blocks%'[s] %integer%[st|nd|rd|th] [banner] pattern"
		);
	}

	private Expression<?> objects;
	private Expression<Integer> patternNumber = null;

	@Override
	@SuppressWarnings("unchecked")
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		if (matchedPattern <= 1) {
			objects = exprs[0];
		} else if (matchedPattern == 2) {
			patternNumber = (Expression<Integer>) exprs[0];
			objects = exprs[1];
		} else {
			patternNumber = (Expression<Integer>) exprs[1];
			objects = exprs[0];
		}
		setExpr(objects);
		return true;
	}

	@Override
	protected BannerPatternLayers.Layer @Nullable [] get(SkriptEvent event, Object[] source) {
		List<BannerPatternLayers.Layer> patterns = new ArrayList<>();
		Integer placement = patternNumber != null ? patternNumber.getSingle(event) : null;
		for (Object object : objects.getArray(event)) {
			BannerPatternLayers layers = getLayers(object);
			if (layers == null)
				continue;
			List<BannerPatternLayers.Layer> layerList = layers.layers();
			if (placement != null && layerList.size() >= placement) {
				patterns.add(layerList.get(placement - 1));
			} else if (placement == null) {
				patterns.addAll(layerList);
			}
		}
		return patterns.toArray(new BannerPatternLayers.Layer[0]);
	}

	private static @Nullable BannerPatternLayers getLayers(Object object) {
		if (object instanceof FabricBlock block) {
			BlockEntity be = block.level().getBlockEntity(block.position());
			if (be instanceof BannerBlockEntity banner)
				return banner.getPatterns();
		} else if (object instanceof ItemStack stack) {
			BannerPatternLayers layers = stack.get(net.minecraft.core.component.DataComponents.BANNER_PATTERNS);
			if (layers != null)
				return layers;
		}
		return null;
	}

	@Override
	public Class<?> @Nullable [] acceptChange(ChangeMode mode) {
		return switch (mode) {
			case SET, DELETE -> (patternNumber != null)
				? new Class[]{BannerPatternLayers.Layer.class}
				: new Class[]{BannerPatternLayers.Layer[].class};
			case REMOVE, ADD -> (patternNumber != null)
				? null
				: new Class[]{BannerPatternLayers.Layer[].class};
			default -> null;
		};
	}

	@Override
	public void change(SkriptEvent event, Object @Nullable [] delta, ChangeMode mode) {
		int placement = 0;
		if (patternNumber != null) {
			Integer patternNum = patternNumber.getSingle(event);
			if (patternNum != null)
				placement = patternNum;
		}
		List<BannerPatternLayers.Layer> deltaLayers = delta != null
			? Arrays.stream(delta).map(BannerPatternLayers.Layer.class::cast).toList()
			: new ArrayList<>();

		for (Object object : objects.getArray(event)) {
			if (object instanceof FabricBlock block) {
				BlockEntity be = block.level().getBlockEntity(block.position());
				if (!(be instanceof BannerBlockEntity banner))
					continue;
				BannerPatternLayers newLayers = applyChange(banner.getPatterns(), mode, placement, deltaLayers, event);
				((BannerBlockEntityAccessor) banner).skript$setPatterns(newLayers);
				banner.setChanged();
			} else if (object instanceof ItemStack stack) {
				BannerPatternLayers current = stack.getOrDefault(
					net.minecraft.core.component.DataComponents.BANNER_PATTERNS, BannerPatternLayers.EMPTY);
				BannerPatternLayers newLayers = applyChange(current, mode, placement, deltaLayers, event);
				stack.set(net.minecraft.core.component.DataComponents.BANNER_PATTERNS, newLayers);
			}
		}
	}

	private BannerPatternLayers applyChange(
		BannerPatternLayers current, ChangeMode mode, int placement,
		List<BannerPatternLayers.Layer> deltaLayers, SkriptEvent event
	) {
		List<BannerPatternLayers.Layer> layers = new ArrayList<>(current.layers());

		if (placement >= 1) {
			BannerPatternLayers.Layer pattern = deltaLayers.size() == 1 ? deltaLayers.getFirst() : null;
			switch (mode) {
				case SET -> {
					// Pad with filler if needed
					while (layers.size() < placement) {
						layers.add(createFillerLayer(event));
					}
					layers.set(placement - 1, pattern);
				}
				case DELETE -> {
					if (layers.size() >= placement)
						layers.remove(placement - 1);
				}
				default -> {}
			}
		} else {
			switch (mode) {
				case SET -> {
					layers.clear();
					layers.addAll(deltaLayers);
				}
				case DELETE -> layers.clear();
				case ADD -> layers.addAll(deltaLayers);
				case REMOVE -> layers.removeAll(deltaLayers);
				default -> {}
			}
		}

		return new BannerPatternLayers(layers);
	}

	private @Nullable BannerPatternLayers.Layer createFillerLayer(SkriptEvent event) {
		// Use "base" pattern as filler
		if (event.server() != null) {
			var holder = ExprNewBannerPattern.resolvePattern(event.server(), "base");
			if (holder != null)
				return new BannerPatternLayers.Layer(holder, DyeColor.WHITE);
		}
		return null;
	}

	@Override
	public boolean isSingle() {
		return patternNumber != null && getExpr().isSingle();
	}

	@Override
	public Class<BannerPatternLayers.Layer> getReturnType() {
		return BannerPatternLayers.Layer.class;
	}

	@Override
	public String toString(@Nullable SkriptEvent event, boolean debug) {
		SyntaxStringBuilder builder = new SyntaxStringBuilder(event, debug);
		if (patternNumber != null) {
			builder.append("banner pattern", patternNumber);
		} else {
			builder.append("banner patterns");
		}
		builder.append("of", objects);
		return builder.toString();
	}
}
