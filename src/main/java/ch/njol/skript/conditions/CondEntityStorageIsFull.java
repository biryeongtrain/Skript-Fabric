package ch.njol.skript.conditions;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Condition;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.SyntaxStringBuilder;
import ch.njol.util.Kleenean;
import net.minecraft.world.level.block.entity.BeehiveBlockEntity;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.fabric.compat.FabricBlock;
import org.skriptlang.skript.lang.event.SkriptEvent;

@Name("Entity Storage Is Full")
@Description("Checks to see if the an entity block storage (i.e beehive) is full.")
@Example("""
	if the entity storage of {_beehive} is full:
		release the entity storage of {_beehive}
	""")
@Since("2.11")
public class CondEntityStorageIsFull extends Condition {

	static {
		Skript.registerCondition(CondEntityStorageIsFull.class,
			"[the] entity storage of %blocks% (is|are) full",
			"%blocks%'[s] entity storage (is|are) full",
			"[the] entity storage of %blocks% (isn't|is not|aren't|are not) full",
			"%blocks%'[s] entity storage (isn't|is not|aren't|are not) full");
	}

	private Expression<FabricBlock> blocks;

	@Override
	@SuppressWarnings("unchecked")
	public boolean init(Expression<?>[] exrps, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		setNegated(matchedPattern >= 2);
		blocks = (Expression<FabricBlock>) exrps[0];
		return true;
	}

	@Override
	public boolean check(SkriptEvent event) {
		return blocks.check(event, block -> block.level().getBlockEntity(block.position()) instanceof BeehiveBlockEntity beehive
			&& beehive.isFull(), isNegated());
	}

	@Override
	public String toString(@Nullable SkriptEvent event, boolean debug) {
		SyntaxStringBuilder builder = new SyntaxStringBuilder(event, debug);
		builder.append("the entity storage of", blocks);
		builder.append(blocks.isSingle() ? "is" : "are");
		builder.appendIf(isNegated(), "not");
		builder.append("full");
		return builder.toString();
	}

}
