package ch.njol.skript.expressions;

import ch.njol.skript.doc.*;
import ch.njol.skript.expressions.base.SimplePropertyExpression;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Kleenean;
import net.minecraft.world.level.block.entity.BeehiveBlockEntity;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.fabric.compat.FabricBlock;

@Name("Entity Storage Entity Count")
@Description({
	"The current number of entities stored inside an entity block storage (i.e. beehive).",
	"The maximum amount of entities an entity block storage can hold."
})
@Example("broadcast the stored entity count of {_beehive}")
@Since("2.11")
public class ExprEntityStorageEntityCount extends SimplePropertyExpression<FabricBlock, Integer> {

	static {
		registerDefault(ExprEntityStorageEntityCount.class, Integer.class, "[max:max[imum]] [stored] entity count", "blocks");
	}

	private boolean withMax;

	@Override
	public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		withMax = parseResult.hasTag("max");
		return super.init(expressions, matchedPattern, isDelayed, parseResult);
	}

	@Override
	public @Nullable Integer convert(FabricBlock block) {
		if (!(block.level().getBlockEntity(block.position()) instanceof BeehiveBlockEntity beehive))
			return null;
		if (withMax)
			return 3;
		return beehive.getOccupantCount();
	}

	@Override
	public Class<Integer> getReturnType() {
		return Integer.class;
	}

	@Override
	protected String getPropertyName() {
		return (withMax ? "maximum " : "") + "stored entity count";
	}

}
