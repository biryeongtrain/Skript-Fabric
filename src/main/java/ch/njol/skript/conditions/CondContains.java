package ch.njol.skript.conditions;

import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Condition;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Kleenean;
import java.util.Locale;
import java.util.Objects;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.fabric.compat.FabricInventory;
import org.skriptlang.skript.fabric.compat.FabricItemType;
import org.skriptlang.skript.lang.comparator.Comparators;
import org.skriptlang.skript.lang.comparator.Relation;
import org.skriptlang.skript.lang.event.SkriptEvent;

@Name("Contains")
@Description("Checks whether an inventory contains an item, a text contains another piece of text, or a list of objects contains another object.")
@Example("player's inventory contains 4 flint")
@Example("\"Skript Fabric\" contains \"Fabric\"")
@Example("{list::*} contains 5")
@Since("1.0")
public final class CondContains extends Condition {

    private enum CheckType {
        INVENTORY,
        STRING,
        OBJECTS
    }

    private Expression<?> containers;
    private Expression<?> items;
    private CheckType checkType;

    @Override
    public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
        containers = exprs[0];
        items = exprs[1];
        checkType = matchedPattern <= 1 ? CheckType.INVENTORY : CheckType.OBJECTS;
        setNegated(matchedPattern % 2 == 1);
        return true;
    }

    @Override
    public boolean check(SkriptEvent event) {
        Object[] containerValues = containers.getAll(event);
        if (containerValues.length == 0) {
            return isNegated();
        }

        CheckType resolvedType = checkType;
        if (resolvedType == CheckType.OBJECTS) {
            boolean allStrings = true;
            for (Object value : containerValues) {
                if (!(value instanceof String)) {
                    allStrings = false;
                    break;
                }
            }
            if (allStrings && containers.isSingle()) {
                resolvedType = CheckType.STRING;
            }
        }

        boolean result = switch (resolvedType) {
            case INVENTORY -> containers.check(event, value -> value instanceof FabricInventory inventory
                    && items.check(event, candidate -> inventoryContains(inventory, candidate)));
            case STRING -> containers.check(event, value -> value instanceof String string
                    && items.check(event, candidate -> candidate instanceof String text && containsIgnoreCase(string, text)));
            case OBJECTS -> items.check(event, needle -> {
                for (Object value : containerValues) {
                    if (Comparators.compare(needle, value) == Relation.EQUAL || Objects.equals(needle, value)) {
                        return true;
                    }
                }
                return false;
            });
        };
        return isNegated() ^ result;
    }

    private boolean inventoryContains(FabricInventory inventory, Object candidate) {
        if (candidate instanceof FabricInventory other) {
            return inventory.container() == other.container();
        }
        if (candidate instanceof FabricItemType itemType) {
            return countMatchingItems(inventory, itemType) >= itemType.amount();
        }
        if (candidate instanceof ItemStack stack) {
            return countMatchingStacks(inventory, stack) >= Math.max(1, stack.getCount());
        }
        return false;
    }

    private int countMatchingItems(FabricInventory inventory, FabricItemType itemType) {
        int amount = 0;
        for (int slot = 0; slot < inventory.container().getContainerSize(); slot++) {
            ItemStack stack = inventory.container().getItem(slot);
            if (itemType.isOfType(stack)) {
                amount += stack.getCount();
            }
        }
        return amount;
    }

    private int countMatchingStacks(FabricInventory inventory, ItemStack expected) {
        int amount = 0;
        for (int slot = 0; slot < inventory.container().getContainerSize(); slot++) {
            ItemStack stack = inventory.container().getItem(slot);
            if (!stack.isEmpty() && ItemStack.isSameItemSameComponents(expected, stack)) {
                amount += stack.getCount();
            }
        }
        return amount;
    }

    private boolean containsIgnoreCase(String value, String text) {
        return value.toLowerCase(Locale.ROOT).contains(text.toLowerCase(Locale.ROOT));
    }

    @Override
    public String toString(@Nullable SkriptEvent event, boolean debug) {
        return containers.toString(event, debug) + (isNegated() ? " does not contain " : " contains ")
                + items.toString(event, debug);
    }
}
