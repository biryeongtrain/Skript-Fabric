package ch.njol.skript.expressions;

import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.util.Kleenean;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.fabric.compat.FabricInventory;
import org.skriptlang.skript.lang.event.SkriptEvent;
import ch.njol.skript.lang.SkriptParser.ParseResult;

@Name("Inventory Holder/Rows/Slots")
@Description("Gets the holder, amount of rows, or amount of slots of an inventory.")
@Example("amount of rows of player's inventory")
@Example("holder of player's ender chest")
@Since("2.2-dev34")
public final class ExprInventoryInfo extends SimpleExpression<Object> {

    private static final int HOLDER = 1;
    private static final int ROWS = 2;
    private static final int SLOTS = 3;

    private Expression<FabricInventory> inventories;
    private int type;

    @Override
    @SuppressWarnings("unchecked")
    public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
        inventories = (Expression<FabricInventory>) exprs[0];
        type = parseResult.mark;
        return true;
    }

    @Override
    protected Object @Nullable [] get(SkriptEvent event) {
        FabricInventory[] values = inventories.getArray(event);
        return switch (type) {
            case HOLDER -> {
                List<Object> holders = new ArrayList<>();
                for (FabricInventory inventory : values) {
                    if (inventory.holder() != null) {
                        holders.add(inventory.holder());
                    }
                }
                yield holders.toArray();
            }
            case ROWS -> {
                List<Number> rows = new ArrayList<>();
                for (FabricInventory inventory : values) {
                    int size = inventory.container().getContainerSize();
                    rows.add(size == 0 ? 0 : Math.max(1, (size + 8) / 9));
                }
                yield rows.toArray(Number[]::new);
            }
            case SLOTS -> {
                List<Number> slots = new ArrayList<>();
                for (FabricInventory inventory : values) {
                    slots.add(inventory.container().getContainerSize());
                }
                yield slots.toArray(Number[]::new);
            }
            default -> (Object[]) Array.newInstance(getReturnType(), 0);
        };
    }

    @Override
    public boolean isSingle() {
        return inventories.isSingle();
    }

    @Override
    public Class<?> getReturnType() {
        return type == HOLDER ? Object.class : Number.class;
    }

    @Override
    public String toString(@Nullable SkriptEvent event, boolean debug) {
        return switch (type) {
            case HOLDER -> "holder of " + inventories.toString(event, debug);
            case ROWS -> "rows of " + inventories.toString(event, debug);
            case SLOTS -> "slots of " + inventories.toString(event, debug);
            default -> inventories.toString(event, debug);
        };
    }
}
