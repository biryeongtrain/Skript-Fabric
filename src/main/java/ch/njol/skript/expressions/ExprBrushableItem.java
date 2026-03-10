package ch.njol.skript.expressions;

import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.RequiredPlugins;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.SimplePropertyExpression;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BrushableBlockEntity;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.fabric.compat.FabricBlock;

@Name("Buried Item")
@Description({
    "Represents the item that is uncovered when dusting.",
    "The only blocks that can currently be \"dusted\" are Suspicious Gravel and Suspicious Sand."
})
@Example("send target block's brushable item")
@Example("set {_gravel}'s brushable item to emerald")
@Since("2.12")
@RequiredPlugins("Minecraft 1.20+")
public class ExprBrushableItem extends SimplePropertyExpression<FabricBlock, ItemStack> {

    static {
        register(ExprBrushableItem.class, ItemStack.class, "(brushable|buried) item", "blocks");
    }

    @Override
    public @Nullable ItemStack convert(FabricBlock block) {
        if (block.level().getBlockEntity(block.position()) instanceof BrushableBlockEntity brushable) {
            return brushable.getItem();
        }
        return null;
    }

    @Override
    public Class<? extends ItemStack> getReturnType() {
        return ItemStack.class;
    }

    @Override
    protected String getPropertyName() {
        return "brushable item";
    }
}
