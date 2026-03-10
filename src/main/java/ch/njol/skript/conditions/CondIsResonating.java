package ch.njol.skript.conditions;

import ch.njol.skript.conditions.base.PropertyCondition;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import java.lang.reflect.Field;
import net.minecraft.world.level.block.entity.BellBlockEntity;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.fabric.compat.FabricBlock;

@Name("Bell Is Resonating")
@Description({
	"Checks to see if a bell is currently resonating.",
	"A bell will start resonating five game ticks after being rung, and will continue to resonate for 40 game ticks."
})
@Example("target block is resonating")
@Since("2.9.0")
public class CondIsResonating extends PropertyCondition<FabricBlock> {

	private static final @Nullable Field RESONATING_FIELD = resolveResonatingField();

	static {
		register(CondIsResonating.class, "resonating", "blocks");
	}

	@Override
	public boolean check(FabricBlock value) {
		if (!(value.level().getBlockEntity(value.position()) instanceof BellBlockEntity bell) || RESONATING_FIELD == null) {
			return false;
		}
		try {
			return RESONATING_FIELD.getBoolean(bell);
		} catch (IllegalAccessException ignored) {
			return false;
		}
	}

	@Override
	protected String getPropertyName() {
		return "resonating";
	}

	private static @Nullable Field resolveResonatingField() {
		try {
			Field field = BellBlockEntity.class.getDeclaredField("resonating");
			field.setAccessible(true);
			return field;
		} catch (ReflectiveOperationException ignored) {
			return null;
		}
	}

}
