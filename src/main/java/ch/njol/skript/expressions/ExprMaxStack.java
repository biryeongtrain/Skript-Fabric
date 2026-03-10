package ch.njol.skript.expressions;

import ch.njol.skript.Skript;
import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.RequiredPlugins;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.SimplePropertyExpression;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.fabric.compat.FabricInventory;
import org.skriptlang.skript.fabric.compat.FabricItemType;
import org.skriptlang.skript.lang.event.SkriptEvent;

@Name("Maximum Stack Size")
@Description({
        "The maximum stack size of an item or inventory.",
        "In 1.20.5+, the maximum stack size of items can be changed to any integer from 1 to 99."
})
@Example("send \"You can hold %max stack size of player's tool% of %type of player's tool% in a slot.\" to player")
@Example("add 8 to the maximum stack size of player's tool")
@Since("2.1, 2.10 (changeable, inventories)")
@RequiredPlugins("Minecraft 1.20.5+ (changeable)")
public class ExprMaxStack extends SimplePropertyExpression<Object, Integer> {

    static {
        register(ExprMaxStack.class, Integer.class, "max[imum] stack[[ ]size]", "itemtypes/inventories");
    }

    @Override
    public @Nullable Integer convert(Object from) {
        if (from instanceof FabricItemType itemType) {
            return itemType.toStack().getMaxStackSize();
        }
        if (from instanceof FabricInventory inventory) {
            return inventory.container().getMaxStackSize();
        }
        return null;
    }

    @Override
    public @Nullable Class<?>[] acceptChange(ChangeMode mode) {
        if (mode != ChangeMode.ADD && mode != ChangeMode.REMOVE && mode != ChangeMode.RESET && mode != ChangeMode.SET) {
            return null;
        }
        if (FabricInventory.class.isAssignableFrom(getExpr().getReturnType())) {
            Skript.error("Changing the maximum stack size of inventories is not available on the current Fabric compat layer.");
            return null;
        }
        return new Class[]{Integer.class};
    }

    @Override
    public void change(SkriptEvent event, Object @Nullable [] delta, ChangeMode mode) {
        int amount = delta == null ? 0 : ((Number) delta[0]).intValue();
        for (Object source : getExpr().getArray(event)) {
            if (!(source instanceof FabricItemType itemType)) {
                continue;
            }
            ItemStack stack = itemType.toStack();
            int size = stack.getMaxStackSize();
            switch (mode) {
                case ADD -> size += amount;
                case SET -> size = amount;
                case REMOVE -> size -= amount;
                case RESET -> stack = new ItemStack(itemType.item(), itemType.amount());
                default -> {
                }
            }
            if (mode != ChangeMode.RESET) {
                stack.set(DataComponents.MAX_STACK_SIZE, Math.clamp(size, 1, 99));
            }
            itemType.applyPrototype(stack);
        }
    }

    @Override
    public Class<? extends Integer> getReturnType() {
        return Integer.class;
    }

    @Override
    protected String getPropertyName() {
        return "maximum stack size";
    }
}
