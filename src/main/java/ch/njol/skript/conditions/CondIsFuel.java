package ch.njol.skript.conditions;
import ch.njol.skript.conditions.base.PropertyCondition;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Condition;
import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.SimplifiedCondition;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.level.block.entity.FuelValues;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.fabric.compat.FabricItemType;

@Name("Is Fuel")
@Description("Checks whether an item can be used as fuel in a furnace.")
@Example("""
	on right click on furnace:
		if player's tool is not fuel:
			send "Please hold a valid fuel item in your hand"
			cancel event
	""")
@Since("2.5.1")
public class CondIsFuel extends PropertyCondition<FabricItemType> {

	private static @Nullable FuelValues fuelValues;

	static {
		register(CondIsFuel.class, "[furnace] fuel", "itemtypes");
	}

	@Override
	public boolean check(FabricItemType item) {
		return getFuelValues().isFuel(item.toStack());
	}

	public Condition simplify() {
		if (getExpr() instanceof Literal<? extends FabricItemType>)
			return SimplifiedCondition.fromCondition(this);
		return this;
	}

	@Override
	protected String getPropertyName() {
		return "fuel";
	}

	private static FuelValues getFuelValues() {
		if (fuelValues == null) {
			RegistryAccess.Frozen access = RegistryAccess.fromRegistryOfRegistries(BuiltInRegistries.REGISTRY);
			fuelValues = FuelValues.vanillaBurnTimes(access, FeatureFlags.DEFAULT_FLAGS);
		}
		return fuelValues;
	}

}
