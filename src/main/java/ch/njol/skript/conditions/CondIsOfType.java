package ch.njol.skript.conditions;

import ch.njol.skript.conditions.base.PropertyCondition;
import ch.njol.skript.conditions.base.PropertyCondition.PropertyType;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.entity.EntityData;
import ch.njol.skript.lang.Condition;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Kleenean;
import java.util.function.Predicate;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.fabric.compat.FabricItemType;
import org.skriptlang.skript.lang.event.SkriptEvent;

@Name("Is of Type")
@Description("Checks whether an item or an entity is of the given type. This is mostly useful for variables," +
	" as you can use the general 'is' condition otherwise (e.g. 'victim is a creeper').")
@Example("tool is of type {selected type}")
@Example("victim is of type {villager type}")
@Since("1.4")
public class CondIsOfType extends Condition {

	static {
		PropertyCondition.register(CondIsOfType.class, "of type[s] %itemtypes/entitydatas%", "itemstacks/entities");
	}

	private Expression<?> what;
	private Expression<?> types;

	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		what = exprs[0];
		types = exprs[1];
		setNegated(matchedPattern == 1);
		return true;
	}

	@Override
	@SuppressWarnings("unchecked")
	public boolean check(SkriptEvent event) {
		return what.check(event,
			(Predicate<Object>) left -> types.check(event,
				(Predicate<Object>) right -> {
					if (right instanceof FabricItemType itemType) {
						if (left instanceof ItemStack stack) {
							return itemType.isOfType(stack);
						}
						if (left instanceof ItemEntity itemEntity) {
							return itemType.isOfType(itemEntity.getItem());
						}
						return false;
					}
					if (right instanceof EntityData<?> entityData && left instanceof Entity entity) {
						return entityData.isInstance(entity);
					}
					return false;
				}),
			isNegated());
	}

	@Override
	public String toString(@Nullable SkriptEvent event, boolean debug) {
		return PropertyCondition.toString(this, PropertyType.BE, event, debug, what,
			"of " + (types.isSingle() ? "type " : "types ") + types.toString(event, debug));
	}

}
