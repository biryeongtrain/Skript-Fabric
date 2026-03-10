package ch.njol.skript.expressions;

import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.util.Kleenean;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.world.inventory.Slot;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.fabric.compat.FabricInventory;
import org.skriptlang.skript.lang.event.SkriptEvent;
import ch.njol.skript.lang.SkriptParser.ParseResult;

@Name("Inventory Slot")
@Description("Represents a slot in an inventory.")
@Example("set slot 0 of player's inventory to 2 stones")
@Since("2.2-dev24")
public final class ExprInventorySlot extends SimpleExpression<Slot> {

    private Expression<Number> slots;
    private Expression<FabricInventory> inventories;

    @Override
    @SuppressWarnings("unchecked")
    public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
        if (matchedPattern == 0) {
            slots = (Expression<Number>) exprs[0];
            inventories = (Expression<FabricInventory>) exprs[1];
        } else {
            inventories = (Expression<FabricInventory>) exprs[0];
            slots = (Expression<Number>) exprs[1];
        }
        return true;
    }

    @Override
    protected Slot @Nullable [] get(SkriptEvent event) {
        List<Slot> resolved = new ArrayList<>();
        for (FabricInventory inventory : inventories.getArray(event)) {
            int size = inventory.container().getContainerSize();
            for (Number number : slots.getArray(event)) {
                int index = number.intValue();
                if (index >= 0 && index < size) {
                    resolved.add(new Slot(inventory.container(), index, 0, 0));
                }
            }
        }
        return resolved.toArray(Slot[]::new);
    }

    @Override
    public boolean isSingle() {
        return inventories.isSingle() && slots.isSingle();
    }

    @Override
    public Class<? extends Slot> getReturnType() {
        return Slot.class;
    }

    @Override
    public String toString(@Nullable SkriptEvent event, boolean debug) {
        return "slot " + slots.toString(event, debug) + " of " + inventories.toString(event, debug);
    }
}
