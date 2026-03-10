package ch.njol.skript.expressions;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.util.Kleenean;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.fabric.compat.FabricItemType;
import org.skriptlang.skript.lang.event.SkriptEvent;

@Name("Items")
@Description("Items or blocks of a specific type, useful for looping.")
@Example("""
    loop tag values of tag "diamond_ores" and tag values of tag "oak_logs":
        block contains loop-item
        message "Theres at least one %loop-item% in this block"
    """)
@Example("drop all blocks at the player # drops one of every block at the player")
@Since("1.0 pre-5")
public class ExprItems extends SimpleExpression<FabricItemType> {

    private static final FabricItemType[] ALL_BLOCKS = BuiltInRegistries.ITEM.stream()
            .filter(item -> item instanceof BlockItem)
            .map(FabricItemType::new)
            .toArray(FabricItemType[]::new);

    static {
        Skript.registerExpression(ExprItems.class, FabricItemType.class,
                "[all [[of] the]|the] block[[ ]type]s",
                "every block[[ ]type]",
                "[all [[of] the]|the|every] block[s] of type[s] %itemtypes%",
                "[all [[of] the]|the|every] item[s] of type[s] %itemtypes%");
    }

    private @Nullable Expression<FabricItemType> itemTypeExpr;
    private boolean items;
    private @Nullable FabricItemType[] buffer;

    @Override
    @SuppressWarnings("unchecked")
    public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
        items = matchedPattern == 3;
        itemTypeExpr = matchedPattern <= 1 ? null : (Expression<FabricItemType>) exprs[0];
        return true;
    }

    @Override
    protected FabricItemType[] get(SkriptEvent event) {
        if (buffer != null) {
            return buffer;
        }
        List<FabricItemType> itemTypes = new ArrayList<>();
        iterator(event).forEachRemaining(itemTypes::add);
        FabricItemType[] resolved = itemTypes.toArray(FabricItemType[]::new);
        if (itemTypeExpr instanceof Literal<?>) {
            buffer = resolved;
        }
        return resolved;
    }

    @Override
    public Iterator<FabricItemType> iterator(SkriptEvent event) {
        if (!items && itemTypeExpr == null) {
            List<FabricItemType> copies = new ArrayList<>(ALL_BLOCKS.length);
            for (FabricItemType itemType : ALL_BLOCKS) {
                copies.add(new FabricItemType(itemType.toStack()));
            }
            return copies.iterator();
        }

        List<FabricItemType> values = new ArrayList<>();
        for (FabricItemType itemType : itemTypeExpr.getArray(event)) {
            Item item = itemType.item();
            if (items || item instanceof BlockItem) {
                values.add(new FabricItemType(itemType.toStack()));
            }
        }
        return values.iterator();
    }

    @Override
    public Class<? extends FabricItemType> getReturnType() {
        return FabricItemType.class;
    }

    @Override
    public boolean isSingle() {
        return false;
    }

    @Override
    public String toString(@Nullable SkriptEvent event, boolean debug) {
        return "all of the " + (items ? "items" : "blocks")
                + (itemTypeExpr != null ? " of type " + itemTypeExpr.toString(event, debug) : "");
    }

    @Override
    public boolean isLoopOf(String input) {
        return items ? input.equals("item") : input.equals("block");
    }
}
