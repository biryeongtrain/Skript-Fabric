package ch.njol.skript.expressions;

import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Keywords;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.SimplePropertyExpression;
import net.minecraft.world.inventory.Slot;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.fabric.compat.FabricInventory;

@Name("First Empty Slot in Inventory")
@Description("Returns the first empty slot in an inventory.")
@Example("set the first empty slot in player's inventory to 5 diamonds")
@Since("2.12")
@Keywords({"full", "inventory", "empty", "air", "slot"})
public final class ExprFirstEmptySlot extends SimplePropertyExpression<FabricInventory, Slot> {

    @Override
    public @Nullable Slot convert(FabricInventory from) {
        for (int slot = 0; slot < from.container().getContainerSize(); slot++) {
            if (from.container().getItem(slot).isEmpty()) {
                return new Slot(from.container(), slot, 0, 0);
            }
        }
        return null;
    }

    @Override
    public Class<? extends Slot> getReturnType() {
        return Slot.class;
    }

    @Override
    protected String getPropertyName() {
        return "first empty slot";
    }
}
