package ch.njol.skript.expressions;

import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.SimplePropertyExpression;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.fabric.compat.FabricBlock;

@Name("Exact Item")
@Description(
    "Get an exact item representation of a block, carrying over any data. "
    + "For example, using this expression on a chest block with items stored inside will return a chest "
    + "item with the exact same items in its inventory as the chest block."
)
@Example("set {_item} to exact item of block at location(0, 0, 0)")
@Since("2.12")
public class ExprExactItem extends SimplePropertyExpression<FabricBlock, ItemStack> {

    static {
        register(ExprExactItem.class, ItemStack.class, "exact item[s]", "blocks");
    }

    @Override
    public @Nullable ItemStack convert(FabricBlock block) {
        Item item = block.block().asItem();
        if (item == Items.AIR) {
            return null;
        }
        return new ItemStack(item);
    }

    @Override
    public Class<? extends ItemStack> getReturnType() {
        return ItemStack.class;
    }

    @Override
    protected String getPropertyName() {
        return "exact item";
    }
}
