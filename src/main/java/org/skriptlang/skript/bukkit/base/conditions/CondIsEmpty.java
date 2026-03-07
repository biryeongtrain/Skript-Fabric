package org.skriptlang.skript.bukkit.base.conditions;

import ch.njol.skript.lang.Condition;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Kleenean;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.skriptlang.skript.fabric.compat.FabricInventory;
import org.skriptlang.skript.lang.event.SkriptEvent;

public final class CondIsEmpty extends Condition {

    private enum Kind {
        ITEMSTACK,
        SLOT,
        INVENTORY
    }

    private Expression<?> target;
    private Kind kind;

    @Override
    public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
        if (expressions.length != 1) {
            return false;
        }

        target = expressions[0];
        kind = switch (matchedPattern / 2) {
            case 0 -> Kind.ITEMSTACK;
            case 1 -> Kind.SLOT;
            case 2 -> Kind.INVENTORY;
            default -> null;
        };
        if (kind == null) {
            return false;
        }

        if (!accepts(kind, target)) {
            return false;
        }

        setNegated((matchedPattern % 2) == 1);
        return true;
    }

    @Override
    public boolean check(SkriptEvent event) {
        Object value = target.getSingle(event);
        if (value == null) {
            return false;
        }

        boolean empty = switch (kind) {
            case ITEMSTACK -> ((ItemStack) value).isEmpty();
            case SLOT -> !((Slot) value).hasItem() || ((Slot) value).getItem().isEmpty();
            case INVENTORY -> isInventoryEmpty((FabricInventory) value);
        };
        return isNegated() ? !empty : empty;
    }

    private boolean accepts(Kind kind, Expression<?> expression) {
        return switch (kind) {
            case ITEMSTACK -> expression.canReturn(ItemStack.class);
            case SLOT -> expression.canReturn(Slot.class);
            case INVENTORY -> expression.canReturn(FabricInventory.class);
        };
    }

    private boolean isInventoryEmpty(FabricInventory inventory) {
        for (int slot = 0; slot < inventory.container().getContainerSize(); slot++) {
            if (!inventory.container().getItem(slot).isEmpty()) {
                return false;
            }
        }
        return true;
    }

    @Override
    public String toString(SkriptEvent event, boolean debug) {
        return target + (isNegated() ? " is not empty" : " is empty");
    }
}
