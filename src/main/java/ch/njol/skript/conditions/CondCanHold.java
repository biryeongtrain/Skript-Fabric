package ch.njol.skript.conditions;

import ch.njol.skript.conditions.base.PropertyCondition;
import ch.njol.skript.conditions.base.PropertyCondition.PropertyType;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Condition;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Kleenean;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.fabric.compat.FabricInventory;
import org.skriptlang.skript.fabric.compat.FabricItemType;
import org.skriptlang.skript.lang.event.SkriptEvent;

@Name("Can Hold")
@Description("Tests whether a player or a chest can hold the given item.")
@Example("block can hold 200 cobblestone")
@Example("player has enough space for 64 feathers")
@Since("1.0")
public class CondCanHold extends Condition {

    static {
        ch.njol.skript.Skript.registerCondition(
                CondCanHold.class,
                "%inventories% (can hold|ha(s|ve) [enough] space (for|to hold)) %itemtypes%",
                "%inventories% (can(no|')t hold|(ha(s|ve) not|ha(s|ve)n't|do[es]n't have) [enough] space (for|to hold)) %itemtypes%"
        );
    }

    private Expression<FabricInventory> inventories;
    private Expression<FabricItemType> items;

    @Override
    @SuppressWarnings("unchecked")
    public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parser) {
        inventories = (Expression<FabricInventory>) exprs[0];
        items = (Expression<FabricItemType>) exprs[1];
        setNegated(matchedPattern == 1);
        return true;
    }

    @Override
    public boolean check(SkriptEvent event) {
        return inventories.check(event, inventory -> {
            if (!items.getAnd()) {
                return items.check(event, item -> canHold(inventory, item));
            }
            ItemStack[] contents = new ItemStack[inventory.container().getContainerSize()];
            for (int slot = 0; slot < contents.length; slot++) {
                contents[slot] = inventory.container().getItem(slot).copy();
            }
            return items.check(event, item -> addTo(contents, item.toStack()));
        }, isNegated());
    }

    private static boolean canHold(FabricInventory inventory, FabricItemType item) {
        ItemStack[] contents = new ItemStack[inventory.container().getContainerSize()];
        for (int slot = 0; slot < contents.length; slot++) {
            contents[slot] = inventory.container().getItem(slot).copy();
        }
        return addTo(contents, item.toStack());
    }

    private static boolean addTo(ItemStack[] contents, ItemStack stack) {
        int remaining = stack.getCount();
        for (ItemStack content : contents) {
            if (content.isEmpty()) {
                remaining -= stack.getMaxStackSize();
            } else if (ItemStack.isSameItemSameComponents(content, stack)) {
                remaining -= Math.max(0, content.getMaxStackSize() - content.getCount());
            }
            if (remaining <= 0) {
                return true;
            }
        }
        return false;
    }

    @Override
    public String toString(@Nullable SkriptEvent event, boolean debug) {
        return PropertyCondition.toString(this, PropertyType.CAN, event, debug, inventories,
                "hold " + items.toString(event, debug));
    }
}
