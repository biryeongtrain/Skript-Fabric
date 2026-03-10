package ch.njol.skript.expressions;

import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.SimplePropertyExpression;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.fabric.compat.FabricItemType;
import org.skriptlang.skript.lang.event.SkriptEvent;

@Name("Item Amount")
@Description("The amount of an <a href='#itemstack'>item stack</a>.")
@Example("send \"You have got %item amount of player's tool% %player's tool% in your hand!\" to player")
@Since("2.2-dev24")
public class ExprItemAmount extends SimplePropertyExpression<Object, Long> {

    static {
        register(ExprItemAmount.class, Long.class, "item[[ ]stack] (amount|size|number)", "slots/itemtypes/itemstacks");
    }

    @Override
    public @Nullable Long convert(Object item) {
        if (item instanceof FabricItemType itemType) {
            return (long) itemType.amount();
        }
        if (item instanceof Slot slot) {
            return (long) slot.getItem().getCount();
        }
        if (item instanceof ItemStack itemStack) {
            return (long) itemStack.getCount();
        }
        return null;
    }

    @Override
    public Class<?> @Nullable [] acceptChange(ChangeMode mode) {
        return switch (mode) {
            case SET, ADD, RESET, DELETE, REMOVE -> new Class[]{Long.class, Number.class};
            default -> null;
        };
    }

    @Override
    public void change(SkriptEvent event, @Nullable Object[] delta, ChangeMode mode) {
        int amount = delta != null && delta.length > 0 ? ((Number) delta[0]).intValue() : 0;
        switch (mode) {
            case REMOVE -> {
                amount = -amount;
                for (Object value : getExpr().getArray(event)) {
                    applyAmount(value, amount, false);
                }
            }
            case ADD -> {
                for (Object value : getExpr().getArray(event)) {
                    applyAmount(value, amount, false);
                }
            }
            case RESET, DELETE -> {
                for (Object value : getExpr().getArray(event)) {
                    applyAmount(value, 1, true);
                }
            }
            case SET -> {
                for (Object value : getExpr().getArray(event)) {
                    applyAmount(value, amount, true);
                }
            }
        }
    }

    private void applyAmount(Object value, int amount, boolean absolute) {
        if (value instanceof FabricItemType itemType) {
            int next = absolute ? amount : itemType.amount() + amount;
            itemType.amount(Math.max(1, next));
            return;
        }
        if (value instanceof Slot slot) {
            ItemStack stack = slot.getItem();
            if (stack.isEmpty()) {
                return;
            }
            int next = absolute ? amount : stack.getCount() + amount;
            slot.set(next <= 0 ? ItemStack.EMPTY : stack.copyWithCount(next));
            return;
        }
        if (value instanceof ItemStack itemStack) {
            int next = absolute ? amount : itemStack.getCount() + amount;
            itemStack.setCount(Math.max(0, next));
        }
    }

    @Override
    public Class<? extends Long> getReturnType() {
        return Long.class;
    }

    @Override
    protected String getPropertyName() {
        return "item amount";
    }
}
