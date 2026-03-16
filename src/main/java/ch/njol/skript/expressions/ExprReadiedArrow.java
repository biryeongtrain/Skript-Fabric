package ch.njol.skript.expressions;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Events;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.util.Kleenean;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.fabric.runtime.FabricReadyArrowEventHandle;
import org.skriptlang.skript.lang.event.SkriptEvent;

@Name("Readied Arrow/Bow")
@Description("The bow or arrow in a ready arrow event.")
@Example("""
	on player ready arrow:
		selected bow's name is "Spectral Bow"
		if selected arrow is not a spectral arrow:
			cancel event
	""")
@Since("2.8.0, Fabric")
@Events("ready arrow")
public class ExprReadiedArrow extends SimpleExpression<ItemStack> {

	static {
		Skript.registerExpression(ExprReadiedArrow.class, ItemStack.class, "[the] (readied|selected|drawn) (:arrow|bow)");
	}

	private boolean isArrow;

	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		isArrow = parseResult.hasTag("arrow");
		if (!getParser().isCurrentEvent(FabricReadyArrowEventHandle.class)) {
			Skript.error("'the readied " + (isArrow ? "arrow" : "bow") + "' can only be used in a ready arrow event");
			return false;
		}
		return true;
	}

	@Override
	protected ItemStack @Nullable [] get(SkriptEvent event) {
		if (!(event.handle() instanceof FabricReadyArrowEventHandle handle)) {
			return null;
		}
		if (isArrow) {
			return new ItemStack[]{handle.arrow()};
		}
		return new ItemStack[]{handle.bow()};
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
		return "the readied " + (isArrow ? "arrow" : "bow");
	}
}
