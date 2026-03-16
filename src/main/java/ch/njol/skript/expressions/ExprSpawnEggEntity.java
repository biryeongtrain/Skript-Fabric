package ch.njol.skript.expressions;

import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.SimplePropertyExpression;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SpawnEggItem;
import net.minecraft.world.item.component.CustomData;
import org.jetbrains.annotations.Nullable;

@Name("Spawn Egg Entity")
@Description({
	"Gets the entity type that the provided spawn eggs will spawn when used.",
	"Returns the entity type as a string (e.g. 'minecraft:zombie')."
})
@Example("set {_item} to a zombie spawn egg")
@Example("broadcast the spawn egg entity of {_item}")
@Since("2.10, Fabric")
public class ExprSpawnEggEntity extends SimplePropertyExpression<ItemStack, String> {

	static {
		register(ExprSpawnEggEntity.class, String.class, "spawn egg entity", "itemstacks");
	}

	@Override
	public @Nullable String convert(ItemStack itemStack) {
		if (itemStack.isEmpty()) {
			return null;
		}
		if (!(itemStack.getItem() instanceof SpawnEggItem)) {
			return null;
		}
		// Look up the entity type from the spawn egg item's registry key
		for (EntityType<?> entityType : BuiltInRegistries.ENTITY_TYPE) {
			SpawnEggItem egg = SpawnEggItem.byId(entityType);
			if (egg != null && egg == itemStack.getItem()) {
				return BuiltInRegistries.ENTITY_TYPE.getKey(entityType).toString();
			}
		}
		return null;
	}

	@Override
	public Class<String> getReturnType() {
		return String.class;
	}

	@Override
	protected String getPropertyName() {
		return "spawn egg entity";
	}
}
