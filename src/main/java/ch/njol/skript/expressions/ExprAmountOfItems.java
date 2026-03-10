package ch.njol.skript.expressions;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.util.Kleenean;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.fabric.compat.FabricInventory;
import org.skriptlang.skript.fabric.compat.FabricItemType;
import org.skriptlang.skript.lang.event.SkriptEvent;

@Name("Amount of Items")
@Description("Counts how many of a particular <a href='#itemtype'>item type</a> are in a given inventory.")
@Example("message \"You have %number of tag values of minecraft tag \"diamond_ores\" in the player's inventory% diamond ores in your inventory.\"")
@Since("2.0")
public class ExprAmountOfItems extends SimpleExpression<Long> {

    static {
        Skript.registerExpression(ExprAmountOfItems.class, Long.class,
                "[the] (amount|number) of %itemtypes% (in|of) %inventories%");
    }

    private Expression<FabricItemType> items;
    private Expression<FabricInventory> inventories;

    @Override
    @SuppressWarnings("unchecked")
    public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
        items = (Expression<FabricItemType>) exprs[0];
        inventories = (Expression<FabricInventory>) exprs[1];
        return true;
    }

    @Override
    protected Long[] get(SkriptEvent event) {
        FabricItemType[] itemTypes = items.getArray(event);
        long amount = 0;
        for (FabricInventory inventory : inventories.getArray(event)) {
            for (int slot = 0; slot < inventory.container().getContainerSize(); slot++) {
                var itemStack = inventory.container().getItem(slot);
                if (itemStack.isEmpty()) {
                    continue;
                }
                for (FabricItemType itemType : itemTypes) {
                    if (itemType.matches(itemStack)) {
                        amount += itemStack.getCount();
                        break;
                    }
                }
            }
        }
        return new Long[]{amount};
    }

    @Override
    public Class<? extends Long> getReturnType() {
        return Long.class;
    }

    @Override
    public boolean isSingle() {
        return true;
    }

    @Override
    public String toString(@Nullable SkriptEvent event, boolean debug) {
        return "the number of " + items.toString(event, debug) + " in " + inventories.toString(event, debug);
    }
}
