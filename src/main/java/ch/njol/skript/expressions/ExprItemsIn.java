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
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.fabric.compat.FabricInventory;
import org.skriptlang.skript.fabric.compat.FabricItemType;
import org.skriptlang.skript.lang.event.SkriptEvent;
import ch.njol.skript.lang.SkriptParser.ParseResult;

@Name("Items In")
@Description("All items or specific item types in an inventory.")
@Example("loop all items in the player's inventory:")
@Example("set {inventory::*} to items in the player's inventory")
@Since("2.0")
public final class ExprItemsIn extends SimpleExpression<Slot> {

    private Expression<FabricInventory> inventories;
    private @Nullable Expression<FabricItemType> types;

    @Override
    @SuppressWarnings("unchecked")
    public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
        if (matchedPattern == 0) {
            inventories = (Expression<FabricInventory>) exprs[0];
        } else {
            types = (Expression<FabricItemType>) exprs[0];
            inventories = (Expression<FabricInventory>) exprs[1];
        }
        return true;
    }

    @Override
    protected Slot @Nullable [] get(SkriptEvent event) {
        List<Slot> results = new ArrayList<>();
        FabricItemType[] allowedTypes = types == null ? null : types.getArray(event);
        for (FabricInventory inventory : inventories.getArray(event)) {
            for (int slot = 0; slot < inventory.container().getContainerSize(); slot++) {
                ItemStack stack = inventory.container().getItem(slot);
                if (isAllowedItem(allowedTypes, stack)) {
                    results.add(new Slot(inventory.container(), slot, 0, 0));
                }
            }
        }
        return results.toArray(Slot[]::new);
    }

    private boolean isAllowedItem(@Nullable FabricItemType[] allowedTypes, ItemStack stack) {
        if (stack.isEmpty()) {
            return false;
        }
        if (allowedTypes == null) {
            return true;
        }
        for (FabricItemType type : allowedTypes) {
            if (type.isOfType(stack)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean isLoopOf(String input) {
        return "item".equalsIgnoreCase(input);
    }

    @Override
    public boolean isSingle() {
        return false;
    }

    @Override
    public Class<? extends Slot> getReturnType() {
        return Slot.class;
    }

    @Override
    public String toString(@Nullable SkriptEvent event, boolean debug) {
        if (types == null) {
            return "items in " + inventories.toString(event, debug);
        }
        return types.toString(event, debug) + " in " + inventories.toString(event, debug);
    }
}
