package ch.njol.skript.conditions;

import ch.njol.skript.conditions.base.PropertyCondition;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import net.minecraft.world.item.BlockItem;
import org.skriptlang.skript.fabric.compat.FabricItemType;

@Name("Is Occluding")
@Description("Checks whether an item is a block and completely blocks vision.")
@Example("player's tool is occluding")
@Since("2.5.1")
public class CondIsOccluding extends PropertyCondition<FabricItemType> {

    static {
        register(CondIsOccluding.class, "occluding", "itemtypes");
    }

    @Override
    public boolean check(FabricItemType itemType) {
        return itemType.item() instanceof BlockItem blockItem
                && blockItem.getBlock().defaultBlockState().canOcclude();
    }

    @Override
    protected String getPropertyName() {
        return "occluding";
    }
}
