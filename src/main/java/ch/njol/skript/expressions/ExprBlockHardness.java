package ch.njol.skript.expressions;

import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.RequiredPlugins;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.SimplePropertyExpression;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.level.block.Block;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.fabric.compat.FabricItemType;

@Name("Block Hardness")
@Description("Obtains the block's hardness level (also known as \"strength\"). This number is used to calculate the time required to break each block.")
@Example("set {_hard} to block hardness of target block")
@Example("if block hardness of target block > 5:")
@RequiredPlugins("Minecraft 1.13+")
@Since("2.6")
public class ExprBlockHardness extends SimplePropertyExpression<FabricItemType, Number> {

    static {
        register(ExprBlockHardness.class, Number.class, "[block] hardness", "itemtypes");
    }

    @Override
    public @Nullable Number convert(FabricItemType itemType) {
        if (!(itemType.item() instanceof BlockItem blockItem)) {
            return null;
        }
        Block block = blockItem.getBlock();
        return block.defaultDestroyTime();
    }

    @Override
    public Class<? extends Number> getReturnType() {
        return Number.class;
    }

    @Override
    protected String getPropertyName() {
        return "block hardness";
    }
}
