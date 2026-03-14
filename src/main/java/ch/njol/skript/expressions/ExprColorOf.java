package ch.njol.skript.expressions;

import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.PropertyExpression;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.util.Color;
import ch.njol.skript.util.ColorRGB;
import ch.njol.skript.util.DyeColorMapping;
import ch.njol.util.Kleenean;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.entity.Display;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.animal.Cat;
import net.minecraft.world.entity.animal.sheep.Sheep;
import net.minecraft.world.entity.animal.wolf.Wolf;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.DyedItemColor;
import net.minecraft.world.level.block.entity.BannerBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.fabric.compat.FabricBlock;
import org.skriptlang.skript.lang.event.SkriptEvent;

@Name("Color of")
@Description({
	"The color of an item, entity, or block.",
	"Supports banner base color, sheep wool color, cat/wolf collar color (read-only), " +
	"dyed items (leather armor, etc.), and text display background color.",
	"Setting is supported for sheep, text displays, and dyed items."
})
@Example("""
	on click on wool:
		message "This wool block is %color of block%!"
	""")
@Since("2.10")
public class ExprColorOf extends PropertyExpression<Object, Color> {

	static {
		register(ExprColorOf.class, Color.class, "colo[u]r[s]", "blocks/itemstacks/entities");
	}

	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		setExpr(exprs[0]);
		return true;
	}

	@Override
	protected Color[] get(SkriptEvent event, Object[] source) {
		return get(source, ExprColorOf::getColor);
	}

	private static @Nullable Color getColor(Object object) {
		if (object instanceof FabricBlock block) {
			BlockEntity be = block.level().getBlockEntity(block.position());
			if (be instanceof BannerBlockEntity banner)
				return DyeColorMapping.toColor(banner.getBaseColor());
		}
		if (object instanceof Entity entity) {
			if (entity instanceof Sheep sheep)
				return DyeColorMapping.toColor(sheep.getColor());
			if (entity instanceof Cat cat)
				return DyeColorMapping.toColor(cat.getCollarColor());
			if (entity instanceof Wolf wolf)
				return DyeColorMapping.toColor(wolf.getCollarColor());
			if (entity instanceof Display.TextDisplay textDisplay) {
				int bgColor = textDisplay.getBackgroundColor();
				if (bgColor == Display.TextDisplay.INITIAL_BACKGROUND)
					return null;
				return ColorRGB.fromRgb(bgColor & 0x00FFFFFF);
			}
		}
		if (object instanceof ItemStack stack) {
			DyedItemColor dyed = stack.get(DataComponents.DYED_COLOR);
			if (dyed != null)
				return ColorRGB.fromRgb(dyed.rgb());
		}
		return null;
	}

	@Override
	public Class<?> @Nullable [] acceptChange(ChangeMode mode) {
		if (mode == ChangeMode.SET)
			return new Class[]{Color.class};
		if (mode == ChangeMode.RESET)
			return new Class[0];
		return null;
	}

	@Override
	public void change(SkriptEvent event, Object @Nullable [] delta, ChangeMode mode) {
		Color color = (delta != null && delta.length > 0) ? (Color) delta[0] : null;
		for (Object object : getExpr().getArray(event)) {
			if (mode == ChangeMode.SET && color != null) {
				setColor(object, color);
			} else if (mode == ChangeMode.RESET) {
				resetColor(object);
			}
		}
	}

	private static void setColor(Object object, Color color) {
		if (object instanceof Entity entity) {
			DyeColor dyeColor = DyeColorMapping.toDyeColor(color);
			if (entity instanceof Sheep sheep && dyeColor != null) {
				sheep.setColor(dyeColor);
				return;
			}
			if (entity instanceof Display.TextDisplay textDisplay) {
				int argb = (0xFF << 24) | color.rgb();
				textDisplay.setBackgroundColor(argb);
				return;
			}
		}
		if (object instanceof ItemStack stack) {
			stack.set(DataComponents.DYED_COLOR, new DyedItemColor(color.rgb()));
		}
	}

	private static void resetColor(Object object) {
		if (object instanceof Entity entity) {
			if (entity instanceof Sheep sheep) {
				sheep.setColor(DyeColor.WHITE);
				return;
			}
			if (entity instanceof Display.TextDisplay textDisplay) {
				textDisplay.setBackgroundColor(Display.TextDisplay.INITIAL_BACKGROUND);
				return;
			}
		}
		if (object instanceof ItemStack stack) {
			stack.remove(DataComponents.DYED_COLOR);
		}
	}

	@Override
	public Class<? extends Color> getReturnType() {
		return Color.class;
	}

	@Override
	public String toString(@Nullable SkriptEvent event, boolean debug) {
		return "color of " + getExpr().toString(event, debug);
	}
}
