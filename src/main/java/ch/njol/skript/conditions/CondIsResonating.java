package ch.njol.skript.conditions;

import ch.njol.skript.conditions.base.PropertyCondition;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import net.minecraft.world.level.block.entity.BellBlockEntity;
import org.skriptlang.skript.fabric.compat.FabricBlock;
import org.skriptlang.skript.fabric.compat.PrivateBellAccess;

@Name("Bell Is Resonating")
@Description({
	"Checks to see if a bell is currently resonating.",
	"A bell will start resonating five game ticks after being rung, and will continue to resonate for 40 game ticks."
})
@Example("target block is resonating")
@Since("2.9.0")
public class CondIsResonating extends PropertyCondition<FabricBlock> {

	static {
		register(CondIsResonating.class, "resonating", "blocks");
	}

	@Override
	public boolean check(FabricBlock value) {
		if (!(value.level().getBlockEntity(value.position()) instanceof BellBlockEntity bell)) {
			return false;
		}
		return PrivateBellAccess.isResonating(bell);
	}

	@Override
	protected String getPropertyName() {
		return "resonating";
	}
}
