package ch.njol.skript.expressions;

import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.RequiredPlugins;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.SimplePropertyExpression;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.fabric.compat.FabricItemType;
import org.skriptlang.skript.lang.event.SkriptEvent;

@Name("Max Durability")
@Description({
        "The maximum durability of an item. Changing requires Minecraft 1.20.5+",
        "Note: 'delete' will remove the max durability from the item (making it a non-damageable item)."
})
@Example("maximum durability of diamond sword")
@Example("if max durability of player's tool is not 0: # Item is damageable")
@Example("set max durability of player's tool to 5000")
@Example("add 5 to max durability of player's tool")
@Example("reset max durability of player's tool")
@Example("delete max durability of player's tool")
@RequiredPlugins("Minecraft 1.20.5+ (custom amount)")
@Since("2.5, 2.9.0 (change)")
public class ExprMaxDurability extends SimplePropertyExpression<Object, Integer> {

    static {
        register(ExprMaxDurability.class, Integer.class, "max[imum] (durabilit(y|ies)|damage)", "itemtypes/itemstacks/slots");
    }

    @Override
    public @Nullable Integer convert(Object object) {
        ItemStack stack = asItemStack(object);
        return stack == null || !stack.isDamageableItem() ? null : stack.getMaxDamage();
    }

    @Override
    public @Nullable Class<?>[] acceptChange(ChangeMode mode) {
        return switch (mode) {
            case SET, ADD, REMOVE, RESET, DELETE -> new Class[]{Number.class};
            default -> null;
        };
    }

    @Override
    public void change(SkriptEvent event, @Nullable Object[] delta, ChangeMode mode) {
        int change = delta == null ? 0 : ((Number) delta[0]).intValue();
        if (mode == ChangeMode.REMOVE) {
            change = -change;
        }

        for (Object object : getExpr().getArray(event)) {
            ItemStack stack = asItemStack(object);
            if (stack == null || !stack.isDamageableItem()) {
                continue;
            }

            int next = switch (mode) {
                case ADD, REMOVE -> stack.getMaxDamage() + change;
                case SET -> change;
                case DELETE -> 0;
                case RESET -> stack.getItem().components().getOrDefault(DataComponents.MAX_DAMAGE, 0);
                default -> stack.getMaxDamage();
            };
            applyMaxDamage(object, stack, Math.max(0, next));
        }
    }

    private void applyMaxDamage(Object holder, ItemStack original, int maxDamage) {
        ItemStack updated = original.copy();
        if (maxDamage <= 0) {
            updated.remove(DataComponents.MAX_DAMAGE);
            updated.remove(DataComponents.DAMAGE);
        } else {
            updated.set(DataComponents.MAX_DAMAGE, maxDamage);
            updated.set(DataComponents.DAMAGE, Math.min(updated.getDamageValue(), maxDamage));
        }
        if (holder instanceof Slot slot) {
            slot.set(updated);
        } else if (holder instanceof ItemStack direct) {
            direct.applyComponents(updated.getComponents());
            direct.setCount(updated.getCount());
        } else if (holder instanceof FabricItemType itemType) {
            itemType.applyPrototype(updated);
        }
    }

    private @Nullable ItemStack asItemStack(Object object) {
        if (object instanceof ItemStack stack) {
            return stack;
        }
        if (object instanceof Slot slot) {
            return slot.getItem();
        }
        if (object instanceof FabricItemType itemType) {
            return itemType.toStack();
        }
        return null;
    }

    @Override
    public Class<? extends Integer> getReturnType() {
        return Integer.class;
    }

    @Override
    protected String getPropertyName() {
        return "max durability";
    }
}
