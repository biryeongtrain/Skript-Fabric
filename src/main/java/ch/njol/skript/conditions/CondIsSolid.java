package ch.njol.skript.conditions;

import ch.njol.skript.conditions.base.PropertyCondition;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import net.minecraft.world.item.BlockItem;
import org.skriptlang.skript.fabric.compat.FabricItemType;

@Name("Is Solid")
@Description("Checks whether an item is solid.")
@Example("grass block is solid")
@Example("player's tool isn't solid")
@Since("2.2-dev36")
public class CondIsSolid extends PropertyCondition<FabricItemType> {

    static {
        register(CondIsSolid.class, "solid", "itemtypes");
    }

    @Override
    public boolean check(FabricItemType itemType) {
        return itemType.item() instanceof BlockItem blockItem
                && blockItem.getBlock().defaultBlockState().isSolid();
    }

    @Override
    protected String getPropertyName() {
        return "solid";
    }
}
